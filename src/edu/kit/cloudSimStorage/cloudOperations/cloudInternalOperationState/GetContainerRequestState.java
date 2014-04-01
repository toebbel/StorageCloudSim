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

import edu.kit.cloudSimStorage.cdmi.CdmiId;
import edu.kit.cloudSimStorage.cdmi.CdmiMetadata;
import edu.kit.cloudSimStorage.cloudOperations.response.CloudResponse;
import edu.kit.cloudSimStorage.cloudOperations.request.GetContainerRequest;
import edu.kit.cloudSimStorage.cloudOperations.response.GetContainerResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Cloud internal representation of a {@link edu.kit.cloudSimStorage.cloudOperations.request.GetContainerRequest}
 *
 * @author Tobias Sturm, 6/5/13 5:41 PM */
public class GetContainerRequestState extends CloudRequestState<GetContainerRequest> {
	private CdmiMetadata metadata;
	private List<CdmiId> childrenIDs;

	public GetContainerRequestState(GetContainerRequest request, int requestor) {
		super(request, requestor);
	}

	/**
	 * Saves a copy of the given parameters in order to be able to generate the response
	 *
	 * @param metadata
	 * @param childrenIDs
	 */
	public void setResult(CdmiMetadata metadata, List<CdmiId> childrenIDs) {
		metadata = new CdmiMetadata();
		metadata.mergeWith(metadata);
		this.childrenIDs = new ArrayList<CdmiId>(childrenIDs);
	}

	@Override
	public CloudResponse generateResponse() {
		ArrayList<String> ids = new ArrayList<>();
		for (CdmiId id : childrenIDs) {
			ids.add(id.toString());
		}
		return new GetContainerResponse(request, metadata, ids);
	}
}
