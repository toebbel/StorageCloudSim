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
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Creates the sum of two {@link edu.kit.cloudSimStorage.ObjectStorageSLAs.SLARating} instances.
 * @author Tobias Sturm, 6/29/13 2:18 PM */
@Root
public class RakingSum extends SLARating {
	@Element(name = "a", required = true)
	SLARating a;
	@Element(name = "b", required = true)
	SLARating b;

	public RakingSum(@Element(name = "a") SLARating a, @Element(name = "b") SLARating b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public double score(CdmiCloudCharacteristics candidate) {
		return a.score(candidate) + b.score(candidate);
	}

	@Override
	public String toString() {
		return "(" + a + ") + (" + b + ")";
	}
}
