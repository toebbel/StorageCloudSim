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

/**
 * @author Tobias Sturm, 4/1/14 12:47 PM
 */
public abstract class SequenceValueOperation {

	public abstract double getResult();

	public abstract void addSample(double val);

	public abstract void reset();

}
