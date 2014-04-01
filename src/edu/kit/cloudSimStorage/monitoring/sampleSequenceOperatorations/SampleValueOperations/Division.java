/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.monitoring.sampleSequenceOperatorations.SampleValueOperations;

import edu.kit.cloudSimStorage.monitoring.sampleSequenceOperatorations.SequenceOperations;

/** @author Tobias Sturm, 6/24/13 5:40 PM */
public class Division extends SequenceOperations {

	double dividend= 0;
	double result = 0;
	boolean hasDividend = false;

	@Override
	protected void reset() {
		dividend = 0;
		result = 0;
		hasDividend = false;
	}

	@Override
	protected void addSample(double val) {
		if(hasDividend) {
			if(val == 0)
				result = dividend > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
			else
				result = dividend / val;
		} else {
			dividend = val;
			hasDividend = true;
		}
	}

	@Override
	protected double getNeutralValue() {
		return 1;
	}

	@Override
	protected double getResult() {
		assert hasDividend;
		hasDividend = false;
		return result;
	}
}
