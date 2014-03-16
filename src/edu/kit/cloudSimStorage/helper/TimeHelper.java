/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.helper;

import org.cloudbus.cloudsim.core.CloudSim;

import java.text.SimpleDateFormat;
import java.util.Date;

/** @author Tobias Sturm, 6/23/13 2:51 PM */
public class TimeHelper {

	private static SimpleDateFormat defaultDateFormat = new SimpleDateFormat("hh:mm:ss:S");
	protected static TimeHelper instance = new TimeHelper();

	public static TimeHelper getInstance() {
		return instance;
	}

	public static String timeToString(long timestamp) {
		return timeToString(timestamp, defaultDateFormat);
	}

	public static String timeToString(long timestamp, SimpleDateFormat format) {
		return format.format(new Date(timestamp));
	}

	public long now() {
		return (long) CloudSim.clock();
	}
}
