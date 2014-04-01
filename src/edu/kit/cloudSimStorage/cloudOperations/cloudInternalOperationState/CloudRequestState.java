/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.cloudOperations.cloudInternalOperationState;


import edu.kit.cloudSimStorage.cloudOperations.request.CloudRequest;
import edu.kit.cloudSimStorage.cloudOperations.response.CloudResponse;
import edu.kit.cloudSimStorage.monitoring.OperationTimeTraceSample;

/**
 * Cloud internal state representation of the processing of a {@link edu.kit.cloudSimStorage.cloudOperations.request.CloudRequest}
 * @author Tobias Sturm, 5/27/13 4:25 PM */
public class CloudRequestState<T extends CloudRequest> extends OperationTimeTraceSample {

	T request;
	int inquiringPartner = 0;

	/**
	 * The request that is associated with this schedule entry
	 * @return the request of this schedule entry
	 */
	public T getRequest() {
		return request;
	}

	/**
	 * The user that created the request
	 * @return user id
	 */
	public int getInquiringPartner() {
		return inquiringPartner;
	}

	/**
	 * Creates an instance of {@link CloudRequestState} with a given request and user
	 * @param request request that will be processed
	 * @param inquiringPartner user that created the request
	 */
	public CloudRequestState(T request, int inquiringPartner) {
		super(request.toString());
		assert request != null;

		this.request = request;
		this.inquiringPartner = inquiringPartner;
	}

	/**
	 * Generates a response for the request of this schedule entry
	 * @return the response
	 */
	public CloudResponse<T> generateResponse() {
		return new CloudResponse<>(request);
	}

	/**
	 * Returns the operation id that is associated with this request / response
	 * @return
	 */
	public String getOperationID() {
		return getRequest().getOperationID();
	}

}
