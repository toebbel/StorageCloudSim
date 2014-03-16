/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.storageModel;

import edu.kit.cloudSimStorage.cdmi.CdmiDataObject;

/**
 * A Blob is a tuple of an {@link CdmiDataObject} and a location.
 *
 * @author Tobias Sturm
 *         created at 4/29/13, 2:17 PM
 */
public class ObjectStorageBlob {

	private StorageBlobLocation location;
	private CdmiDataObject data;

	public ObjectStorageBlob(StorageBlobLocation location, CdmiDataObject data) {
		this.location = location;
		this.data = data;
	}

	public StorageBlobLocation getLocation() {
		return location;
	}

	public CdmiDataObject getData() {
		return data;
	}
}
