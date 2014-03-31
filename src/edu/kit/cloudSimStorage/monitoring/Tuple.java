/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.monitoring;

/** @author Tobias Sturm, 6/23/13 6:20 PM */
public class Tuple<X extends Comparable, Y> implements Comparable<Tuple<? extends Comparable, ?>> {
	public final X x;
	public final Y y;

	public Tuple(X x, Y y) {
		this.x = x;
		this.y = y;
	}

	public Tuple(X x, Class<Y> yType) throws InstantiationException, IllegalAccessException {
		this.x = x;
		this.y = yType.newInstance();
	}

	@Override
	public int compareTo(Tuple<? extends Comparable, ?> o) {
		return x.compareTo(o.x);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Tuple && ((Tuple) obj).compareTo(this) == 0 && ((Tuple) obj).y.equals(y);
	}

	@Override
	public String toString() {
		return "(" + x + "|" + y + ")";
	}
}
