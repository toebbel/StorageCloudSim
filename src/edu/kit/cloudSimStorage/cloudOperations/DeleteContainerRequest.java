/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.cloudOperations;

import edu.kit.cloudSimStorage.cdmi.CdmiOperationVerbs;

/**
 * Requests a DELETE container
 *
 * Response is modeled with a generic {@link edu.kit.cloudSimStorage.cloudOperations.CloudResponse}
 * @author Tobias Sturm, 6/5/13 3:11 PM */
public class DeleteContainerRequest extends CloudRequest {

	private String containerName;

	public DeleteContainerRequest(String containerName, int user) {
		super(CdmiOperationVerbs.DELETE, containerName, user, 0, CloudRequest.DELETE);
		this.containerName = containerName;
	}

	public String getContainerName() {
		return containerName;
	}

}
