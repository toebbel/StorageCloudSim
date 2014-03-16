/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.cdmi;

import org.cloudbus.cloudsim.core.CloudSim;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

import java.util.*;

/**
 * @author Tobias Sturm
 * Date: 4/26/13
 * Time: 1:14 PM
 */
@Root
public class CdmiMetadata {
	@ElementMap
	protected HashMap<String, String> metadata;

	public final static String SIZE = "cdmi_size";
	public final static String CREATED_AT = "created_at";
	public final static String LAST_WRITE_ACCESS = "last_write";
	public final static String TYPE = "type";
	public final static String LOCATION = "location";
	public final static String NUM_REPLICA = "number_replicas";
	public final static String NUM_VERSIONS = "keep_n_versions";
	public static final String MAX_OBJECT_SIZE = "max_size";
	public static final String MAX_CONTAINER_SIZE = "max_container_size"; /** in byte */
	public static final String MAX_CHILD_COUNT = "max_container_items";
	boolean cacheSize = false;
	long cachedSize = 0;

	public static final String[] objectMetadataKeys = {SIZE, CREATED_AT, LAST_WRITE_ACCESS, TYPE};

	public static final String[] objectContainerMetadataKeys = {LOCATION, LAST_WRITE_ACCESS, CREATED_AT, MAX_CONTAINER_SIZE, MAX_OBJECT_SIZE, MAX_CHILD_COUNT};
	public static final String[] doNotInheritFromObjectContainer = {LOCATION, NUM_REPLICA, NUM_VERSIONS, MAX_CONTAINER_SIZE, MAX_CHILD_COUNT, MAX_OBJECT_SIZE};

	public static final String[] rootContainerMetadataKeys = {LOCATION, NUM_REPLICA, NUM_VERSIONS, MAX_CONTAINER_SIZE, MAX_OBJECT_SIZE};
	public static final String[] doNotInheritFromRootContainerToObject = {LOCATION, NUM_REPLICA, NUM_VERSIONS, MAX_CONTAINER_SIZE, MAX_CHILD_COUNT};
	public static final String[] doNotInheritFromRootContainerToContainer = {LOCATION};

	public static String getNow() {
		return String.valueOf(CloudSim.getSimulationCalendar().getTime().getTime());
	}

	public CdmiMetadata() {
		this.metadata = new HashMap<>();
	}

	/**
	 * Returns the size of the meta-data in Byte.
	 * <p/>
	 * Assumes, that metadata are stored as UTF-8, separated with a key-value-separator and a CR-separator.
	 *
	 * @return #of bytes that are required to store the metadata on disc.
	 */
	public double getMetadataSize() {
		if(cacheSize)
			return cachedSize;

		int size = 0;
		for (String key : metadata.keySet())
			size += key.length() + 1 + metadata.get(key).length() + 1; //key, separator, value, separator

		cachedSize = size;
		cacheSize = true;
		return size;
	}

	public void set(String key, String value) {
		metadata.put(key, value);
		cacheSize = false;
	}

	public String get(String key) {
		if (!metadata.containsKey(key))
			throw new IllegalArgumentException("No such meta-data '" + key + "'");
		return metadata.get(key);
	}

	public boolean contains(String key) {
		return metadata.containsKey(key);
	}


	/**
	 * Takes all metadata-entries from the given metadata field and puts them into this instance of {@code CdmiMetadata}
	 * <p/>
	 * By this, all entries with the same key are overwritten in this instance.
	 * Does nothing, if {@code metadata} is {@code null}
	 *
	 * @param metadata another metadata instance, that shall be merged with this instance
	 */
	public void mergeWith(CdmiMetadata metadata) {
		mergeWith(metadata, Collections.EMPTY_LIST);
	}

	/**
	 * Takes metadata-entries from the given metadata field and puts them into this instance of {@code CdmiMetadata}, except the given keys
	 * <p/>
	 * By this, all entries with the same key are overwritten in this instance.
	 * Does nothing, if {@code metadata} is {@code null}
	 *
	 * @param metadata another metadata instance, that shall be merged with this instance
	 * @param except   excludes this keys from merge
	 */
	public void mergeWith(CdmiMetadata metadata, List<String> except) {
		assert except != null;
		cacheSize = false;
		if (metadata == null)
			return;
		for (String key : metadata.getKeys())
			if (!except.contains(key))
				this.metadata.put(key, metadata.get(key));
	}

	protected List<String> getKeys() {
		return new ArrayList<>(metadata.keySet());
	}

	@Override
	public String toString() {
		return toString(true);
	}

	public String toString(boolean flat) {
		String newLine = "\n";
		String indent = "  ";
		if (flat) {
			newLine = "";
			indent = " ";
		}

		StringBuilder b = new StringBuilder("{").append(newLine);
		for (String key : metadata.keySet()) {
			b.append(indent).append(key).append(": ");
			if (key == CREATED_AT || key == LAST_WRITE_ACCESS)
				b.append(new Date(Long.valueOf(metadata.get(key))).toString());
			else
				b.append(metadata.get(key));
			b.append(",").append(newLine);
		}
		b.deleteCharAt(b.length() - newLine.length() - 1);
		b.append("}");
		return b.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass())
			return false;
		return obj.toString().equals(toString());
	}
}
