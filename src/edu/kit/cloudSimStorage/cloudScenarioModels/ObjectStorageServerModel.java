/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.cloudScenarioModels;

import edu.kit.cloudSimStorage.storageModel.resourceUtilization.TimeawareResourceLimitation;
import org.simpleframework.xml.Default;

import java.util.List;

/**
 * Models a object storage server that can be serialized/deserialized from XML
 * @author Tobias Sturm
 */
@Default
public class ObjectStorageServerModel {
	/**
	 * name of the server
	 */
	public String name;

	/**
	 * IO limits of the server
	 */
	public TimeawareResourceLimitation ioLimitations;

	/**
	 * disks attached to this server
	 */
	public List<ObjectStorageDiskModel> disks;
}
