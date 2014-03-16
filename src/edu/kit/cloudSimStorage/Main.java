/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage;

import edu.kit.cloudSimStorage.cloudBroker.StorageBroker;
import edu.kit.cloudSimStorage.cloudBroker.StorageMetaBroker;
import edu.kit.cloudSimStorage.cloudFactory.StorageCloudFactory;
import edu.kit.cloudSimStorage.cloudOperations.*;
import edu.kit.cloudSimStorage.helper.FileSizeHelper;
import edu.kit.cloudSimStorage.helper.TupleSequence;
import edu.kit.cloudSimStorage.monitoring.LogDeflector;
import edu.kit.cloudSimStorage.monitoring.OperationTimeTraceSample;
import edu.kit.cloudSimStorage.monitoring.TrackableResource;
import edu.kit.cloudSimStorage.helper.Tuple;
import edu.kit.cloudSimStorage.monitoring.report.CSVGenerator;
import edu.kit.cloudSimStorage.monitoring.report.GraphGenerator;
import edu.kit.cloudSimStorage.monitoring.report.ReportGenerator;
import edu.kit.cloudSimStorage.monitoring.sampleSequenceOperators.SampleCombinator;
import org.cloudbus.cloudsim.core.CloudSim;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

import static edu.kit.cloudSimStorage.helper.TimeHelper.timeToString;

