/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.storageModel.resourceUtilization;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/** @author Tobias Sturm, 6/22/13 3:49 PM */
public class TestUtilizationSequenceSample {
	private static final double DELTA = 0.00001;
	/*
		s1 is from 0 to 100, value 10
		s2 is from 10 to 80 value 2
		s3 is from 110 to 110 value 1
		 */
	UtilizationSequenceSample s1, s2, s3;

	@Before
	public void setUp() throws Exception {
		s1 = new UtilizationSequenceSample(0, 100, 10);
		s2 = new UtilizationSequenceSample(10, 80, 2);
		s3 = new UtilizationSequenceSample(110, 110, 1);

	}

	@Test
	public void testGetDuration() throws Exception {
		assertEquals(100, s1.getDuration());
		assertEquals(70, s2.getDuration());
		assertEquals(0, s3.getDuration());
	}

	@Test
	public void testAddValue() throws Exception {
		s1.addValue(-1);
		assertEquals(9, s1.getValue(), DELTA);

		s3.addValue(-2);
		assertEquals(-1, s3.getValue(), DELTA);
	}

	@Test
	public void testSplitAt() throws Exception {
		UtilizationSequenceSample[] seq1 = s1.splitAt(50);

		assertEquals(2, seq1.length);
		assertEquals(100, seq1[0].getDuration() + seq1[1].getDuration());
		assertEquals(50, seq1[0].getEndTimestamp());
		assertEquals(50, seq1[1].getBeginTimestamp());
		assertEquals(0, seq1[0].getBeginTimestamp());
		assertEquals(100, seq1[1].getEndTimestamp());
		assertEquals(s1.getValue(), seq1[0].getValue(), DELTA);
		assertEquals(s1.getValue(), seq1[1].getValue(), DELTA);
	}

	@Test
	public void testCompareTo() throws Exception {
		//independent of value
		assertTrue(s1.compareTo(new UtilizationSequenceSample(0, 100, 5)) == 0);

		assertTrue(s1.compareTo(s2) < 0);
		assertTrue(s2.compareTo(s1) > 0);

		assertTrue(s1.compareTo(s3) < 0);
		assertTrue(s3.compareTo(s1) > 0);

		assertTrue(s2.compareTo(s3) < 0);
		assertTrue(s3.compareTo(s2) > 0);

		UtilizationSequenceSample s4 = new UtilizationSequenceSample(10, 70, 3);
		assertTrue(s4.compareTo(s2) < 0);

		UtilizationSequenceSample s5 = new UtilizationSequenceSample(10, 120, 3);
		assertTrue(s5.compareTo(s2) > 0);
	}

	@Test
	public void testDuring() throws Exception {
		assertTrue(s1.during(0));
		assertTrue(s1.during(40));
		assertTrue(s1.during(100));
		assertFalse(s1.during(101));
	}
}
