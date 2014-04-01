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
import edu.kit.cloudSimStorage.monitoring.sampleSequenceOperatorations.SampleValueOperations.Division;
import edu.kit.cloudSimStorage.monitoring.sampleSequenceOperatorations.SampleValueOperations.Min;
import edu.kit.cloudSimStorage.monitoring.sampleSequenceOperatorations.SampleValueOperations.SequenceValueOperation;
import edu.kit.cloudSimStorage.monitoring.sampleSequenceOperatorations.SampleValueOperations.Sum;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/*
WARNING! This class has NOT been tested and may contain bugs
 */
/** @author Tobias Sturm, 6/24/13 4:40 PM */
public class SequenceOperations {

	/**
	 * Calculates the sum of each group of {@link edu.kit.cloudSimStorage.monitoring.Tuple}s that have the same X value in multiple {@link edu.kit.cloudSimStorage.monitoring.TupleSequence}s.
	 *
	 * If a sequence contains multiple samples with the same X value, the sum of these values will be taken into account.
	 *
	 * @param inputs
	 * @return one sequence of tuples where each tuple is the sum  of all input sequences for it's index
	 */
	public static TupleSequence<Double> sum(List<TupleSequence<Double>> inputs) {
		for(int i = 0; i < inputs.size(); i++)
			inputs.set(i, UniquifyPolicy.uniquifyIndex(inputs.get(i), ValueSumPolicy.class));
		return fold(TupleSequence.align(inputs, 0.0), new Sum());
	}

	/**
	 * Selects the min of each group of {@link edu.kit.cloudSimStorage.monitoring.Tuple}s that have the same X value in multiple {@link edu.kit.cloudSimStorage.monitoring.TupleSequence}s.
	 *
	 * If a sequence contains multiple samples with the minimum of these will be taken into account
	 *
	 * @param inputs set of input sequences
	 * @return one sequence of tuples where each tuple is the minimum of all input sequences for it's index
	 */
	public static TupleSequence<Double> min(List<TupleSequence<Double>> inputs) {
		for(int i = 0; i < inputs.size(); i++)
			inputs.set(i, uniquifyIndex_takeMinValue(inputs.get(i)));
		return fold(TupleSequence.align(inputs, 0.0), new Min());
	}

	/**
	 * Divides one tuple sequence from another.
	 *
	 * If a sequence contains multiple samples for one X value, the last will be chosen.
	 *
	 * @param dividend the dividend
	 * @param divisor the divisor
	 * @return tuple-wise division of the two inputs
	 */
	public static TupleSequence<Double> divide(TupleSequence<Double> dividend, TupleSequence<Double> divisor) {
		List<TupleSequence<Double>> list = new ArrayList<>();

		list.add(uniquifyIndex_takeLast(dividend));
		list.add(uniquifyIndex_takeLast(divisor));

		return fold(TupleSequence.align(list, 1.0), new Division());
	}

	public static TupleSequence<Double> fold(List<TupleSequence<Double>> inputs, SequenceValueOperation combinator) {
		if (inputs == null || inputs.isEmpty())
			return new TupleSequence<>();

		//check if sequences have same lengths
		for(TupleSequence<Double> seq : inputs)
		{
			if (seq.size() != inputs.get(0).size())
			{
				throw new IllegalStateException("can't fold tuple sequences with different length. Use align before!");
			}
		}

		//combine every tuple sample with partners from each stream
		TupleSequence<Double> result = new TupleSequence<>();
		for(int tupleIndex = 0; tupleIndex < inputs.get(0).size(); tupleIndex++)
		{
			long sampleXValue = inputs.get(0).get(tupleIndex).x;
			for(TupleSequence<Double> seq : inputs)
			{
				assert sampleXValue == seq.get(tupleIndex).x : "Samples in sequences have different X values and are therefore not aligned!";
				combinator.addSample(seq.get(tupleIndex).y);
			}
			result.add(sampleXValue, combinator.getResult());
			combinator.reset();
		}
		return result;
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
		 * @return list of samples, each in given distance, contains the number of events that occurred inside that time slot
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

