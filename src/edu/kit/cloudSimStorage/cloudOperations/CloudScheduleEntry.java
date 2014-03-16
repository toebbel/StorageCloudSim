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


import edu.kit.cloudSimStorage.monitoring.OperationTimeTraceSample;

/** @author Tobias Sturm, 5/27/13 4:25 PM */
public class CloudScheduleEntry<T extends CloudRequest> extends OperationTimeTraceSample {

	T request;
	int inquiringPartner = 0;


	public T getRequest() {
		return request;
	}

	public int getInquiringPartner() {
		return inquiringPartner;
	}

	public CloudScheduleEntry(T request, int inquiringPartner) {
		super(request.toString());
		assert request != null;

		this.request = request;
		this.inquiringPartner = inquiringPartner;
	}

	public CloudResponse<T> generateResponse() {
		return new CloudResponse<>(request);
	}

	public String getOperationID() {
		return getRequest().getOperationID();
	}

}
