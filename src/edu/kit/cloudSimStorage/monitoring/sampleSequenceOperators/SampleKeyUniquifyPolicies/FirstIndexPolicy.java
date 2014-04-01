package edu.kit.cloudSimStorage.monitoring.sampleSequenceOperators.SampleKeyUniquifyPolicies;

import edu.kit.cloudSimStorage.monitoring.Tuple;

/**
 * @author Tobias Sturm, 4/1/14 12:01 PM
 */
public class FirstIndexPolicy<T> extends UniquifyPolicy<T>
{
	@Override
	protected Tuple<Long, T> choose() {
		return elements.get(0);
	}
}
