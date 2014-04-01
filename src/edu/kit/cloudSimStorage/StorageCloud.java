/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage;

import edu.kit.cloudSimStorage.cdmi.*;
import edu.kit.cloudSimStorage.cloudOperations.cloudInternalOperationState.*;
import edu.kit.cloudSimStorage.cloudOperations.request.*;
import edu.kit.cloudSimStorage.helper.TimeHelper;
import edu.kit.cloudSimStorage.monitoring.TupleSequence;
import edu.kit.cloudSimStorage.monitoring.*;
import edu.kit.cloudSimStorage.monitoring.sampleSequenceOperatorations.SequenceOperations;
import edu.kit.cloudSimStorage.policies.ChooseStorageBlobWithLowestUtilization;
import edu.kit.cloudSimStorage.storageModel.ObjectStorageBlob;
import edu.kit.cloudSimStorage.storageModel.StorageBlobLocation;
import edu.kit.cloudSimStorage.storageModel.ObjectStorageServer;
import edu.kit.cloudSimStorage.storageModel.resourceUtilization.TimeawareResourceLimitation;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.*;
import java.util.logging.Logger;

import static edu.kit.cloudSimStorage.helper.FileSizeHelper.toHumanReadable;

/**
 * Created by: Tobias Sturm
 * Date: 4/26/13
 * Time: 2:20 PM
 */
public class StorageCloud extends SimEntity implements TraceableResource, ILoggable {
	private Logger logger;

	private final CdmiCloudCharacteristics characteristics;
	protected String regionalCisName;

	protected HashMap<String, CloudRequestState> runningOperations;
	protected EventTracker<OperationTimeTraceSample> requestTracker;

	protected String rootUrl;
	protected HashMap<Integer, CdmiRootContainer> userToRootContainerMapping;
	protected HashMap<Integer, IUsageHistory> userDebts;
	protected HashMap<String, ObjectStorageServer> servers;
	protected IUsageHistory pricingPolicy;

	protected TimeawareResourceLimitation cloudIOLimits;
	private int timeAwareGarbageCounter;

	/**
	 * Creates a new entity.
	 *
	 * The cloudID and cloudName are set into the characteristics by this ctor
	 *
	 * @param name the name to be associated with this entity
	 */
	public StorageCloud(String name, CdmiCloudCharacteristics characteristics, String region, String rootUrl, IUsageHistory pricingPolicy, TimeawareResourceLimitation cloudIO) {
		super(name);
		logger = Logger.getLogger("cloudLoggger_" + name);
		this.characteristics = characteristics.setCloudID(getId()).setCloudName(name);
		this.regionalCisName = region;
		this.rootUrl = rootUrl;
		runningOperations = new HashMap<>();
		userToRootContainerMapping = new HashMap<>();
		servers = new HashMap<>();
		userDebts = new HashMap<>();
		this.pricingPolicy = pricingPolicy;
		this.cloudIOLimits = cloudIO;
		requestTracker = new EventTracker<>("all cloud request", "all cloud request to " + name);
	}

	/**
	 * Adds a server to this StorageCloud instance
	 * <p/>
	 * The id of the server has to be unique within this StorageCloud system
	 *
	 * @param server the server to add
	 * @return true if operation succeeded
	 */
	public boolean installServer(ObjectStorageServer server) {
		if (servers.containsKey(server.getId()))
			return false;

		logger.fine("Installing server " + server);
		servers.put(server.getId(), server);
		return true;
	}

	@Override
	public void startEntity() {
		//TODO implement method
		//TODO start storage servers
		//TODO create cdmi_capabilities object
	}

	@Override
	public void shutdownEntity() {
		//TODO do more!
		for(IUsageHistory usage: userDebts.values())
			usage.endAccountingPeriod();
	}

	@Override
	public void processEvent(SimEvent ev) {
		if (ev.getData() == null)
			return;
		switch (ev.getTag()) {
			//delegate handling of external request
			case CloudRequest.GET:
				if (ev.getData() instanceof GetContainerRequest)
					handleGetContainerRequest((GetContainerRequest) ev.getData(), ev.getSource());
				else if (ev.getData() instanceof GetObjectRequest)
					handleGetObjectRequest((GetObjectRequest) ev.getData(), ev.getSource());
				else if (ev.getData() instanceof CloudDiscoverRequest)
					handleCloudDiscoveryRequest((CloudDiscoverRequest) ev.getData(), ev.getSource());
				break;

			case CloudRequest.DELETE:
				if (ev.getData() instanceof DeleteContainerRequest)
					handleDeleteContainerRequest((DeleteContainerRequest) ev.getData(), ev.getSource());
				else if (ev.getData() instanceof DeleteObjectRequest)
					handleDeleteObjectRequest((DeleteObjectRequest) ev.getData(), ev.getSource());
				break;

			case CloudRequest.PUT:
				if (ev.getData() instanceof PutObjectRequest)
					handlePutObjectRequest((PutObjectRequest) ev.getData(), ev.getSource());
				else if (ev.getData() instanceof PutContainerRequest)
					handlePutContainerRequest((PutContainerRequest) ev.getData(), ev.getSource());
				break;

			//delegate internal signals
			case CloudRequest.FAIL:
				assert ev.getData() instanceof String;
				handleFailedOperation((String) ev.getData());
				break;

			case CloudRequest.SUCC:
				assert ev.getData() instanceof String;
				handleSucceededOperation((String) ev.getData());
		}

	}

