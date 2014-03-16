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
import edu.kit.cloudSimStorage.helper.TupleSequence;
import edu.kit.cloudSimStorage.monitoring.StorageUsageHistory;
import edu.kit.cloudSimStorage.monitoring.TrackableResourceAliasing;
import edu.kit.cloudSimStorage.policies.DefaultObjectToServerAllocation;
import edu.kit.cloudSimStorage.storageModel.DiskProbeType;
import edu.kit.cloudSimStorage.storageModel.StorageBlobLocation;
import edu.kit.cloudSimStorage.storageModel.ObjectStorageServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents a object storage container, a virutal construct, that creates a namespace for objects. The names
 * of the objects inside one container have to be unique (except the empty name). Object containers are managed by one
 * {@link CdmiRootContainer}, which again is owned by exactly one user.
 * <p/>
 * Containers do have meta-data that are inherited by objects on their creation time.
 * The metadata of containers can include policies about replica and visioning.
 *
 * @author Tobias Sturm
 *         created at 4/29/13, 2:08 PM
 */
public class CdmiObjectContainer extends CdmiContainer<CdmiDataObject> {

	protected String name;

	/** List of all storedObjects (aka. storage objects) in this container and where they are stored */
	protected HashMap<CdmiId, List<StorageBlobLocation>> physicalFileLocations;

	/** A list of all servers, this container can store storedObjects on. The key is the SimEntity ID of the server */
	protected HashMap<String, ObjectStorageServer> associatedServers;

	TrackableResourceAliasing trackableSubResources;
	protected StorageUsageHistory physicalStorageHistory;

//    /**
//     * Creates an instance of object (aka blob) storage container.
//     *
//     * @param rootURI the URI of the StorageCloud
//     * @param metadata the metadata of the container, which may contain policies
//     */
//    public CdmiObjectContainer(String rootURI, String name, CdmiMetadata metadata) {
//        super(rootURI);
//
//	    this.setMetadata(metadata);
//	    this.setEntityName(name);
//    }

	public CdmiObjectContainer(CdmiRootContainer root, String name, CdmiMetadata metadata) {
		super(root);

		trackableSubResources = new TrackableResourceAliasing();
		trackableSubResources.addMapping(AVAILABLE_STORAGE_VIRTUAL, AVAILABLE_STORAGE, virtualStorageHistory);
		trackableSubResources.addMapping(USED_STORAGE_VIRTUAL_ABS, USED_STORAGE_ABS, virtualStorageHistory);
		trackableSubResources.addMapping(AVAILABLE_STORAGE_PHYICAL, AVAILABLE_STORAGE, physicalStorageHistory);
		trackableSubResources.addMapping(USED_STORAGE_PHYSICAL_ABS, USED_STORAGE_ABS, physicalStorageHistory);

		this.name = name;
		this.setEntityName(name);
		this.setMetadata(metadata);
		physicalFileLocations = new HashMap<>();
		associatedServers = new HashMap<>();

		virtualStorageHistory = new StorageUsageHistory(getRootURI() + "/" + getEntityName(), FileSizeHelper.Magnitude.BYTE);
		physicalStorageHistory = new StorageUsageHistory(getRootURI() + "/" + getEntityName(), FileSizeHelper.Magnitude.BYTE);
		physicalStorageHistory.setAvailableStorage(0);
		virtualStorageHistory.setAvailableStorage(Long.MAX_VALUE);
	}

	/**
	 * Adds an associated server to this container.
	 * <p/>
	 * An associated server is considered as physical storage for objects.
	 * Duplicates won't be added.
	 *
	 * @param server the server to add
	 */
	public void addAssociatedServer(ObjectStorageServer server) {
		associatedServers.put(server.getId(), server);
		physicalStorageHistory.addAvailableStorageDiff(server.getTotalCapacity());
	}

	/**
	 * Removes an associated server from this container.
	 * <p/>
	 * An associated server is considered as physical storage for objects.
	 * The server can only be removed, of there are no files from this container on it.
	 * <p/>
	 * If the server has not been associated before, nothing will happen.
	 *
	 * @param server the servers to remove
	 * @return false, if there are some objects stored on the server (that are inside this container)
	 */
	public boolean removeAssociatedServer(ObjectStorageServer server) {
		if (!getFilesOnServer(server).isEmpty())
			return false;
		associatedServers.remove(server.getId());
		physicalStorageHistory.addAvailableStorageDiff(server.getTotalCapacity() * (-1));
		return true;
	}

