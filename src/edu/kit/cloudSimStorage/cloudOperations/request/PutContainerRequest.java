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
 * Requests a PUT container operation
 *
 * Containers can't be nested, therefore every container is created as direct child of the user's root contianer
 *
 * Response is modeled as {@link edu.kit.cloudSimStorage.cloudOperations.response.PutContainerResponse}
 *
 * @author Tobias Sturm, 5/22/13 2:23 PM */
public class PutContainerRequest extends CloudRequest {
	protected CdmiMetadata metadata;
	protected String containerName;

	public PutContainerRequest(String containerName, CdmiMetadata metadata, int user) {
		super(CdmiOperationVerbs.PUT, "/" + containerName + "/", user, 0, PUT);
		this.containerName = containerName;
		this.metadata = metadata;
	}

	public CdmiMetadata getMetadata() {
		return metadata;
	}

	public String getContainerName() {
		return containerName;
	}

	@Override
	public String toString() {
		return super.getRequestString() + " (PUT Container, requested by " + super.getUser() + ")";
	}
}
