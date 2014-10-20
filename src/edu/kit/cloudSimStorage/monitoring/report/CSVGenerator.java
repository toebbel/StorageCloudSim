/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.monitoring.report;

import edu.kit.cloudSimStorage.cloudOperations.request.CloudRequest;
import edu.kit.cloudSimStorage.StorageCloud;
import edu.kit.cloudSimStorage.monitoring.TraceableResource;
import edu.kit.cloudSimStorage.monitoring.TupleSequence;
import edu.kit.cloudSimStorage.monitoring.OperationTimeTraceSample;
import edu.kit.cloudSimStorage.monitoring.Tuple;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static edu.kit.cloudSimStorage.helper.TimeHelper.timeToString;

/** @author Tobias Sturm, 6/26/13 9:59 AM */
public class CSVGenerator extends ReportGenerator {

	private static final String DELIMITER = ";";
	private static final String FILE_EXTENTION = ".csv";

	@Override
	public void generate(File rootDir) throws IOException {
		//print all clouds
		for(StorageCloud cloud : this.clouds) {
			printTrackableObject(rootDir, "Cloud_" + cloud.getName() + "_", cloud);
			printCloudOperationTraces(rootDir, "Cloud_" + cloud.getName() + "_", cloud.getOperationTimeTraces());


		}
	}

	private static List<OperationTimeTraceSample> downcast(List<CloudRequest> in) {
		List<OperationTimeTraceSample> tmp = new ArrayList<>();
		for(CloudRequest cr : in)
			tmp.add(cr);
		return tmp;
	}

	private static void printCloudOperationTraces(File rootDir, String cloudName,  List<OperationTimeTraceSample> traces) throws IOException {
		StringBuilder b = new StringBuilder();

		//write head
		b.append("Operation Traces for " + cloudName).append(NEW_LINE);
		b.append("start timestamp").append(DELIMITER)
				.append("end timestamp").append(DELIMITER)
				.append("delay in ms").append(DELIMITER)
				.append("duration in ms").append(DELIMITER)
				.append("description").append(NEW_LINE);

		//write content
		for(OperationTimeTraceSample trace : traces) {
			b.append(timeToString(trace.getOmmittedTimestamp())).append(DELIMITER)
					.append(timeToString(trace.getOmmittedTimestamp() + trace.getDelay() + trace.getDuration())).append(DELIMITER)
					.append(trace.getDelay()).append(DELIMITER)
					.append(trace.getDuration()).append(DELIMITER)
					.append(trace.getDescriptor())
					.append(NEW_LINE);
		}

		writeToFile(rootDir, cloudName + "OperationTrace" + FILE_EXTENTION, b.toString());
	}

	public static void printTrackableObject(File rootDir, String prefix, TraceableResource resource) throws IOException {
		for(String key : resource.getAvailableTrackingKeys()) {
			TupleSequence<Double> samples = resource.getSamples(key);
			if(samples == null)
				continue;

			//determine data in correct format and title row
			Tuple<String, TupleSequence<String>> formatedData = format(samples, key);

			//generate header and data
			StringBuilder content = new StringBuilder();
			content.append(prefix).append(NEW_LINE);
			content.append("timestamp").append(DELIMITER).append(formatedData.x).append(NEW_LINE);
			content.append(generateCSV(formatedData.y));

			String fileName = prefix + key + FILE_EXTENTION;
			writeToFile(rootDir, fileName, content.toString());
		}
	}



	public static void writeTrackSequence(File root, String path, List<String> labels, List<TupleSequence<Double>> content) throws IOException {
		assert labels.size() == content.size();
		StringBuilder bw = new StringBuilder();

		int[] is = new int[content.size()];
		List<Integer> finished = new ArrayList<>();
		for(int i = 0; i < labels.size(); i++) {
			if(content.get(i) == null || content.get(i).isEmpty())
				finished.add(i);
			is[i] = 0;
			bw.append("time (");
			bw.append(labels.get(i));
			bw.append(" in ms)");
			bw.append(DELIMITER);
			bw.append("time (");
			bw.append(labels.get(i));
			bw.append(")");
			bw.append(DELIMITER);
			bw.append(labels.get(i));
			bw.append(DELIMITER);
			bw.deleteCharAt(bw.length() - 1);
			bw.append(DELIMITER);
		}
        if (bw.length() > 0)
		    bw.deleteCharAt(bw.length() - 1);
		bw.append("\n");

		while(finished.size() < content.size()) {
			for(int i = 0; i < is.length; i++) {
				if(!finished.contains(i)) {
					bw.append(content.get(i).get(is[i]).x);
					bw.append(DELIMITER);
					bw.append(timeToString(content.get(i).get(is[i]).x));
					bw.append(DELIMITER);
					bw.append(content.get(i).get(is[i]).y);
					bw.append(DELIMITER);
					is[i] += 1;
					if(is[i] >= content.get(i).size())
						finished.add(i);
				}
				else {
					bw.append(DELIMITER);
					bw.append(DELIMITER);
					bw.append(DELIMITER);
				}
			}
			bw.deleteCharAt(bw.length() - 1);
			bw.append("\n");
		}

		writeToFile(root, path + FILE_EXTENTION, bw.toString());
	}




	private static String generateCSV(TupleSequence<String> in) {
		StringBuilder b = new StringBuilder();
		for(Tuple<Long, String> t : in) {
			if(t == null)
				continue;
			b.append(timeToString(t.x));
			b.append(DELIMITER);
			b.append(t.y);
			b.append(NEW_LINE);
		}
		return b.toString();
	}
}

