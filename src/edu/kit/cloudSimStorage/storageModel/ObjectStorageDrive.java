/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.storageModel;

import edu.kit.cloudSimStorage.helper.FileSizeHelper;
import edu.kit.cloudSimStorage.cdmi.CdmiDataObject;
import edu.kit.cloudSimStorage.cdmi.CdmiId;
import edu.kit.cloudSimStorage.monitoring.StorageUsageHistory;
import edu.kit.cloudSimStorage.storageModel.resourceUtilization.TimeawareResourceLimitation;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Tobias Sturm
 *         created at 4/26/13, 4:02 PM
 */
@Root
public abstract class ObjectStorageDrive implements IObjectStorageDrive {

	/** The server this harddrive is inside * */
	protected ObjectStorageServer location;

	protected HashMap<CdmiId, CdmiDataObject> storedObjects;

	@Attribute(name = "name")
	protected String name;

	protected String rootUrl;

	@Element(name = "capacity", required = false)
	protected long capacity;

	@Element(name = "reserverdSpace", required = false)
	protected long reservedSpace;

	@Element(name = "usedSpace", required = false)
	protected long usedSpace;

	@Element(name = "readRate", required = false)
	protected double readRate;

	@Element(name = "writeRate", required = false)
	protected double writeRate;

	@Element(name = "readLatency", required = false)
	protected double readLatency;

	@Element(name = "writeLatency", required = false)
	protected double writeLatency;

	@Element(name = "ioLimits", required = false)
	protected TimeawareResourceLimitation ioLimitation;

	protected StorageUsageHistory storageUsageHistory;

	public ObjectStorageDrive(@Attribute(name = "name") String name) {
		this.name = name;
		init();
	}

	private void init() {
		storedObjects = new HashMap<>();
		capacity = reservedSpace = usedSpace = 0;
		readLatency = writeLatency = 0;
		storageUsageHistory = new StorageUsageHistory("not attached disk " + name , FileSizeHelper.Magnitude.BYTE);
	}

	public ObjectStorageDrive(String rootUrl, ObjectStorageServer location, String name) {
		init();
		this.location = location;
		this.name = name;
		if (location != null) //overwrite history object if location is known
			storageUsageHistory = new StorageUsageHistory(rootUrl + " " + location.getId() + "/" + name, FileSizeHelper.Magnitude.BYTE);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public long getCapacity() {
		return capacity;
	}

	@Override
	public long getCurrentSize() {
		return usedSpace + reservedSpace;
	}

	@Override
	public double getMaxReadTransferRate() {
		return readRate;
	}

	@Override
	public double getMaxWriteTransferRate() {
		return writeRate;
	}

	@Override
	public double getReadLatency() {
		return readLatency;
	}

	@Override
	public double getWriteLatency() {
		return writeLatency;
	}

	@Override
	public boolean hasPotentialAvailableSpace(long size) {
		return getCurrentSize() + size <= capacity;
	}

	@Override
	public boolean reserveSpaceForObject(long size) {
		if (hasPotentialAvailableSpace(size)) {
			reservedSpace += size;
			storageUsageHistory.addUsedStorageDiff(size);
			return true;
		}
		return false;
	}

	@Override
	public boolean addReservedObject(CdmiDataObject object) {
		assert reservedSpace >= object.getPhysicalSize();

		if (storedObjects.containsKey(object.getEntityId()))
			return false;

		reservedSpace -= object.getPhysicalSize();
		usedSpace += object.getPhysicalSize();
		storedObjects.put(object.getEntityId(), object);
		return true;
	}

	@Override
	public boolean addObject(CdmiDataObject object) {
		if (!hasPotentialAvailableSpace(object.getPhysicalSize()))
			return false;
		if (storedObjects.containsKey(object.getEntityId()))
			return false;

		usedSpace += object.getPhysicalSize();
		storageUsageHistory.addUsedStorageDiff(object.getPhysicalSize());
		storedObjects.put(object.getEntityId(), object);
		return true;
	}

	@Override
	public List<CdmiDataObject> getStoredObjects() {
		return new ArrayList<>(storedObjects.values());
	}

	@Override
	public CdmiDataObject getStoredObject(CdmiId id) {
		if (storedObjects.containsKey(id))
			return storedObjects.get(id);
		else
			return null;
	}

	@Override
	public List<CdmiId> getStoredObjectsIDs() {
		return new ArrayList<>(storedObjects.keySet());
	}

	@Override
	public CdmiDataObject deleteObject(CdmiId id) {
		if (!containsObject(id))
			return null;
		CdmiDataObject result = getStoredObject(id);
		usedSpace -= result.getPhysicalSize();
		storageUsageHistory.addUsedStorageDiff(-1 * result.getPhysicalSize());

		storedObjects.remove(id);
		return result;
	}

	@Override
	public boolean containsObject(CdmiId id) {
		return storedObjects.containsKey(id);
	}

	@Override
	public TimeawareResourceLimitation getIOLimitation() {
		return ioLimitation;
	}
}
