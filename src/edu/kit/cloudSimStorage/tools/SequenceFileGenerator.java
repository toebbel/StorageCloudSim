/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.tools;

import edu.kit.cloudSimStorage.CdmiCloudCharacteristics;
import edu.kit.cloudSimStorage.ObjectStorageSLAs.matchingSLA.MinimumCharactersisticValue;
import edu.kit.cloudSimStorage.ObjectStorageSLAs.RateCharacteristicsWithInverse;
import edu.kit.cloudSimStorage.ObjectStorageSLAs.StorageCloudSLARequest;
import edu.kit.cloudSimStorage.SharedConstants;
import edu.kit.cloudSimStorage.UsageSequence;
import edu.kit.cloudSimStorage.UsageSequenceGenerator;
import edu.kit.cloudSimStorage.cdmi.CdmiMetadata;
import edu.kit.cloudSimStorage.cloudBroker.UserRequest;
import edu.kit.cloudSimStorage.helper.FileSizeHelper;
import org.cloudbus.cloudsim.distributions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.List;
import java.util.Random;


import static edu.kit.cloudSimStorage.helper.FileSizeHelper.Magnitude.GIGA_BYTE;
import static edu.kit.cloudSimStorage.helper.FileSizeHelper.toBytes;
import static edu.kit.cloudSimStorage.helper.FileSizeHelper.toHumanReadable;

/** @author Tobias Sturm, 8/5/13 7:02 PM */
public class SequenceFileGenerator {

