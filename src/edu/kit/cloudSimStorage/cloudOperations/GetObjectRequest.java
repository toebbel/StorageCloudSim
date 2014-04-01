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

import edu.kit.cloudSimStorage.cdmi.CdmiId;
import edu.kit.cloudSimStorage.cdmi.CdmiOperationVerbs;

/**
 * Requests an object from the Cloud.
 *
 * Response is modeled in {@link edu.kit.cloudSimStorage.cloudOperations.GetObjectResponse}
 * @author Tobias Sturm, 5/27/13 4:31 PM */
public class GetObjectRequest extends CloudRequest {
	public CdmiId getRequestedID() {
		return requestedID;
	}

	public String getRequestedName() {
		return requestedName;
	}

	public String getRequestedContainer() {
		return requestedContainer;
	}

	CdmiId requestedID;
	String requestedName;
	String requestedContainer;

	public GetObjectRequest(String rootUrl, String containerName, String objectName, int user) {
		super(CdmiOperationVerbs.GET, rootUrl + containerName + objectName, user, 0, CloudRequest.GET);
		requestedContainer = containerName;
		requestedName = objectName;
		requestedID = CdmiId.UNKNOWN;

	}

	public GetObjectRequest(String rootUrl, String objectID, int user) {
		super(CdmiOperationVerbs.GET, rootUrl + "object_by_id/" + objectID, user, 0, CloudRequest.GET);
		requestedName = requestedContainer = "";
		requestedID = new CdmiId(objectID);
	}

	@Override
	public String toString() {
		return requestString + "(GET Object by user " + user + ")";
	}
}
