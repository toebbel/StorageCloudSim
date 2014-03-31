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

import edu.kit.cloudSimStorage.helper.TupleSequence;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/** @author Tobias Sturm, 3/31/14 2:01 PM */
public class TupleSequenceOperationsTest {

	@Test
	public void testAlignTwoEmptyLists()
	{
		TupleSequence<Double>a = new TupleSequence<>();
		TupleSequence<Double>b = new TupleSequence<>();

		TupleSequence.align(a, b, 0.0);
		assertEquals(0, a.size());
		assertEquals(0, b.size());
	}

	@Test
	public void testOneListEmpty()
	{
		TupleSequence<Double>a = new TupleSequence<>();
		a.add(1, 1.0);
		a.add(3, 1.1);
		a.add(4, 1.3);

		TupleSequence<Double>b = new TupleSequence<>();

		List<TupleSequence<Double>> result = TupleSequence.align(a, b, 0.0);
		a = result.get(0);
		b = result.get(1);

		assertEquals(3, a.size());
		assertEquals(3, b.size());

		assertEquals((Long)1l, b.get(0).x);
		assertEquals((Double)0.0, b.get(0).y);

		assertEquals((Long)3l, b.get(1).x);
		assertEquals((Double)0.0, b.get(1).y);

		assertEquals((Long)4l, b.get(2).x);
		assertEquals((Double)0.0, b.get(2).y);
	}

	@Test
	public void testListsHaveIdenticalXValues()
	{
		TupleSequence<Double>a = new TupleSequence<>();
		TupleSequence<Double>b = new TupleSequence<>();
		a.add(1, 1.0);
		a.add(3, 1.1);
		b.add(1, 2.0);
		b.add(3, 2.1);

		List<TupleSequence<Double>> result = TupleSequence.align(a, b, 0.0);
		a = result.get(0);
		b = result.get(1);

		assertEquals(2, a.size());
		assertEquals(2, b.size());

		assertEquals((Long)1l, b.get(0).x);
		assertEquals((Double)2.0, b.get(0).y);

		assertEquals((Long)3l, b.get(1).x);
		assertEquals((Double)2.1, b.get(1).y);

		assertEquals((Long)1l, a.get(0).x);
		assertEquals((Double)1.0, a.get(0).y);

		assertEquals((Long)3l, a.get(1).x);
		assertEquals((Double)1.1, a.get(1).y);
	}

	@Test
	public void testListsWithSomeSharedXValues()
	{
		TupleSequence<Double>a = new TupleSequence<>();
		TupleSequence<Double>b = new TupleSequence<>();
		a.add(1, 1.1);
		a.add(3, 1.3);
		a.add(5, 1.5);
		a.add(6, 1.6);
		b.add(1, 2.1);
		b.add(3, 2.3);
		b.add(4, 2.4);
		b.add(6, 2.6);

		List<TupleSequence<Double>> result = TupleSequence.align(a, b, 0.0);
		a = result.get(0);
		b = result.get(1);

		assertEquals(5, a.size());
		assertEquals(5, b.size());

		assertEquals((Long)1l, b.get(0).x);
		assertEquals((Long)3l, b.get(1).x);
		assertEquals((Long)4l, b.get(2).x);
		assertEquals((Long)5l, b.get(3).x);
		assertEquals((Long)6l, b.get(4).x);

		assertEquals((Long)1l, a.get(0).x);
		assertEquals((Long)3l, a.get(1).x);
		assertEquals((Long)4l, a.get(2).x);
		assertEquals((Long)5l, a.get(3).x);
		assertEquals((Long)6l, a.get(4).x);

		assertEquals((Double)2.1, b.get(0).y);
		assertEquals((Double)2.3, b.get(1).y);
		assertEquals((Double)2.4, b.get(2).y);
		assertEquals((Double)2.4, b.get(3).y);
		assertEquals((Double)2.6, b.get(4).y);

		assertEquals((Double)1.1, a.get(0).y);
		assertEquals((Double)1.3, a.get(1).y);
		assertEquals((Double)1.3, a.get(2).y);
		assertEquals((Double)1.5, a.get(3).y);
		assertEquals((Double)1.6, a.get(4).y);
	}

