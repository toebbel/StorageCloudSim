/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.monitoring.sampleSequenceOperators;

/** @author Tobias Sturm, 6/24/13 4:42 PM */
class Average extends SampleCombinator {
	double val;
	int count;

	@Override
	protected void reset() {
		val = count = 0;
	}

	@Override
	public void addSample(double val) {
		this.val += val;
		count++;
	}

	@Override
	protected double getNeutralValue() {
		return 0;
	}

	@Override
	public double getResult() {
		return val / count;
	}
}
