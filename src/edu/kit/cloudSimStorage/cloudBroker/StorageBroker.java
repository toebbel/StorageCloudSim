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

import edu.kit.cloudSimStorage.CdmiCloudCharacteristics;
import edu.kit.cloudSimStorage.StorageCloud;
import edu.kit.cloudSimStorage.cdmi.CdmiId;
import edu.kit.cloudSimStorage.cdmi.CdmiOperationVerbs;
import edu.kit.cloudSimStorage.cloudOperations.request.*;
import edu.kit.cloudSimStorage.cloudOperations.response.CloudDiscoveryResponse;
import edu.kit.cloudSimStorage.cloudOperations.response.CloudResponse;
import edu.kit.cloudSimStorage.monitoring.TupleSequence;
import edu.kit.cloudSimStorage.monitoring.*;
import edu.kit.cloudSimStorage.monitoring.sampleSequenceOperatorations.SampleFilter;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author Tobias Sturm
 *         created at 4/29/13, 1:00 PM
 */
public class StorageBroker extends SimEntity implements ILoggable, TraceableResource {

	protected static final int WAIT_PROBE_INTERVAL = 500; //simulated ms

	protected int connectedCloudID;
	protected Queue<UserRequest> userRequests;
	protected String waitingForOperation; //state variable for blocking calls
	protected HashMap<String, CloudRequest> runningRequests;
	protected HashMap<String, CloudResponse> receivedResponses;

	//for monitoring
	protected Logger logger;
	protected EventTracker<CdmiOperationVerbs> failedRequestsTracker, succRequestTracker, ackRequestTracker;
	public static final String NUM_TOTAL_REQUESTS = "# sent request", NUM_REQUESTS_PER_SECOND = "# sent request per second", NUM_REQUESTS_PER_MINUTE = "# sent request per minute"; //to be delegated to EventTracker
	public static final String NUM_TOTAL_SUCC_REQUESTS = "# succ request", NUM_TOTAL_ACK_REQUESTS = "# acked request", NUM_TOTAL_FAILED_REQUESTS = "# failed request";
	protected EventTracker<CloudRequest> cloudRequestTracker;
	TraceableResourceAliasing trackableSubResources;


	//for multi broker usage
	protected boolean waitingForDiscovery;
	protected int metaBrokerID; // negative value -> no meta broker in use
	protected CdmiCloudCharacteristics lastKnownCharacteristics; //of the cloud

	/**
	 * Creates a new entity that acts on behalf of a meta broker
	 *
	 * @param associatedCloudID the simID of the {@link StorageCloud}
	 * @param metaBroker the simID of the {@link StorageMetaBroker}
	 */
	public StorageBroker(int associatedCloudID, int metaBroker) {
		super("StorageCloudBrokerOnBehalfOf" + metaBroker  + "ToAccessCloud" + associatedCloudID);
		logger = Logger.getLogger("StorageCloudBroker_" + metaBroker + "-" + getId() + "-" + associatedCloudID);
		logger.info("Create StorageCloudBroker, with ID " + getId() + " that serves on behalf of " + metaBroker + " and interacts with cloud " + associatedCloudID);

		this.metaBrokerID = metaBroker;
		init(associatedCloudID);
		trackableSubResources = new TraceableResourceAliasing();

		trackableSubResources.addMapping(NUM_TOTAL_REQUESTS, NUM_EVENTS_TOTAL, cloudRequestTracker);
		trackableSubResources.addMapping(NUM_REQUESTS_PER_SECOND, NUM_EVENTS_PER_SECOND, cloudRequestTracker);
		trackableSubResources.addMapping(NUM_REQUESTS_PER_MINUTE, NUM_EVENTS_PER_MINUTE, cloudRequestTracker);
		trackableSubResources.addMapping(NUM_TOTAL_ACK_REQUESTS, NUM_EVENTS_TOTAL, ackRequestTracker);
		trackableSubResources.addMapping(NUM_TOTAL_SUCC_REQUESTS, NUM_EVENTS_TOTAL, succRequestTracker);
		trackableSubResources.addMapping(NUM_TOTAL_FAILED_REQUESTS, NUM_EVENTS_TOTAL, failedRequestsTracker);

	}

	/**
	 * startRequest
	 * Creates a new entity of a broker, that interacts with a Clod instance, independent of any meta broker
	 *
	 * @param associatedCloudID the simID of the {@link StorageCloud}
	 */
	public StorageBroker(int associatedCloudID) {
		super("StorageCloudBrokerToAccessCloud" + associatedCloudID);
		logger = Logger.getLogger("StorageCloudBroker" + getId() + "-" + associatedCloudID);
		logger.info("Create StorageCloudBroker, with ID " + getId() + " that interacts with cloud " + associatedCloudID);

		init(associatedCloudID);
	}

