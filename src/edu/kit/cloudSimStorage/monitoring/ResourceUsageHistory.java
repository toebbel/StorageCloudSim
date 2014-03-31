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


import edu.kit.cloudSimStorage.helper.TimeHelper;

import java.util.Collections;

/** @author Tobias Sturm, 6/23/13 5:43 PM */
public class ResourceUsageHistory implements TrackableResource {
	private String trackingKey;
	public final String yAxis;
	public final String units;
	public final String name;

	double last = 0;
	TupleSequence<Double> samples;

	public ResourceUsageHistory(String trackingKey, String name, String yAxis, String units) {
		this.trackingKey = trackingKey;
		this.yAxis = yAxis;
		this.units = units;
		this.name = name;

		samples = new TupleSequence<>();
		Monitoring.register(this);
	}

	public void addSample(long timestamp, double val) {
		samples.add(new Tuple<>(timestamp, val));
		last = val;
	}

	public void addDiff(long timestamp, double diff) {
		addSample(timestamp, last + diff);
	}

	@Override
	public TupleSequence<Double> getSamples(String key) {
		if (key == trackingKey)
			return getSamples();
		return null;
	}

	public TupleSequence<Double> getSamples() {
		Collections.sort(samples);
		return samples;
	}

	@Override
	public String[] getAvailableTrackingKeys() {
		return new String[]{trackingKey};
	}

	public void addDiff(double diff) {
		addDiff(TimeHelper.getInstance().now(), diff);
	}


	public String getTrackingKey() {
		return trackingKey;
	}

	public void setTrackingKey(String trackingKey) {
		this.trackingKey = trackingKey;
	}
}

