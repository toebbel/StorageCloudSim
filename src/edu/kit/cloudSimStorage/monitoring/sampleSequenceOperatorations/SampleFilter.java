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

import edu.kit.cloudSimStorage.cdmi.CdmiOperationVerbs;
import edu.kit.cloudSimStorage.cloudOperations.request.CloudRequest;
import edu.kit.cloudSimStorage.monitoring.Tuple;
import edu.kit.cloudSimStorage.monitoring.TupleSequence;

/** @author Tobias Sturm, 6/30/13 4:08 PM */
public class SampleFilter<T>{

	private ISampleFilterPredicate predicate;

	public SampleFilter(ISampleFilterPredicate predicate) {
		this.predicate = predicate;
	}

	public TupleSequence<T> apply(TupleSequence<T> input) {
		TupleSequence<T> result = new TupleSequence<>();
		for(Tuple<Long, T> t : input)
			if(predicate.match(t))
				result.add(t);
		return result;
	}

	public static SampleFilter<CloudRequest> cdmiVerbFilter(final CdmiOperationVerbs verb) {
		return new SampleFilter<>(new ISampleFilterPredicate<Tuple<Long, CloudRequest>>() {
			@Override
			public boolean match(Tuple<Long, CloudRequest> t) {
				return t.y.getVerb() == verb;
			}
		});
	}
}


