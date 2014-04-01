/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.monitoring.sampleSequenceOperatorations;

import edu.kit.cloudSimStorage.monitoring.Tuple;
import edu.kit.cloudSimStorage.monitoring.TupleSequence;
import edu.kit.cloudSimStorage.monitoring.sampleSequenceOperatorations.SampleKeyUniquifyPolicies.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/*
WARNING! This class has NOT been tested and may contain bugs
 */
/** @author Tobias Sturm, 6/24/13 4:40 PM */
public abstract class SampleCombinator {

	public static TupleSequence<Double> sum(List<TupleSequence<Double>> inputs) {
		return fold(inputs, new Sum());
	}

	public static TupleSequence<Double> fold(List<TupleSequence<Double>> inputs, SampleCombinator combinator) {
		if (inputs == null || inputs.isEmpty())
			return new TupleSequence<>();

		//append a sample at the end of every input. Determine latest timestamp, append last value of each list again with that timestamp.
		long lastTimestamp = Long.MIN_VALUE;
		long firstTimestamp = Long.MAX_VALUE;
		for (TupleSequence<Double> l : inputs) {
			Collections.sort(l);
			if (!l.isEmpty()) {
				lastTimestamp = Math.max(l.get(l.size() - 1).x, lastTimestamp);
				firstTimestamp = Math.min(l.get(0).x, firstTimestamp);
			}
		}

		for (TupleSequence<Double> l : inputs) {
			l.add(0, new Tuple<>(firstTimestamp, combinator.getNeutralValue()));
			l.add(new Tuple<>(lastTimestamp, l.get(l.size() - 1).y));
			combinator.prepareStream(l);
		}

		TupleSequence<Double> result = new TupleSequence<>();
		int[] is = new int[inputs.size()];

		while (hasItemsLeft(inputs, is)) {
			int smallestTimestampIndex = 0;
			long smallestTimestamp = Long.MAX_VALUE;
			for (int i = 0; i < inputs.size(); i++) {
				if (inputs.get(i).get(is[i]).x < smallestTimestamp) {
					smallestTimestamp = inputs.get(i).get(is[i]).x;
					smallestTimestampIndex = i;
				}
			}

			for (int i = 0; i < inputs.size(); i++) {
				combinator.addSample(inputs.get(i).get(is[i]).y);
			}
			is[smallestTimestampIndex] += 1;
			result.add(new Tuple<>(smallestTimestamp, combinator.getResult()));
			combinator.reset();
		}

		return result;
	}

	protected void prepareStream(TupleSequence<Double> input){
		SampleCombinator.uniquifyIndex_takeLast(input);
	}

	protected abstract double getNeutralValue();

	protected abstract double getResult();

	protected abstract void addSample(double val);

	private static boolean hasItemsLeft(List<TupleSequence<Double>> inputs, int[] is) {
		boolean hasItemsLeft = true;

		for (int i = 0; i < is.length; i++) {
			if (is[i] >= inputs.get(i).size())
				hasItemsLeft = false;
		}
		return hasItemsLeft;
	}

	protected abstract void reset();

	public static TupleSequence<Double> min(List<TupleSequence<Double>> inputs) {
		return fold(inputs, new Min());
	}

	public static TupleSequence<Double> divide(TupleSequence<Double> dividend, TupleSequence<Double> divisor) {
		List<TupleSequence<Double>> list = new ArrayList<>();

		list.add(dividend);
		list.add(divisor);

		return fold(list, new Division());
	}

	public static TupleSequence<Double> getTotalNumOfEvents(TupleSequence<?> samples) {
		if(samples.isEmpty())
			return new TupleSequence<>();

		TupleSequence<Double> result = new TupleSequence<>();

		//Count all events inside one time bucket.
		double numEvents = 0;
		for(Tuple<Long, ?> event : samples) {
			numEvents++;
			result.add(new Tuple<>(event.x, numEvents));
		}

		return result;
	}

