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

import edu.kit.cloudSimStorage.cdmi.CdmiDataObject;

/**
 * Cloud internal state of a {@link edu.kit.cloudSimStorage.cloudOperations.GetObjectRequest}
 *
 * @author Tobias Sturm, 6/5/13 5:37 PM */
public class GetObjectScheduleEntry extends CloudScheduleEntry<GetObjectRequest> {
	private CdmiDataObject object;

	public GetObjectScheduleEntry(GetObjectRequest request, int requestor) {
		super(request, requestor);
	}


	public void setObject(CdmiDataObject object) {
		this.object = object;
	}

	@Override
	public CloudResponse generateResponse() {
		return new GetObjectResponse(getRequest(), object);
	}
}
