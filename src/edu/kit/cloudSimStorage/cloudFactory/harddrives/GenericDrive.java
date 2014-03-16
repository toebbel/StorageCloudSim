/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.cloudFactory.harddrives;

import edu.kit.cloudSimStorage.storageModel.IObjectStorageDrive;
import edu.kit.cloudSimStorage.storageModel.ObjectStorageDrive;
import edu.kit.cloudSimStorage.storageModel.ObjectStorageServer;
import edu.kit.cloudSimStorage.storageModel.resourceUtilization.TimeawareResourceLimitation;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;


/** @author Tobias Sturm, 8/7/13 12:57 PM */
public class GenericDrive extends ObjectStorageDrive {


	private int busLimit;

	public GenericDrive(@Attribute(name = "name") String name) {
		super(name);
	}

	public void init(@Element(name = "capacity") long capacity, @Element(name = "writeRate")  double writeRate, @Element(name = "readRate")  double readRate,
@Element(name = "writeLatency") double writeLatency, @Element(name = "readLatency") double readLatency, @Element(name = "ioLimits") TimeawareResourceLimitation ioLimitation) {
		this.capacity = capacity;
		this.readRate = readRate;
		this.writeRate = writeRate;
		this.readLatency = readLatency;
		this.writeLatency = writeLatency;
		this.ioLimitation = ioLimitation;
	}

	public GenericDrive(String rootUrl, ObjectStorageServer location, String name) {
		super(rootUrl, location, name);
	}

	@Override
	public IObjectStorageDrive clone(ObjectStorageServer location, String name) {
		GenericDrive result = new GenericDrive(rootUrl, location,name);
		result.init(capacity, writeRate,readRate,writeLatency,readLatency,ioLimitation);
		return result;
	}
}
