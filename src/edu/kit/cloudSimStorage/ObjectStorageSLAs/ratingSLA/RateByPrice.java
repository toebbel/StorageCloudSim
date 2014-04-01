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
import org.simpleframework.xml.Default;

import static edu.kit.cloudSimStorage.CdmiCloudCharacteristics.DOWNLOAD_COSTS;
import static edu.kit.cloudSimStorage.CdmiCloudCharacteristics.STORAGE_COSTS;
import static edu.kit.cloudSimStorage.CdmiCloudCharacteristics.UPLOAD_COSTS;

/**
 * Helper class for more convenience.
 *
 * Rates a cloud using 1/(price per stored GB) + 1/(price per uploaded GB) + 1/(price per downloaded GB)
 * See {@link RateCharacteristicsWithInverse} for details.
 *
 * @author Tobias Sturm, 6/29/13 12:53 PM */
@Default
public class RateByPrice extends SLARating {

	public RateByPrice() {
		description = "rate 1/price for up and download and storage costs";
	}

	private static RakingSum rater = new RakingSum(new RakingSum(new RateCharacteristicsWithInverse(DOWNLOAD_COSTS, "rate 1/download cost", 0), new RateCharacteristicsWithInverse(UPLOAD_COSTS, "rate 1/upload cost", 0)), new RateCharacteristicsWithInverse(STORAGE_COSTS, "rate 1/storage cost", 0));

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
