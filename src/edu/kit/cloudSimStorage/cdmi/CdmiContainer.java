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
import edu.kit.cloudSimStorage.exceptions.EntityAlreadyExistsException;
import edu.kit.cloudSimStorage.exceptions.EntityNameException;
import edu.kit.cloudSimStorage.exceptions.EntityNotFoundException;
import edu.kit.cloudSimStorage.helper.TimeHelper;
import edu.kit.cloudSimStorage.monitoring.StorageUsageHistory;
import edu.kit.cloudSimStorage.monitoring.TrackableResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * Instances of this class represent a CMDI container in general. A container stores multiple children, whereas children must be a sub-type of {@link CdmiEntity}.
 * Containers do have meta-data. Sub-types of this container specify which of these metadata are inherited by the children or not.
 * Children can be retrieved via their name or their ID.
 * <p/>
 * @author Tobias Sturm
 * Date: 4/26/13
 * Time: 12:58 PM
 */
public abstract class CdmiContainer<T extends CdmiEntity> extends CdmiEntity implements TrackableResource {
	protected Hashtable<CdmiId, CdmiEntity> children;
	protected Hashtable<String, CdmiId> childrenNameIdMapping;
	protected CdmiMetadata metadata;

	public static final String[] reservedNames = {"cdmi_objectid", "cdmi_domains", "cdmi_capabilities", "cdmi_snapshots", "cdmi_versions"};

	protected StorageUsageHistory virtualStorageHistory;


	/**
	 * Creates an entity within the given rootURI and creates a uniqueID ({@link edu.kit.cloudSimStorage.cdmi.CdmiId#generateId(String)}
	 *
	 * @param rootURI the URI of the StorageCloud
	 */
	public CdmiContainer(String rootURI) {
		super(rootURI);
		init();
	}


	public CdmiContainer(CdmiRootContainer root) {
		super(root);
		init();
	}

	protected void init() {
		childrenNameIdMapping = new Hashtable<>();
		children = new Hashtable<>();
		metadata = new CdmiMetadata();
	}


	/**
	 * Returns all children of this container.
	 *
	 * @return children of this container
	 */
	public Hashtable<CdmiId, T> getChildren() {
		//noinspection unchecked
		return (Hashtable<CdmiId, T>) children;
	}

	/**
	 * Retrieves the child
	 *
	 * @param id the ID of the child to retrieve
	 * @return the child
	 */
	public T getChild(CdmiId id) {
		if (!children.containsKey(id))
			throw new EntityNotFoundException("Could not find child with id " + id + " in container " + this.getEntityId());
		//noinspection unchecked
		return (T) children.get(id);
	}

	/**
	 * Resolves the id of the child by name.
	 * <p/>
	 * Not every child must have a name. Children with empty names can't be resolved with this function.
	 *
	 * @param name name of the child (case-sensitive)
	 * @return the ID of the child.
	 */
	public CdmiId getChildId(String name) {
		if (!childrenNameIdMapping.containsKey(name))
			throw new EntityNotFoundException("Could not find child  '" + name + "' in container " + this.getEntityId());
		return childrenNameIdMapping.get(name);
	}

	public List<String> getChildrenNames() {
		ArrayList<String> result = new ArrayList<>();
		for (T e : getChildren().values())
			result.add(e.getEntityName());
		return result;
	}

	/**
	 * Indicates whether this container stores a child with the given name.
	 * <p/>
	 * Not every child must have a name.
	 *
	 * @param name name of the child (chase-sensitive)
	 * @return true if a child with that name is stored in the container.
	 */
	public boolean containsChildWithName(String name) {
		return childrenNameIdMapping.containsKey(name);
	}

	/**
	 * Indicates whether this container stores a child with the given ID.
	 *
	 * @param id id of the child.
	 * @return true if a child with that ID is stored in the container.
	 */
	public boolean containsChild(CdmiId id) {
		return children.containsKey(id);
	}

	/**
	 * Adds / updates a child in the container.
	 * <p/>
	 * Any child with the same ID in the container will be replaced.
	 * Any child with the same name in the container will be replaced.
	 * If the name of the child is renamed, the old name-id-mapping will still exist (use {@link CdmiContainer#renameChild(CdmiId, String)} instead).
	 * <p/>
	 * Updates the LAST_WRITE_ACCESS value of the metadata of the container if operation succeeds
	 *
	 * @param child the child to put into the container.
	 */
	public void putChild(T child) {
		if (child.getEntityName() != null)
			childrenNameIdMapping.put(child.getEntityName(), child.getEntityId());

		updateLastWriteTimestamp();
		children.put(child.getEntityId(), child);
	}

