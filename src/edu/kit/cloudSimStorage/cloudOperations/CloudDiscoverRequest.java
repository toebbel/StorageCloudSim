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

import edu.kit.cloudSimStorage.cdmi.CdmiOperationVerbs;

/** @author Tobias Sturm, 6/26/13 3:55 PM */
public class CloudDiscoverRequest extends CloudRequest {
	public CloudDiscoverRequest(int user) {
		super(CdmiOperationVerbs.GET, "cloud_charactersitics", user, 0, CloudRequest.GET);
	}

	@Override
	public String toString() {
		return "cloud characteristics discovery request by user " + user;
	}
}
