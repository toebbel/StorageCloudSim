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

@Root
public abstract class SLACloudRater {

	@Element(name = "description", required = false)
	protected String description;

	/**
	 * Calculates a score for a candidate, based on a it's characteristics
	 *
	 * @param candidate the candidate to evaluate
	 * @return
	 */
	public abstract double score(CdmiCloudCharacteristics candidate);

	@Override
	public String toString() {
		return description;
	}
}
