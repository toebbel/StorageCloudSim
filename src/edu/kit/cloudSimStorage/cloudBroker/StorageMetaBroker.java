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
import edu.kit.cloudSimStorage.UsageSequence;
import edu.kit.cloudSimStorage.cloudOperations.request.CloudDiscoverRequest;
import edu.kit.cloudSimStorage.cloudOperations.response.CloudDiscoveryResponse;
import edu.kit.cloudSimStorage.cloudOperations.request.CloudRequest;
import edu.kit.cloudSimStorage.monitoring.TraceableResource;
import edu.kit.cloudSimStorage.monitoring.TraceableResourceAliasing;
import edu.kit.cloudSimStorage.monitoring.TupleSequence;
import edu.kit.cloudSimStorage.monitoring.EventTracker;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** @author Tobias Sturm, 6/26/13 3:36 PM */
public class StorageMetaBroker extends SimEntity implements TraceableResource {

	protected HashMap<Integer, Integer> sequenceToCloudMapping, cloudToBrokerMapping, sequenceToBrokerMapping;
	protected List<StorageBroker> availableBrokers;
	protected List<Integer> availableClouds;
	protected List<DiscoveryState> runningDiscoveries, waitingDiscoveries;

	// for monitoring
	private static final String TOTAL_ACCPECTED_SEQUENCES = "total number of accpected sequences in metabroker";
	private static final String TOTAL_DECLINED_SEQUENCES = "total number of declined sequences in metabroker";
	protected EventTracker<DiscoveryState> declinedSequences, accepctedSequences;
	protected TraceableResourceAliasing trackableSubResources;


	/**
	 * Creates a new entity.
	 *
	 * @param name the name to be associated with this entity
	 */
	public StorageMetaBroker(String name) {
		super(name);
		runningDiscoveries = new ArrayList<>();
		waitingDiscoveries = new ArrayList<>();
		availableBrokers = new ArrayList<>();
		availableClouds = new ArrayList<>();
		sequenceToCloudMapping = new HashMap<>();
		sequenceToBrokerMapping = new HashMap<>();
		cloudToBrokerMapping = new HashMap<>();
		declinedSequences = new EventTracker<>("unsatisyable sequences", "fail tracker");
		accepctedSequences = new EventTracker<>("accepcted sequences", "accept tracker");

		trackableSubResources = new TraceableResourceAliasing();
		trackableSubResources.addMapping(TOTAL_DECLINED_SEQUENCES, NUM_EVENTS_TOTAL, declinedSequences);
		trackableSubResources.addMapping(TOTAL_ACCPECTED_SEQUENCES, NUM_EVENTS_TOTAL, accepctedSequences);
	}

	/**
	 * Adds a Cloud provider, that will be considered for all sequences. A new {@link StorageBroker} instance will be created to enable the access to the Cloud
	 * @param cloudID the Cloud to add
	 */
	public void addCloud(int cloudID) {
		StorageBroker broker = new StorageBroker(cloudID, getId());
		availableBrokers.add(broker);
		cloudToBrokerMapping.put(cloudID, broker.getId());
		availableClouds.add(cloudID);
	}


	public void addNewUsageSequence(UsageSequence seq) {
		DiscoveryState dp = new DiscoveryState(seq, getId());
		waitingDiscoveries.add(dp);
	}


	@Override
	public void startEntity() {
		startDiscoveryForNextSequence();
	}

	private void startDiscoveryForNextSequence() {
		if(waitingDiscoveries.isEmpty())
			return;
		DiscoveryState dp = waitingDiscoveries.remove(0);
		runningDiscoveries.add(dp);
		for(Integer cloudID : availableClouds) {
			CloudRequest r = new CloudDiscoverRequest(getId());
			dp.startedDiscovery(r.getOperationID());
			scheduleNow(cloudID, r.getTag(), r);
		}
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()) {
			case CloudRequest.SUCC:
				String requestOperationID = ((CloudDiscoveryResponse)ev.getData()).getOperationID();
				for(DiscoveryState ds : runningDiscoveries) {
					if(ds.isWaitingForOperation(requestOperationID)) {
						ds.endedDiscovery((CloudDiscoveryResponse)ev.getData());
						if(!ds.isWaitingForOperations()) { //dispatch sequence on cloud and start next discovery
							dispatchSequenceToBestCloud(ds);
							startDiscoveryForNextSequence();
						}
						break;
					}
				}
			case UserMetaRequest.BROKER_FINISHED:
				break;
		}
	}

	private void dispatchSequenceToBestCloud(DiscoveryState ds) {
		assert ds.isWaitingForOperations();
		List<CdmiCloudCharacteristics> candidates = ds.getMatches();
		if(candidates.isEmpty()) {
			declinedSequences.addEvent(ds);
			return;
		}

		accepctedSequences.addEvent(ds);
		int bestCloud = candidates.get(0).getCloudID();
		StorageBroker connectedBroker = getBrokerWithID(cloudToBrokerMapping.get(bestCloud));
		sequenceToCloudMapping.put(ds.getAssociatedSequenceId(), bestCloud);
		sequenceToBrokerMapping.put(ds.getAssociatedSequenceId(), connectedBroker.getId());
		for(UserRequest u : ds.sequence.getRequests()) {
			connectedBroker.enqueueUserRequest(u);
		}
        connectedBroker.startEntity();
	}

	private StorageBroker getBrokerWithID(int id) {
		for(StorageBroker b : availableBrokers) {
			if(b.getId() == id)
				return b;
		}
		return null;
	}


	@Override
	public void shutdownEntity() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public TupleSequence<Double> getSamples(String key) {
		return trackableSubResources.getSamples(key);
	}

	@Override
	public String[] getAvailableTrackingKeys() {
		return trackableSubResources.getAvailableTrackingKeys();
	}

	public TraceableResource getStatsforCloudBroker(int cloudID) {
		return getBrokerWithID(cloudToBrokerMapping.get(cloudID));
	}

	public static List<Integer> getAllCloudsIDs() {
		List<Integer> result = new ArrayList<>();
		for(SimEntity entity : CloudSim.getEntityList())
			if(entity instanceof StorageCloud)
				result.add(entity.getId());

		return result;
	}
}