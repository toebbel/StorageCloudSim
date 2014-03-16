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

import edu.kit.cloudSimStorage.cdmi.CdmiId;

/**
 * An {@code StorageBlobLocation} is a triple of contentID, server and drive.
 *
 * @author Tobias Sturm
 *         created at 4/29/13, 2:14 PM
 */
public class StorageBlobLocation {
	private CdmiId contentID;
	private ObjectStorageServer server;
	private String drive;

	public StorageBlobLocation(CdmiId objectID, ObjectStorageServer server, String driveName) {
		this.contentID = objectID;
		this.drive = driveName;
		this.server = server;
	}


	public CdmiId getContentID() {
		return contentID;
	}

	public ObjectStorageServer getServer() {
		return server;
	}

	public String getDriveName() {
		return drive;
	}

	@Override
	public String toString() {
		return "(" + contentID + ", " + server + ", " + drive + ")";
	}
}
