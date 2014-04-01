/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.cloudOperations.response;

import edu.kit.cloudSimStorage.cdmi.CdmiDataObject;
import edu.kit.cloudSimStorage.cloudOperations.request.PutObjectRequest;

/**
 * Response to a {@link edu.kit.cloudSimStorage.cloudOperations.request.PutObjectRequest}
 *
 * @author Tobias Sturm, 5/22/13 3:10 PM */
public class PutObjectResponse extends CloudResponse<PutObjectRequest> {
	protected CdmiDataObject object;

	public CdmiDataObject getObject() {
		return object;
	}

	public PutObjectResponse(PutObjectRequest request, CdmiDataObject object) {
		super(request);
		this.object = object;
	}

	@Override
	public String toString() {
		return "PutObjectResponse: created Object '" + object + "'";
	}
}
