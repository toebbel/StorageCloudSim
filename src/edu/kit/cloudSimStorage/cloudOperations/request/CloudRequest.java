/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.cloudOperations.request;

import edu.kit.cloudSimStorage.cdmi.CdmiOperationVerbs;
import edu.kit.cloudSimStorage.monitoring.OperationTimeTraceSample;

/**
 * General model for CloudRequests. Instances of this class are sent to the {@link edu.kit.cloudSimStorage.StorageCloud}
 *
 * @author Tobias Sturm, 5/16/13 5:33 PM */
public class CloudRequest extends OperationTimeTraceSample {

	private final int tag;

	/** unique string that ties request and response together */
	protected String operationID;

	/** The user who requested the associatedRequest */
	protected int user;

	/** The transfer size from and to the StorageCloud that is associated with this associatedRequest */
	protected long size;

	/** The string that request the resource to PUT/DELETE/GET */
	protected String requestString;

	/** The CDMI operation verb */
	CdmiOperationVerbs verb;

	public CloudRequest(CdmiOperationVerbs verb, String requestString, int user, long size, int tag) {
		super(verb.toString() + requestString);
		this.requestString = requestString;
		this.operationID = java.util.UUID.randomUUID().toString();
		this.user = user;
		this.size = size;
		this.verb = verb;
		this.tag = tag;
	}

	/**
	 * The cmdi operation verb associated with this request
	 * @return the operation verb
	 */
	public CdmiOperationVerbs getVerb() {
		return verb;
	}

	/**
	 * The string that request the resource to PUT/DELETE/GET
	 *
	 * @return request string
	 */
	public String getRequestString() {
		return requestString;
	}

	/**
	 * Returns the unique string that ties request and response together
	 *
	 * @return unique string
	 */
	public String getOperationID() {
		return operationID;
	}

	/**
	 * Returns the upload/download size that is associated with this associatedRequest
	 *
	 * @return up/download in KByte
	 */
	public long getSize() {
		return size;
	}

	/**
	 * The user that requested this opertation
	 *
	 * @return sequenceID
	 */
	public int getUser() {
		return user;
	}

	private static final int offset = 100;
	public static final int GET = offset;
	public static final int PUT = offset + 1;
	public static final int DELETE = offset + 2;
	public static final int ACK = offset + 3; //send to sender if all prerequisites are fulfilled
	public static final int SUCC = offset + 4; //send to sender if operation succeeded
	public static final int FAIL = offset + 5; //send to sender if operation failed

	/**
	 * The tag associated with this operation
	 * @return the tag
	 */
	public int getTag() {
		return tag;
	}
}
