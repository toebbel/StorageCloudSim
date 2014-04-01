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

import edu.kit.cloudSimStorage.cdmi.CdmiObjectContainer;
import edu.kit.cloudSimStorage.cloudOperations.response.CloudResponse;
import edu.kit.cloudSimStorage.cloudOperations.request.PutContainerRequest;
import edu.kit.cloudSimStorage.cloudOperations.response.PutContainerResponse;

/**
 * Cloud internal representation of the processing of a {@link edu.kit.cloudSimStorage.cloudOperations.request.PutContainerRequest}
 *
 * @author Tobias Sturm, 6/5/13 3:37 PM */
public class PutContainerRequestState extends CloudRequestState<PutContainerRequest> {


	/** Stores the created Container to generate the response */
	private CdmiObjectContainer container;

	public PutContainerRequestState(PutContainerRequest request, int requestor) {
		super(request, requestor);
	}


	/**
	 * Sets the created container. This is required to generate the response.
	 *
	 * @param container
	 */
	public void setContainer(CdmiObjectContainer container) {
		this.container = container;
	}

	@Override
	public CloudResponse generateResponse() {
		assert container != null;
		return new PutContainerResponse(getRequest(), container);
	}
}
