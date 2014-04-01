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

import edu.kit.cloudSimStorage.cdmi.CdmiDataObject;
import edu.kit.cloudSimStorage.cdmi.CdmiId;
import edu.kit.cloudSimStorage.helper.TimeHelper;
import edu.kit.cloudSimStorage.monitoring.TraceableResource;
import edu.kit.cloudSimStorage.monitoring.TraceableResourceAliasing;
import edu.kit.cloudSimStorage.monitoring.TupleSequence;
import edu.kit.cloudSimStorage.monitoring.StorageUsageHistory;
import edu.kit.cloudSimStorage.storageModel.resourceUtilization.TimeawareResourceLimitation;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static edu.kit.cloudSimStorage.helper.FileSizeHelper.Magnitude.BYTE;

/**
 * This class represents a server, that contains multiple harddrives. The server manages the access to these discs and
 * calculates the duration that is required to perform the operation.
 * <p/>
 * The units that are stored on disc are called {@link edu.kit.cloudSimStorage.storageModel.ObjectStorageBlob} and holds exactly one {@link edu.kit.cloudSimStorage.cdmi.CdmiDataObject}
 * The coordinating {@link edu.kit.cloudSimStorage.cdmi.CdmiObjectContainer} has to dispatch the storedObjects on different servers
 * and different discs, say: the server takes no decision which blob is stored on which disc. It provides an interface to
 * access the discs and simulates the time for these actions.
 * <p/>
 * All disk names are case-sensitive.
 * <p/>
 * Created by: Tobias Sturm
 * Date: 4/26/13
 * Time: 2:24 PM
 */
public class ObjectStorageServer implements TraceableResource {
	/**
	 * The system-wide unique simulation intern id
	 * <p/>
	 * best ID would be <rootURL>/<server-IP>
	 */
	private String id;
	TimeawareResourceLimitation ioLimitations;

	/** Harddrives that operate inside this server, accessed by e.G. '/dev/sda1' */
	private HashMap<String, IObjectStorageDrive> harddrives;

	private StorageUsageHistory totalUsage;
	private TraceableResourceAliasing trackableSubResources;

	private HashMap<CdmiId, List<IObjectStorageDrive>> objectDriveMapping;
	private long currentCapacity;
	private int ioLimitationsGarbageCounter;

	/**
	 * Creates a new entity.
	 *
	 * @param id the id to be associated with this entity
	 */
	public ObjectStorageServer(String rootUrl, String id, TimeawareResourceLimitation ioLimits) {
		assert ioLimits != null;
		objectDriveMapping = new HashMap<>();
		harddrives = new HashMap<>();
		this.id = id;
		this.ioLimitations = ioLimits;
		totalUsage = new StorageUsageHistory(rootUrl + " " + id, BYTE);
		trackableSubResources = new TraceableResourceAliasing();
		trackableSubResources.addMapping(AVAILABLE_STORAGE_PHYICAL, AVAILABLE_STORAGE, totalUsage);
		trackableSubResources.addMapping(USED_STORAGE_PHYSICAL_ABS, USED_STORAGE_ABS, totalUsage);
		trackableSubResources.addMapping(USED_STORAGE_PERCENTAGE_PHYSICAL, USED_STORAGE_PERCENTAGE, totalUsage);
	}


	/**
	 * Add a harddrive to this server instance
	 * <p/>
	 * The name of the harddrive has to be unique within this server.
	 * The harddrive will be wiped as soon as it is attached to the server.
	 *
	 * @param drive the drive to install in this server
	 * @return
	 */
	public boolean installHarddrive(IObjectStorageDrive drive) {
		if (harddrives.containsKey(drive.getName()))
			return false;
		harddrives.put(drive.getName(), drive);
		totalUsage.addAvailableStorageDiff(drive.getCapacity());

		for (CdmiId id : drive.getStoredObjectsIDs())
			drive.deleteObject(id);

		return true;
	}


	public double getTotalWorkload() {
		return ioLimitations.getValueAt(TimeHelper.getInstance().now()) / ioLimitations.getMaxPossible();
	}

