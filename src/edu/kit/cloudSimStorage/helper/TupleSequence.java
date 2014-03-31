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

	public static <T> List<TupleSequence<T>> align(List<TupleSequence<T>> inputs, T nullValue) {
		if(inputs.size() < 2)
			return inputs;

		//instead of aligning every list with every other (n^2) it is sufficient to align all lists with
		//one partner list (where the partner is the same for all other lists). When we then align all lists
		// a second time with that partner, we achieved the same as we would with the n^2 solution, but we need only 2*n
		for(int n = 0; n < 2; n++)
		{
			for(int i = 1; i < inputs.size(); i++)
			{
				List<TupleSequence<T>> two_aligned = align(inputs.get(0), inputs.get(i), nullValue);
				inputs.set(i, two_aligned.get(1));
				inputs.set(0, two_aligned.get(0));
			}
		}
		return inputs;
	}

	public static <T> List<TupleSequence<T>> align(TupleSequence<T> a, TupleSequence<T> b, T nullValue) {
		assert isSorted(a) && isSorted(b);

		TupleSequence<T> resultA = new TupleSequence<>();
		TupleSequence<T> resultB = new TupleSequence<>();

		int indexA = 0, indexB = 0;
		if (!(a.size() == 0 && b.size() == 0)) {
			if (a.size() == 0)
				a.add(0, new Tuple<>(b.get(0).x, nullValue));
			else if (b.size() == 0)
				b.add(0, new Tuple<>(a.get(0).x, nullValue));

			while (indexA < a.size() || indexB < b.size()) {
				if (indexA == a.size()) {
					for (; indexB < b.size(); indexB++) {
						resultA.add(b.get(indexB).x, a.get(indexA - 1).y);
						resultB.add(b.get(indexB));
					}
				} else if (indexB == b.size()) {
					for (; indexA < a.size(); indexA++) {
						resultB.add(a.get(indexA).x, b.get(indexB - 1).y);
						resultA.add(a.get(indexA));
					}
				} else if (a.get(indexA).x < b.get(indexB).x) {
					resultB.add(a.get(indexA).x, getClosestToIndex(resultB, resultB.size() - 1, nullValue));
					resultA.add(a.get(indexA));
					indexA++;
				} else if (a.get(indexA).x > b.get(indexB).x) {
					resultA.add(b.get(indexB).x, getClosestToIndex(resultA, resultA.size() - 1, nullValue));
					resultB.add(b.get(indexB));
					indexB++;
				} else {
					resultA.add(a.get(indexA));
					resultB.add(b.get(indexB));
					indexA++;
					indexB++;
				}
			}
		}

		assert isSorted(a) && isSorted(b);

		List<TupleSequence<T>> result = new ArrayList<>();
		result.add(resultA);
		result.add(resultB);
		return result;
	}

	private static <T> T getClosestToIndex(TupleSequence<T> sequence, int index, T nullValue) {
		if (index < sequence.size() && index >= 0)
			return sequence.get(index).y;
		else if (index < 0)
			return nullValue;
		else
			return sequence.get(sequence.size() - 1).y;
	}

	private static <T> boolean isSorted(TupleSequence<T> seq) {
		for (int i = 1; i < seq.size() - 1; i++) {
			if (seq.get(i).x < seq.get(i - 1).x)
				return false;
		}
		return true;
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
