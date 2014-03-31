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

import edu.kit.cloudSimStorage.helper.FileSizeHelper;
import edu.kit.cloudSimStorage.helper.TimeHelper;

import static edu.kit.cloudSimStorage.helper.FileSizeHelper.Magnitude;

/** @author Tobias Sturm, 6/23/13 6:09 PM */
public class StorageUsageHistory implements TrackableResource {

	ResourceUsageHistory availableStorage, usedStorage;
	Magnitude inputMagnitude;

	public StorageUsageHistory(String instanceName, Magnitude inputMagnitude) {
		this.inputMagnitude = inputMagnitude;

		availableStorage = new ResourceUsageHistory(TrackableResource.AVAILABLE_STORAGE, "Available storage on '" + instanceName + "'", "available storage", "Byte");
		usedStorage = new ResourceUsageHistory(TrackableResource.USED_STORAGE_ABS, "Used storage on '" + instanceName + "'", "used storage", "Byte");
	}

	public void setUsedStorage(long timestamp, long amount) {
		usedStorage.addSample(timestamp, FileSizeHelper.toBytes(amount, inputMagnitude));
	}

	public void addUsedStorageDiff(long timestamp, long dif) {
		usedStorage.addDiff(timestamp, FileSizeHelper.toBytes(dif, inputMagnitude));
	}

	public void addUsedStorageDiff(long dif) {
		usedStorage.addDiff(FileSizeHelper.toBytes(dif, inputMagnitude));
	}

	public void setAvailableStorage(long timestamp, long amount) {
		availableStorage.addSample(timestamp, FileSizeHelper.toBytes(amount, inputMagnitude));
	}

	public void setAvailableStorage(long allowedSize) {
		setAvailableStorage(TimeHelper.getInstance().now(), allowedSize);
	}

	public void addAvailableStorageDiff(long timestamp, long dif) {
		availableStorage.addDiff(timestamp, FileSizeHelper.toBytes(dif, inputMagnitude));
	}

	public void addAvailableStorageDiff(long dif) {
		availableStorage.addDiff(FileSizeHelper.toBytes(dif, inputMagnitude));
	}

	public ResourceUsageHistory getAvailabeStorageHistory() {
		return availableStorage;
	}

	public ResourceUsageHistory getUsedStorageHistory() {
		return usedStorage;
	}

	public TupleSequence<Double> getPercentageUsed() {
		TupleSequence<Double> result = new TupleSequence<>();
		TupleSequence<Double> availableStorageHistory = availableStorage.getSamples();
		TupleSequence<Double> usedStorageHistory = usedStorage.getSamples();

		int ia = 0, iu = 0;
		while (ia < availableStorageHistory.size() || iu < usedStorageHistory.size()) {
			Long smallerTimestamp;
			Double val = usedStorageHistory.get(iu).y / availableStorageHistory.get(ia).y;
			if (availableStorageHistory.get(ia).x < usedStorageHistory.get(iu).x) {
				smallerTimestamp = availableStorageHistory.get(ia).x;
				ia++;
			} else {
				smallerTimestamp = usedStorageHistory.get(iu).x;
				iu++;
			}
			result.add(new Tuple<>(smallerTimestamp, val));
		}

		return result;
	}

	@Override
	public TupleSequence<Double> getSamples(String key) {
		switch (key) {
			case AVAILABLE_STORAGE:
				return availableStorage.getSamples();
			case USED_STORAGE_ABS:
				return usedStorage.getSamples();
			case USED_STORAGE_PERCENTAGE:
				return getPercentageUsed();
		}
		return null;
	}

	@Override
	public String[] getAvailableTrackingKeys() {
		return new String[]{TrackableResource.AVAILABLE_STORAGE, TrackableResource.USED_STORAGE_ABS, TrackableResource.USED_STORAGE_PERCENTAGE};
	}

}

