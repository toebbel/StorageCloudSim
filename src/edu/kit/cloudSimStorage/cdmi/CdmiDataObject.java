/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.cdmi;

import edu.kit.cloudSimStorage.helper.FileSizeHelper;

/**
 * Instances of this class represent an object, that is stored in a {@link edu.kit.cloudSimStorage.StorageCloud}.
 * Every Object has to be inside a {@link CdmiObjectContainer}, where it has a container-wide unique name (case-sensitive)
 * and a StorageCloud-wide unique ID.
 * <p/>
 * @author Tobias Sturm
 * Date: 4/26/13
 * Time: 12:51 PM
 */
public class CdmiDataObject extends CdmiEntity {
	/** objects that are currently processed (e.G. uploaded) are not in state 'completed' */
	private CdmiCompletionStatus completionStatus;

	/** meta-data that are stored on the disc, together with the object itself. */
	private CdmiMetadata metadata;

	/** size of object that is required to store it (excluding meta-data) in Bytes */
	long size;

	/**
	 * Creates a data-object with given size and metadata. The CompletionStatus is set to {@link CdmiCompletionStatus#Complete}
	 * and a new ID is generated.
	 *
	 * @param size     in Bytes
	 * @param metadata
	 * @param rootURI  the root URI of the StorageCloud
	 * @param name     the name of the object to create. Can be empty or null.
	 * @throws IllegalArgumentException if size < 0 or metadata is null
	 */
	public CdmiDataObject(long size, CdmiMetadata metadata, String rootURI, String name) throws IllegalArgumentException {
		super(rootURI);
		if (size < 0)
			throw new IllegalArgumentException("DataObjects must have size > 0");
		if (metadata == null)
			throw new IllegalArgumentException("DataObjects must have metadata");
		this.size = size;
		this.metadata = metadata;
		this.completionStatus = CdmiCompletionStatus.Complete;
		this.setEntityName(name);
	}


	/**
	 * Indicates the {@link CdmiCompletionStatus} of the object.
	 *
	 * @return status of the object
	 */
	public CdmiCompletionStatus getCompletionStatus() {
		return completionStatus;
	}

	/**
	 * Updates the size of the object
	 *
	 * @param size new size in Byte
	 */
	public void setSize(long size) {
		if (size < 0)
			throw new IllegalArgumentException("DataObjects must have size > 0");
		this.size = size;
	}

	/**
	 * Updates the {@link CdmiCompletionStatus} of the object
	 *
	 * @param completionStatus the new status to set.
	 */
	public void setCompletionStatus(CdmiCompletionStatus completionStatus) {
		this.completionStatus = completionStatus;
	}

	/**
	 * Retrieves the {@link CdmiMetadata} of the object.
	 *
	 * @return the meta-data of the object
	 */
	public CdmiMetadata getMetadata() {
		return metadata;
	}

	/**
	 * replaces the {@link CdmiMetadata} of the object.
	 *
	 * @param metadata new meta-data to set.
	 */
	public void setMetadata(CdmiMetadata metadata) {
		if (metadata == null)
			throw new IllegalArgumentException("DataObjects must have metadata");
		this.metadata = metadata;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public long getPhysicalSize() {
		return size + (int) metadata.getMetadataSize();
	}

	@Override
	public void Destroy() {
		//TODO implement method
	}

	@Override
	public String toString() {
		return "Object '" + getEntityName() + "' (" + getEntityId() + "), size " + FileSizeHelper.toHumanReadable(getSize()) + "  (" + getPhysicalSize() + "B), metadata: " + getMetadata().toString();
	}
}