	public static void main(String[] args) throws Exception {
		assert args.length >= 2;

		//read parameters
		File targetDir = new File(args[0]);
		assert targetDir.isDirectory();
		if(!targetDir.exists() && !targetDir.mkdirs()) {
			System.out.println("could not create target directory. Use . instead");
			targetDir = new File(".");
		}

		int numSequences = Integer.parseInt(args[1]);
		int digits = (int) Math.log10(numSequences) + 1;
		int slaSpaceDisturbance = 10; //in percentage. Actual value will be disturbed by +/- that percentage

		int alpha = 3;
		double beta = 2;
		long scales[] = {toBytes(1, GIGA_BYTE), toBytes(1, GIGA_BYTE)};
		if(args.length == 4) {
			alpha = Integer.parseInt(args[2]);
			beta = Double.parseDouble(args[3]);

			System.out.println("read parameters for traffic size distribution: alpha = " + alpha + ", beta = " + beta + " scale = " + toHumanReadable(scales[0]) + "/" + toHumanReadable(scales[1]));
		} else {
			System.out.println("use for traffic size distribution: alpha = 3, beta = 2 scale = 1GB");
		}

		StorageCloudSLARequest slas[] = new StorageCloudSLARequest[2];
		UsageSequenceGenerator generators[] = new UsageSequenceGenerator[2];
		String names[] = new String[2];
		FileWriter statWriter[] = new FileWriter[3]; //last one is for common stats

		//some default Cloud user
		slas[0] = new StorageCloudSLARequest();
		slas[0].canCreateContainers().canDeleteContainers().rateByPrice();
		generators[0] = new UsageSequenceGenerator();
		generators[0].setDownUpDistribution(new UniformDistr(0,0.75));
		names[0] = "default";


		//big data storage space (write only)
		slas[1] = new StorageCloudSLARequest();
		slas[1].canCreateContainers().canDeleteContainers().addRating(new RateCharacteristicsWithInverse(CdmiCloudCharacteristics.UPLOAD_COSTS, "lowest upload costs", Double.NEGATIVE_INFINITY)).addRating(new RateCharacteristicsWithInverse(CdmiCloudCharacteristics.STORAGE_COSTS, "lowest storage costs", Double.NEGATIVE_INFINITY)).hasNoContainerSizeLimit();
		generators[1] = new UsageSequenceGenerator();
		generators[1].setDownUpDistribution(new UniformDistr(0,0.1)); //uploads only
		generators[1].setRequestIntervallDistribution(new ContinuousDistribution() {
			private int count = 0;
			Random rnd = new Random();
			@Override
			public double sample() {
				if(count++ < 4)
					return rnd.nextInt(10);
				else {
					count = 0;
					return (rnd.nextInt(6) + 5) * 1000 * 60; //between 5 and 10 minutes
				}
			}
		});
		generators[1].setFileSizeDistribution(new UniformDistr(toBytes(1, GIGA_BYTE), toBytes(100, GIGA_BYTE)));
		names[1] = "scientific";

		ContinuousDistribution idleDistance = new UniformDistr(1 * 1000, 10 * 1000);
		int idle = 0;

		//initialize stat writer
		for (int i = 0; i < names.length; i++) {
			String filename = targetDir.getPath() + File.separator + names[i] + ".stat.csv";;
			statWriter[i] = new FileWriter(filename);
			statWriter[i].write("size\tdelay\topcode\n");
		}
		statWriter[names.length] = new FileWriter(targetDir.getPath() + File.separator + "stat.csv");
		statWriter[names.length].write("id\tmodel\tmaxObjSize\tmaxObjSize in SLA\treqiredSpace\trequiredSpace in SLA\tinitial delay\n");

		//create sequences
		ContinuousDistribution trafficSize = new GammaDistr(alpha, beta);

		Random rnd = new Random();
		for(int i = 0; i < numSequences; i++) {
			int chosenModel = rnd.nextInt(2);
			StorageCloudSLARequest sla = slas[chosenModel];
			UsageSequenceGenerator gen = generators[chosenModel];

			long traffic = (long) (trafficSize.sample() * scales[chosenModel]);
			List<UserRequest> requests = gen.generate(traffic);

			for(UserRequest r : requests) {
				statWriter[chosenModel].write(r.getSize() + "\t" + r.getDelay() + "\t" + r.getOpCode() + "\n");
			}

			StorageCloudSLARequest customSLA = sla.clone();
			long requiredSpace = requiredStorageSpace(requests);
			long disturbedSpace = requiredSpace + (long)(requiredSpace / 100.0 * slaSpaceDisturbance * (rnd.nextDouble() * 2  - 1));
			customSLA.minCapacity(disturbedSpace);

			long maxObjSize = biggestObject(requests);
			long disturedMaxObjSize = -1;

			if(chosenModel == 1) {
				maxObjSize = biggestObject(requests);
				disturedMaxObjSize = maxObjSize + (long)(maxObjSize / 100.0 * slaSpaceDisturbance * (rnd.nextDouble() * 2  - 1));
				customSLA.addRequirement(new MinimumCharactersisticValue(CdmiCloudCharacteristics.MAX_OBJECT_SIZE, disturedMaxObjSize));
			}
			UsageSequence sequence = new UsageSequence(i, customSLA, requests);

			int initialIdle = (int) (idle + idleDistance.sample());
			sequence.getRequests().add(0, UserRequest.idle(initialIdle));
			idle = initialIdle;

			statWriter[names.length].write(i + "\t" + chosenModel + "\t" + maxObjSize + "\t" +disturedMaxObjSize + "\t" + requiredSpace + "\t" + disturbedSpace + "\t" + idle + "\n");


			System.out.println("generate " + names[chosenModel] + " sequence with total traffic of " + FileSizeHelper.toHumanReadable(traffic) + " which will cause a total required storage capacity of " + toHumanReadable(requiredSpace) + "(SLA requires " + FileSizeHelper.toHumanReadable(disturbedSpace) + ")");
			String filename = padLeft(i, digits)  + "." + SharedConstants.SEQUENCE_FILE_EXTENTION;
			FileOutputStream out = new FileOutputStream(new File(targetDir.toString() + File.separator + filename));
			sequence.serialize(out);
			out.close();
		}

		//close stat writer
		for (int i = 0; i < statWriter.length; i++) {
			statWriter[i].close();
		}
	}

	public static String padLeft(int s, int n) {
		return String.format("%0" + n + "d", s);
	}

	private static long requiredStorageSpace(List<UserRequest> sequence) {
		long current = 0, max = 0;
		for(UserRequest r : sequence) {
			if(r.getOpCode() == UserRequest.DELETE_OBJECT)
				current = current - Long.valueOf(r.getMetadata().get(CdmiMetadata.SIZE));
			else if (r.getOpCode() == UserRequest.PUT_OBJECT) {
				current = current + Long.valueOf(r.getMetadata().get(CdmiMetadata.SIZE));
				max = Math.max(current, max);
			}
		}
		return max;
	}

	private static long biggestObject(List<UserRequest> sequence) {
		long max = 0;
		for(UserRequest r : sequence) {
			if (r.getOpCode() == UserRequest.PUT_OBJECT) {
				max = Math.max(Long.valueOf(r.getMetadata().get(CdmiMetadata.SIZE)), max);
			}
		}
		return max;
	}
}
