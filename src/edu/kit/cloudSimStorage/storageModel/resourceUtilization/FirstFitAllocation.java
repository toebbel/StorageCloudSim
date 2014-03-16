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

import edu.kit.cloudSimStorage.helper.FileSizeHelper;
import org.cloudbus.cloudsim.core.CloudSim;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/** @author Tobias Sturm, 6/21/13 4:35 PM */
@Root
public class FirstFitAllocation implements TimeawareResourceLimitation {

	//the absolute maxAmountPerTime. available amount of the resource
	@Attribute(name = "maxRate")
	private double maxAmountPerTime;

	private UtilizationSequence utilization;

	/**
	 * Create a resource, that has the given rate [amount/ms^-1].
	 *
	 * @param maxRate the max possible rate of this resource
	 */
	public FirstFitAllocation(@Attribute(name = "maxRate") double maxRate) {
		this.maxAmountPerTime = maxRate;
		utilization = new UtilizationSequence();
	}

	/**
	 * More convenient way to create an instance
	 *
	 * @param volumePerSecond for example 100MB/s -> 100
	 * @param magnitude       magnitude of volume
	 * @return new instance with given limitations (in byte / ms)
	 */
	public static TimeawareResourceLimitation create(int volumePerSecond, FileSizeHelper.Magnitude magnitude) {
		return new FirstFitAllocation(FileSizeHelper.toBytes(volumePerSecond, magnitude) * 1000);
	}

	@Override
	public UtilizationSequence use(double amount, double maxRate) {
		return use(CloudSim.getSimulationCalendar().getTimeInMillis(), amount, maxRate);
	}

	@Override
	public UtilizationSequence use(double amount) {
		return use(CloudSim.getSimulationCalendar().getTimeInMillis(), amount, maxAmountPerTime);
	}

	@Override
	public UtilizationSequence use(long start, double amount) {
		return use(start, amount, maxAmountPerTime);
	}

	@Override
	public UtilizationSequence use(long start, double amount, double maxRate) {
		UtilizationSequence result = new UtilizationSequence();

		long from = start;
		double remainingAmount = amount;
		double rate = Math.min(maxRate, maxAmountPerTime);

		while (remainingAmount > 0 && utilization.hasSamplePointBeyond(from)) {
			double limitation = utilization.getValuesAt(from + 1);
			double remainingResourceRate = Math.min(maxAmountPerTime - limitation, rate);

			//sequence won't change before variable {@code to}. Take complete block, or so much time inside the block to work off remainingAmount.
			long to = utilization.getNextSamplePointFrom(from);
			long sampleDuration = Math.min(to - from, (long) (remainingAmount / rate));

			if (sampleDuration > 0 && remainingResourceRate > 0) {
				remainingAmount -= sampleDuration * remainingResourceRate;

				UtilizationSequenceSample use = new UtilizationSequenceSample(from, from + sampleDuration, remainingResourceRate);
				utilization.insertSample(use);
				result.insertSample(use);
			}
			from = to;
		}

		if (remainingAmount > rate) {
			long duration = (long) (remainingAmount / rate);
			remainingAmount -= duration * rate;
			UtilizationSequenceSample use = new UtilizationSequenceSample(from, from + duration, rate);
			utilization.insertSample(use);
			result.insertSample(use);
		}

		assert remainingAmount < maxRate;
		return result;
	}

	@Override
	public long getFirstFreeTimeslot(long time) {
		long probe = time;
		while (maxAmountPerTime - utilization.getValuesAt(probe) <= (maxAmountPerTime / 1000)) {//Utilization has to drop below 99.9%
			probe = utilization.getNextSamplePointFrom(probe);
		}
		return probe;
	}

	@Override
	public void removeSamplesBefore(long time) {
		utilization.optimize(time);
	}

	@Override
	public double getValueAt(long time) {
		return utilization.getValuesAt(time);
	}

	@Override
	public double getMaxPossible() {
		return maxAmountPerTime;
	}
}