	private void init(int associatedCloudID) {
		this.connectedCloudID = associatedCloudID;

		runningRequests = new HashMap<>();
		receivedResponses = new HashMap<>();

		userRequests = new LinkedList<>();
		waitingForOperation = "";
		waitingForDiscovery = false;

		cloudRequestTracker = new EventTracker<>("broker request", "broker request tracker");
		failedRequestsTracker = new EventTracker<>("failed request", "broker failed request tracker");
		ackRequestTracker = new EventTracker<>("acked request", "broker ack request tracker");
		succRequestTracker = new EventTracker<>("succeeded request", "broker succ request tracker");
	}

	/**
	 * Adds a {@link UserRequest} to the execution queue.
	 *
	 * @param request the request to add
	 */
	public void enqueueUserRequest(UserRequest request) {
		logger.fine("enqueue user request " + request);
		userRequests.add(request);
	}


	@Override
	public void startEntity() {
		startNextOperation();
	}

	@Override
	public void shutdownEntity() {

	}

	/**
	 * Starts a new request, starts the trace timer and adds it to the list of running request
	 *
	 * Checks if this request has already been started.
	 * Creates a log entry
	 * @param request the request to start
	 */
	private void startRequest(CloudRequest request) {
		assert !runningRequests.containsKey(request.getOperationID());

		logger.info("send user request to cloud: " + request);
		request.startsNow();
		runningRequests.put(request.getOperationID(), request);

		scheduleNow(connectedCloudID, request.getTag(), request);
	}

	/**
	 * starts next operation or halts broker if queue is empty
	 * The {@link CloudRequest} instances are created here, depending on the {@link UserRequest} in the queue and then dispatched.
	 */
	private void startNextOperation() {
		if (userRequests.isEmpty()) {
			logger.fine("user request queue empty - halt broker");
			if(metaBrokerID >= 0)
				scheduleNow(metaBrokerID, UserMetaRequest.BROKER_FINISHED, getId());
			return;
		}

		//get next request
		UserRequest req = userRequests.remove();
		while(req == null && !userRequests.isEmpty()) //throw away all null request in queue
			req = userRequests.remove();

		//create Cloud request out of the user request
		CloudRequest cloudReq = null;
		switch (req.getOpCode()) {
			case UserRequest.PAUSE:
				schedule(getId(), req.getDelay(), CloudSimTags.CLOUDLET_PAUSE_ACK);
				logger.info("pause for " + req.getDelay() + "ms");
				return; //abort further exec. of method
			case UserRequest.WAIT:
				scheduleNow(getId(), CloudSimTags.CLOUDLET_PAUSE_ACK);
				logger.info("pause for until prev. operation returned");
				return; //abort further exec. of method
			case UserRequest.DELETE_CONTAINER:
				cloudReq = new DeleteContainerRequest(req.getContainerName(), getId());
				break;
			case UserRequest.DELETE_OBJECT:
				if (req.getObjectID().isEmpty())
					cloudReq = new DeleteObjectRequest(req.getContainerName(), req.getObjectName(), getId());
				else
					cloudReq = new DeleteObjectRequest(req.getObjectID(), getId());
				break;
			case UserRequest.PUT_CONTAINER:
				cloudReq = new PutContainerRequest(req.containerName, req.getMetadata(), getId());
				break;
			case UserRequest.PUT_OBJECT:
				cloudReq = new PutObjectRequest(req.getContainerName(), req.getObjectName(), req.getMetadata(), getId());
				break;
			case UserRequest.GET_CONTAINER:
				cloudReq = new GetContainerRequest(req.rootUrl, req.getContainerName(), getId());
				break;
			case UserRequest.GET_OBJECT:
				if (req.getObjectID().isEmpty() || req.getObjectID().equals(CdmiId.UNKNOWN.toString()))
					cloudReq = new GetObjectRequest(req.rootUrl, req.getContainerName(), req.getObjectName(), getId());
				else
					cloudReq = new GetObjectRequest(req.rootUrl, req.getObjectID(), getId());
				break;
			case UserMetaRequest.DISCOVER_CLOUD:
				cloudReq = new CloudDiscoverRequest(getId());
				waitingForDiscovery = true;
		}

		if(cloudReq == null)
			throw new IllegalStateException("Could not create CloudRequest out of UserRequest.");

		if (req.blockingCall) {
			if(!waitingForOperation.isEmpty())
				throw new IllegalStateException("Already waiting for the return of a blocking call. Can't wait for a second call");
			waitingForOperation = cloudReq.getOperationID();
		}

		startRequest(cloudReq);
	}

