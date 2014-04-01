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

import edu.kit.cloudSimStorage.CdmiCloudCharacteristics;

/**
 * Cloud internal state of a {@link edu.kit.cloudSimStorage.cloudOperations.CloudDiscoverRequest}
 *
 * @author Tobias Sturm, 6/26/13 4:00 PM */
public class CloudDiscoveryScheduleEntry extends CloudScheduleEntry<CloudDiscoverRequest> {

	CdmiCloudCharacteristics characteristics;

	public CloudDiscoveryScheduleEntry(CloudDiscoverRequest request, int inquiringPartner) {
		super(request, inquiringPartner);
	}

	public void setCharacteristics(CdmiCloudCharacteristics characteristics) {
		this.characteristics = characteristics;
	}

	@Override
	public CloudResponse<CloudDiscoverRequest> generateResponse() {
		CloudDiscoveryResponse rsp = new CloudDiscoveryResponse(request);
		rsp.characteristics = characteristics;
		return rsp;
	}
}