/** @author Tobias Sturm, 6/7/13 1:26 PM */
public class Main {
	public static void main(String[] args) throws Exception {
		//read parameters
		File outputDir = new File(".");
		File inputDir = new File(".");
		int nSequences = -1;
		boolean generateDump = true;
		boolean generateGraphs = true;
		boolean generateCSV = true;
		boolean preventLogging = false;
		for(int i = 0; i < args.length; i++) {
			if((args[i].toLowerCase().equals("-o") || args[i].toLowerCase().equals("--output")) && args.length > i) {
				File tmp = new File(args[i+1]);
				if(!tmp.exists() && tmp.mkdirs()) {
					System.err.println("Could not create output directory '" + tmp.getPath() + "'. Use './' default");
				}
				outputDir = tmp;
				i++;
			} else if((args[i].toLowerCase().equals("-i") || args[i].toLowerCase().equals("--input"))  && args.length > i) {
				File tmp = new File(args[i+1]);
				if(!tmp.exists() ) {
					System.err.println("Could not open input directory '" + tmp.getPath() + "'.");
					return;
				}
				inputDir = tmp;
				i++;
			} else if((args[i].toLowerCase().equals("-n") || args[i].toLowerCase().equals("--numsequences"))  && args.length > i) {
				if(args[i+1].toLowerCase() != "all")
					nSequences = Integer.parseInt(args[i+1]);
				i++;
			} else if (args[i].toLowerCase().equals("-nodump")) {
				generateDump = false;
			} else if (args[i].toLowerCase().equals("-nocsv")) {
				generateCSV = false;
			} else if (args[i].toLowerCase().equals("-nographs")) {
				generateGraphs = false;
			} else if (args[i].toLowerCase().equals("-nologs" )) {
				preventLogging = true;
			} else {
				System.out.print("unknown parameter '" + args[i] + "'!\n" +
						"Usage:\n" +
						"   -o  --output        | set the output directory [./]\n" +
						"   -i  --input         | set the input directory [./]\n" +
						"   -n  --nsequences    | set the number of sequences to read (number or 'all') [all]\n" +
						"   -noDump             | no cloud dumps\n" +
						"   -noCSV              | no csv output of monitoring\n" +
						"   -noGraphs           | no graphical output\n" +
						"   -noLogs             | no logging files\n\n" +
						"Use file extentions '.cloud.xml' and  '.usageSequence.xml'!");
				return;
			}
		}
		System.out.printf("take '%s' as input%n", inputDir.getPath());
		System.out.printf("take '%s' as output%n", outputDir.getPath());
		if(nSequences != -1)
			System.out.printf("take first %d sequences%n", nSequences);
		else
			System.out.printf("take all sequences%n");

		// Initialize the CloudSim library
		CloudSim.init(2, Calendar.getInstance(), false);

		// Initialize logging
		LogDeflector deflector = new LogDeflector(outputDir);
		if(!preventLogging)
			deflector.setLevel(Level.ALL);
		else
			deflector.setLevel(Level.OFF);
		CSVGenerator csvGenerator = new CSVGenerator();
		GraphGenerator gGen = new GraphGenerator();

		//read cloud definitions
		List<StorageCloud> clouds = new ArrayList<>();
		for(File f : getFilesThatEndWithSorted(SharedConstants.CLOUD_FILE_EXTENTION, inputDir)) {
			StorageCloud cloud = StorageCloudFactory.createCloud(StorageCloudFactory.deserialize(new FileInputStream(f)));

			clouds.add(cloud);
			if(!preventLogging)
				deflector.add(cloud);
			gGen.addCloud(cloud);
			csvGenerator.addCloud(cloud);
		}

		//initialize usage sequences and metabroker
		StorageMetaBroker meta = new StorageMetaBroker("meta_broker");
		for(StorageCloud c : clouds) {
			meta.addCloud(c.getId());
		}

		List<UsageSequence> sequences = new ArrayList<>();
		for(File f : getFilesThatEndWithSorted(SharedConstants.SEQUENCE_FILE_EXTENTION, inputDir)) {
			if(!isScientific(f)) continue;
			//take only first n sequences. won't interrupt if set to -1 (=all sequences)
			if(nSequences == 0)
				break;
			nSequences --;

			FileInputStream stream = new FileInputStream(f);
			UsageSequence seq = UsageSequence.deserialize(stream);
			stream.close();

			//add logging facilities
			if(!preventLogging)
				deflector.add(seq);
			csvGenerator.addSequence(seq);
			gGen.addSequence(seq);
			sequences.add(seq);

			meta.addNewUsageSequence(seq);
		}

		//start simulation
		CloudSim.startSimulation();

		//create outputs
		if(generateDump) {
			for(StorageCloud c : clouds)  {
				java.io.FileWriter dumpWriter = new FileWriter(outputDir.getPath() + "/" + c.getName() + ".cloudDump");
				dumpWriter.write(c.dumpCloudFiles());
				dumpWriter.close();
			}
		}

		if(generateGraphs)
			gGen.generate(outputDir);
		if(generateCSV)
			csvGenerator.generate(outputDir);

		//Custom generators

		//dump request statistics
		FileWriter writer = new FileWriter(outputDir.getPath() + "/" + "requests.stats.csv");
		writer.write("started\tverb\tsize\tduration\tdelay\n");

		for(StorageCloud c : clouds) {
			for(OperationTimeTraceSample req : c.getOperationTimeTraces()) {
				writer.write(String.valueOf(req.getOmmittedTimestamp()));
				writer.write("\t");
			    writer.write(req.getDescriptor());
				writer.write("\t");
				if(((CloudScheduleEntry)req) instanceof GetObjectScheduleEntry )
					if(((GetObjectResponse)(((GetObjectScheduleEntry)req).generateResponse())).getObject() != null)
					writer.write(String.valueOf(((GetObjectResponse)(((GetObjectScheduleEntry)req).generateResponse())).getObject().getPhysicalSize()));
					else
						writer.write("0");
				else
					writer.write(String.valueOf(((CloudScheduleEntry)req).getRequest().getSize()));
				writer.write("\t");
				writer.write(String.valueOf(req.getDuration()));
				writer.write("\t");
				writer.write(String.valueOf(req.getDelay()));
				writer.write("\n");
			}
		}
		writer.close();

		List<TupleSequence<Double>> ackRequests = new ArrayList<>();
		List<TupleSequence<Double>> failedRequests = new ArrayList<>();
		List<TupleSequence<Double>> succRequests = new ArrayList<>();

		for(StorageCloud c : clouds) {
			ackRequests.add(meta.getStatsforCloudBroker(c.getId()).getSamples(StorageBroker.NUM_TOTAL_ACK_REQUESTS));
			failedRequests.add(meta.getStatsforCloudBroker(c.getId()).getSamples(StorageBroker.NUM_TOTAL_FAILED_REQUESTS));
			succRequests.add(meta.getStatsforCloudBroker(c.getId()).getSamples(StorageBroker.NUM_TOTAL_SUCC_REQUESTS));
		}

		//dump SLA violations
		List<TupleSequence<Double>> unaligned = new ArrayList<>();
		unaligned.add(ReportGenerator.removeDoublicateValues(SampleCombinator.sum(ackRequests), 5));
		unaligned.add(ReportGenerator.removeDoublicateValues(SampleCombinator.sum(failedRequests), 5));
		unaligned.add(ReportGenerator.removeDoublicateValues(SampleCombinator.sum(succRequests), 5));
		unaligned.add(ReportGenerator.removeDoublicateValues(meta.getSamples(TrackableResource.NUM_EVENTS_TOTAL)));
		List<String> labels = new ArrayList<>();
		labels.add("acked requests");
		labels.add("failed requests");
		labels.add("succ requests");
		labels.add("declined requests");
		CSVGenerator.writeTrackSequence(outputDir, "sla.stats", labels, unaligned);

		//dump costs
		List<TupleSequence<Double>> cloudEarnings = new ArrayList<>();
		labels = new ArrayList<>();
		for(StorageCloud cloud : clouds) {
			cloudEarnings.add(ReportGenerator.removeDoublicateValues(ReportGenerator.roundTo(cloud.getSamples(TrackableResource.TOTAL_EARNINGS), 2), 0.01));
			labels.add(cloud.getName());
		}
		CSVGenerator.writeTrackSequence(outputDir, "earnings", labels, cloudEarnings);

		//dump storage
		List<TupleSequence<Double>> cloudStorage = new ArrayList<>();
		labels = new ArrayList<>();
		for(StorageCloud cloud : clouds) {
			labels.add(cloud.getName());
			cloudStorage.add(ReportGenerator.roundTo(ReportGenerator.changeFileMagnitude(ReportGenerator.removeDoublicateValues(cloud.getSamples(StorageCloud.USED_STORAGE_PHYSICAL_ABS), FileSizeHelper.toBytes(1, FileSizeHelper.Magnitude.GIGA_BYTE)), FileSizeHelper.Magnitude.GIGA_BYTE), 2));
		}
		CSVGenerator.writeTrackSequence(outputDir, "storage.stats", labels, cloudStorage);

	}

	private static boolean isScientific(File f) {
		boolean result = false;
		try {
			Scanner in = new Scanner(new FileReader(f));
			while(in.hasNext() && !result) {
				if(in.nextLine().contains("SLA storage costs"))
					result = true;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();  //TODO handle exception
		}
		return result;
	}

	private static void printTrackHistory(String caption, TupleSequence<Double> history) {
		printTrackHistory(caption, history, true);
	}

	private static void printTrackHistory(String caption, TupleSequence<Double> history, boolean scale) {
		System.out.println("History of " + caption + " has " + history.size() + " samples");
		java.text.SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy hh:mm:ss:S");
		for (int i = 0; i < history.size(); i++) {
			Date timestamp = new Date(history.get(i).x);
			if(scale)
				System.out.println(format.format(timestamp) + ": " + FileSizeHelper.toHumanReadable(history.get(i).y.longValue()));
			else
				System.out.println(format.format(timestamp) + ": " + history.get(i).y);
		}
	}

	private static List<File> getFilesThatEndWithSorted(final String end, File inDirectory) {
		List<File> result = new ArrayList<>();
		for(File f : inDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(end);
			}
		})) {
			result.add(f);
		}
		Collections.sort(result);
		return result;
	}
}
