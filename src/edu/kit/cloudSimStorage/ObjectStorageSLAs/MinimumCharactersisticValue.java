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

/** @author Tobias Sturm, 6/26/13 5:39 PM */
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
