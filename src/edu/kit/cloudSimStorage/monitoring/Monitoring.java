/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.monitoring;

import java.util.ArrayList;
import java.util.List;

/** @author Tobias Sturm, 6/23/13 6:01 PM */
public class Monitoring {
	private static List<ResourceUsageHistory> histories = new ArrayList<>();

	public static void register(ResourceUsageHistory h) {
		histories.add(h);
	}
}
