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

import edu.kit.cloudSimStorage.helper.TimeHelper;


/** @author Tobias Sturm, 6/26/13 11:32 AM */
public class OperationTimeTraceSample {

	private long ommittedTimestamp;
	private long delay;
	private  long duration;
	private String descriptor;

	/**
	 * Creates an instance with the {@link #ommittedTimestamp} = now.
	 */
	public OperationTimeTraceSample(String descriptor) {
		this.ommittedTimestamp = TimeHelper.getInstance().now();
		this.descriptor = descriptor;
	}

	/**
	 * Manually set the delay of the trace in my
	 * @param delay in ms
	 */
	public void setDelay(int delay) {
		this.delay = delay;
	}

	/**
	 * Manually set the duration of the trace in ms
	 * @param duration     in ms
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}

	/**
	 * Call this method when the delay of the operation is over and the current simulation time is the basis for the delay value.
	 */
	public void startsNow() {
		delay = TimeHelper.getInstance().now() - ommittedTimestamp;
	}

	/**
	 * Call this method when the operation is finished and the current simulation time is the basis for the duration value.
	 */
	public void endsNow() {
		duration = TimeHelper.getInstance().now() - ommittedTimestamp - delay;
	}

	public OperationTimeTraceSample(long ommittedTimestamp, long delay, long duration) {
		assert ommittedTimestamp >= 0;
		assert delay >= 0;
		assert duration >= 0;

		this.ommittedTimestamp = ommittedTimestamp;
		this.delay = delay;
		this.duration = duration;
	}

	public long getOmmittedTimestamp() {
		return ommittedTimestamp;
	}

	public long getDelay() {
		return delay;
	}


	public long getDuration() {
		return duration;
	}

	public String getDescriptor() {
		return descriptor;
	}
}
