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

import edu.kit.cloudSimStorage.cdmi.CdmiId;
import edu.kit.cloudSimStorage.cdmi.CdmiOperationVerbs;

/** @author Tobias Sturm, 6/5/13 3:09 PM */
public class DeleteObjectRequest extends CloudRequest {
	private String objectName, containerName;
	private CdmiId cdmiID;

	public DeleteObjectRequest(String container, String objectName, int user) {
		super(CdmiOperationVerbs.DELETE, container + "/" + objectName, user, 0, CloudRequest.DELETE);
		this.objectName = objectName;
		this.containerName = container;
		cdmiID = CdmiId.UNKNOWN;
	}

	public DeleteObjectRequest(String cdmiId, int user) {
		super(CdmiOperationVerbs.DELETE, "cdmi_objectid/" + cdmiId, user, 0, CloudRequest.DELETE); //TODO correct CMDI syntax here
		this.cdmiID = new CdmiId(cdmiId);
		this.containerName = "";
	}

	public String getObjectName() {
		return objectName;
	}

	public CdmiId getCdmiID() {
		return cdmiID;
	}

	public String getContainerName() {
		return containerName;
	}

	@Override
	public String toString() {
		return super.getRequestString() + " (DELETE object by " + super.getUser() + ")";
	}
}
