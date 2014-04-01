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
import edu.kit.cloudSimStorage.cloudOperations.request.GetObjectRequest;

/**
 * Response of a {@link edu.kit.cloudSimStorage.cloudOperations.request.GetObjectRequest}.
 *
 * Contains a {@link edu.kit.cloudSimStorage.cdmi.CdmiDataObject} that carries information about the
 * object that is stored in the {@link edu.kit.cloudSimStorage.StorageCloud}
 *
 * @author Tobias Sturm, 6/5/13 5:35 PM */
public class GetObjectResponse extends CloudResponse<GetObjectRequest> {

	public CdmiDataObject getObject() {
		return object;
	}

	CdmiDataObject object;

	public GetObjectResponse(GetObjectRequest request, CdmiDataObject object) {
		super(request);
		this.object = object;
	}

	@Override
	public String toString() {
		return "getObjectResponse: " + object;
	}
}
