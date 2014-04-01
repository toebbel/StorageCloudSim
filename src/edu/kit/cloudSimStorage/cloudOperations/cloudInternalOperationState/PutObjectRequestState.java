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

import edu.kit.cloudSimStorage.cdmi.CdmiDataObject;
import edu.kit.cloudSimStorage.cloudOperations.request.PutObjectRequest;
import edu.kit.cloudSimStorage.cloudOperations.response.PutObjectResponse;
import edu.kit.cloudSimStorage.storageModel.StorageBlobLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Cloud internal representation of the processing of a {@link edu.kit.cloudSimStorage.cloudOperations.request.PutObjectRequest}
 *
 * @author Tobias Sturm, 5/27/13 5:01 PM */
public class PutObjectRequestState extends CloudRequestState<PutObjectRequest> {

	CdmiDataObject object;
	PutObjectRequestType type;
	private List<StorageBlobLocation> usedLocations;

	public PutObjectRequestState(PutObjectRequest request, int requestor) {
		super(request, requestor);
		usedLocations = new ArrayList<>();
	}

	public void setObject(CdmiDataObject object) {
		this.object = object;
	}

	@Override
	public PutObjectResponse generateResponse() {
		assert object != null;
		return new PutObjectResponse(request, object);
	}

	public void setType(PutObjectRequestType type) {
		this.type = type;
	}

	public void addUsedLocation(StorageBlobLocation location) {
		usedLocations.add(location);
	}

	public List<StorageBlobLocation> getUsedLocations() {
		return usedLocations;
	}


	public enum PutObjectRequestType {
		Creation,
		Update
	}
}
