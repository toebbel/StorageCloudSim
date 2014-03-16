/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.policies;

import edu.kit.cloudSimStorage.cdmi.CdmiId;
import edu.kit.cloudSimStorage.storageModel.StorageBlobLocation;
import edu.kit.cloudSimStorage.storageModel.ObjectStorageServer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/** @author Tobias Sturm, 5/24/13 11:51 AM */
public class DefaultObjectToServerAllocation implements Comparator<ObjectStorageServer> {
	CdmiId objectID;
	HashMap<CdmiId, List<StorageBlobLocation>> blobLocations;


	public DefaultObjectToServerAllocation(CdmiId objectID, HashMap<CdmiId, List<StorageBlobLocation>> blobLocations) {
		this.objectID = objectID;
		this.blobLocations = blobLocations;
	}

	@Override
	public int compare(ObjectStorageServer o1, ObjectStorageServer o2) {
		return getNumOccurencesOfFileOnServer(o1, objectID, blobLocations) - getNumOccurencesOfFileOnServer(o2, objectID, blobLocations);
	}

	public static int getNumOccurencesOfFileOnServer(ObjectStorageServer server, CdmiId objectID, HashMap<CdmiId, List<StorageBlobLocation>> physicalFileLocations) {
		int result = 0;
		for (StorageBlobLocation location : getFilesOnServer(server, physicalFileLocations)) {
			if (location.getContentID().equals(objectID))
				result++;
		}
		return result;
	}

	public static List<String> getUsedHarddrivesOfFileOnServer(ObjectStorageServer server, CdmiId objectID, HashMap<CdmiId, List<StorageBlobLocation>> physicalFileLocations) {
		List<String> result = new ArrayList<>();
		for (StorageBlobLocation location : getFilesOnServer(server, physicalFileLocations)) {
			if (location.getContentID().equals(objectID))
				result.add(location.getDriveName());
		}
		return result;
	}

	/**
	 * Returns a lost of BlobLocators, that describe an object, that is stored inside this container and is put onto the given server
	 *
	 * @param server the server to scan
	 * @return all blobs that are on that server and in this container
	 */
	public static List<StorageBlobLocation> getFilesOnServer(ObjectStorageServer server, HashMap<CdmiId, List<StorageBlobLocation>> physicalFileLocations) {
		ArrayList<StorageBlobLocation> result = new ArrayList<>();
		for (CdmiId o : physicalFileLocations.keySet()) {
			for (StorageBlobLocation location : physicalFileLocations.get(o)) {
				if (location.getServer().equals(server))
					result.add(location);
			}
		}

		return result;
	}
}
