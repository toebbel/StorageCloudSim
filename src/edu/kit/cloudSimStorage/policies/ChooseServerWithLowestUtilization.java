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

import edu.kit.cloudSimStorage.storageModel.ObjectStorageServer;

import java.util.Comparator;

/** @author Tobias Sturm, 6/23/13 2:27 PM */
public class ChooseServerWithLowestUtilization implements Comparator<ObjectStorageServer> {
	@Override
	public int compare(ObjectStorageServer o1, ObjectStorageServer o2) {
		return (int) ((o2.getTotalWorkload() - o1.getTotalWorkload()) * 100);
	}
}
