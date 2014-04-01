package edu.kit.cloudSimStorage.monitoring.sampleSequenceOperators.SampleKeyUniquifyPolicies;

import edu.kit.cloudSimStorage.monitoring.Tuple;

/**
 * @author Tobias Sturm, 4/1/14 12:02 PM
 */
public class LastIndexPolicy<T> extends UniquifyPolicy<T>
{
	@Override
	protected Tuple<Long, T> choose() {
		return elements.get(elements.size() - 1);
	}
}
