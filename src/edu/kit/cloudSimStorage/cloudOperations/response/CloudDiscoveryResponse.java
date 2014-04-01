/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.cloudOperations.response;

import edu.kit.cloudSimStorage.CdmiCloudCharacteristics;
import edu.kit.cloudSimStorage.cloudOperations.request.CloudDiscoverRequest;

/**
 * CDMI response for with characteristics of a Cloud.
 * Requested with a {@link edu.kit.cloudSimStorage.cloudOperations.request.CloudDiscoverRequest}
 * @author Tobias Sturm, 6/26/13 3:59 PM */
public class CloudDiscoveryResponse extends CloudResponse<CloudDiscoverRequest>{

	private CdmiCloudCharacteristics characteristics;

	public CloudDiscoveryResponse(CloudDiscoverRequest request, CdmiCloudCharacteristics characteristics) {
		super(request);
		this.characteristics = characteristics;
	}

	public CdmiCloudCharacteristics getCharacteristics() {
		return characteristics;
	}
}
