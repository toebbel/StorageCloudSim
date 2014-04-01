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

import java.util.HashMap;

/**
 * This class can be used if one {@link TraceableResource} delegates different offered external tracking keys to multiple children-resources, that deal with different (internal) keys.
 * @author Tobias Sturm, 9/10/13 9:54 PM */
public class TraceableResourceAliasing implements TraceableResource {
	private HashMap<String, TraceableResource> externalKeyResourceMapping = new HashMap<>();
	private HashMap<String, String> externalKeyInternalKeyMapping = new HashMap<>();


	/**
	 * Adds a mapping.
	 * @param externalKey the key that will be visible to callers of this class
	 * @param internalKey the key that is offered by the child resource (r)
	 * @param r the child resource
	 */
	public void addMapping(String externalKey, String internalKey, TraceableResource r) {
		if(externalKeyResourceMapping.containsKey(externalKey) || externalKeyInternalKeyMapping.containsKey(externalKey))
			throw new IllegalStateException("External key already registered");
		externalKeyResourceMapping.put(externalKey, r);
		externalKeyInternalKeyMapping.put(externalKey, internalKey);
	}

	@Override
	public TupleSequence<Double> getSamples(String key) {
		if(externalKeyResourceMapping.containsKey(key) && externalKeyInternalKeyMapping.containsKey(key))
			externalKeyResourceMapping.get(key).getSamples(externalKeyInternalKeyMapping.get(key));
		return null;
	}

	@Override
	public String[] getAvailableTrackingKeys() {
		return externalKeyResourceMapping.keySet().toArray(new String[0]);
	}
}
