/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.cloudBroker;

/** @author Tobias Sturm, 6/26/13 4:11 PM */
public class UserMetaRequest extends UserRequest {
	public static final int DISCOVER_CLOUD = offset + 8;
	public static final int BROKER_FINISHED = offset + 9;
	public static final int START_SEQUENCE = offset + 10;

	protected int sequenceID;

	public static UserMetaRequest discoverCloud(int discoveryRequestID) {
		UserMetaRequest request = new UserMetaRequest();
		request.opCode = DISCOVER_CLOUD;
		request.sequenceID = discoveryRequestID;
		return request;
	}

	public static UserMetaRequest associateWithSequence(UserRequest req, int seqID) {
		UserMetaRequest result = (UserMetaRequest) req;
		result.sequenceID = seqID;
		return result;
	}
}
