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

/** @author Tobias Sturm, 6/23/13 2:46 PM */
public interface TimeawareResourceLimitation {
	/**
	 * Use the resource
	 *
	 * @param amount  amount of units of the use
	 * @param maxRate max possible rate of the resource utilization [amount/ms^-1]
	 * @return the sequence of samples that are produced by this use.
	 */
	UtilizationSequence use(double amount, double maxRate);

	/**
	 * Use the resource
	 *
	 * @param amount amount of units of the use
	 * @return the sequence of samples that are produced by this use.
	 */
	UtilizationSequence use(double amount);

	/**
	 * Use the resource
	 *
	 * @param start  start timestamp of use (ms of simulation time)
	 * @param amount amount of units of the use
	 * @return the sequence of samples that are produced by this use.
	 */
	UtilizationSequence use(long start, double amount);

	/**
	 * Use the resource
	 *
	 * @param start   start timestamp of use (ms of simulation time)
	 * @param amount  amount of units of the use
	 * @param maxRate max possible rate of the resource utilization [amount/ms^-1]
	 * @return the sequence of samples that are produced by this use.
	 */
	UtilizationSequence use(long start, double amount, double maxRate);

	long getFirstFreeTimeslot(long time);

	/**
	 * removes al sequences that end before the given time to speed up further computation
	 * @param time timestamp of current time
	 */
	void removeSamplesBefore(long time);

	double getValueAt(long now);

	double getMaxPossible();
}
