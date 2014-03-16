/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.ObjectStorageSLAs;

import edu.kit.cloudSimStorage.CdmiCloudCharacteristics;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import java.util.logging.Logger;

/** @author Tobias Sturm, 6/26/13 5:24 PM */
@Root
public abstract class SLARequirement {

	@Attribute(required = false)
	protected String description;

	public abstract boolean match(CdmiCloudCharacteristics characteristics);

	@Override
	public String toString() {
		return description;
	}

	protected boolean logAndReturn(boolean result, int cloudID) {
		if(result)
			Logger.getAnonymousLogger().info("cloud " + cloudID + " succeeded SLARequirement '" + this + "'");
		else
			Logger.getAnonymousLogger().info("cloud " + cloudID + " failed SLARequirement '" + this + "'");
		return result;
	}
}