	private void handleCloudDiscoveryRequest(CloudDiscoverRequest request, int requestor) {
		CloudDiscoveryRequestState scheduleEntry = new CloudDiscoveryRequestState(request, requestor);
		runningOperations.put(request.getOperationID(), scheduleEntry);
		logger.info(request.toString() + " -> operation '" + scheduleEntry.getOperationID() + "'");

		scheduleEntry.setCharacteristics(characteristics.copy()
				.setAvailableCapacity(calculateCurrentCapacity())
				.setMinBandwidth(NetworkTopology.getBandwidth(getId(), requestor))
				.setMaxLatency(NetworkTopology.getDelay(getId(), requestor))
				.setCloudID(getId())
		);
		letOperationSucceed(scheduleEntry);
	}

	private long calculateCurrentCapacity() {
		long result = 0;
		for(ObjectStorageServer s : servers.values()) {
			result += s.getCurrentCapacity();
		}
		return result;
	}

	private void handleSucceededOperation(String operationID) {
		assert runningOperations.containsKey(operationID);

		CloudRequestState entry = runningOperations.get(operationID);
		logger.fine("Operation '" + operationID + "' succeeded (callback)");
		runningOperations.remove(operationID);
		scheduleNow(entry.getInquiringPartner(), CloudRequest.SUCC, entry.generateResponse());
	}

	private void handleFailedOperation(String operationID) {
		assert runningOperations.containsKey(operationID);

		CloudRequestState entry = runningOperations.get(operationID);
		logger.fine("Operation '" + operationID + "' failed (callback)");
		runningOperations.remove(operationID);
		scheduleNow(entry.getInquiringPartner(), CloudRequest.FAIL, entry.getOperationID());
	}

	/**
	 * Creates a user, if it does not exist.
	 * <p/>
	 * Does nothing, if the user already exists
	 *
	 * @param id the ID of the user to create
	 */
	private void createUser(int id) {
		if (userToRootContainerMapping.containsKey(id))
			return;

		logger.info("Create user with ID " + id);

		CdmiMetadata rootMetadata = new CdmiMetadata();
		rootMetadata.mergeWith(characteristics, Arrays.asList(CdmiCloudCharacteristics.doNotInheritFromCloudCharacteristics));
		rootMetadata.set(CdmiMetadata.CREATED_AT, CdmiMetadata.getNow());
		rootMetadata.set(CdmiMetadata.LAST_WRITE_ACCESS, CdmiMetadata.getNow());

		CdmiRootContainer root = new CdmiRootContainer(rootUrl, id);
		root.setMetadata(rootMetadata);

		userToRootContainerMapping.put(id, root);
		userDebts.put(id, pricingPolicy.clone(id));
	}

	/**
	 * Checks if a user exists.
	 *
	 * @param scheduleEntry The scheduleEntry that was created by a user and has to be checked
	 * @return true if user exists
	 */
	private boolean checkUser(CloudRequestState scheduleEntry) {
		return checkUser(scheduleEntry, false);
	}

	/**
	 * Checks if a user exists, if not the user is created
	 *
	 * @param scheduleEntry the schedule entry of the user to check
	 * @param createIfFails true to create a user, if the user des not exist yet
	 * @return false if user does not exist and {@code createIfFails} is {@code false}. True in all other cases.
	 */
	private boolean checkUser(CloudRequestState scheduleEntry, boolean createIfFails) {
		if (userToRootContainerMapping.containsKey(scheduleEntry.getRequest().getUser()))
			return true;


		if (createIfFails) {
			createUser(scheduleEntry.getRequest().getUser());
			return true;
		} else {
			letOperationFail(scheduleEntry, "User " + scheduleEntry.getRequest().getUser() + " that requested operation " + scheduleEntry + " is unknown");
			return false;
		}
	}

	private void letOperationFail(CloudRequestState entry, String reason) {
		logger.info("Operation '" + entry.getOperationID() + "' fails, because: " + reason);
		entry.endsNow();
		requestTracker.addEvent(entry.getOmmittedTimestamp(), entry);
		scheduleNow(getId(), CloudRequest.FAIL, entry.getOperationID());
	}

	private void letOperationSucceed(CloudRequestState entry) {
		logger.info("operation " + entry.getOperationID() + " succeeds");
		entry.endsNow();
		requestTracker.addEvent(entry.getOmmittedTimestamp(), entry);
		scheduleNow(getId(), CloudRequest.SUCC, entry.getOperationID());
	}

