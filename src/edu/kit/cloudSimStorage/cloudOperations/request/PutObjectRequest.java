/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.cloudOperations.request;

import edu.kit.cloudSimStorage.cdmi.CdmiMetadata;
import edu.kit.cloudSimStorage.cdmi.CdmiOperationVerbs;

/**
 * Requests a PUT object operation
 *
 * The name of the object can be empyt.
 *
 * Response is modeled as {@link edu.kit.cloudSimStorage.cloudOperations.response.PutObjectResponse}
 *
 * @author Tobias Sturm, 5/22/13 2:21 PM */
public class PutObjectRequest extends CloudRequest {
	protected String containerName;
	protected String objectName;
	protected CdmiMetadata metadata;

	public PutObjectRequest(String containerName, String objectName, CdmiMetadata metadata, int user) {
		super(CdmiOperationVerbs.PUT, "/" + containerName + "/" + objectName, user, Long.valueOf(metadata.get(CdmiMetadata.SIZE)), PUT);
		this.containerName = containerName;
		this.objectName = objectName;
		this.metadata = metadata;
	}

	public String getContainerName() {
		return containerName;
	}

	public String getObjectName() {
		return objectName;
	}

	public CdmiMetadata getMetadata() {
		return metadata;
	}

	@Override
	public String toString() {
		return super.getRequestString() + " (PUT object by user " + super.getUser() + ")";
	}
}
