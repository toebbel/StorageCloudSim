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


    public void testRemoveDoublicateValues_noDupes() throws Exception {
        TupleSequence<Double> input = new TupleSequence<Double>();
        input.add(1, -1.0);
        input.add(2,  0.0);

        TupleSequence<Double> expected = new TupleSequence<Double>();
        expected.add(1,  -1.5);
        expected.add(2,  0.0);

        TupleSequence<Double> result = ReportGenerator.removeDoublicateValues(input);
        assertEquals(expected, result);
    }

    public void testRemoveDoublicateValues_null() throws Exception {
        TupleSequence<Double> expected = new TupleSequence<Double>();

        TupleSequence<Double> result = ReportGenerator.removeDoublicateValues(null);
        assertEquals(expected, result);
    }

    public void testRemoveDoublicateValues_dupesSameXValue() throws Exception {
        TupleSequence<Double> input = new TupleSequence<Double>();
        input.add(1, -1.0);
        input.add(1, -1.0);
        input.add(1,  0.0);
        input.add(1,  1.0);

        TupleSequence<Double> expected = new TupleSequence<Double>();
        expected.add(1, -1.0);
        expected.add(1,  0.0);
        expected.add(1,  1.0);

        TupleSequence<Double> result = ReportGenerator.removeDoublicateValues(input);
        assertEquals(expected, result);
    }

    public void testRemoveDoublicateValues_dupesDifferentXValue() throws Exception {
        TupleSequence<Double> input = new TupleSequence<Double>();
        input.add(1, -1.0);
        input.add(2, -1.0);
        input.add(3,  0.0);
        input.add(4,  1.0);
        input.add(5,  1.0);

        TupleSequence<Double> expected = new TupleSequence<Double>();
        expected.add(1, -1.0);
        expected.add(2, -1.0);
        expected.add(3,  0.0);
        expected.add(4,  1.0);
        expected.add(5,  1.0);

        TupleSequence<Double> result = ReportGenerator.removeDoublicateValues(input);
        assertEquals(expected, result);
    }
}