	@Override
	public void processEvent(SimEvent ev) {
		//for pause user request
		if (ev.getSource() == getId()) {
			if (ev.getType() == CloudSimTags.CLOUDLET_PAUSE_ACK)
				startNextOperation();
		}

		//for all responses by the Cloud
		switch (ev.getTag()) {
			case CloudRequest.FAIL:
				String id = (String) ev.getData();
				assert runningRequests.containsKey(id);
				failedRequestsTracker.addEvent(runningRequests.get(id).getVerb());
				operationFinished(id);
				break;
			case CloudRequest.ACK:
				id = (String) ev.getData();
				assert runningRequests.containsKey(id);
				ackRequestTracker.addEvent(runningRequests.get(id).getVerb());
				break;
			case CloudRequest.SUCC:
				CloudResponse rsp = ((CloudResponse) ev.getData());
				receivedResponses.put(rsp.getOperationID(), rsp);
				succRequestTracker.addEvent(runningRequests.get(rsp.getOperationID()).getVerb());
				processSuccResponse(rsp);
				operationFinished(rsp.getOperationID());
				break;
			case CloudSimTags.CLOUDLET_PAUSE_ACK:
				if (runningRequests.isEmpty())
					startNextOperation();
				else
					schedule(getId(), WAIT_PROBE_INTERVAL, CloudSimTags.CLOUDLET_PAUSE_ACK);
		}
	}

	/**
	 * This method is called whenever an operation returned from the Cloud.
	 *
	 * The request is taken out of the list of running request, the trace timers are set.
	 * The next operation will be started with respect to any blocking states
	 * @param opId
	 */
	protected void operationFinished(String opId) {
		assert runningRequests.containsKey(opId);
		logger.info("operation " + opId + " finished");

		runningRequests.get(opId).endsNow();

		cloudRequestTracker.addEvent(runningRequests.get(opId));

		runningRequests.remove(opId);
		if (!waitingForOperation.isEmpty() && waitingForOperation.equals(opId)) {
			waitingForOperation = "";
			logger.info("waited for this operation to finish - continue with execution");
			startNextOperation();
		} else if (waitingForOperation.isEmpty()) {
			startNextOperation();
		}
	}

	/**
	 * reacts to discovery request responses
	 *
	 * only if the {@link StorageMetaBroker} ID is set
	 * @param rsp the returning response
	 */
	protected void processSuccResponse(CloudResponse rsp) {
		logger.info("Response of successful operation: '" + rsp + "'");
		if (waitingForDiscovery && rsp instanceof CloudDiscoveryResponse && metaBrokerID >= 0) {
			lastKnownCharacteristics = ((CloudDiscoveryResponse) rsp).getCharacteristics();
			logger.info("received cloud characteristics: " + lastKnownCharacteristics);
			scheduleNow(metaBrokerID, UserMetaRequest.DISCOVER_CLOUD, getId());
		}
	}

	/**
	 * returns the latest received Characteristics of the connected Cloud
	 * @return {@link CdmiCloudCharacteristics} of the Cloud or {@code null}
	 */
	public CdmiCloudCharacteristics getLastKnownCloudCharacteristics() {
		return lastKnownCharacteristics;
	}

	/**
	 * Returns the ID of the connected {@link StorageCloud}
	 * @return  id of the cloud
	 */
	public int getAssociatedCloudID() {
		return connectedCloudID;
	}

	@Override
	public Logger getLogger() {
		return logger;
	}


	@Override
	public TupleSequence<Double> getSamples(String key) {
		return trackableSubResources.getSamples(key);
	}

	@Override
	public String[] getAvailableTrackingKeys() {
		return trackableSubResources.getAvailableTrackingKeys();
	}

	/**
	 * Returns a list of all {@link CloudRequest}, that have been sent
	 * @return all cloud request
	 */
	public List<CloudRequest> getCloudRequestTraces() {
		return cloudRequestTracker.getTraces();
	}

	/**
	 * Returns a list of all {@link CloudRequest}, that have been sent and match the given {@link CdmiOperationVerbs}
	 * @param verb the verb of interest
	 * @return all cloud request
	 */
	public List<CloudRequest> getCloudRequestTraces(final CdmiOperationVerbs verb) {
		return cloudRequestTracker.getTracesWhere(SampleFilter.cdmiVerbFilter(verb));
	}


}
