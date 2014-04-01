/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */

package edu.kit.cloudSimStorage.monitoring.sampleSequenceOperatorations.SampleKeyUniquifyPolicies;

import edu.kit.cloudSimStorage.monitoring.Tuple;
import edu.kit.cloudSimStorage.monitoring.TupleSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tobias Sturm, 4/1/14 12:01 PM
 */
public abstract class UniquifyPolicy<T>
{
	List<Tuple<Long, T>> elements = new ArrayList<>();

	protected abstract Tuple<Long, T> choose();

	public boolean hasElements()
	{
		return elements.size() != 0;
	}

	public void add(Tuple<Long, T> t)
	{
		elements.add(t);
	}

	public Tuple<Long, T> get()
	{
		if(elements.size() == 0)
			throw new IllegalStateException("Can't choose Element from bucket when bucket is empty");
		return choose();
	}

	public static <T> TupleSequence<T> uniquifyIndex(TupleSequence<T> sampleStream, Class<? extends UniquifyPolicy> uniquifyPolicy) {

		if(sampleStream == null || sampleStream.size() == 0)
			return sampleStream;

		try {
			UniquifyPolicy bucket = uniquifyPolicy.newInstance();
			long lastIndex = sampleStream.get(0).x;
			TupleSequence<T> result = new TupleSequence<>();

			for(int i = 0; i < sampleStream.size(); i++)
			{
				if(lastIndex != sampleStream.get(i).x && bucket.hasElements())
				{
					result.add(bucket.get());
					bucket = uniquifyPolicy.newInstance();
					lastIndex = sampleStream.get(i).x;
				}
				bucket.add(sampleStream.get(i));
			}
			if(bucket.hasElements())
				result.add(bucket.get());

			return result;

		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("Could not instantiate key uniquify policy");
		}
	}
}