	public List<String> getAvailableDiskLabels() {
		return new ArrayList<>(harddrives.keySet());
	}

	/**
	 * Checks if there is a disk that has enough space to store a file of the {@code requestedSize}
	 * <p/>
	 * Types:
	 * <ul>
	 * <li>{@code DiskProbeType#ANY} to list all available disks that match the space-request. {@code drives} can be {@code null}</li>
	 * <li>{@link DiskProbeType#PREFERRED} to try all given disks first, then all remaining. The result will include remaining disks, even if one or more preferred disk matched</li>
	 * <li>{@link DiskProbeType#EXACT} probe only given disks, no others</li>
	 * <li>{@link DiskProbeType#NOT} probe all disks, but not given</li>
	 * </ul>
	 * <p/>
	 * Order of result will be the same as the order of the requested drives.
	 *
	 * @param type          the type of probe
	 * @param drives        disks to exclude, prefer, match, ...
	 * @param requestedSize size in Byte. Has to be > 0.
	 * @return a list of all matching disks
	 */
	public List<String> probeDisk(DiskProbeType type, List<String> drives, long requestedSize) {
		assert requestedSize > 0;
		LinkedList<String> result = new LinkedList<>();

		//generate list of candidates
		LinkedList<String> candidates = new LinkedList<>(harddrives.keySet());
		if (type == DiskProbeType.EXACT) {
			candidates.clear();
			candidates.addAll(drives);
		} else if (type == DiskProbeType.NOT) {
			candidates.removeAll(drives);
		} else if (type == DiskProbeType.PREFERRED) {
			candidates.removeAll(drives);
			candidates.addAll(0, drives);
		}

		//check candidates
		for (String candidate : candidates)
			if (probeDisk(candidate, requestedSize)) {
				result.add(candidate);
			}

		return result;
	}

	/**
	 * Checks if a given disk has space left to store a file.
	 * <p/>
	 * This method checks if the disk exists. If not the result will be {@code false}, disregarding the {@code fileSize}
	 *
	 * @param name          of the disc
	 * @param requestedSize size in Byte
	 * @return true if the given disk exists and can store given amount of data
	 */
	public boolean probeDisk(String name, long requestedSize) {
		return harddrives.containsKey(name) && harddrives.get(name).hasPotentialAvailableSpace(requestedSize);
	}

	public boolean deleteBlob(StorageBlobLocation location) {
		assert location != null;
		assert location.getServer() == this;
		assert harddrives.containsKey(location.getDriveName());

		//delete object from drive
		IObjectStorageDrive drive = harddrives.get(location.getDriveName());
		if (!drive.containsObject(location.getContentID()))
			return false;

		long size = drive.getStoredObject(location.getContentID()).getPhysicalSize();
		totalUsage.addUsedStorageDiff(size * -1);

		drive.deleteObject(location.getContentID());

		//remove object->drive mapping
		objectDriveMapping.get(location.getContentID()).remove(drive);
		if (objectDriveMapping.get(location.getContentID()).isEmpty())
			objectDriveMapping.remove(location.getContentID());

		return true;
	}


	/**
	 * Tries to save a blob on any of the server
	 *
	 * @param data the data to save.
	 * @return Blob instance if usccessfully, null otherwise
	 */
	public ObjectStorageBlob saveBlob(CdmiDataObject data) {
		return saveBlob(data, probeDisk(DiskProbeType.ANY, null, data.getPhysicalSize()));
	}

	/**
	 * Tries to save a blob an the given drive
	 *
	 * @param data  the data to save
	 * @param drive the drive to try
	 * @return Blob instance if successfully, null otherwise
	 */
	public ObjectStorageBlob saveBlob(CdmiDataObject data, String drive) {
		List<String> list = new ArrayList<>();
		list.add(drive);
		return saveBlob(data, list);
	}

