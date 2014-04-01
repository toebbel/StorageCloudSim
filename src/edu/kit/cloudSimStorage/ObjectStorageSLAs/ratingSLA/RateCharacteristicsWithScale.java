/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.ObjectStorageSLAs.ratingSLA;

import edu.kit.cloudSimStorage.CdmiCloudCharacteristics;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.Element;

/**
 * Rates a given characteristic with a scale.
 *
 * Can be used to model 'score clouds with bandwith in MB/s * 50'
 * If the certain key is not given in the presented {@link edu.kit.cloudSimStorage.CdmiCloudCharacteristics} instance, a default score is assigned.
 *
 * @author Tobias Sturm, 6/26/13 5:58 PM */
@Default
public class RateCharacteristicsWithScale extends SLARating {
	@Attribute(name = "key")
	private String key;
	@Attribute(name = "scale")
	private double scale;
	@Attribute(name = "defaultScore")
	private double defaultScore;

	public RateCharacteristicsWithScale(@Attribute(name = "key") String key, @Attribute(name = "scale") double scale, @Element(name = "description") String description, @Attribute(name = "defaultScore") double scoreIfNotExistent) {
		this.scale = scale;
		this.description = description;
		this.key = key;
		defaultScore = scoreIfNotExistent;
	}

	@Override
	public double score(CdmiCloudCharacteristics candidate) {
		if(!candidate.contains(key))
			return defaultScore;
		return Double.valueOf(candidate.get(key)) * scale;
	}
}
