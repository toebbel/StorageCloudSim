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

import edu.kit.cloudSimStorage.helper.TimeHelper;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * Use multiple instances of this class inside an {@link UtilizationSequence} to measure a non-constant use of a resource (e.G. Bandwith). A Sample begins at a given time (measured in ms of {@link CloudSim#getSimulationCalendar()} and ends at a specific time. Therefor a sample has a duration (which can be 0ms) and a value, that is constant over the described time period.
 * <p/>
 * Samples are compared by their begin timestamp (natural order). If two samples begin at the same time, the one that is shorter is defined as "smaller" as the other one
 *
 * @author Tobias Sturm, 6/21/13 3:04 PM
 */
public class UtilizationSequenceSample implements Comparable<UtilizationSequenceSample> {

	private long begin, end; //timestamps of simulated time
	private double value; //sample value

	/**
	 * Creates an instance that begins at given timestamp and ends a given timestamp
	 *
	 * @param begin begin timestamp in ms
	 * @param end   end timestamp in ms
	 * @param value value that is constant over given duration
	 */
	public UtilizationSequenceSample(long begin, long end, double value) {
		assert begin <= end;
		assert begin >= 0;

		this.begin = begin;
		this.end = end;
		this.value = value;
	}

	/**
	 * Creates an instance that begins at the current simulation time and lasts for the given time (in ms)
	 *
	 * @param duration in ms.
	 * @param value    value that is constant over given duration
	 */
	public UtilizationSequenceSample(int duration, double value) {
		assert duration >= 0;
		assert begin > 0;

		this.begin = TimeHelper.getInstance().now();
		this.end = begin + duration;
		this.value = value;
	}


	/**
	 * The beginning of the sample
	 *
	 * @return timestamp in ms
	 */
	public long getBeginTimestamp() {
		return begin;
	}

	/**
	 * The end of the sample
	 *
	 * @return in ms
	 */
	public long getEndTimestamp() {
		return end;
	}

	/**
	 * The duration of the sample
	 *
	 * @return in ms
	 */
	public long getDuration() {
		return end - begin;
	}

	/**
	 * The value that is fixed for this sample
	 *
	 * @return
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Adds the given value to the value of this sample
	 *
	 * @param delata positive or negative delta
	 */
	public void addValue(double delata) {
		value += delata;
	}

	/**
	 * Splits this sample into two Samples.
	 * <p/>
	 * Will create two new instances. The first one begins at the same timestamp as this instance and runs until the split point. The other one starts at the split point and ends at the end of this instance.
	 * <p/>
	 * Both samples will have the same value.
	 *
	 * @param at timestamp where to split this sample
	 * @return
	 */
	public UtilizationSequenceSample[] splitAt(long at) {
		assert at >= begin;
		assert at <= end;

		UtilizationSequenceSample[] result = new UtilizationSequenceSample[2];
		result[0] = new UtilizationSequenceSample(begin, at, value);
		result[1] = new UtilizationSequenceSample(at, end, value);
		return result;
	}

	@Override
	public int compareTo(UtilizationSequenceSample o) {
		//TODO make this smarter than this
		if (getBeginTimestamp() < o.getBeginTimestamp())
			return -1;
		else if (o.getBeginTimestamp() == getBeginTimestamp()) {
			if (getEndTimestamp() < o.getEndTimestamp())
				return -1;
			else if (getEndTimestamp() > o.getEndTimestamp())
				return 1;
			else
				return 0;
		} else
			return 1;
	}

	/**
	 * Probes if a timestamp lies within this sample
	 *
	 * @param timestamp in ms
	 * @return true if timestamp is after or during beginning and timestamp is before or during end
	 */
	public boolean during(long timestamp) {
		return getBeginTimestamp() <= timestamp && getEndTimestamp() >= timestamp;
	}

	@Override
	public String toString() {
		return "[" + getBeginTimestamp() + ", " + getEndTimestamp() + "] => " + getValue();
	}
}
