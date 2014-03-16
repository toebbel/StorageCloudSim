/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.monitoring.sampleSequenceOperators;

import edu.kit.cloudSimStorage.helper.Tuple;
import edu.kit.cloudSimStorage.helper.TupleSequence;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/** @author Tobias Sturm, 9/4/13 6:54 PM */
public class TestSampleCombinator {
	TupleSequence<Double> a, b, c, empty;

	@Before
	public void setUp() throws Exception {
		empty = new TupleSequence<>();
		a = new TupleSequence<>();
		a.add(new Tuple<>(0l,0.0));
		a.add(new Tuple<>(0l,1.0));
		a.add(new Tuple<>(5l,3.0));

		b = new TupleSequence<>();
		b.add(new Tuple<>(1l,0.0));
		b.add(new Tuple<>(3l,1.0));
		b.add(new Tuple<>(4l,7.0));
		b.add(new Tuple<>(4l,3.0));
		b.add(new Tuple<>(5l,4.0));

		c = new TupleSequence<>();
		c.add(new Tuple<>(6l,0.0));
		c.add(new Tuple<>(6l,1.0));
		c.add(new Tuple<>(6l,3.0));
		c.add(new Tuple<>(6l,1.0));
		c.add(new Tuple<>(6l,3.0));
	}

	@Test
	public void testSum() throws Exception {
		TupleSequence<Double> expected = new TupleSequence<>();
		List<TupleSequence<Double>> in = new ArrayList<>();
		in.add(a);
		in.add(b);

		expected.add(new Tuple<>(0l, 1.0));
		expected.add(new Tuple<>(1l, 1.0));
		expected.add(new Tuple<>(3l, 2.0));
		expected.add(new Tuple<>(3l, 8.0));
		expected.add(new Tuple<>(5l, 7.0));
		expected.add(new Tuple<>(6l, 10.0));
		assertEquals(expected, SampleCombinator.sum(in));
	}

	@Test
	public void testMin() throws Exception {

	}

	@Test
	public void testAvg() throws Exception {

	}

	@Test
	public void testDivide() throws Exception {

	}

	@Test
	public void testGetTotalNumOfEvents() throws Exception {

	}

	@Test
	public void testSamplesPerTime() throws Exception {

	}

	@Test
	public void testFlatten() throws Exception {

	}

	@Test
	public void testAlign() throws Exception {

	}

	@Test
	public void testUniquifyTimestamps_takeMin() {
		TupleSequence<Double> expectedA = new TupleSequence<>();
		expectedA.add(new Tuple<>(0l, 0.0));
		expectedA.add(new Tuple<>(5l, 3.0));

		TupleSequence<Double> expectedB = new TupleSequence<>();
		expectedB.add(new Tuple<>(1l,0.0));
		expectedB.add(new Tuple<>(3l,1.0));
		expectedB.add(new Tuple<>(4l,3.0));
		expectedB.add(new Tuple<>(5l,4.0));

		TupleSequence<Double> expectedC = new TupleSequence<>();
		expectedC.add(new Tuple<>(6l,0.0));

		assertEquals(expectedA, SampleCombinator.uniquifyTimestamps_takeMinValue(a));
		assertEquals(expectedB, SampleCombinator.uniquifyTimestamps_takeMinValue(b));
		assertEquals(expectedC, SampleCombinator.uniquifyTimestamps_takeMinValue(c));
	}

	@Test
	public void testUniquifyTimestamps_takeFirst() {
		TupleSequence<Double> expectedA = new TupleSequence<>();
		expectedA.add(new Tuple<>(0l, 0.0));
		expectedA.add(new Tuple<>(5l, 3.0));

		TupleSequence<Double> expectedB = new TupleSequence<>();
		expectedB.add(new Tuple<>(1l,0.0));
		expectedB.add(new Tuple<>(3l,1.0));
		expectedB.add(new Tuple<>(4l,7.0));
		expectedB.add(new Tuple<>(5l,4.0));

		TupleSequence<Double> expectedC = new TupleSequence<>();
		expectedC.add(new Tuple<>(6l,0.0));

		assertEquals(expectedA, SampleCombinator.uniquifyTimestamps_takeFirst(a));
		assertEquals(expectedB, SampleCombinator.uniquifyTimestamps_takeFirst(b));
		assertEquals(expectedC, SampleCombinator.uniquifyTimestamps_takeFirst(c));
	}

	@Test
	public void testUniquifyTimestamps_takeMax() {
		TupleSequence<Double> expectedA = new TupleSequence<>();
		expectedA.add(new Tuple<>(0l,1.0));
		expectedA.add(new Tuple<>(5l,3.0));

		TupleSequence<Double> expectedB = new TupleSequence<>();
		expectedB.add(new Tuple<>(1l,0.0));
		expectedB.add(new Tuple<>(3l,1.0));
		expectedB.add(new Tuple<>(4l,7.0));
		expectedB.add(new Tuple<>(5l,4.0));

		TupleSequence<Double> expectedC = new TupleSequence<>();
		expectedC.add(new Tuple<>(6l,3.0));

		assertEquals(expectedA, SampleCombinator.uniquifyTimestamps_takeMaxValue(a));
		assertEquals(expectedB, SampleCombinator.uniquifyTimestamps_takeMaxValue(b));
		assertEquals(expectedC, SampleCombinator.uniquifyTimestamps_takeMaxValue(c));
	}

	@Test
	public void testUniquifyTimestamps_takeLast() {
		TupleSequence<Double> expectedA = new TupleSequence<>();
		expectedA.add(new Tuple<>(0l,1.0));
		expectedA.add(new Tuple<>(5l,3.0));

		TupleSequence<Double> expectedB = new TupleSequence<>();
		expectedB.add(new Tuple<>(1l,0.0));
		expectedB.add(new Tuple<>(3l,1.0));
		expectedB.add(new Tuple<>(4l,3.0));
		expectedB.add(new Tuple<>(5l,4.0));

		TupleSequence<Double> expectedC = new TupleSequence<>();
		expectedC.add(new Tuple<>(6l,3.0));

		assertEquals(expectedA, SampleCombinator.uniquifyTimestamps_takeLast(a));
		assertEquals(expectedB, SampleCombinator.uniquifyTimestamps_takeLast(b));
		assertEquals(expectedC, SampleCombinator.uniquifyTimestamps_takeLast(c));
	}
}