		/**
		 * Counts events per time and creates a list of samples from that data.
		 *
		 * @param timeDistance equidistant size of time buckets.
		 * @param samples the sample sequence
		 * @return list of samples, each in given distance, containt the number of events that occured inside that time slot
		 */
		public static TupleSequence<Double> samplesPerTime(long timeDistance, TupleSequence<Double> samples) {
			assert timeDistance > 0;
			if(samples.isEmpty())
				return new TupleSequence<>();

			TupleSequence<Double> result = new TupleSequence<>();

			//Count all events inside one time bucket.
			double currentTimeBucket = 0;
			long sweepLine = samples.get(0).x;
			for(Tuple<Long, ?> event : samples) {
				while(event.x - sweepLine > timeDistance) {
					result.add(new Tuple<>(sweepLine, currentTimeBucket));
					sweepLine += timeDistance;
					currentTimeBucket = 0;
				}
				currentTimeBucket++;
			}

			//add last bucket
			if(currentTimeBucket > 0) {
				result.add(new Tuple<>(sweepLine, currentTimeBucket));
			}

			return result;
		}

	/**
	 * Takes a collection of sample streams and combines them into one single sequence stream in the correct order
	 * @param sampleStream multiple sequence streams
	 * @return samples of all streams reordered into one single stream.
	 */
	public static <T> TupleSequence<T> flatten(List<TupleSequence<T>> sampleStream) {
		PriorityQueue<Tuple<Long, T>> q = new PriorityQueue<>();
		for(TupleSequence<T> l : sampleStream)
			q.addAll(l);
		return new TupleSequence<>(q);
	}


	/**
	 * Replaces adjacent entries of a stream, that have the same timestamps, with the first occuring entry
	 *
	 * The input stream must be sorted.
	 * Tuples should never carry the timestamp {@code Long.MIN_VALUE} othwerwise they will be removed
	 * @param sampleStream sample stream that may contain multiple entries with the same timestamp
	 * @param <T> sampled metric in tuples
	 * @return sampleStream without multiple entries with the same timestamp
	 */
	public static <T> TupleSequence<T> uniquifyIndex_takeFirst(TupleSequence<T> sampleStream) {
		return UniquifyPolicy.uniquifyIndex(sampleStream, FirstIndexPolicy.class);
	}

	/**
	 * See {@link #uniquifyIndex_takeFirst}, except that the last instance within a group of entries with the same timestamp will be taken.
	 * @param sampleStream See {@link #uniquifyIndex_takeFirst} ()}
	 * @param <T> See {@link #uniquifyIndex_takeFirst}
	 * @return See {@link #uniquifyIndex_takeFirst}
	 */
	public static <T> TupleSequence<T> uniquifyIndex_takeLast(TupleSequence<T> sampleStream) {
		return UniquifyPolicy.uniquifyIndex(sampleStream, LastIndexPolicy.class);
	}

	/**
	 * See {@link #uniquifyIndex_takeFirst}, except that the instance with the smallest value within a group of entries with the same timestamp will be taken.
	 * @param sampleStream See {@link #uniquifyIndex_takeFirst}
	 * @param <T> See {@link #uniquifyIndex_takeFirst}
	 * @return See {@link #uniquifyIndex_takeFirst}
	 */
	public static <T extends Comparable<T>> TupleSequence<T> uniquifyIndex_takeMinValue(TupleSequence<T> sampleStream) {
		return UniquifyPolicy.uniquifyIndex(sampleStream, MinValuePolicy.class);
	}


	/**
	 * See {@link #uniquifyIndex_takeFirst}, except that the instance with the biggest value within a group of entries with the same timestamp will be taken.
	 * @param sampleStream See {@link #uniquifyIndex_takeFirst}
	 * @param <T> See {@link #uniquifyIndex_takeFirst}
	 * @return See {@link #uniquifyIndex_takeFirst}
	 */
	public static <T extends Comparable<T>> TupleSequence<T> uniquifyIndex_takeMaxValue(TupleSequence<T> sampleStream) {
		return UniquifyPolicy.uniquifyIndex(sampleStream, MaxValuePolicy.class);
	}
}

