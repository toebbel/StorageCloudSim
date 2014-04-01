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
 * CDMI response for with characteristics of a Cloud.
 * Requested with a {@link edu.kit.cloudSimStorage.cloudOperations.CloudDiscoverRequest}
 * @author Tobias Sturm, 6/26/13 3:59 PM */
public class CloudDiscoveryResponse extends CloudResponse<CloudDiscoverRequest>{

	CdmiCloudCharacteristics characteristics;

	public CloudDiscoveryResponse(CloudDiscoverRequest request) {
		super(request);
	}

	public CdmiCloudCharacteristics getCharacteristics() {
		return characteristics;
	}
}