	private void letOperationSucceed(CloudRequestState entry, int delay, int duration) {
		logger.info("operation " + entry.getOperationID() + " succeeds with a delay of " + delay + "ms and a duration of " + duration + " ms");
		entry.setDuration(duration);
		entry.setDelay(delay);
		requestTracker.addEvent(entry.getOmmittedTimestamp(), entry);
		schedule(getId(), delay + duration, CloudRequest.SUCC, entry.getOperationID());
	}

	private void sendAckToSender(CloudRequestState entry) {
		logger.info("operation " + entry.getOperationID() + " is acked");
		entry.startsNow();
		scheduleNow(entry.getInquiringPartner(), CloudRequest.ACK, entry.getOperationID());
	}

	private void handlePutContainerRequest(PutContainerRequest request, int requestor) {
		//create internalOperation entry
		PutContainerRequestState scheduleEntry = new PutContainerRequestState(request, requestor);
		runningOperations.put(request.getOperationID(), scheduleEntry);
		logger.info(request.toString() + " -> operation '" + scheduleEntry.getOperationID() + "'");

		if (!checkUser(scheduleEntry, true))
			return;

		userDebts.get(scheduleEntry.getRequest().getUser()).query(CdmiOperationVerbs.PUT);

		if (characteristics.contains(CdmiCloudCharacteristics.CAPABILITY_CREATE_CONTAINER) &&
				!characteristics.get(CdmiCloudCharacteristics.CAPABILITY_CREATE_CONTAINER).equals("true")) {
			letOperationFail(scheduleEntry, "This cloud does not has the capability to create new containers");
			return;
		}

		CdmiRootContainer root = userToRootContainerMapping.get(request.getUser());
		if(root.getMetadata().contains(CdmiMetadata.MAX_CHILD_COUNT)) {
			if(root.getChildren().size() >= Integer.parseInt(root.getMetadata().get(CdmiMetadata.MAX_CHILD_COUNT)));
				letOperationFail(scheduleEntry, "Maximum number of containers reached");
		}

		if(root.getMetadata().contains(CdmiMetadata.MAX_CONTAINER_SIZE)) {
			if(root.getSize() >= Integer.parseInt(root.getMetadata().get(CdmiMetadata.MAX_CONTAINER_SIZE)));
			letOperationFail(scheduleEntry, "Maximum used storage of container 'root' reached");
		}

		if (root.containsChildWithName(request.getContainerName())) {
			letOperationFail(scheduleEntry, "There is already a container with the name " + request.getContainerName());
			return;
		}

		//create metadata: 1) inherit from root, then overwrite with request
		CdmiMetadata metadata = new CdmiMetadata();
		metadata.mergeWith(root.getMetadata(), Arrays.asList(CdmiMetadata.doNotInheritFromRootContainerToContainer));
		metadata.mergeWith(request.getMetadata());
		logger.fine("assigned the metadata " + metadata.toString(true));

		//create container
		CdmiObjectContainer newContainer = new CdmiObjectContainer(root, request.getContainerName(), metadata);
		logger.fine("assigned the CDMI ID '" + newContainer.getEntityId() + "'");

		//assign servers to container and hang container into root
		for (ObjectStorageServer server : getServerRingForNewContainer(newContainer)) {
			newContainer.addAssociatedServer(server);
		}
		root.putChild(newContainer);

		scheduleEntry.setContainer(newContainer);
		letOperationSucceed(scheduleEntry);
	}