	@Override
	public void putChild(CdmiDataObject child) {
		super.putChild(child);

		int numLocations = getLocatinsFor(child.getEntityId()).size();

		virtualStorageHistory.addUsedStorageDiff(child.getSize());
		physicalStorageHistory.addUsedStorageDiff(child.getPhysicalSize() * numLocations);
	}


	@Override
	public void deleteChild(CdmiId id) {
		CdmiEntity child = getChild(id);
		int numLocations = getLocatinsFor(id).size();

		super.deleteChild(id);

		virtualStorageHistory.addUsedStorageDiff(child.getSize() * -1);
		physicalStorageHistory.addUsedStorageDiff(numLocations * child.getPhysicalSize() * -1);
	}

	/**
	 * Creates possible allocations for a new {@link CdmiDataObject}.
	 * <p/>
	 * This method creates different disk probe requests for every associated server, that this container is scheduled on
	 * The {@link edu.kit.cloudSimStorage.storageModel.StorageBlobLocation}s are sorted by best match to worst match.
	 * <p/>
	 * Overwrite this method to change the policy. these conditions have to be met:
	 * - One file can only be stored exactly once on a disc.
	 *
	 * @param obj the object to allocate
	 * @return a list of all locations where the object can be stored
	 */
	public List<StorageBlobLocation> getPossibleBlobToStorageAllocations(CdmiDataObject obj) {
		List<StorageBlobLocation> result = new ArrayList<>();
		DefaultObjectToServerAllocation policy = new DefaultObjectToServerAllocation(obj.getEntityId(), physicalFileLocations);

		List<ObjectStorageServer> servers = new ArrayList<>(associatedServers.values());
		Collections.sort(servers, policy);

		for (ObjectStorageServer s : servers) {
			List<String> usedHardDisks = DefaultObjectToServerAllocation.getUsedHarddrivesOfFileOnServer(s, obj.getEntityId(), physicalFileLocations);
			List<String> availableDisks = s.probeDisk(DiskProbeType.NOT, usedHardDisks, obj.getPhysicalSize());
			for (String disk : availableDisks)
				result.add(new StorageBlobLocation(obj.getEntityId(), s, disk));
		}

		return result;
	}

	/**
	 * Sets the locations for an object
	 * @param id of the object
	 * @param locations physical locations of the object (aka blobs)
	 */
	public void assignPhysicalLocations(CdmiId id, List<StorageBlobLocation> locations) {
		physicalFileLocations.put(id, locations);
	}


	/**
	 * Returns a lost of BlobLocators, that describe an object, that is stored inside this container and is put onto the given server
	 *
	 * @param server the server to scan
	 * @return all blobs that are on that server and in this container
	 */
	public List<StorageBlobLocation> getFilesOnServer(ObjectStorageServer server) {
		ArrayList<StorageBlobLocation> result = new ArrayList<>();
		for (CdmiId o : physicalFileLocations.keySet()) {
			for (StorageBlobLocation location : physicalFileLocations.get(o)) {
				if (location.getServer().equals(server))
					result.add(location);
			}
		}

		return result;
	}


	/**
	 * Checks if there is any associated server, that provides enough free space to store the given object
	 *
	 * @param object the object to allocate
	 * @return true if there is space left.
	 */
	public boolean spaceAvailableFor(CdmiDataObject object) {
		for (ObjectStorageServer server : associatedServers.values())
			if (server.hasSpaceLeftFor(object.getPhysicalSize()))
				return true;
		return false;
	}

	@Override
	public void Destroy() {
		for (CdmiEntity child : children.values()) {
			List<StorageBlobLocation> locations = physicalFileLocations.get(child.getEntityId());
			for (StorageBlobLocation location : locations) {
				location.getServer();
			}
			child.Destroy();
		}
	}

	public List<StorageBlobLocation> getLocatinsFor(CdmiId objectID) {
		if (!physicalFileLocations.containsKey(objectID))
			return new ArrayList<>();
		return physicalFileLocations.get(objectID);
	}

	@Override
	public String[] getAvailableTrackingKeys() {
		return trackableSubResources.getAvailableTrackingKeys();
	}

	@Override
	public TupleSequence<Double> getSamples(String key) {
		return trackableSubResources.getSamples(key);
	}
}
