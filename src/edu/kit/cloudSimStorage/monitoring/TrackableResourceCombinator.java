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
import java.util.Arrays;
import java.util.List;

/** @author Tobias Sturm, 9/10/13 9:41 PM */
public class TrackableResourceCombinator implements TraceableResource {
	List<TraceableResource> children = new ArrayList<>();

	public void addResource(TraceableResource r) {
		for(String k : r.getAvailableTrackingKeys())
			if(contains(k, getAvailableTrackingKeys()))
				throw new IllegalStateException("Can't combine two trackable resources that offer the same key");
		children.add(r);
	}

	private boolean contains(String term, String[] a) {
		return Arrays.binarySearch(a, term) != -1;
	}

	@Override
	public TupleSequence<Double> getSamples(String key) {
		for(TraceableResource r : children) {
			if(contains(key, r.getAvailableTrackingKeys()))
				return r.getSamples(key);
		}
		return null;
	}

	@Override
	public String[] getAvailableTrackingKeys() {
		List<String> keys = new ArrayList<>();
		for(TraceableResource r : children) {
			keys.addAll(Arrays.asList(r.getAvailableTrackingKeys()));
		}
		return keys.toArray(new String[0]);
	}
}