	private void handlePutObjectRequest(PutObjectRequest request, int requestor) {
		//create internal operation entry
		PutObjectRequestState scheduleEntry = new PutObjectRequestState(request, requestor);
		runningOperations.put(scheduleEntry.getOperationID(), scheduleEntry);
		logger.info(request.toString() + " -> operation '" + scheduleEntry.getOperationID() + "'");

		if (!checkUser(scheduleEntry, true))
			return;

		userDebts.get(scheduleEntry.getRequest().getUser()).query(CdmiOperationVerbs.PUT);

		CdmiRootContainer root = userToRootContainerMapping.get(request.getUser());
		if (!root.containsChildWithName(request.getContainerName())) {
			letOperationFail(scheduleEntry, "Container " + request.getContainerName() + " could not be found to put object");
			return;
		}

		CdmiObjectContainer container = root.getChild(root.getChildId(request.getContainerName()));
		if(container.getMetadata().contains(CdmiMetadata.MAX_CHILD_COUNT)) {
			if(container.getChildren().size() >= Integer.parseInt(container.getMetadata().get(CdmiMetadata.MAX_CHILD_COUNT))) {
				letOperationFail(scheduleEntry, "Maximum number of objects in container '" + container.getEntityName() + "' reached");
				return;
			}
		}

		if(container.getMetadata().contains(CdmiMetadata.MAX_CONTAINER_SIZE)) {
			if(container.getSize() >= Long.parseLong(container.getMetadata().get(CdmiMetadata.MAX_CONTAINER_SIZE))) {
				letOperationFail(scheduleEntry, "Maximum used storage of container '" + container.getEntityName() + "' reached");
				return;
			}
		}

		if(container.getMetadata().contains(CdmiMetadata.MAX_OBJECT_SIZE)) {
			if(Long.parseLong(request.getMetadata().get(CdmiMetadata.SIZE)) >= Long.parseLong(container.getMetadata().get(CdmiMetadata.MAX_OBJECT_SIZE))) {
				letOperationFail(scheduleEntry, "object too big for container limit is " + container.getMetadata().get(CdmiMetadata.MAX_OBJECT_SIZE) + ", but object is "+ request.getMetadata().get(CdmiMetadata.SIZE) + ".");
				return;
			}
		}

		CdmiDataObject dataObject = null;

		if (request.getObjectName().isEmpty() ||
				!request.getObjectName().isEmpty() && !container.containsChildWithName(request.getObjectName())) {
			//the PUT is a put of a new object
			scheduleEntry.setType(PutObjectRequestState.PutObjectRequestType.Creation);
			logger.fine(request + " will create a new object");

			//create the metadata for the object by merging the parent's metadata and overwrite with request's metadata
			//TODO check capabilities if metadata can be changed. If not -> throw away
			CdmiMetadata metadata = new CdmiMetadata();
			metadata.mergeWith(root.getMetadata(), Arrays.asList(CdmiMetadata.doNotInheritFromRootContainerToObject));
			metadata.mergeWith(container.getMetadata(), Arrays.asList(CdmiMetadata.doNotInheritFromObjectContainer));
			metadata.mergeWith(request.getMetadata());
			metadata.set(CdmiMetadata.CREATED_AT, String.valueOf(CloudSim.getSimulationCalendar().getTime().getTime()));
			metadata.set(CdmiMetadata.LAST_WRITE_ACCESS, String.valueOf(CloudSim.getSimulationCalendar().getTime().getTime()));
			metadata.set(CdmiMetadata.SIZE, String.valueOf(request.getSize()));
			logger.fine("assigned metadata " + metadata.toString(true));

			dataObject = new CdmiDataObject(request.getSize(), metadata, rootUrl, request.getObjectName());
			logger.fine("assigned CDMI ID " + dataObject.getEntityId());

			if (!container.spaceAvailableFor(dataObject)) {
				letOperationFail(scheduleEntry, "Not enough space in container " + container);
				return;
			}

			sendAckToSender(scheduleEntry);
			int succPuts = 0;
			int targetPuts = Integer.parseInt(container.getMetadata().get(CdmiMetadata.NUM_REPLICA));

			scheduleEntry.setObject(dataObject);
			List<StorageBlobLocation> possibleLocations = container.getPossibleBlobToStorageAllocations(dataObject);
			for (StorageBlobLocation location : possibleLocations) {
				if (location.getServer().probeDisk(location.getDriveName(), dataObject.getPhysicalSize())) {
					logger.fine("store blob on " + location);
					location.getServer().saveBlob(dataObject, location.getDriveName());
					succPuts++;
					scheduleEntry.addUsedLocation(location);
					if (succPuts == targetPuts)
						break;
				} else
					logger.fine("could not store blob on " + location);

			}

			if (succPuts != targetPuts) {
				for (StorageBlobLocation location : scheduleEntry.getUsedLocations()) {
					logger.fine("rollback on " + location);
					location.getServer().deleteBlob(location);
				}
				letOperationFail(scheduleEntry, "Could not create enough replica for the object");
			} else {
				root.updateIdToObjectContainerMapping(dataObject.getEntityId(), container);
				container.putChild(dataObject);
				container.assignPhysicalLocations(dataObject.getEntityId(), scheduleEntry.getUsedLocations());

				//calculate delays
				int minDelay = (int) (Integer.MAX_VALUE / 2.0 - 1), minDuration = (int) (Integer.MAX_VALUE / 2.0 - 1);
				int maxDelay = 0, maxDuration = 0;

				for (StorageBlobLocation l : scheduleEntry.getUsedLocations()) {
					ObjectStorageBlob writeBlob = new ObjectStorageBlob(l, dataObject);
					int delay = l.getServer().calculateDelay(l.getDriveName());
					int duration = l.getServer().calculateWriteDuration(writeBlob, delay);
					assert delay >= 0;
					assert duration >= 0;

					logger.fine("transfer to " + l.toString() + " will take " + delay + " + " + duration + "ms");

					if (delay + duration < minDelay + minDuration) {
						minDelay = delay;
						minDuration = duration;
					}
					if (delay + duration > maxDelay + maxDuration) {
						maxDelay = delay;
						maxDuration = duration;
					}
				}

				int transferDuration = 0;
				int transferDelay = calculateNextFreeTransferSlot(0);
				assert transferDelay >= 0;
				if (minDelay + minDuration == 0) {
					transferDuration = calculateTransferDelay(getId(), requestor, transferDelay, dataObject.getPhysicalSize());
				} else {
					transferDuration = calculateTransferDelay(getId(), requestor, transferDelay, dataObject.getPhysicalSize(), (int) (dataObject.getPhysicalSize() / (minDelay + minDuration)));
				}
				assert transferDuration >= 0;
				logger.fine("transfer to the cloud will take " + transferDelay + " + " + transferDuration + "ms");

				int totalDuration = Math.min(transferDuration, maxDuration + maxDelay);
				letOperationSucceed(scheduleEntry, transferDelay, totalDuration);

				//accounting
				userDebts.get(scheduleEntry.getRequest().getUser()).UploadTraffic(dataObject.getSize());
			}

		} else if (container.containsChildWithName(request.getObjectName())) {
			//the PUT is an update of the object
			logger.fine(request + " will update an existing");
			scheduleEntry.setType(PutObjectRequestState.PutObjectRequestType.Update);
			dataObject = container.getChild(container.getChildId(request.getObjectName()));

			//TODO check capabilities if metadata can be changed. If not -> throw away
			CdmiMetadata newMetadata = new CdmiMetadata();
			newMetadata.mergeWith(dataObject.getMetadata());
			newMetadata.mergeWith(request.getMetadata());
			logger.fine("assigning metadata" + newMetadata.toString(true));

			//create temporal object
			CdmiDataObject alteredObject = new CdmiDataObject(request.getSize(), newMetadata, dataObject.getRootURI(), dataObject.getEntityName());

			if (!container.spaceAvailableFor(alteredObject)) {
				letOperationFail(scheduleEntry, "Not enough space in container " + container);
				return;
			}

			sendAckToSender(scheduleEntry);
			int succPuts = 0;
			int targetPuts = Integer.parseInt(container.getMetadata().get(CdmiMetadata.NUM_REPLICA)); //TODO read from object metadata ?

			List<StorageBlobLocation> oldLocations = container.getLocatinsFor(dataObject.getEntityId());
			List<StorageBlobLocation> possibleLocations = container.getPossibleBlobToStorageAllocations(dataObject);
			for (StorageBlobLocation location : possibleLocations) {
				if (location.getServer().probeDisk(location.getDriveName(), alteredObject.getPhysicalSize())) {
					if (oldLocations.contains(location)) {
						location.getServer().deleteBlob(location);
						oldLocations.remove(location);
						logger.fine("reuse location " + location + " -> delete old blob");
					}

					//TODO problem: if some locations are overwritten, but the number of replcias can't be satisfied, the system is in a invalid state.
					location.getServer().saveBlob(dataObject, location.getDriveName());
					logger.fine("use location " + location + " to store blob");
					succPuts++;
					scheduleEntry.addUsedLocation(location);
					if (succPuts == targetPuts) {
						for (StorageBlobLocation oldLocation : oldLocations) {
							logger.fine("delete old blob from location " + location);
							location.getServer().deleteBlob(oldLocation);
						}
						break;
					}
				}
			}

			if (succPuts != targetPuts) {
				for (StorageBlobLocation location : scheduleEntry.getUsedLocations()) {
					logger.fine("rollback blob from " + location);
					location.getServer().deleteBlob(location);
				}
				letOperationFail(scheduleEntry, "Could not create enough replica for the object - System may be in corrupt state");
			} else {
				dataObject.setSize(alteredObject.getSize());
				dataObject.setMetadata(alteredObject.getMetadata());
				scheduleEntry.setObject(dataObject);
				container.assignPhysicalLocations(dataObject.getEntityId(), scheduleEntry.getUsedLocations());

				//calculate delays
				int maxDelay = 0, maxDuration = 0;
				int minDelay = (int) (Integer.MAX_VALUE / 2.0 - 1), minDuration = (int) (Integer.MAX_VALUE / 2.0 - 1);

				for (StorageBlobLocation l : scheduleEntry.getUsedLocations()) {
					ObjectStorageBlob writeBlob = new ObjectStorageBlob(l, dataObject);
					int delay = l.getServer().calculateDelay(l.getDriveName());
					int duration = l.getServer().calculateWriteDuration(writeBlob, delay);

					logger.fine("transfer to " + l.toString() + " will take " + delay + " + " + duration + "ms");

					minDelay = Math.min(delay, minDelay);
					minDuration = Math.min(duration, minDuration);
					if (delay + duration < minDelay + minDuration) {
						minDelay = delay;
						minDuration = duration;
					}
					if (delay + duration > maxDelay + maxDuration) {
						maxDelay = delay;
						maxDuration = duration;
					}

				}
				int transferDuration;
				int transferDelay = calculateNextFreeTransferSlot(0);
				if (minDelay + minDuration == 0) {
					transferDuration = calculateTransferDelay(getId(), requestor, transferDelay, alteredObject.getPhysicalSize());
				} else {
					transferDuration = calculateTransferDelay(getId(), requestor, transferDelay, alteredObject.getPhysicalSize(), (int) (alteredObject.getPhysicalSize() / (minDelay + minDuration)));
				}
				logger.fine("transfer to the cloud will take " + transferDelay + " + " + transferDuration + "ms");

				letOperationSucceed(scheduleEntry, maxDelay, maxDuration);

				//accounting
				userDebts.get(scheduleEntry.getRequest().getUser()).UploadTraffic(alteredObject.getSize());
				updateUsedSizeOfUser(scheduleEntry.getRequest().getUser());
			}

		}
	}


