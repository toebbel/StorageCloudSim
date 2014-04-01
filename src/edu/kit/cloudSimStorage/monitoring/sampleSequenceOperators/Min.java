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

import edu.kit.cloudSimStorage.monitoring.TupleSequence;

/** @author Tobias Sturm, 6/24/13 4:42 PM */
class Min extends SampleCombinator {
	 double val = Double.MAX_VALUE;

	@Override
	protected void reset() {
		val = Double.MAX_VALUE;
	}

	@Override
	public void addSample(double val) {
		val = Math.min(val, this.val);
	}

	@Override
	protected double getNeutralValue() {
		return Double.MAX_VALUE;
	}

	@Override
	public double getResult() {
		return val;
	}

	@Override
	protected void prepareStream(TupleSequence<Double> input){
		SampleCombinator.uniquifyIndex_takeMinValue(input);
	}

}
