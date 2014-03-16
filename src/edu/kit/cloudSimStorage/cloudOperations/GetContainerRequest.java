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

/** @author Tobias Sturm, 5/27/13 4:29 PM */
public class GetContainerRequest extends CloudRequest {

	private String containerName;

	public GetContainerRequest(String rootUrl, String containerName, int user) {
		super(CdmiOperationVerbs.GET, rootUrl + containerName, user, 0, CloudRequest.GET);

		this.containerName = containerName;
	}

	public String getContainerName() {
		return containerName;
	}

	@Override
	public String toString() {
		return "GET " + containerName + " (get container of user " + user + ")";
	}
}