	private void handleDeleteObjectRequest(DeleteObjectRequest request, int requestor) {
		//create internal operation entry
		CloudRequestState<DeleteObjectRequest> scheduleEntry = new CloudRequestState<>(request, requestor);
		logger.fine(request + "-> operation '" + scheduleEntry.getOperationID() + "'");
		runningOperations.put(scheduleEntry.getOperationID(), scheduleEntry);

		if (!checkUser(scheduleEntry))
			return;

		//accounting
		userDebts.get(scheduleEntry.getRequest().getUser()).query(CdmiOperationVerbs.DELETE);

		//resolve container
		CdmiRootContainer root = userToRootContainerMapping.get(request.getUser());
		CdmiObjectContainer container;
		CdmiDataObject object;
		if (request.getCdmiID() != CdmiId.UNKNOWN) {
			CdmiId id = request.getCdmiID();
			if (!root.hasIdToContainerMapping(id)) {
				letOperationFail(scheduleEntry, "The ObjectID " + request.getCdmiID() + " is unknown");
				return;
			}
			container = root.getContainerOfObject(id);
			object = container.getChild(id);
		} else {
			if (!root.containsChildWithName(request.getContainerName())) {
				letOperationFail(scheduleEntry, "Container " + request.getContainerName() + " could not be found to put object");
				return;
			}
			container = root.getChild(root.getChildId(request.getContainerName()));
			object = container.getChild(container.getChildId(request.getObjectName()));
		}
		logger.fine("resolved to " + container.getEntityName() + "/" + object.getEntityName());

		sendAckToSender(scheduleEntry);

		List<StorageBlobLocation> locations = container.getLocatinsFor(object.getEntityId());
		for (StorageBlobLocation location : locations) {
			logger.fine("remove blob from " + location);
			location.getServer().deleteBlob(location);
		}

		//remove all references
		container.deleteChild(object.getEntityId());
		root.deleteIdToObjectContainerMapping(object.getEntityId());

		letOperationSucceed(scheduleEntry);

		//accounting
		updateUsedSizeOfUser(scheduleEntry.getRequest().getUser());
	}

