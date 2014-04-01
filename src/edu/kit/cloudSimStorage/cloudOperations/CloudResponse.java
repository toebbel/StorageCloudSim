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

/**
 * General model for responses to {@link edu.kit.cloudSimStorage.cloudOperations.CloudRequest}
 *
 * @author Tobias Sturm, 5/27/13 4:22 PM */
public class CloudResponse<T extends CloudRequest> {

	private T request;

	/**
	 * Creates a response for a given request
	 * @param request
	 */
	public CloudResponse(T request) {
		this.request = request;
	}

	/**
	 * The operation ID that is associated with this request/repsonse
	 * @return
	 */
	public String getOperationID() {
		return request.getOperationID();
	}

	@Override
	public String toString() {
		return "Response to request '" + request.toString() + "'";
	}
}
