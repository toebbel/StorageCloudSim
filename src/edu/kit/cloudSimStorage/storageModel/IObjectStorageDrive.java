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
import edu.kit.cloudSimStorage.storageModel.resourceUtilization.TimeawareResourceLimitation;

import java.util.List;

/**
 * An interface which models a drive, that is used for ObjectStorage systems. Objects are identified via their fixed {@link CdmiId} and thus can not be renamed.
 * <p/>
 * Object sizes are always in Byte. The drive can manage up to ~8 Exabyte.
 *
 * @author Tobias Sturm, 5/19/13 12:05 PM
 */
public interface IObjectStorageDrive {
	/**
	 * The device name in the OS, e.G. '/dev/sda1'
	 *
	 * @return device name
	 */
	String getName();

	/**
	 * Returns the total capacity of the drive in byte.
	 *
	 * @return capacity in byte
	 */
	long getCapacity();

	/**
	 * Returns the number of <i>used bytes</i> on the drive plus the reserved size.
	 *
	 * @return number of used bytes
	 */
	long getCurrentSize();

	/**
	 * Returns the max possible transfer rate for read operations in byte/ms
	 *
	 * @return max read rate in byte/ms
	 */
	double getMaxReadTransferRate();

	/**
	 * Returns the max possible transfer rate for write operations in byte/ms
	 *
	 * @return max write rate in byte/ms
	 */
	double getMaxWriteTransferRate();

	/**
	 * Returns the average latency of the drive in ms for read operations.
	 * <p/>
	 * The latency includes the rotational latency, the command processing latency and the settle latency
	 *
	 * @return avg. latency in ms
	 */
	double getReadLatency();

	/**
	 * Returns the average latency of the drive in ms for write operations.
	 * <p/>
	 * The latency includes the rotational latency, the command processing latency and the settle latency
	 *
	 * @return avg. latency in ms
	 */
	double getWriteLatency();

	/**
	 * Indicates whether there is space left.
	 *
	 * @param size number of bytes to request
	 * @return true if requested bytes is available
	 */
	boolean hasPotentialAvailableSpace(long size);

	/**
	 * Reserves space on the disc. Use {@link #addReservedObject(edu.kit.cloudSimStorage.cdmi.CdmiDataObject)} to add the object afterwards.
	 *
	 * @param size size to request on drive
	 * @return true if space could be reserved
	 */
	boolean reserveSpaceForObject(long size);

	/**
	 * Adds an object without changing the used size, because the size of the object has already been reserved before via {@link #reserveSpaceForObject(long)}.
	 * <p/>
	 * The operation fails, if there is already a stored object on the drive, that has the same {@link CdmiId}
	 *
	 * @param object the object to store on the drive
	 * @return true if succeeded
	 */
	boolean addReservedObject(CdmiDataObject object);

	/**
	 * Adds and object to the drive.
	 * <p/>
	 * The operation fails, if there is not enough space left.
	 * The operation fails, if there is already a stored object on the drive, that has the same {@link CdmiId}
	 *
	 * @param object the object to store
	 * @return true if succeeded
	 */
	boolean addObject(CdmiDataObject object);

	/**
	 * Returns a list of all stored objects on this drive
	 *
	 * @return list of all stored objects
	 */
	List<CdmiDataObject> getStoredObjects();

	/**
	 * Returns a previously stored object from the drive.
	 *
	 * @param id the ID of the requested object
	 * @return the object with the given ID or {@code null} if the object could not be found.
	 */
	CdmiDataObject getStoredObject(CdmiId id);

	/**
	 * Returns a list of all IDs of all stored objects on this drive
	 *
	 * @return list of IDs of all stored objects
	 */
	List<CdmiId> getStoredObjectsIDs();

	/**
	 * Removes an object from the drive
	 *
	 * @param id the ID of the object to delete
	 * @return the deleted object or {@code null} if object could not be found
	 */
	CdmiDataObject deleteObject(CdmiId id);


	/**
	 * Indicates whether there is an object with the given ID on the drive
	 *
	 * @param id the ID to check
	 * @return true if object is on drive
	 */
	boolean containsObject(CdmiId id);

	/**
	 * Creates a new instance of an {@code IObjectStorageDrive}, that has the same specs like this instance, but no stored blobs on it.
	 *
	 * @return new instance with same specs
	 */
	IObjectStorageDrive clone(ObjectStorageServer location, String name);


	/**
	 * The time-aware limitation for IO operations for this storage drive. Measurements are taken in byte / ms
	 *
	 * @return instance that limits the IO operations over time
	 */
	TimeawareResourceLimitation getIOLimitation();

}