	private void handleDeleteContainerRequest(DeleteContainerRequest request, int requestor) {
		//create internal operation entry
		CloudRequestState<DeleteContainerRequest> scheduleEntry = new CloudRequestState<>(request, requestor);
		logger.fine(request + "-> operation '" + scheduleEntry.getOperationID() + "'");
		runningOperations.put(scheduleEntry.getOperationID(), scheduleEntry);

		if (!checkUser(scheduleEntry))
			return;

		userDebts.get(scheduleEntry.getRequest().getUser()).query(CdmiOperationVerbs.DELETE);

		if (characteristics.contains(CdmiCloudCharacteristics.CAPABILITY_DELETE_CONTAINER) &&
				!characteristics.get((CdmiCloudCharacteristics.CAPABILITY_DELETE_CONTAINER)).equals("true")) {
			letOperationFail(scheduleEntry, "This cloud has not the capability to delete containers");
			return;
		}

		//check if there is a container with that name
		CdmiRootContainer root = userToRootContainerMapping.get(request.getUser());
		if (!root.containsChildWithName(request.getContainerName())) {
			letOperationFail(scheduleEntry, "Container " + request.getContainerName() + " could not be found to put object");
			return;
		}
		CdmiObjectContainer container = root.getChild(root.getChildId(request.getContainerName()));

		//delete all children
		for (CdmiId childId : container.getChildren().keySet()) {
			for (StorageBlobLocation location : container.getLocatinsFor(childId)) {
				logger.fine("delete " + location);
				location.getServer().deleteBlob(location);
			}
			root.deleteIdToObjectContainerMapping(childId);
		}

		//delete all references
		root.deleteChild(container.getEntityId());

		letOperationSucceed(scheduleEntry);

		//accounting
		updateUsedSizeOfUser(scheduleEntry.getRequest().getUser());
	}

	private void updateUsedSizeOfUser(int user) {
		userDebts.get(user).updateCurrentlyUsedSpace(userToRootContainerMapping.get(user).getSize());
	}

