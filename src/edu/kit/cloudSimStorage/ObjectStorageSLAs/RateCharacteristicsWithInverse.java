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
import org.simpleframework.xml.Default;
import org.simpleframework.xml.Element;

/** @author Tobias Sturm, 6/26/13 5:58 PM */
@Default
public class RateCharacteristicsWithInverse extends SLACloudRater {
	@Attribute(name = "key")
	private String key;

	@Attribute(name = "defaultScore")
	double defaultScore;

	@Attribute(name = "scale", required = false)
	double scale;

	public RateCharacteristicsWithInverse(@Attribute(name = "key") String key, @Element(name = "description") String description, @Attribute(name = "defaultScore") double scoreIfNotExistent, @Attribute(name = "scale") double scale) {
		this.description = description;
		this.key = key;
		defaultScore = scoreIfNotExistent;
		this.scale = scale;
	}

	public RateCharacteristicsWithInverse(@Attribute(name = "key") String key, @Element(name = "description") String description, @Attribute(name = "defaultScore") double scoreIfNotExistent) {
		this.description = description;
		this.key = key;
		defaultScore = scoreIfNotExistent;
		scale = 1;
	}

	@Override
	public double score(CdmiCloudCharacteristics candidate) {
		if(!candidate.contains(key))
			return defaultScore;
		return scale / Double.valueOf(candidate.get(key));
	}
}
