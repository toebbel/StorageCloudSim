/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.ObjectStorageSLAs.matchingSLA;

import edu.kit.cloudSimStorage.CdmiCloudCharacteristics;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Represents a {@link SLARequirement} that must be met by a Cloud before the attached {@link edu.kit.cloudSimStorage.UsageSequence} can be dispatched to it.
 *
 * Requires a cloud to  support a given capability or property. The key that describes the capability must be present and it's value must be true.
 *
 * @author Tobias Sturm, 6/26/13 5:33 PM */
@Root
public class SupportsCapability extends SLARequirement {
	@Attribute(name="capability_key")
	private String key;

	public SupportsCapability(@Attribute(name="capability_key") String key) {
		this.key = key;
		description = key + "!";
	}


	@Override
	public boolean match(CdmiCloudCharacteristics characteristics) {
		boolean result = characteristics.contains(key) && characteristics.get(key).toLowerCase().equals("true");
		return logAndReturn(result, characteristics.getCloudID());
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.getClass() == this.getClass() && ((SupportsCapability)obj).key.equals(key);
	}
}
