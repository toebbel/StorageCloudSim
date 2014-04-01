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
import org.simpleframework.xml.Root;

/**
 * Rates a cloud by characteristics that have bool values.
 *
 * Assigns a positive score if value is present and positive
 * Ageings a negative score if value is present and negative
 * Assigns a neutral score if value is not present.
 *
 * All three values can be choose independently.
 *
 * @author Tobias Sturm, 6/26/13 6:03 PM */
@Root
public class RateBoolCharacteristics extends SLARating {
	@Attribute(name = "positive")
	private double positiveScore;
	@Attribute(name = "negative")
	private double negativeScore;
	@Attribute(name = "neutral")
	private double neutralScore;

	@Attribute(name = "key")
	private String key;
	@Attribute(name = "desired")
	private String desiredValue;

	public RateBoolCharacteristics(@Attribute(name = "positive") double positiveScore, @Attribute(name = "negative") double negativeScore, @Attribute(name = "neutral") double neutralScore, @Attribute(name = "key") String key, @Attribute(name = "desired") String desiredValue) {
		this.positiveScore = positiveScore;
		this.negativeScore = negativeScore;
		this.neutralScore = neutralScore;
		this.key = key;
		this.desiredValue = desiredValue;
	}

	@Override
	public double score(CdmiCloudCharacteristics candidate) {
		if(!candidate.contains(key))
			return neutralScore;
		else if(candidate.contains(key) && candidate.get(key).equals(desiredValue))
			return positiveScore;
		else
			return neutralScore;
	}
}