	private void handleGetObjectRequest(GetObjectRequest request, int requestor) {
		//create internal operation entry
		GetObjectRequestState scheduleEntry = new GetObjectRequestState(request, requestor);
		logger.fine(request + "-> operation '" + scheduleEntry.getOperationID() + "'");
		runningOperations.put(scheduleEntry.getOperationID(), scheduleEntry);

		if (!checkUser(scheduleEntry))
			return;

		//accounting (even for failed request)
		userDebts.get(scheduleEntry.getRequest().getUser()).query(CdmiOperationVerbs.GET);

		//find the object by container+name or id
		CdmiRootContainer root = userToRootContainerMapping.get(request.getUser());
		CdmiObjectContainer container;
		CdmiDataObject object;
		if (request.getRequestedID() != CdmiId.UNKNOWN) {
			CdmiId id = request.getRequestedID();
			if (!root.hasIdToContainerMapping(id)) {
				letOperationFail(scheduleEntry, "The ObjectID " + request.getRequestedID() + " is unknown");
				return;
			}
			container = root.getContainerOfObject(id);
			object = container.getChild(id);
		} else {
			if (!root.containsChildWithName(request.getRequestedContainer())) {
				letOperationFail(scheduleEntry, "Container " + request.getRequestedContainer() + " could not be found to put object");
				return;
			}
			container = root.getChild(root.getChildId(request.getRequestedContainer()));
			if(!container.containsChildWithName(request.getRequestedName())) {
				letOperationFail(scheduleEntry, "Container " + request.getRequestedContainer() + " does not contain a file with the name " + request.getRequestedName());
				return;
			}
			object = container.getChild(container.getChildId(request.getRequestedName()));
		}
		logger.fine("resolved to " + container.getEntityName() + "/" + object.getEntityName());

		sendAckToSender(scheduleEntry);

		//choose blob to read from
		List<StorageBlobLocation> locations = container.getLocatinsFor(object.getEntityId());
		Collections.sort(locations, new ChooseStorageBlobWithLowestUtilization());
		StorageBlobLocation readLocation = locations.get(0);
		ObjectStorageBlob readBlob = new ObjectStorageBlob(readLocation, object);
		logger.fine(locations.size() + " blobs to choose from. Chose " + readLocation);

		//calculate delay
		int delay = readLocation.getServer().calculateDelay(readLocation.getDriveName());
		int duration = readLocation.getServer().calculateReadDuration(readBlob, delay);
		calculateTransferDelay(getId(), requestor, delay, object.getPhysicalSize(), (int) (object.getPhysicalSize() / duration));
		logger.fine("the read will last " + duration + "ms after a delay of " + delay + "ms");

		//finish off          At the
		scheduleEntry.setObject(object);
		letOperationSucceed(scheduleEntry, delay, duration);

		//accounting
		userDebts.get(scheduleEntry.getRequest().getUser()).DownloadTraffic(object.getSize());
	}

	/**
	 * Calculates the delay from a given simEntity at the given maximum rate.
	 *
	 * @param fromSimEntity communication partner that sends data
	 * @param toSimEntity   communication partner that receives data
	 * @param delay         time in ms before operation starts
	 * @param amount        amount in bytes to transfer
	 * @param maxRate       limitations of disks and servers in byte/ms
	 */
	private int calculateTransferDelay(int fromSimEntity, int toSimEntity, int delay, long amount, int maxRate) {
		assert delay >= 0;
		assert fromSimEntity >= 0;
		assert toSimEntity >= 0;

		int rate = 0;

		if(!NetworkTopology.isNetworkEnabled())
			rate = maxRate;
		else {
			double clientBandwidth = NetworkTopology.getBandwidth(fromSimEntity, toSimEntity); //TODO check magnitude
			rate = (int) Math.min(clientBandwidth, maxRate);
		}

		return (int) cloudIOLimits.use(TimeHelper.getInstance().now() + delay, amount, rate).getDuration();
	}

	/**
	 * Calculates the delay from a given simEntity at the given maximum rate.
	 *
	 * @param fromSimEntity communication partner that sends data
	 * @param toSimEntity   communication partner that receives data
	 * @param delay         time in ms before operation starts
	 * @param amount        amount in bytes to transfer
	 */
	private int calculateTransferDelay(int fromSimEntity, int toSimEntity, int delay, long amount) {
		assert fromSimEntity >= 0;
		assert toSimEntity >= 0;

		double clientBandwidth = NetworkTopology.getBandwidth(fromSimEntity, toSimEntity); //TODO check magnitude
		return calculateTransferDelay(fromSimEntity, toSimEntity, delay, amount, (int) clientBandwidth);
	}

	private int calculateNextFreeTransferSlot(int delay) {
		if(timeAwareGarbageCounter++ > 100) {
			cloudIOLimits.removeSamplesBefore(TimeHelper.getInstance().now());
			timeAwareGarbageCounter = 0;
		}

		return (int) (cloudIOLimits.getFirstFreeTimeslot(TimeHelper.getInstance().now() + delay) - TimeHelper.getInstance().now());
	}

	private void handleGetContainerRequest(GetContainerRequest request, int requestor) {
		//create internal operation entry
		GetContainerRequestState scheduleEntry = new GetContainerRequestState(request, requestor);
		logger.fine(request + "-> operation '" + scheduleEntry.getOperationID() + "'");
		runningOperations.put(scheduleEntry.getOperationID(), scheduleEntry);

		if (!checkUser(scheduleEntry))
			return;

		//accounting
		userDebts.get(scheduleEntry.getRequest().getUser()).query(CdmiOperationVerbs.GET);

		//check if container exists
		CdmiRootContainer root = userToRootContainerMapping.get(request.getUser());
		if (!root.containsChildWithName(request.getContainerName())) {
			letOperationFail(scheduleEntry, "Container " + request.getContainerName() + " could not be found.");
			return;
		}
		CdmiObjectContainer container = root.getChild(root.getChildId(request.getContainerName()));

		boolean listChilden = characteristics.contains(CdmiCloudCharacteristics.CAPABILITY_LIST_CHILDREN) && characteristics.get(CdmiCloudCharacteristics.CAPABILITY_LIST_CHILDREN).equals("true");

		if (listChilden)
			scheduleEntry.setResult(container.getMetadata(), new ArrayList<>(container.getChildren().keySet()));
		else
			scheduleEntry.setResult(container.getMetadata(), Collections.EMPTY_LIST);

		letOperationSucceed(scheduleEntry);
	}