	@Test
	public void testListsWithIndexGap()
	{
		TupleSequence<Double>a = new TupleSequence<>();
		TupleSequence<Double>b = new TupleSequence<>();
		a.add(1, 1.0);
		a.add(10, 2.0);

		b.add(2, 9.1);
		b.add(3, 7.4);
		b.add(4, 10.11);
		b.add(6, 0.0);

		List<TupleSequence<Double>> result = TupleSequence.align(a, b, 0.1);
		a = result.get(0);
		b = result.get(1);

		assertEquals(6, a.size());
		assertEquals(6, b.size());

		assertEquals((Long)1l, b.get(0).x);
		assertEquals((Long)2l, b.get(1).x);
		assertEquals((Long)3l, b.get(2).x);
		assertEquals((Long)4l, b.get(3).x);
		assertEquals((Long)6l, b.get(4).x);
		assertEquals((Long)10l, b.get(5).x);

		assertEquals((Long)1l, a.get(0).x);
		assertEquals((Long)2l, a.get(1).x);
		assertEquals((Long)3l, a.get(2).x);
		assertEquals((Long)4l, a.get(3).x);
		assertEquals((Long)6l, a.get(4).x);
		assertEquals((Long)10l, a.get(5).x);

		assertEquals((Double)0.1, b.get(0).y); //nullValue
		assertEquals((Double)9.1, b.get(1).y);
		assertEquals((Double)7.4, b.get(2).y);
		assertEquals((Double)10.11, b.get(3).y);
		assertEquals((Double)0.0, b.get(4).y);
		assertEquals((Double)0.0, b.get(5).y);



		assertEquals((Double)1.0, a.get(0).y);
		assertEquals((Double)1.0, a.get(1).y);
		assertEquals((Double)1.0, a.get(2).y);
		assertEquals((Double)1.0, a.get(3).y);
		assertEquals((Double)1.0, a.get(4).y);
		assertEquals((Double)2.0, a.get(5).y);
	}


	@Test
	public void testAlignThreeLists()
	{
		TupleSequence<Double>a = new TupleSequence<>();
		TupleSequence<Double>b = new TupleSequence<>();
		TupleSequence<Double>c = new TupleSequence<>();

		a.add(1, 1.1);
		b.add(1, 3.1);

		b.add(2, 2.1);
		c.add(2, 3.1);

		a.add(5, 1.2);

		b.add(7, 2.2);
		c.add(7, 3.3);

		b.add(8, 2.4);

		List<TupleSequence<Double>> tmp = new ArrayList<>();
		tmp.add(a);
		tmp.add(b);
		tmp.add(c);
		tmp = TupleSequence.align(tmp, 0.1);
		a = tmp.get(0);
		b = tmp.get(1);
		c = tmp.get(2);

		assertEquals(5, a.size());
		assertEquals(5, b.size());
		assertEquals(5, c.size());

		assertEquals((Long)1l, a.get(0).x);
		assertEquals((Long)1l, b.get(0).x);
		assertEquals((Long)1l, c.get(0).x);

		assertEquals((Long)2l, a.get(1).x);
		assertEquals((Long)2l, b.get(1).x);
		assertEquals((Long)2l, c.get(1).x);

		assertEquals((Long)5l, a.get(2).x);
		assertEquals((Long)5l, b.get(2).x);
		assertEquals((Long)5l, c.get(2).x);

		assertEquals((Long)7l, a.get(3).x);
		assertEquals((Long)7l, b.get(3).x);
		assertEquals((Long)7l, c.get(3).x);

		assertEquals((Long)8l, a.get(4).x);
		assertEquals((Long)8l, b.get(4).x);
		assertEquals((Long)8l, c.get(4).x);

		assertEquals((Double)1.1, a.get(0).y);
		assertEquals((Double)3.1, b.get(0).y);
		assertEquals((Double)0.1, c.get(0).y); //nullValue

		assertEquals((Double)1.1, a.get(1).y);
		assertEquals((Double)2.1, b.get(1).y);
		assertEquals((Double)3.1, c.get(1).y);

		assertEquals((Double)1.2, a.get(2).y);
		assertEquals((Double)2.1, b.get(2).y);
		assertEquals((Double)3.1, c.get(2).y);

		assertEquals((Double)1.2, a.get(3).y);
		assertEquals((Double)2.2, b.get(3).y);
		assertEquals((Double)3.3, c.get(3).y);

		assertEquals((Double)1.2, a.get(4).y);
		assertEquals((Double)2.4, b.get(4).y);
		assertEquals((Double)3.3, c.get(4).y);

	}
}
