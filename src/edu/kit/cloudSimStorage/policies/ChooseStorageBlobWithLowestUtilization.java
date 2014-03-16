/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.policies;

import edu.kit.cloudSimStorage.storageModel.StorageBlobLocation;

import java.util.Comparator;

/** @author Tobias Sturm, 6/23/13 2:33 PM */
public class ChooseStorageBlobWithLowestUtilization implements Comparator<StorageBlobLocation> {
	private static ChooseServerWithLowestUtilization policy = new ChooseServerWithLowestUtilization();

	@Override
	public int compare(StorageBlobLocation o1, StorageBlobLocation o2) {
		return policy.compare(o1.getServer(), o2.getServer());
	}
}
