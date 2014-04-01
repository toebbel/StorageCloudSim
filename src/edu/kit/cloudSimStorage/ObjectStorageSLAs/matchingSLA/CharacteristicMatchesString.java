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
 * Requires a certain characteristic to be equal to some given string.
 * The characteristics key is case sensitive where as the value is case in-sensitive
 *
 * @author Tobias Sturm, 6/26/13 6:24 PM */
@Root
public class CharacteristicMatchesString extends SLARequirement {
	@Attribute(name="key", required = true)
	private String key;

	@Attribute(name="value", required = true)
	private String value;

	public CharacteristicMatchesString(@Attribute(name="key") String key, @Attribute(name="value") String value) {
		super();
		this.key = key;
		this.value = value.toLowerCase();
		description = key + "==" + value;
	}

	@Override
	public boolean match(CdmiCloudCharacteristics characteristics) {
		boolean result = characteristics.contains(key) && characteristics.get(key).toLowerCase().equals(value);
		return logAndReturn(result, characteristics.getCloudID());
	}
}
