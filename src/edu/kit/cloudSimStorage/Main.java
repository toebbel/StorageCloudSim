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
import edu.kit.cloudSimStorage.helper.*;
import edu.kit.cloudSimStorage.monitoring.*;
import edu.kit.cloudSimStorage.monitoring.sampleSequenceOperators.SampleCombinator;
import edu.kit.cloudSimStorage.monitoring.report.*;

import org.cloudbus.cloudsim.core.CloudSim;

import org.apache.commons.cli.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/** @author Tobias Sturm, 6/7/13 1:26 PM */
public class Main {
	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.addOption(OptionBuilder
				.withArgName("input-directory")
				.withDescription("directory that contains simulation scenario. " +
						"Use file extentions '" + SharedConstants.CLOUD_FILE_EXTENTION + "'" +
						" and  '" + SharedConstants.SEQUENCE_FILE_EXTENTION + "'")
				.create("i")
		);
		options.addOption(OptionBuilder
				.withArgName("output-directory")
				.withDescription("directory for log outputs")
				.create("o")
		);
		options.addOption(OptionBuilder
				.withDescription("enable cloud dumps at end of simulation")
				.create("clouddump")
		);
		options.addOption(OptionBuilder
				.withDescription("enable csv outputs at end of simulation")
				.create("csv")
		);
		options.addOption(OptionBuilder
				.withDescription("enable graph outputs at end of simulation")
				.create("graphs")
		);
		options.addOption(OptionBuilder
				.withDescription("enable log outputs at end of simulation")
				.create("logs")
		);
		options.addOption(OptionBuilder
				.withDescription("number of sequences")
				.create("n")
		);

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse( options, args);

		//read parameters
		File outputDir = new File(cmd.hasOption("i") ? cmd.getOptionValue("i") : ".");
		File inputDir = new File(cmd.hasOption("o") ? cmd.getOptionValue("o") : ".");
		if(!inputDir.exists() && inputDir.mkdirs()) {
			System.err.println("Could not create input directory '" + inputDir.getPath() + "'");
		}
		if(!outputDir.exists() && outputDir.mkdirs()) {
			System.err.println("Could not create output directory '" + outputDir.getPath() + "'");
		}
		int nSequences = -1;
		if(cmd.hasOption("n"))
		{
			try
			{
				nSequences = Integer.parseInt(cmd.getOptionValue("n"));
			}
			catch(NumberFormatException nfe){ }
		}
		boolean generateDump = cmd.hasOption("clouddump");
		boolean generateGraphs = cmd.hasOption("graphs");;
		boolean generateCSV = cmd.hasOption("csv");;
		boolean preventLogging = cmd.hasOption("logs");;

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
