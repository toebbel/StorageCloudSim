package edu.kit.cloudSimStorage.monitoring.sampleSequenceOperatorations.SampleValueOperations;

/**
 * @author Tobias Sturm, 4/1/14 12:47 PM
 */
public abstract class SequenceValueOperation {

	public abstract double getResult();

	public abstract void addSample(double val);

	public abstract void reset();

}
