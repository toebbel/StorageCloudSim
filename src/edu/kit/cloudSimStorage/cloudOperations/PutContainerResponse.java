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

import edu.kit.cloudSimStorage.cdmi.CdmiObjectContainer;

/** @author Tobias Sturm, 5/22/13 2:52 PM */
public class PutContainerResponse extends CloudResponse<PutContainerRequest> {
	protected CdmiObjectContainer container;

	public CdmiObjectContainer getContainer() {
		return container;
	}

	public PutContainerResponse(PutContainerRequest request, CdmiObjectContainer container) {
		super(request);
		this.container = container;
	}

	@Override
	public String toString() {
		return "PutContainerResponse: created '" + container + "'";
	}
}
