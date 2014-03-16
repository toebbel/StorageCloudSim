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

/** @author Tobias Sturm, 6/24/13 4:40 PM */
class Sum extends SampleCombinator {
	double sum = 0;

	@Override
	protected void reset() {
		sum = 0;
	}

	@Override
	public void addSample(double val) {
		sum += val;
	}

	@Override
	protected double getNeutralValue() {
		return 0;
	}

	@Override
	public double getResult() {
		return sum;
	}
}
