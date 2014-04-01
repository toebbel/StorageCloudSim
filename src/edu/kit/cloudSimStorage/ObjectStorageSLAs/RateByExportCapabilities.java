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
import org.simpleframework.xml.Default;

import static edu.kit.cloudSimStorage.CdmiCloudCharacteristics.*;

/**
 * Helper class for more convenience.
 *
 * Rates every cloud with a +1 if a export capability is present, -1 of not.
 * Rates the capabilities NFS, webdav and iscsi.
 *
 * See {@link edu.kit.cloudSimStorage.ObjectStorageSLAs.RateBoolCharacteristics} for details.
 *
 * @author Tobias Sturm, 6/29/13 12:53 PM */
@Default
public class RateByExportCapabilities extends SLARating {

	public RateByExportCapabilities() {
		description = "+1 for every export capability, -1 otherwise";
	}

	private static RakingSum rater = new RakingSum(new RakingSum(new RateBoolCharacteristics(1, -1, 0, CAPABILITY_EXPORT_NFS, "true"), new RateBoolCharacteristics(1, -1, 0, CAPABILITY_EXPORT_WEBDAV, "true")), new RateBoolCharacteristics(1, -1, 0, CAPABILITY_EXPORT_ISCSI, "true"));

	@Override
	public double score(CdmiCloudCharacteristics candidate) {
		return rater.score(candidate);
	}

	@Override
	public String toString() {
		return rater.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.getClass() == this.getClass();
	}
}
