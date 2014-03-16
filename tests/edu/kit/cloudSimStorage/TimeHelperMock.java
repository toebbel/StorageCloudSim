/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage;

import edu.kit.cloudSimStorage.helper.TimeHelper;

/** @author Tobias Sturm, 9/11/13 5:02 PM */
public class TimeHelperMock extends TimeHelper {

	public long time;

	public static void init() {
		instance = new TimeHelperMock();
	}

	public static TimeHelperMock getInstance() {
		assert instance instanceof TimeHelperMock;
		return (TimeHelperMock) instance;
	}

	@Override
	public long now() {
		return time;
	}
}
