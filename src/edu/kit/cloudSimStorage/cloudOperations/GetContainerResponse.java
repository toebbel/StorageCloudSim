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

import edu.kit.cloudSimStorage.cdmi.CdmiMetadata;

import java.util.List;

/**
 * Models the response of a {@link edu.kit.cloudSimStorage.cloudOperations.GetContainerRequest}
 *
 * The response contains the metadata of a container. The list of children is optional.
 *
 * @author Tobias Sturm, 5/22/13 12:40 PM */
public class GetContainerResponse extends CloudResponse<GetContainerRequest> {
	protected List<String> children;
	protected CdmiMetadata metadata;

	/**
	 * Creates a response, that follows to a GET on a container
	 *
	 * @param op       the request, that triggered this response
	 * @param metadata the metadata of the container
	 * @param result   names of children in requested container
	 */
	public GetContainerResponse(GetContainerRequest op, CdmiMetadata metadata, List<String> result) {
		super(op);
		this.children = result;
		this.metadata = metadata;
	}

	/**
	 * The names of the children inside the requested container
	 *
	 * @return
	 */
	public List<String> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		String result = "GetContainerResponse: \n";
		for (String child : children)
			result += child + "\n";
		return result + "metadata: " + metadata;
	}
}
