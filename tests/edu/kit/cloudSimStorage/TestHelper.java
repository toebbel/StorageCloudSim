/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

/** @author Tobias Sturm, 6/7/13 5:05 PM */
public class TestHelper {
	public static void assertSameItemsInSet(List expected, List actual) {
		if(actual == null && expected != null)
			junit.framework.Assert.fail("expected list is NULL - did not expect that :-?");

		List missing = new ArrayList(), tooMuch = new ArrayList();
		for(Object it : expected)
			if (!actual.contains(it))
				missing.add(it);

		for(Object it : actual)
			if(!expected.contains(it))
				tooMuch.add(it);

		String error = "";
		if(!tooMuch.isEmpty()) {
			error += "the following elements are too much: ";
			for(Object it : tooMuch)
				error += "'" + it + "' ";
		}

		if(!missing.isEmpty()) {
			error += "the following elements are missing: ";
			for(Object it : missing)
				error += "'" + it + "' ";
		}

		if(!error.isEmpty())
			fail(error);
	}
}
