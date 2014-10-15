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
import edu.kit.cloudSimStorage.UsageSequence;
import edu.kit.cloudSimStorage.cloudOperations.response.CloudDiscoveryResponse;
import edu.kit.cloudSimStorage.monitoring.ILoggable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/** @author Tobias Sturm, 6/30/13 3:15 PM */
class DiscoveryState implements ILoggable {
	protected List<String> activeRequests, returnedRequests;
	protected HashMap<Integer, CdmiCloudCharacteristics> discoveredCharacteristics;
	protected UsageSequence sequence;

	protected Logger logger;

	public DiscoveryState(UsageSequence sequence, int metaBrokerId) {

		discoveredCharacteristics = new HashMap<>();
		this.activeRequests = new ArrayList<String>();
		this.returnedRequests = new ArrayList<String>();
		this.sequence = sequence;

		logger = Logger.getLogger("DiscoveryProcess_seq" + sequence + "_meta" + metaBrokerId);
		logger.setParent(sequence.getLogger());
		logger.setUseParentHandlers(true);
	}


	public void startedDiscovery(String operationID) {
		if(activeRequests.contains(operationID))
			throw new IllegalStateException("Broker is already active for this discovery");
		activeRequests.add(operationID);
	}


	public List<CdmiCloudCharacteristics> getMatches() {
		return  sequence.getSLA().getMatches(new ArrayList<>(discoveredCharacteristics.values()));
	}


	public Integer getAssociatedSequenceId() {
		return sequence.getId();
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	public boolean isWaitingForOperations() {
		return !activeRequests.isEmpty();
	}

	public boolean isWaitingForOperation(String requestOperationID) {
		return activeRequests.contains(requestOperationID);
	}

	public void endedDiscovery(CloudDiscoveryResponse data) {
		if(!activeRequests.contains(data.getOperationID()))
			throw new IllegalStateException("Returning DiscoveryResponse withour known request");
		activeRequests.remove(data.getOperationID());
		returnedRequests.add(data.getOperationID());
		discoveredCharacteristics.put(data.getCharacteristics().getCloudID(), data.getCharacteristics());
	}
}