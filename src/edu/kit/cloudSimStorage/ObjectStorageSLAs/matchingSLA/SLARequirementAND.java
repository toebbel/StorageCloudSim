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
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Used to logical link two {@link SLARequirement}s with each other.
 *
 * Both requirements must be fulfilled to match a {@link edu.kit.cloudSimStorage.StorageCloud} against a {@link edu.kit.cloudSimStorage.UsageSequence}'s attached SLA
 * @author Tobias Sturm, 6/26/13 6:07 PM */
@Root
public class SLARequirementAND extends SLARequirement {
	@Element(name = "a", required = true)
	SLARequirement a;
	@Element(name = "b", required = true)
	SLARequirement b;

	public SLARequirementAND(@Element(name = "a") SLARequirement a, @Element(name = "b") SLARequirement b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public boolean match(CdmiCloudCharacteristics characteristics) {
		return a.match(characteristics) && b.match(characteristics);
	}

	@Override
	public String toString() {
		return "(" + a + ") & (" + b + ")";
	}
}
