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

import java.util.Collections;

/**
 * @author Tobias Sturm, 4/1/14 12:02 PM
 */
public class MaxValuePolicy<T extends Comparable<T>> extends UniquifyPolicy<T>
{
	@Override
	protected Tuple<Long, T> choose() {
		Collections.sort(elements, Tuple.<T>ValueComparator());
		return elements.get(elements.size());
	}
}
