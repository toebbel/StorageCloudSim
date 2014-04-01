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

/** @author Tobias Sturm, 6/24/13 2:08 PM */
public interface TraceableResource {

	/**
	 * Returns all samples that are collected under the given key.
	 * <p/>
	 * The key has to be inside the array {@link #getAvailableTrackingKeys()}, otherwise {@code null} will be returned.
	 *
	 *
	 * @param key the key to collect
	 * @return collected samples or null
	 */
	TupleSequence<Double> getSamples(String key);

	/**
	 * Returns all collection keys. Is never empty.
	 *
	 * @return all available keys
	 */
	String[] getAvailableTrackingKeys();

	public static final String AVAILABLE_STORAGE = "available storage";
	public static final String AVAILABLE_STORAGE_VIRTUAL = "available storage (virtual)";
	public static final String AVAILABLE_STORAGE_PHYICAL = "available storage (physical)";

	public static final String USED_STORAGE_ABS = "used storage (total)";
	public static final String USED_STORAGE_PHYSICAL_ABS = "used storage (total physical)";
	public static final String USED_STORAGE_VIRTUAL_ABS = "used storage (total virtual)";

	public static final String USED_STORAGE_PERCENTAGE = "used storage (in %)";
	public static final String USED_STORAGE_PERCENTAGE_VIRTUAL = "used storage (in %, virtual)";
	public static final String USED_STORAGE_PERCENTAGE_PHYSICAL = "used storage (in %, physical)";

	public static final String USED_BANDWIDTH_PERCENTAGE = "used bandwith (in %)";
	public static final String USED_BANDWIDTH = "used bandwidth";

	public static final String DEBTS = "debts";
	public static final String TOTAL_EARNINGS = "total earnings";

	public static final String TRAFFIC = "traffic";
	public static final String NUM_REQUESTS = "# request";
	public static final String NUM_REQUESTS_PER_MINUTE = "# request per minute";
	public static final String NUM_REQUESTS_PER_SECOND = "# request per second";
	public static final String NUM_REQUESTS_LIST = "# list request";
	public static final String NUM_REQUESTS_LIST_PER_MINUTE = "# list request per minute";
	public static final String NUM_REQUESTS_LIST_PER_SECOND = "# list request per second";
	public static final String NUM_REQUESTS_OTHER = "# other request";
	public static final String NUM_REQUESTS_OTHER_PER_MINUTE = "# other request per minute";
	public static final String NUM_REQUESTS_OTHER_PER_SECOND = "# other request per second";
	public static final String TRAFFIC_DOWNLOAD = "traffic (download)";
	public static final String TRAFFIC_UPLOAD = "traffic (upload)";

	public static final String NUM_EVENTS_PER_SECOND = "# events per second";
	public static final String NUM_EVENTS_PER_MINUTE = "# events per minute";
	public static final String NUM_EVENTS_TOTAL = "# events";
}
