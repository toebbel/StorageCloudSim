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

import edu.kit.cloudSimStorage.cdmi.CdmiMetadata;
import org.simpleframework.xml.Default;

/**
 * Created by: Tobias Sturm
 * Date: 4/26/13          Storage.ObjectStorageSLAs;

import org.junit.Test;

import java.io.ByteArrayInputStream;

 * Time: 2:23 PM
 */
@Default
public class CdmiCloudCharacteristics extends CdmiMetadata {

	public final static String MAX_METADATA_ITEMS = "cdmi_metadata_maxitems";
	public final static String MAX_MEDATADA_ITEM_SIZE = "cdmi_metadata_maxsize";
	public final static String CAPABILITY_DOMAINS = "cdmi_domains";
	public final static String CAPABILITY_EXPORT_NFS = "cdmi_export_nfs";
	public final static String CAPABILITY_EXPORT_WEBDAV = "cdmi_export_webdav";
	public final static String CAPABILITY_EXPORT_ISCSI = "cdmi_export_iscsi";
	public final static String CAPABILITY_QUEUES = "cdmi_queues";
	public final static String CAPABILITY_NOTIFICATIONS = "cdmi_notification";
	public final static String CAPABILITY_QUERY = "cdmi_query";
	public final static String CAPABILITY_LIST_CHILDREN = "cdmi_list_children";
	public final static String CAPABILITY_READ_METADATA = "cdmi_read_metadata";
	public final static String CAPABILITY_MOD_METADATA = "cdmi_modify_metadata";
	public final static String CAPABILITY_CREATE_CONTAINER = "cdmi_create_container";
	public final static String CAPABILITY_DELETE_CONTAINER = "cdmi_delete_container";

	public final static String UPLOAD_COSTS = "SLA upload costs";
	public final static String DOWNLOAD_COSTS = "SLA download costs";
	public final static String STORAGE_COSTS = "SLA storage costs";
	public final static String AVAILABLE_CAPACITY = "SLA available capacity";
	public final static String CLOUD_ID = "SLA cloud id";

	public final static String PROVIDER_NAME = "SLA provider name";

	//depend on requesting user
	public final static String MAX_LATENCY = "SLA maximum latency";
	public final static String MIN_BANDWIDTH = "SLA minimum bandwidth";

	public final static String[] CHARACTERISTIC_KEYS  =new String[] {MIN_BANDWIDTH, MAX_LATENCY, PROVIDER_NAME, AVAILABLE_CAPACITY, STORAGE_COSTS, DOWNLOAD_COSTS, UPLOAD_COSTS, CAPABILITY_CREATE_CONTAINER, CAPABILITY_DOMAINS, CAPABILITY_EXPORT_ISCSI, CAPABILITY_EXPORT_NFS, CAPABILITY_EXPORT_WEBDAV, CAPABILITY_LIST_CHILDREN, CAPABILITY_MOD_METADATA, CAPABILITY_NOTIFICATIONS, CAPABILITY_QUERY, CAPABILITY_QUEUES, AVAILABLE_CAPACITY, MAX_METADATA_ITEMS, MAX_MEDATADA_ITEM_SIZE, CAPABILITY_DELETE_CONTAINER, CAPABILITY_READ_METADATA, CLOUD_ID, MAX_OBJECT_SIZE, MAX_CHILD_COUNT, NUM_VERSIONS, NUM_REPLICA};

	public static String[] doNotInheritFromCloudCharacteristics = {MIN_BANDWIDTH, MAX_LATENCY, PROVIDER_NAME, AVAILABLE_CAPACITY, STORAGE_COSTS, DOWNLOAD_COSTS, UPLOAD_COSTS, CAPABILITY_CREATE_CONTAINER, CAPABILITY_DOMAINS, CAPABILITY_EXPORT_ISCSI, CAPABILITY_EXPORT_NFS, CAPABILITY_EXPORT_WEBDAV, CAPABILITY_LIST_CHILDREN, CAPABILITY_MOD_METADATA, CAPABILITY_NOTIFICATIONS, CAPABILITY_QUERY, CAPABILITY_QUEUES, AVAILABLE_CAPACITY, CAPABILITY_DELETE_CONTAINER, CAPABILITY_READ_METADATA, CLOUD_ID};

