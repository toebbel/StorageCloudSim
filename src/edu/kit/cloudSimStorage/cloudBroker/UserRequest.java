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

import edu.kit.cloudSimStorage.cdmi.CdmiId;
import edu.kit.cloudSimStorage.cdmi.CdmiMetadata;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.Element;

/** @author Tobias Sturm, 6/7/13 3:28 PM */
@Default
public class UserRequest {

	@Element(required = false)
	protected String objectName;
	@Element(required = false)
	protected String containerName;
	@Element(required = true)
	protected String objectID = "UNKNOWN";
	@Element(required = false)
	protected String rootUrl;

	@Element(required = false)
	protected CdmiMetadata metadata;

	@Attribute(required = false)
	protected int delay;
	@Attribute(required = false)
	protected boolean blockingCall;
	@Attribute(required = false)
	protected int opCode;
	@Attribute(required = false)
	protected long size; //byte

	public static final int offset = 0;
	public static final int PUT_OBJECT = offset;
	public static final int PUT_CONTAINER = offset + 1;
	public static final int GET_OBJECT = offset + 2;
	public static final int GET_CONTAINER = offset + 3;
	public static final int DELETE_OBJECT = offset + 4;
	public static final int DELETE_CONTAINER = offset + 5;
	public static final int PAUSE = offset + 6;
	public static final int WAIT = offset + 7;

	public static UserRequest downloadObject(String container, String objectName) {
		UserRequest request = new UserRequest();
		request.opCode = GET_OBJECT;
		request.containerName = container;
		request.objectName = objectName;
		return request;
	}

	public static UserRequest downloadObject(String objectID) {
		UserRequest request = new UserRequest();
		request.opCode = GET_OBJECT;
		request.objectID = objectID;
		return request;
	}

	public static UserRequest putContainer(String containerName) {
		UserRequest request = new UserRequest();
		request.opCode = PUT_CONTAINER;
		request.containerName = containerName;
		request.metadata = new CdmiMetadata();
		return request;
	}

	public static UserRequest putObject(String container, String objectName, long size) {
		UserRequest request = new UserRequest();
		request.containerName = container;
		request.objectName = objectName;
		request.opCode = PUT_OBJECT;
		request.size = size;
		request.metadata = new CdmiMetadata();
		request.metadata.set(CdmiMetadata.SIZE, String.valueOf(size));
		return request;
	}

	public static UserRequest getObject(String containerName, String objectName) {
		UserRequest request = new UserRequest();
		request.objectName = objectName;
		request.containerName = containerName;
		request.opCode = GET_OBJECT;
		return request;
	}

	public static UserRequest getObject(String objectID) {
		UserRequest request = new UserRequest();
		request.objectID = objectID;
		request.opCode = GET_OBJECT;
		return request;
	}

	public static UserRequest getContainer(String containerName) {
		UserRequest request = new UserRequest();
		request.containerName = containerName;
		request.opCode = GET_CONTAINER;
		return request;
	}

	public static UserRequest deleteObject(String container, String objectName) {
		UserRequest request = new UserRequest();
		request.containerName = container;
		request.objectName = objectName;
		request.objectID = "";
		request.opCode = DELETE_OBJECT;
		return request;
	}

	public static UserRequest deleteObject(String objectID) {
		UserRequest request = new UserRequest();
		request.objectID = objectID;
		request.objectName = "";
		request.containerName = "";
		request.opCode = DELETE_OBJECT;
		return request;
	}

	public static UserRequest deleteContainer(String containerName) {
		UserRequest request = new UserRequest();
		request.containerName = containerName;
		request.opCode = DELETE_CONTAINER;
		return request;
	}

	/**
	 * Use this to create a pause in a request-stream.
	 * <p/>
	 * If a delay is followed by a blocking request, the pause begins at the time the blocking call returns and ends
	 * after the given delay.
	 *
	 * @param delay pause in ms.
	 * @return
	 */
	public static UserRequest idle(int delay) {
		UserRequest request = new UserRequest();
		request.opCode = PAUSE;
		request.delay = delay;
		return request;
	}

	/**
	 * Use this to create a wait operation, that lets the broker wait until all previously operations are finished.
	 *
	 * @return
	 */
	public static UserRequest waitForAllPreviousOperations() {
		UserRequest request = new UserRequest();
		request.opCode = WAIT;
		return request;
	}

	/**
	 * Use this method to convert a {@code UserRequest} into a blocking call, which means, the broker will continue with the execution of the request stream only after this {@code UserRequest} has been processed by the StorageCloud (failed or succeeded).
	 * <p/>
	 * The parameter will be modified directly, as well as returned
	 *
	 * @param request the request that shall be a blocking call
	 * @return the modified request
	 */
	public static UserRequest blocking(UserRequest request) {
		assert request.getOpCode() != PAUSE;
		request.blockingCall = true;
		return request;
	}

	public int getOpCode() {
		return opCode;
	}

	public String getObjectName() {
		return objectName;
	}

	public String getContainerName() {
		return containerName;
	}

	public String getObjectID() {
		return objectID;
	}

	public int getDelay() {
		return delay;
	}

	public long getSize() {
		return size;
	}

	public boolean isBlockingCall() {
		return blockingCall;
	}

	public CdmiMetadata getMetadata() {
		return metadata;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.getClass() == getClass() &&
				(
						(((UserRequest) obj).getContainerName() == null && null == getContainerName())
							||
						(((UserRequest) obj).getContainerName() != null && ((UserRequest) obj).getContainerName().equals(getContainerName()))
				) &&
				((UserRequest) obj).getDelay() == getDelay() &&
				(
						(((UserRequest) obj).getMetadata() == null && null == getMetadata())
							||
						(((UserRequest) obj).getMetadata() != null && ((UserRequest) obj).getMetadata().equals(getMetadata()))
				) &&
				((UserRequest) obj).isBlockingCall() == isBlockingCall() &&
				(
						(((UserRequest) obj).getObjectID() == null && getObjectID() == null)
							||
						((UserRequest) obj).getObjectID().equals(getObjectID())
				) &&
				(
						(((UserRequest) obj).getObjectName() == null && getObjectName() == null)
								||
						(((UserRequest) obj).getObjectName() != null && ((UserRequest) obj).getObjectName().equals(getObjectName()))
				)
				 &&
				((UserRequest) obj).getOpCode() == getOpCode();
	}
}
