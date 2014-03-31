/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/** @author Tobias Sturm, 8/30/13 5:28 PM */
public class TupleSequence<T> extends ArrayList<Tuple<Long, T>> {

	public TupleSequence() {
		super();
	}

	public TupleSequence(TupleSequence<T> original) {
		super(original);
	}

	public TupleSequence(Collection<Tuple<Long, T>> original) {
		super(original);
	}

	public void add(long x, T y)
	{
		super.add(new Tuple<>(x, y));
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();

		ListIterator it = listIterator();
		while(it.hasNext()) {
			b.append(it.next().toString());
			b.append("; ");
		}
		return b.toString();
	}
}
