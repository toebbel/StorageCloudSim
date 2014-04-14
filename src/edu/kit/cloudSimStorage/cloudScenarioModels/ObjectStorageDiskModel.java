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

import edu.kit.cloudSimStorage.storageModel.IObjectStorageDrive;
import org.simpleframework.xml.Default;

/**
 * Model of a object storage server disk which is installed in a server and can be serialized to XML.
 *
 * @author Tobias Sturm
 */
@Default
public class ObjectStorageDiskModel {
	public IObjectStorageDrive drive;

	/**
	 * Mount point in server
	 */
	public String name;
}
