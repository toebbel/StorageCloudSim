/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.storageModel.resourceUtilization;


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/** @author Tobias Sturm, 6/21/13 3:04 PM */
public class UtilizationSequence {
	private static final int GARBAGE_THRESH = 25;
	private LinkedList<UtilizationSequenceSample> samples;
	private LinkedList<UtilizationSequenceSample> pastSamples;
	private List<Long> timestamps;
	private int garbageCount;


	public UtilizationSequence() {
		this.samples = new LinkedList<>();
		this.pastSamples = new LinkedList<>();
		timestamps = new ArrayList<>();
	}

	public void insertSample(UtilizationSequenceSample sample) {
		int index = 0;
		while (index < samples.size() && samples.get(index).compareTo(sample) < 0) {
			index++;
		}
		samples.add(index, sample);
		timestamps.add(sample.getBeginTimestamp());
		timestamps.add(sample.getEndTimestamp());
		Collections.sort(timestamps);
		if(garbageCount++ > GARBAGE_THRESH) {
			optimize(edu.kit.cloudSimStorage.helper.TimeHelper.getInstance().now());
			garbageCount = 0;
		}
	}

	/**
	 * Returns the value to a given timestamp in the sequence
	 *
	 * @param timestamp
	 * @return
	 */
	public double getValuesAt(long timestamp) {
		double result = 0;
		for (UtilizationSequenceSample s : samples) {
			if (s.during(timestamp))
				result += s.getValue();

			if (s.getBeginTimestamp() > timestamp) //abort if no event can be during given timestamp (ordered list)
				break;
		}

		return result;
	}

	/**
	 * Returns the next timestamp where the sequence changes.
	 *
	 * @param timestamp
	 * @return
	 */
	public long getNextSamplePointFrom(long timestamp) {
		for (long current : timestamps) {
			if (current > timestamp)
				return current;
		}

		return -1;
	}

	/**
	 * Indicates whether there is a change in the sequence after the given timestamp.
	 *
	 * @param timestamp timestamp to probe
	 * @return true if the value will change after the timestamp.
	 */
	public boolean hasSamplePointBeyond(long timestamp) {
		return getNextSamplePointFrom(timestamp) != -1;
	}


	/**
	 * Returns the number of samples
	 *
	 * @return
	 */
	public int getNumSamples() {
		return samples.size();
	}

	/**
	 * Returns the duration from the beginning of the very first sample until the end of very last one
	 *
	 * @return duration in ms
	 */
	public long getDuration() {
		if (getNumSamples() == 0)
			return 0;
		if (getNumSamples() == 1)
			return samples.get(0).getDuration();
		return getLastSamplePoint() - getFirstSamplePoint();
	}


	/**
	 * Removes all samples that end before the given timestamp and returns them
	 *
	 * @param currentTime current simulation time
	 * @return all items that are removed
	 */
	public List<UtilizationSequenceSample> optimize(long currentTime) {
		List<UtilizationSequenceSample> move = new ArrayList<>();
		for (UtilizationSequenceSample s : samples) {
			if (s.getEndTimestamp() <= currentTime)
				move.add(s);
		}

		List<Long> oldTimestamps = new ArrayList<>();
		for (long ts : timestamps) {
			if (ts <= currentTime)
				oldTimestamps.add(ts);
		}

		timestamps.removeAll(oldTimestamps);
		samples.removeAll(move);
		return move;
	}

	/**
	 * Returns the first point where a sample has a value (value can be can be 0), or -1 if there are no samples.
	 *
	 * @return timestamp of last sample or -1
	 */
	public long getLastSamplePoint() {
		if (timestamps.size() > 0)
			return timestamps.get(timestamps.size() - 1);
		return 1;
	}

	/**
	 * Returns the last point where a sample has a value (value can be can be 0), or -1 if there are no samples.
	 *
	 * @return timestamp of first sample or -1
	 */
	public long getFirstSamplePoint() {
		if (timestamps.size() > 0)
			return timestamps.get(0);
		return -1;
	}
}