	/**
	 * Tries to save a blob on drives and stops after the first disk with enough space could be found
	 *
	 * @param data   the data to save
	 * @param drives the drives to try (may be empty)
	 * @return Blob instance if successfully, null otherwise
	 */
	public ObjectStorageBlob saveBlob(CdmiDataObject data, List<String> drives) {
		assert data != null;

		for (String drive : drives) {
			if (probeDisk(drive, data.getPhysicalSize())) {
				if (!harddrives.containsKey(data.getEntityId())) {

					//store object
					harddrives.get(drive).addObject(data);

					//track size
					long size = data.getPhysicalSize();
					totalUsage.addUsedStorageDiff(size);

					//add physical location of file to mapping
					if (!objectDriveMapping.containsKey(data.getEntityId())) {
						ArrayList<IObjectStorageDrive> tmp = new ArrayList<>();
						tmp.add(harddrives.get(drive));
						objectDriveMapping.put(data.getEntityId(), tmp);
					} else
						objectDriveMapping.get(data.getEntityId()).add(harddrives.get(drive));

					//create blob
					return new ObjectStorageBlob(new StorageBlobLocation(data.getEntityId(), this, drive), data);
				}
			}
		}
		return null;
	}


	/**
	 * Calculates the time that will pass before the operation can be sent to harddrive
	 * <p/>
	 * This is NOT the write/read delay of the HDD, this is the delay that is caused by the workload due other operations on this server and the harddrive
	 *
	 * @return delay in ms
	 */
	public int calculateDelay(String targetDrive) {
		assert harddrives.containsKey(targetDrive);
		long hddDelay = (time() - harddrives.get(targetDrive).getIOLimitation().getFirstFreeTimeslot(time()));
		long serverDelay = time() - ioLimitations.getFirstFreeTimeslot(time());

		return (int) StrictMath.max(hddDelay, serverDelay);
	}

	private static long time() {
		return CloudSim.getSimulationCalendar().getTimeInMillis();  //TODO use Helper
	}

	/**
	 * Calculate the duration of a write operation at maximum possible rate
	 * <p/>
	 * This includes other processes that run at the same time, that might slow down this operation as well as the write delay at the beginning of the operation.
	 *
	 * @param blob  The blob to store
	 * @param delay the delay before the operation is sent to drive ({@link #calculateDelay(String)}
	 * @return duration in ms
	 */
	public int calculateWriteDuration(ObjectStorageBlob blob, int delay) {
		String drive = blob.getLocation().getDriveName();
		assert harddrives.containsKey(drive);
		return calculateWriteDuration(blob, delay, harddrives.get(drive).getMaxWriteTransferRate());
	}

	/**
	 * Calculate the duration of a write operation
	 * <p/>
	 * This includes other processes that run at the same time, that might slow down this operation as well as the write delay at the beginning of the operation.
	 *
	 * @param blob    The blob to store
	 * @param delay   the delay before the operation is sent to drive ({@link #calculateDelay(String)}
	 * @param maxRate max. write rate in byte / ms.
	 * @return duration in ms
	 */
	public int calculateWriteDuration(ObjectStorageBlob blob, int delay, double maxRate) {
		String drive = blob.getLocation().getDriveName();
		assert harddrives.containsKey(drive);

		double rate = Math.min(maxRate, harddrives.get(drive).getMaxWriteTransferRate());
		long amount = blob.getData().getPhysicalSize();

		double hddDuration = harddrives.get(drive).getIOLimitation().use(delay + time(), amount, rate).getDuration() + harddrives.get(drive).getWriteLatency();
		double serverDuration = ioLimitations.use(delay, amount, maxRate).getDuration();

		if(ioLimitationsGarbageCounter++ > 100) {
			ioLimitationsGarbageCounter = 0;
			ioLimitations.removeSamplesBefore(TimeHelper.getInstance().now());
			harddrives.get(drive).getIOLimitation().removeSamplesBefore(TimeHelper.getInstance().now());
		}


		return (int) Math.max(hddDuration, serverDuration);
	}