	private void updateLastWriteTimestamp() {
		metadata.set(CdmiMetadata.LAST_WRITE_ACCESS, CdmiMetadata.getNow());
	}

	/**
	 * Renames a child. The ID won't change.
	 * <p/>
	 * Fails if there is no child in this container with the ID {@code id} or if there is already a child with the name {@code newName}.
	 * All names are case-sensitive.
	 * <p/>
	 * Updates the LastWrite timestamp if Operation succeeds
	 *
	 * @param id      of the object to rename
	 * @param newName of the object, can be empty.
	 */
	public void renameChild(CdmiId id, String newName) {
		if (!containsChild(id))
			throw new EntityNotFoundException("Could not find child with id '" + id + "' in container " + this.getEntityId());

		//remove old name
		String oldName = children.get(id).getEntityName();
		if (!oldName.trim().isEmpty())
			childrenNameIdMapping.remove(oldName);

		//rename child itself
		children.get(id).setEntityName(newName);

		//re-map name to ID, if new name not empty
		if (!newName.trim().isEmpty()) {
			if (containsChildWithName(newName))
				throw new EntityAlreadyExistsException("Could not rename child to '" + newName + "' inside of container " + this.getEntityId());
			childrenNameIdMapping.put(newName, id);
		}
		updateLastWriteTimestamp();
	}

	/**
	 * Removes a child and all links of it from the container.
	 * <p/>
	 * Updates the LastWrite timestamp if the operation succeeds.
	 * <p/>
	 * Fails if there is no child with the given ID.
	 *
	 * @param id the ID of the child to delete
	 */
	public void deleteChild(CdmiId id) {
		if (!containsChild(id))
			throw new EntityNotFoundException("Could not find child with id " + id + " in container " + this.getEntityId());
		CdmiEntity child = children.get(id);
		childrenNameIdMapping.remove(children.get(id).getEntityName());
		children.remove(id);

		if (!childrenNameIdMapping.containsValue(id)) {  //if there are more links to the objectID
			for (String name : childrenNameIdMapping.keySet()) {
				if (childrenNameIdMapping.get(name).equals(id))
					childrenNameIdMapping.remove(name);
				if (!childrenNameIdMapping.containsValue(id)) //short-cut if no more link exists
					break;
			}
		}

		updateLastWriteTimestamp();
	}

	/**
	 * Returns ref. to the metadata of this container
	 *
	 * @return the metadata
	 */
	public CdmiMetadata getMetadata() {
		return metadata;
	}

	/**
	 * Overwrites the metadata of the container.
	 *
	 * @param metadata the new metadata of the container
	 */
	public void setMetadata(CdmiMetadata metadata) {
		this.metadata = metadata;
	}

	public void setAllowedSize(long allowedSize) {
		metadata.set(CdmiMetadata.MAX_OBJECT_SIZE, String.valueOf(allowedSize));
		this.virtualStorageHistory.setAvailableStorage(allowedSize);
	}

	public long getAllowedSize() {
		if(metadata.contains(CdmiMetadata.MAX_OBJECT_SIZE))
			return Long.parseLong(metadata.get(CdmiMetadata.MAX_OBJECT_SIZE));
		return Long.MAX_VALUE;
	}

	@Override
	public long getSize() {
		long sum = 0;
		for (CdmiEntity child : children.values()) {
			sum += child.getSize();
		}
		return sum;
	}

	@Override
	public long getPhysicalSize() {
		long sum = 0;
		for (CdmiEntity child : children.values()) {
			sum += child.getPhysicalSize();
		}
		return (long) (sum + metadata.getMetadataSize());
	}

	@Override
	public String toString() {
		return "Container '" + getEntityName() + "' (" + getEntityId() + "), size " + FileSizeHelper.toHumanReadable(getSize()) + " (" + getPhysicalSize() + "B), " + getChildren().size() + " children, metadata " + getMetadata();
	}

	@Override
	void setEntityName(String name) {
		if (Arrays.asList(reservedNames).contains(name))
			throw new EntityNameException(name + " is a forbidden name for containers");
		super.setEntityName(name);    //To change body of overridden methods use File | Settings | File Templates.
	}
}