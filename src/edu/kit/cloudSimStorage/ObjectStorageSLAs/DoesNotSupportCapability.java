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

/**
 * Represents a {@link edu.kit.cloudSimStorage.ObjectStorageSLAs.SLARequirement} that must be met by a Cloud before the attached {@link edu.kit.cloudSimStorage.UsageSequence} can be dispatched to it.
 *
 * Requires a cloud to not support a given capability or property. The key that describes the capability must be either not present or it's value must not be true to match this requirement.
 *
 * @author Tobias Sturm, 6/26/13 5:39 PM */
@Root
public class DoesNotSupportCapability extends SLARequirement {
	@Attribute(name="capability_key")
	private String key;

	public DoesNotSupportCapability(@Attribute(name="capability_key") String key) {
		this.key = key;
		description = "does not support " + key;
	}

	@Override
	public boolean match(CdmiCloudCharacteristics characteristics) {
		boolean result = !characteristics.contains(key) || characteristics.get(key).toLowerCase() != "true";
		logAndReturn(result, characteristics.getCloudID());
		return result;
	}
}
