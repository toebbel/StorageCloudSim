package edu.kit.cloudSimStorage.monitoring.sampleSequenceOperatorations.SampleKeyUniquifyPolicies;

import edu.kit.cloudSimStorage.monitoring.Tuple;

/**
 * @author Tobias Sturm, 4/1/14 12:02 PM
 */
public class ValueSumPolicy<T extends Double> extends UniquifyPolicy<T>
{
	@Override
	protected Tuple<Long, T> choose() {
		Double result = 0.0;
		for(Tuple<Long, T> t : elements)
		{
			result += (Double)t.y;
		}
		return new Tuple<>(elements.get(0).x, (T)result);
	}
}
