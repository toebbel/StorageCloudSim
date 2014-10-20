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

import edu.kit.cloudSimStorage.StorageCloud;
import edu.kit.cloudSimStorage.UsageSequence;
import edu.kit.cloudSimStorage.cloudBroker.StorageBroker;
import edu.kit.cloudSimStorage.helper.FileSizeHelper;
import edu.kit.cloudSimStorage.monitoring.TraceableResource;
import edu.kit.cloudSimStorage.monitoring.TupleSequence;
import edu.kit.cloudSimStorage.monitoring.Tuple;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** @author Tobias Sturm, 6/25/13 11:46 AM */
public abstract class ReportGenerator {
	protected List<StorageCloud> clouds;
	protected List<UsageSequence> sequences;



	protected static final String NEW_LINE = "\n";

	protected ReportGenerator() {
		this.clouds = new ArrayList<>();
		this.sequences = new ArrayList<>();
	}

	public void addCloud(StorageCloud cloud) {
		clouds.add(cloud);
	}

	public void addSequence(UsageSequence sequence) {
		sequences.add(sequence);
	}

	public abstract void generate(File rootDir) throws IOException;


	/**
	 * Formats a sequence of Tuples to
	 * @param raw
	 * @param key
	 * @return
	 */
	protected static Tuple<String, TupleSequence<String>> format(TupleSequence<Double> raw, String key) {
		switch (key) {
			case TraceableResource.TRAFFIC:
			case TraceableResource.USED_BANDWIDTH:
				return new Tuple<>(key + " in MegaByte", toString(roundTo(changeFileMagnitude(raw, FileSizeHelper.Magnitude.MEGA_BYTE), 0)));
			case TraceableResource.TOTAL_EARNINGS:
			case TraceableResource.DEBTS:
				return new Tuple<>(key + " in $", toString(roundTo(changeMagnitude(raw, 0.01), 4)));
			case TraceableResource.USED_STORAGE_PERCENTAGE:
			case TraceableResource.USED_BANDWIDTH_PERCENTAGE:
			case TraceableResource.USED_STORAGE_PERCENTAGE_PHYSICAL:
			case TraceableResource.USED_STORAGE_PERCENTAGE_VIRTUAL:
				return new Tuple<>(key + " in % ", toString(roundTo(changeMagnitude(raw, 100), 2)));
			case TraceableResource.NUM_REQUESTS_LIST:
			case TraceableResource.NUM_REQUESTS:
			case TraceableResource.NUM_REQUESTS_OTHER:
			case StorageBroker.NUM_TOTAL_REQUESTS:
				return new Tuple<>("total " + key, toString(raw));
			case TraceableResource.NUM_REQUESTS_LIST_PER_SECOND:
			case TraceableResource.NUM_REQUESTS_PER_SECOND:
			case TraceableResource.NUM_REQUESTS_OTHER_PER_SECOND:
			case StorageBroker.NUM_REQUESTS_PER_SECOND:
				return new Tuple<>("request per second " + key, toString(raw));
			case TraceableResource.NUM_REQUESTS_LIST_PER_MINUTE:
			case TraceableResource.NUM_REQUESTS_PER_MINUTE:
			case TraceableResource.NUM_REQUESTS_OTHER_PER_MINUTE:
			case StorageBroker.NUM_REQUESTS_PER_MINUTE:
				return new Tuple<>("request per minute " + key, toString(raw));
			default:
				return new Tuple<>(key + " in GigaByte", toString(roundTo(changeFileMagnitude(raw, FileSizeHelper.Magnitude.GIGA_BYTE), 4)));
		}
	}

	public static TupleSequence<Double> roundTo(TupleSequence<Double> raw, int decimalDigits) {
		TupleSequence<Double> result = new TupleSequence<>();
		for(Tuple<Long, Double> t : raw) {
			result.add(new Tuple<>(t.x, Double.valueOf(String.format(Locale.US, "%1$."+ decimalDigits + "f", t.y))));
		}
		return result;
	}

	public static TupleSequence<Double> changeFileMagnitude(TupleSequence<Double> raw, FileSizeHelper.Magnitude magnitude) {
		TupleSequence<Double> result = new TupleSequence<>();
		for(Tuple<Long, Double> t : raw) {
			result.add(new Tuple<>(t.x, FileSizeHelper.fromBytes(t.y.longValue(), magnitude)));
		}
		return result;
	}

	public static TupleSequence<Double> changeMagnitude(TupleSequence<Double> raw, double scale) {
		TupleSequence<Double> result = new TupleSequence<>();
        if (raw != null)
        {
            for(Tuple<Long, Double> t : raw) {
                result.add(new Tuple<>(t.x, t.y * scale));
            }
        }
		return result;
	}

	public static TupleSequence<Double> removeDoublicateValues(TupleSequence<Double> in, double epsilon) {
		TupleSequence<Double> result = new TupleSequence<>();
        if (in != null) {
            double lastValue = Double.NEGATIVE_INFINITY;
            long lastX = Long.MIN_VALUE;
            for (Tuple<Long, Double> t : in) {
                if (Math.abs(t.y - lastValue) >= epsilon || lastX != t.x) {
                    result.add(new Tuple<>(t.x, t.y));
                    lastValue = t.y;
                    lastX = t.x;
                }
            }
        }
		return result;
	}

	public static TupleSequence<Double> removeDoublicateValues(TupleSequence<Double> in) {
		return removeDoublicateValues(in, 0.000001);
	}

	private static TupleSequence<String> toString(TupleSequence<Double> raw) {
		return toString(raw, "", "");
	}


	private static TupleSequence<String> toString(TupleSequence<Double> raw, String prefix, String suffix) {
		TupleSequence<String> result = new TupleSequence<>();
		for(Tuple<Long, Double> t : raw) {
			result.add(new Tuple<>(t.x, prefix + String.valueOf(t.y) + suffix));
		}
		return result;
	}

	/**
	 * Creates a file and writes the content. If a directory does not exist, it will be created
	 * @param root the root directory
	 * @param path relative path to root. Use only / as path seperator!
	 * @param content the content to write
	 */
	protected static void writeToFile(File root, String path, String content) throws IOException {
		assert root.isDirectory();
		String[] subDirs = path.split("/");
		String trailing = root.getPath() + File.separator;
		for(int i = 0; i < subDirs.length - 2; i++) {
			trailing += subDirs[i] + File.pathSeparator;
			File tmp = new File(trailing);
			if(!tmp.exists()) {
			    System.out.println("create dir " + tmp.getAbsoluteFile());
				tmp.createNewFile();
			}
		}
		trailing += subDirs[subDirs.length - 1];

		System.out.println("write to " + trailing);
		BufferedWriter writer = new BufferedWriter(new FileWriter(trailing, false));
		writer.write(content);
		writer.close();
		return;
	}



}
