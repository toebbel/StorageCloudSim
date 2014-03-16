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
import org.simpleframework.xml.Root;

/** @author Tobias Sturm, 6/23/13 2:47 PM */
@Root
public class UnlimitedResource implements TimeawareResourceLimitation {


	@Override
	public UtilizationSequence use(double amount, double maxRate) {
		UtilizationSequence seq = new UtilizationSequence();
		seq.insertSample(new UtilizationSequenceSample((int) (amount / maxRate), maxRate));
		return seq;
	}

	@Override
	public UtilizationSequence use(double amount) {
		return use(TimeHelper.getInstance().now(), amount, amount);
	}

	@Override
	public UtilizationSequence use(long start, double amount) {
		return use(start, amount, amount);
	}

	@Override
	public UtilizationSequence use(long start, double amount, double maxRate) {
		UtilizationSequence seq = new UtilizationSequence();
		long duration = (new Double(amount / maxRate)).longValue();
		seq.insertSample(new UtilizationSequenceSample(start, start + duration, maxRate));
		return seq;
	}

	@Override
	public long getFirstFreeTimeslot(long time) {
		return time;
	}

	@Override
	public void removeSamplesBefore(long time) {}

	@Override
	public double getValueAt(long now) {
		return 0;
	}

	@Override
	public double getMaxPossible() {
		return 1;
	}
}
