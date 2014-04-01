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
import org.simpleframework.xml.Default;

/**
 * Represents a {@link SLARequirement} that must be met by a Cloud before the attached {@link edu.kit.cloudSimStorage.UsageSequence} can be dispatched to it.
 *
 * Requires some numerical value of a characteristic to be greater or equal than the given level.
 * Can be used for example to model the requirement 'bandwith must be greater than x'
 *
 * The existence of the characteristic key is always mandatory.
 *
 * @author Tobias Sturm, 6/26/13 5:39 PM */
@Default
public class MinimumCharactersisticValue extends SLARequirement {

	@Attribute(name="key")
	private String key;

	@Attribute(name="min")
	private double minValue;

	public MinimumCharactersisticValue(@Attribute(name="key") String key, @Attribute(name="min") double minValue) {
		this.key = key;
		this.minValue = minValue;
		description = key + ">=" + minValue;
	}

	@Override
	public boolean match(CdmiCloudCharacteristics characteristics) {
		boolean result = characteristics.contains(key) && Double.valueOf(characteristics.get(key)) >= minValue;
		return logAndReturn(result, characteristics.getCloudID());
	}
}
