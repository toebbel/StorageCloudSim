/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.cloudFactory;

import edu.kit.cloudSimStorage.storageModel.resourceUtilization.TimeawareResourceLimitation;
import org.simpleframework.xml.Default;

import java.util.List;

@Default
public class ObjectStorageServerModel {
	public String name;
	public TimeawareResourceLimitation ioLimitations;
	public List<ObjectStorageDiskModel> disks;
}