	/**
	 * Calculates the duration of a read operation at maximum possible rate.
	 * <p/>
	 * This includes other processes that run ath the same time, that might slow down this operation as well as the read delay at the beginning of the operation
	 *
	 * @param blob  the blob to read
	 * @param delay the delay before the operation is sent to the drive ({@link #calculateDelay(String)}
	 * @return duration in ms
	 */
	public int calculateReadDuration(ObjectStorageBlob blob, int delay) {
		String drive = blob.getLocation().getDriveName();
		assert harddrives.containsKey(drive);
		return calculateReadDuration(blob, delay, harddrives.get(drive).getMaxReadTransferRate());
	}

	/**
	 * Calculate the duration of a read operation at the given rate or slower
	 * <p/>
	 * This includes other processes that run ath the same time, that might slow down this operation as well as the read delay at the beginning of the operation
	 *
	 * @param blob    the blob to read
	 * @param delay   the delay before the operation is sent to the drive ({@link #calculateDelay(String)}
	 * @param maxRate max. read rate in byte / ms
	 * @return duration in ms
	 */
	public int calculateReadDuration(ObjectStorageBlob blob, int delay, double maxRate) {
		String drive = blob.getLocation().getDriveName();
		assert harddrives.containsKey(drive);

		double rate = Math.min(maxRate, harddrives.get(drive).getMaxReadTransferRate());
		long amount = blob.getData().getPhysicalSize();

		double hddDuration = harddrives.get(drive).getIOLimitation().use(delay + time(), amount, rate).getDuration() + harddrives.get(drive).getReadLatency();
		double serverDuration = ioLimitations.use(delay, amount, maxRate).getDuration();
		return (int) Math.max(hddDuration, serverDuration);
	}

	public int calculateInternCopyDuration(long blobSize, String fromDisk, String toDisk, int delay, double maxRate) {
		assert harddrives.containsKey(fromDisk);
		assert harddrives.containsKey(toDisk);

		double rate = Math.min(Math.min(harddrives.get(fromDisk).getMaxReadTransferRate(), harddrives.get(toDisk).getMaxWriteTransferRate()), maxRate);

		double hddReadDuration = harddrives.get(fromDisk).getIOLimitation().use(delay + time(), blobSize, rate).getDuration();
		double avgReadRate = blobSize / hddReadDuration;

		double hddWriteDuration = harddrives.get(toDisk).getIOLimitation().use(delay + time(), blobSize, avgReadRate).getDuration();
		double avgTransferRate = blobSize / hddWriteDuration;

		//this includes the two other transfers, because the used max. rate is the min of the avg. read/write operaton
		double serverDuration = ioLimitations.use(delay + time(), blobSize, avgTransferRate).getDuration();


		return (int) serverDuration;
	}

	public String getId() {
		return id;
	}

	/**
	 * Checks if there is space on any disc on this server
	 *
	 * @param size requested size in MByte
	 * @return true if space is left
	 */
	public boolean hasSpaceLeftFor(long size) {
		for (IObjectStorageDrive drive : harddrives.values())
			if (drive.hasPotentialAvailableSpace(size))
				return true;
		return false;
	}

	@Override
	public String toString() {
		return getId();
	}

	@Override
	public TupleSequence<Double> getSamples(String key) {
		return trackableSubResources.getSamples(key);
	}

	@Override
	public String[] getAvailableTrackingKeys() {
		return trackableSubResources.getAvailableTrackingKeys();
	}

	/**
	 * Sum of capacities of all installed disks in this server
	 * @return capacity in bytes
	 */
	public long getTotalCapacity() {
		long sum = 0;
		for(IObjectStorageDrive disk : harddrives.values())
			sum += disk.getCapacity();
		return sum;
	}

	/**
	 * Sum of remaining capacity of all installed disks in this server
	 * @return capacity in bytes
	 */
	public long getCurrentCapacity() {
		long sum = 0;
		for(IObjectStorageDrive disk : harddrives.values())
			sum += (disk.getCapacity() - disk.getCurrentSize());
		return sum;
	}
}
