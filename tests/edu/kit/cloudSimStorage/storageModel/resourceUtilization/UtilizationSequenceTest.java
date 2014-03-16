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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** @author Tobias Sturm, 6/22/13 4:05 PM */
public class UtilizationSequenceTest {
	private static final double DELTA = 0.00001;

	/*
	sample |    10      20      30      40      50      60
	  a         -----(1)---------
	  b     -(2)-
	  c                 ---(4)---
	  d                                 ---(1)---
	 */

	UtilizationSequence s;
	UtilizationSequenceSample sampleA, sampleB, sampleC, sampleD;

	@Before
	public void setUp() throws Exception {
		s = new UtilizationSequence();

		sampleA = new UtilizationSequenceSample(10, 30, 1);
		sampleB = new UtilizationSequenceSample(0, 10, 2);
		sampleC = new UtilizationSequenceSample(20, 30, 4);
		sampleD = new UtilizationSequenceSample(40, 50, 1);
		s.insertSample(sampleA);
		s.insertSample(sampleB);
		s.insertSample(sampleC);
		s.insertSample(sampleD);
		
	}


	@Test
	public void testGetValuesAt() throws Exception {
		assertEquals(2, s.getValuesAt(0), DELTA);
		assertEquals(3, s.getValuesAt(10), DELTA);
		assertEquals(1, s.getValuesAt(15), DELTA);
		assertEquals(1, s.getValuesAt(11), DELTA);
		assertEquals(5, s.getValuesAt(20), DELTA);
		assertEquals(5, s.getValuesAt(30), DELTA);
		assertEquals(0, s.getValuesAt(31), DELTA);
		assertEquals(1, s.getValuesAt(40), DELTA);
		assertEquals(0, s.getValuesAt(51), DELTA);
	}


		/*
	sample |    10      20      30      40      50      60
	  a         -----(1)---------
	  b     -(2)-
	  c                 ---(4)---
	  d                                 ---(1)---
	 */

	@Test
	public void testGetNextSamplePointFrom() throws Exception {
		assertEquals(10, s.getNextSamplePointFrom(0));
		assertEquals(10, s.getNextSamplePointFrom(4));

		assertEquals(20, s.getNextSamplePointFrom(10));
		assertEquals(20, s.getNextSamplePointFrom(11));

		assertEquals(30, s.getNextSamplePointFrom(20));
		assertEquals(40, s.getNextSamplePointFrom(30));
		assertEquals(50, s.getNextSamplePointFrom(43));

		assertEquals(-1, s.getNextSamplePointFrom(50));
	}

	@Test
	public void testHasSamplePointBeyond() throws Exception {
		assertTrue(s.hasSamplePointBeyond(42));
		assertFalse(s.hasSamplePointBeyond(50));
	}

	@Test
	public void testGetNumSamples() throws Exception {
	 	assertEquals(4, s.getNumSamples());
	}

	@Test
	public void testGetDuration() throws Exception {
	 	assertEquals(50, s.getDuration());
	}
}
