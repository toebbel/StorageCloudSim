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

/** @author Tobias Sturm, 5/27/13 4:22 PM */
public class CloudResponse<T extends CloudRequest> {
	private T request;

	public CloudResponse(T request) {
		this.request = request;
	}

	public String getOperationID() {
		return request.getOperationID();
	}

	@Override
	public String toString() {
		return "Response to request '" + request.toString() + "'";
	}
}
