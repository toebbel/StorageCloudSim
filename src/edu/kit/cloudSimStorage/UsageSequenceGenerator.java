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

import edu.kit.cloudSimStorage.cloudBroker.UserRequest;
import edu.kit.cloudSimStorage.helper.FileSizeHelper;
import org.cloudbus.cloudsim.distributions.ContinuousDistribution;
import org.cloudbus.cloudsim.distributions.UniformDistr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static edu.kit.cloudSimStorage.helper.FileSizeHelper.Magnitude.*;

/** @author Tobias Sturm, 6/30/13 7:07 PM */
public class UsageSequenceGenerator {
	ContinuousDistribution fileSizeDistribution;
	ContinuousDistribution intervalDistribution;
	ContinuousDistribution downloadProbability;
	HashMap<String, Long> createdFiles;
	List<String> usedFilenames;

	/**
	 * Creates a usageSequenceGenerator with the following distributions:
	 * file size uniformly distributed from 1KB to 1GB
	 * intervals between 10ms and 5min
	 * download probability distribution between 0 and 0.6
	 */
	public UsageSequenceGenerator() {
		fileSizeDistribution = new UniformDistr(FileSizeHelper.toBytes(1, KILO_BYTE), FileSizeHelper.toBytes(1, GIGA_BYTE));
		intervalDistribution = new UniformDistr(0, 1000.0 * 30.0); //10 ms - 30sec
		downloadProbability = new UniformDistr(0, 0.6);
		usedFilenames = new ArrayList<>();
	}

	/**
	 * Sets the distribution of file sizes. The size is given in byte.
	 * @param dist Distribution that describes the filesizes
	 * @return      instance of this UsageGenerator
	 */
	public UsageSequenceGenerator setFileSizeDistribution(ContinuousDistribution dist) {
		fileSizeDistribution = dist;
		return this;
	}

	/**
	 * Sets the distribution for delays between two requests
	 * @param dist distribution of wait times in ms
	 * @return instance of this UsageGenerator
	 */
	public UsageSequenceGenerator setRequestIntervallDistribution(ContinuousDistribution dist) {
		intervalDistribution = dist;
		return this;
	}


	/**
	 * Sets the probability distribution of an upload. If the Distribution sample is > .5 a download request is generated. If not, an upload request is generated
	 * @param dist Probability for a upload
	 * @return instance of this UsageGenerator
	 */
	public UsageSequenceGenerator setDownUpDistribution(ContinuousDistribution dist) {
		downloadProbability = dist;
		return this;
	}

		public List<UserRequest> generate(long totalTraffic) {
		long remainingTraffic = totalTraffic;

		List<UserRequest> requests = new ArrayList<>();
		createdFiles = new HashMap<>();

		requests.add(UserRequest.blocking(UserRequest.putContainer("files")));
		Random rnd = new Random();
		while(remainingTraffic > 0) {
			if(downloadProbability.sample() > 0.5 && !createdFiles.isEmpty()) {
				String file = getRandomFile();
			 	requests.add(UserRequest.downloadObject("files", file));
				remainingTraffic -= createdFiles.get(file);
			} else {
				long size = (long) fileSizeDistribution.sample();
				remainingTraffic -= size;
				int fileNameLen = rnd.nextInt(11) + 10; //filenames have length between 10 and 20 chars
				String filename = generateRandomFilename(fileNameLen);

				while(usedFilenames.contains(filename)) {
					filename = generateRandomFilename(fileNameLen++);
				}

				usedFilenames.add(filename);
				createdFiles.put(filename, size);
				requests.add(UserRequest.putObject("files", filename, size));
			}
			requests.add(UserRequest.idle((int) intervalDistribution.sample()));
		}

		return requests;
	}

	private String generateRandomFilename(int length) {
		final String validChars = "abcdefghijklmnopqrstuvwxyz0987654321-_";
		String result = "";
		while(result.length() < length)
			result += validChars.charAt((int)(Math.random() * (validChars.length() - 1)));
		return result;
	}

	private String getRandomFile() {
		return new ArrayList<>(createdFiles.keySet()).get((int)(Math.random() * (createdFiles.keySet().size() - 1)));
	}

}
