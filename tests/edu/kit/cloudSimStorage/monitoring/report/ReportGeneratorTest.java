package edu.kit.cloudSimStorage.monitoring.report;

import edu.kit.cloudSimStorage.monitoring.TupleSequence;
import junit.framework.TestCase;

public class ReportGeneratorTest extends TestCase {

    public void testChangeMagnitude() throws Exception {
        TupleSequence<Double> input = new TupleSequence<Double>();
        input.add(1, -1.0);
        input.add(3,  0.0);
        input.add(4,  1.0);
        input.add(10, 3.0);

        TupleSequence<Double> expected = new TupleSequence<Double>();
        expected.add(1,   0.5);
        expected.add(3,  -0.0);
        expected.add(4,  -0.5);
        expected.add(10, -1.5);

        TupleSequence<Double> result = ReportGenerator.changeMagnitude(input, -0.5);
        assertEquals(expected, result);
    }

    public void testChangeMagnitude_nullSequence() throws Exception {
        TupleSequence<Double> expected = new TupleSequence<Double>();

        TupleSequence<Double> result = ReportGenerator.changeMagnitude(null, 1);
        assertEquals(expected, result);
    }

    public void testRemoveDoublicateValues() throws Exception {

    }

    public void testRemoveDoublicateValues1() throws Exception {

    }
}