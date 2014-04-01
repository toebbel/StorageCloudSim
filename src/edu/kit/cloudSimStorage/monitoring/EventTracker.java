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
import edu.kit.cloudSimStorage.monitoring.sampleSequenceOperatorations.SequenceOperations;
import edu.kit.cloudSimStorage.monitoring.sampleSequenceOperatorations.SampleFilter;

import java.util.*;


/** @author Tobias Sturm, 6/23/13 6:19 PM */
public class EventTracker<T> implements TraceableResource {
	protected TupleSequence<T> events;

	public final String eventNames;
	public final String trackerName;

	public EventTracker(String eventNames, String trackerName) {
		this.eventNames = eventNames;
		this.trackerName = trackerName;
		events = new TupleSequence<>();
	}

	public void addEvent(T e) {
		addEvent(TimeHelper.getInstance().now(), e);
	}

	public void addEvent(long timestamp, T e) {
		events.add(new Tuple<>(timestamp, e));
	}


	public TupleSequence<Double> getEventsPerTime(long timeDistance) {
		return SequenceOperations.samplesPerTime(timeDistance, forgetType(events));
	}

	public TupleSequence<Double> forgetType(TupleSequence<T> in) {
		TupleSequence<Double> result = new TupleSequence<>();
		for(Tuple<Long, T> sample : in) {
			result.add(new Tuple<>(sample.x, 1.0));
		}
		return result;
	}

	public TupleSequence<Double> getTotalNumOfEvents() {
		return SequenceOperations.getTotalNumOfEvents(events);
	}

	public TupleSequence<Double> getSamples() {
		return getSamples(NUM_EVENTS_TOTAL);
	}

	@Override
	public TupleSequence<Double> getSamples(String key) {
		Collections.sort(events);
		switch (key) {
			case TraceableResource.NUM_EVENTS_TOTAL:
				return getTotalNumOfEvents();
			case TraceableResource.NUM_EVENTS_PER_MINUTE:
				return getEventsPerTime(60 * 1000);
			case TraceableResource.NUM_EVENTS_PER_SECOND:
				return getEventsPerTime(1000);
			default:
				return new TupleSequence<>();
		}
	}

	@Override
	public String[] getAvailableTrackingKeys() {
		return new String[] {NUM_EVENTS_TOTAL, NUM_EVENTS_PER_MINUTE, NUM_EVENTS_PER_SECOND};
	}

	public List<T> getTraces() {
		return stripTimestamps(events);
	}

	private List<T> stripTimestamps(TupleSequence<T> in) {
		List<T> result = new ArrayList<>();
		for(Tuple<Long, T> t : in)
			result.add(t.y);
		return result;
	}

	public List<T> getTracesWhere(SampleFilter<T> filter) {
		return stripTimestamps(filter.apply(events));
	}
}
