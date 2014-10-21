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
import edu.kit.cloudSimStorage.ObjectStorageSLAs.StorageCloudSLARequirements;
import edu.kit.cloudSimStorage.ObjectStorageSLAs.matchingSLA.MinimumCharactersisticValue;
import edu.kit.cloudSimStorage.ObjectStorageSLAs.ratingSLA.RateCharacteristicsWithInverse;
import edu.kit.cloudSimStorage.SharedConstants;
import edu.kit.cloudSimStorage.UsageSequence;
import edu.kit.cloudSimStorage.UsageSequenceGenerator;
import edu.kit.cloudSimStorage.cdmi.CdmiMetadata;
import edu.kit.cloudSimStorage.cloudBroker.UserRequest;
import edu.kit.cloudSimStorage.helper.FileSizeHelper;
import org.cloudbus.cloudsim.distributions.ContinuousDistribution;
import org.cloudbus.cloudsim.distributions.GammaDistr;
import org.cloudbus.cloudsim.distributions.UniformDistr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.List;
import java.util.Random;

import static edu.kit.cloudSimStorage.helper.FileSizeHelper.Magnitude.GIGA_BYTE;
import static edu.kit.cloudSimStorage.helper.FileSizeHelper.Magnitude.KILO_BYTE;
import static edu.kit.cloudSimStorage.helper.FileSizeHelper.toBytes;
import static edu.kit.cloudSimStorage.helper.FileSizeHelper.toHumanReadable;

/** @author Tobias Sturm, 8/5/13 7:02 PM */
public class SimpleFileSequenceGenerator {

	public static void main(String[] args) throws Exception {
        //Check if we got two parameters (target dir and number of sequences to generate)
		if(args.length < 2)
		{
			System.err.print("Please select a target directory and number of sequences to generate. For example: 'java -jar SequenceGenerator big_set 25'/n");
			System.exit(1);
		}

		//read output dir parameter and check if dir exist / create it
		File targetDir = new File(args[0]);
		assert targetDir.isDirectory();
		if(!targetDir.exists() && !targetDir.mkdirs()) {
			System.out.println("could not create target directory. Use . instead");
			targetDir = new File(".");
		}

        //how many sequences do we have to generate?
		int numSequences = Integer.parseInt(args[1]);

        //determine how many digits are required to make file names equally long
		int digits = (int) Math.log10(numSequences) + 1;

		//some default cloud-user that will fire all our requests
        StorageCloudSLARequirements sla = new StorageCloudSLARequirements();

        //Determine the SLA for every request that is generated
		sla.canCreateContainers().canDeleteContainers().rateByPrice();

        //setup generator that will generate requests for request sequences
        UsageSequenceGenerator generator = new UsageSequenceGenerator();
		generator.setDownUpDistribution(new UniformDistr(0, 0.5));

        //Time to wait between requests (1-10s)
		ContinuousDistribution idleDistance = new UniformDistr(1 * 1000, 10 * 1000);
        generator.setRequestIntervallDistribution(idleDistance);

        //setup max file size for files in requests
        ContinuousDistribution fileSize = new UniformDistr(toBytes(1, KILO_BYTE), toBytes(10, GIGA_BYTE));
        generator.setFileSizeDistribution(fileSize);

        //Sequences claim storage space with their SLA. The actual required space will be disturbed by +/- that percentage
        int fileSizeDistortionForSLA = 10; //in percentage.
        long trafficPerSequence = toBytes(25, GIGA_BYTE);

        Random rnd = new Random();
		for(int i = 0; i < numSequences; i++) {

            //generate 25Gig of Traffic
			List<UserRequest> requests = generator.generate(trafficPerSequence);

			//attach the SLA to the requests
			StorageCloudSLARequirements customSLA = sla.clone();
			long requiredSpace = requiredStorageSpace(requests);
			long disturbedSpace = requiredSpace + (long)(requiredSpace / 100.0 * fileSizeDistortionForSLA * (rnd.nextDouble() * 2  - 1));
			customSLA.minCapacity(disturbedSpace);

			//create the sequence out of the requests and the custom SLA
			UsageSequence sequence = new UsageSequence(i, customSLA, requests);

            //print some useful information
			System.out.println("generate sequence with total traffic of " + FileSizeHelper.toHumanReadable(trafficPerSequence) + " which will cause a total required storage capacity of " + toHumanReadable(requiredSpace) + "(SLA requires " + FileSizeHelper.toHumanReadable(disturbedSpace) + ")");

            //generate output file
			String sequenceFilename = padLeft(i, digits)  + "." + SharedConstants.SEQUENCE_FILE_EXTENTION;
			FileOutputStream out = new FileOutputStream(new File(targetDir.toString() + File.separator + sequenceFilename));
			sequence.serialize(out);
			out.close();
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