	@Override
	public String get(String key) {
		return super.get(key);    //To change body of overridden methods use File | Settings | File Templates.
	}

	/**
	 * The currently available capacity in byte
	 * @return bytes
	 */
	public long getAvailableCapacity() {
		return Long.parseLong(super.get(AVAILABLE_CAPACITY));
	}

	@Override
	public boolean contains(String key) {
		return super.contains(key);
	}

	/**
	 * Cost factor amount of uploaded data in GByte -> US Dollar per day
	 * @return $/GByte/day
	 */
	public double getUploadCosts() {
		return Double.parseDouble(super.get(UPLOAD_COSTS));
	}

	/**
	 * Cost factor amount of downloaded data in GByte -> US Dollar per day
	 * @return $/GByte/day
	 */
	public double getDownloadCosts() {
		return Double.parseDouble(super.get(DOWNLOAD_COSTS));
	}

	/**
	 * Cost factor amount of stored data in GByte -> US Dollar per day
	 * @return $/GByte/day
	 */
	public double getStorageCosts() {
		return Double.parseDouble(super.get(STORAGE_COSTS));
	}

	public CdmiCloudCharacteristics setCloudName(String name) {
		super.set(PROVIDER_NAME, name);
		return this;
	}

	public CdmiCloudCharacteristics setUploadCosts(double uploadCosts) {
		super.set(UPLOAD_COSTS, String.valueOf(uploadCosts));
		return this;
	}

	public CdmiCloudCharacteristics setMaxLatency(double maxLatency) {
		super.set(MAX_LATENCY, String.valueOf(maxLatency));
		return this;
	}

	public CdmiCloudCharacteristics setMinBandwidth(double minBandwidth) {
		super.set(MIN_BANDWIDTH, String.valueOf(minBandwidth));
		return this;
	}

	public CdmiCloudCharacteristics setCloudID(int id) {
		super.set(CLOUD_ID, String.valueOf(id));
		return this;
	}

	public CdmiCloudCharacteristics setDownloadCosts(double downloadCosts) {
		super.set(DOWNLOAD_COSTS, String.valueOf(downloadCosts));
		return this;
	}

	public CdmiCloudCharacteristics setStorageCosts(double storageCosts) {
		super.set(STORAGE_COSTS, String.valueOf(storageCosts));
		return this;
	}

	public CdmiCloudCharacteristics setAvailableCapacity(long availableCapacity) {
		super.set(AVAILABLE_CAPACITY, String.valueOf(availableCapacity));
		return this;
	}


	public static CdmiCloudCharacteristics getDefault() {
		CdmiCloudCharacteristics result = new CdmiCloudCharacteristics();
		result.set(MAX_MEDATADA_ITEM_SIZE, "4096");
		result.set(MAX_METADATA_ITEMS, "1024");
		result.set(NUM_VERSIONS, "1");
		result.set(NUM_REPLICA, "3");
		result.set(CAPABILITY_CREATE_CONTAINER, "true");
		result.set(CAPABILITY_DELETE_CONTAINER, "true");
		result.set(CAPABILITY_DOMAINS, "false");
		result.set(CAPABILITY_EXPORT_ISCSI, "false");
		result.set(CAPABILITY_EXPORT_NFS, "false");
		result.set(CAPABILITY_EXPORT_WEBDAV, "false");
		result.set(CAPABILITY_QUEUES, "false");
		result.set(CAPABILITY_NOTIFICATIONS, "false");
		result.set(CAPABILITY_QUERY, "false");
		result.set(CAPABILITY_LIST_CHILDREN, "true");
		result.set(CAPABILITY_READ_METADATA, "true");
		result.set(CAPABILITY_MOD_METADATA, "true");
		return result;
	}

	public CdmiCloudCharacteristics copy() {
		CdmiCloudCharacteristics result = new CdmiCloudCharacteristics();
		result.mergeWith(this);
		return result;
	}

	public int getCloudID() {
		return Integer.parseInt(super.get(CLOUD_ID));
	}

	public double getMaximimLatency() {
		return Double.parseDouble(super.get(MAX_LATENCY));
	}

	public double getMinimumBandwidth() {
		return Double.parseDouble(super.get(MIN_BANDWIDTH));
	}
}