	/**
	 * Chooses server for a given container.
	 * <p/>
	 * Overwrite this method to change the container-to-server-allocation policy.
	 *
	 * @param container the container to allocate on one ore multiple servers
	 * @return a list of all servers, this container is allocated on
	 */
	protected List<ObjectStorageServer> getServerRingForNewContainer(CdmiObjectContainer container) {
		return new ArrayList<>(this.servers.values());
	}


	public IUsageHistory getUsageFor(int userId) {
		if (userDebts.containsKey(userId)) {
			return userDebts.get(userId);
		}
		return null;
	}

	public String dumpCloudFiles() {
		StringBuilder builder = new StringBuilder();
		final String s = "--";

		builder.append("cloud " + rootUrl + " located in " + regionalCisName).append("capabilities: " + characteristics.toString()).append("\n");
		for (int userID : userToRootContainerMapping.keySet()) {
			CdmiRootContainer root = userToRootContainerMapping.get(userID);
			builder.append(s).append("User " + userID + " with root container " + root.getEntityId()).append(" (").
					append(toHumanReadable(root.getSize())).append("/").
					append(toHumanReadable(root.getPhysicalSize())).append("\n");

			for (CdmiId containerId : root.getChildren().keySet()) {
				CdmiObjectContainer container = root.getChild(containerId);
				builder.append(s).append(s).append(container).append("\n");
				for (CdmiId oId : container.getChildren().keySet()) {
					builder.append(s).append(s).append(s).append(container.getChild(oId)).append("\n");
				}
				builder.append("\n");
			}
			builder.append("\n");
		}

		return builder.toString();
	}

	public List<OperationTimeTraceSample> getOperationTimeTraces() {
		return requestTracker.getTraces();
	}

	@Override
	public TupleSequence<Double> getSamples(String key) {
		List<TupleSequence<Double>> sampleStreams = new ArrayList<>();
		switch (key) {
			case USED_STORAGE_PHYSICAL_ABS:
			case AVAILABLE_STORAGE_PHYICAL:
				for (ObjectStorageServer s : servers.values())
					sampleStreams.add(s.getSamples(key));
				return SequenceOperations.sum(sampleStreams);
			case USED_STORAGE_VIRTUAL_ABS:
			case AVAILABLE_STORAGE_VIRTUAL:
				for(CdmiRootContainer c : userToRootContainerMapping.values())
					sampleStreams.add(c.getSamples(key));
				return SequenceOperations.sum(sampleStreams);
			case USED_STORAGE_PERCENTAGE_PHYSICAL:
				return SequenceOperations.divide(getSamples(USED_STORAGE_PHYSICAL_ABS), getSamples(AVAILABLE_STORAGE_PHYICAL));
			case USED_STORAGE_PERCENTAGE_VIRTUAL:
				return SequenceOperations.divide(getSamples(USED_STORAGE_VIRTUAL_ABS), getSamples(AVAILABLE_STORAGE_VIRTUAL));
			case NUM_REQUESTS:
				return requestTracker.getSamples(NUM_EVENTS_TOTAL);
			case NUM_REQUESTS_PER_MINUTE:
				return requestTracker.getSamples(NUM_EVENTS_PER_MINUTE);
			case NUM_REQUESTS_PER_SECOND:
				return requestTracker.getSamples(NUM_EVENTS_PER_SECOND);
			case TOTAL_EARNINGS:
				for(int user : userToRootContainerMapping.keySet()) {
					sampleStreams.add(getUsageFor(user).getSamples(DEBTS));
				}
				return SequenceOperations.sum(sampleStreams);
		}
		return null;
	}

	CdmiCloudCharacteristics CloudCharacteristics() {
		return characteristics;
	}

	@Override
	public String[] getAvailableTrackingKeys() {
		return new String[]{USED_STORAGE_PHYSICAL_ABS, USED_STORAGE_VIRTUAL_ABS,
				USED_STORAGE_PERCENTAGE_PHYSICAL, USED_STORAGE_PERCENTAGE_VIRTUAL,
				AVAILABLE_STORAGE_PHYICAL, AVAILABLE_STORAGE_VIRTUAL,
				NUM_REQUESTS, NUM_REQUESTS_PER_MINUTE, NUM_REQUESTS_PER_SECOND,
				TOTAL_EARNINGS
		};
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	public String toString() {
		return getName();
	}
}