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
import edu.kit.cloudSimStorage.cdmi.CdmiMetadata;
import edu.kit.cloudSimStorage.cloudScenarioModels.GenericDrive;
import edu.kit.cloudSimStorage.storageModel.resourceUtilization.UnlimitedResource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static edu.kit.cloudSimStorage.Helper.*;

/** @author Tobias Sturm, 6/7/13 4:50 PM */
public class TestObjectStorageServer {

	ObjectStorageServer candidate;
	IObjectStorageDrive driveA, driveB, driveC;

	@org.junit.Before
	public void setUp() throws Exception {
		String root = "cloud";
		candidate = new ObjectStorageServer(root, "127.0.0.1", new UnlimitedResource());
		driveA = new GenericDrive("dev/sda1");
		driveB = new GenericDrive("dev/sda2");
		driveC = new GenericDrive("dev/sda3");
	}

	@org.junit.Test
	public void testInstallHarddrive() throws Exception {
		assertTrue(candidate.installHarddrive(driveA));
		assertTrue(candidate.installHarddrive(driveB));
		assertFalse(candidate.installHarddrive(driveA));

	}

	@org.junit.Test
	public void testGetAvailableDiskLabels() throws Exception {
		candidate.installHarddrive(driveA);
		assertArrayEquals(new String[] {"/dev/sda1"}, candidate.getAvailableDiskLabels().toArray());

		candidate.installHarddrive(driveB);
		assertArrayEquals(new String[] {"/dev/sda1", "/dev/sda2"}, candidate.getAvailableDiskLabels().toArray());
	}

	@org.junit.Test
	public void testProbeDiskSingleDisk() throws Exception {
		candidate.installHarddrive(driveA);

		assertTrue(candidate.probeDisk(driveA.getName(), 0));

		driveA.reserveSpaceForObject(driveA.getCapacity() - 1024 * 3);
		assertTrue(candidate.probeDisk(driveA.getName(), 1024 * 3));
		assertFalse(candidate.probeDisk(driveA.getName(), 1024 * 3 + 1));

		assertFalse(candidate.probeDisk("/dev/sdb1", 10));
	}

	@org.junit.Test
	public void testProbeDiskMultipleDisks() throws Exception {
		candidate.installHarddrive(driveA);
		candidate.installHarddrive(driveB);
		candidate.installHarddrive(driveC);

		driveA.reserveSpaceForObject(driveA.getCapacity() - 1024 * 1024 * 2);  //A has 1MB space
		driveB.reserveSpaceForObject(driveB.getCapacity() - 1024 * 1024 * 512); //B as 0.5GB
		driveC.reserveSpaceForObject(driveC.getCapacity() - 1024 * 1024 * 1024 * 2); //C as 2GB

		List<String> driveNames = new ArrayList<>();
		driveNames.add(driveA.getName());
		driveNames.add(driveB.getName());
		driveNames.add(driveC.getName());

		//all disks have one MB left
		assertSameItemsInSet(driveNames, candidate.probeDisk(DiskProbeType.ANY, driveNames, 1024 * 1024));

		//probe 5 MB
		List<String> driveNamesBC = new ArrayList<>();
		driveNamesBC.add(driveB.getName());
		driveNamesBC.add(driveC.getName());
		assertSameItemsInSet(driveNamesBC, candidate.probeDisk(DiskProbeType.ANY, null, 1024 * 1024 * 5));

		//probe 0.5GB
		assertSameItemsInSet(driveNamesBC, candidate.probeDisk(DiskProbeType.ANY, driveNames, 1024 * 1024 * 512));

		//probe 1 GB
		List<String> driveNamesC = new ArrayList<>();
		driveNamesC.add(driveC.getName());
		assertSameItemsInSet(driveNamesC, candidate.probeDisk(DiskProbeType.ANY, driveNames, 1024 * 1024 * 1024));

		//probe 1 KB, but exclude B
		List<String> driveNamesAC = new ArrayList<>();
		driveNamesAC.add(driveA.getName());
		driveNamesAC.add(driveC.getName());
		List<String> driveNamesB = new ArrayList<>();
		driveNamesB.add(driveB.getName());
		assertSameItemsInSet(driveNamesAC, candidate.probeDisk(DiskProbeType.NOT, driveNamesB, 1024));

		//probe 2 KB, but exclude B and C
		List<String> driveNamesA = new ArrayList<>();
		driveNamesA.add(driveA.getName());
		assertSameItemsInSet(driveNamesA, candidate.probeDisk(DiskProbeType.NOT, driveNamesBC, 1024 * 2));

		//probe 5 KB, but prefer b. Order has to be the same
		List<String> driveNamesCB = new ArrayList<>();
		driveNamesCB.add(driveC.getName());
		driveNamesCB.add(driveB.getName());
		assertSameItemsInSet(candidate.probeDisk(DiskProbeType.NOT, driveNamesCB, 1024 * 5), candidate.probeDisk(DiskProbeType.NOT, driveNamesBC, 1024 * 5));

		//probe 5 KB, but exclude all disks
		assertTrue(candidate.probeDisk(DiskProbeType.NOT, driveNames, 1024 * 5).isEmpty());
	}


	@org.junit.Test
	public void testSaveBlob() throws Exception {
		candidate.installHarddrive(driveA);

		CdmiMetadata meta = new CdmiMetadata();
		CdmiDataObject object = new CdmiDataObject(1024 * 5, meta, "cloudfront.net", "");

		//succ for object
		ObjectStorageBlob blob = candidate.saveBlob(object);
		assertNotNull(blob);
		assertEquals(object.getEntityId(), blob.getLocation().getContentID());
		assertEquals(driveA.getName(), blob.getLocation().getDriveName());
		assertSame(object, blob.getData());
		assertSame(candidate, blob.getLocation().getServer());

		//check if blob is stored on drive and server knowns, that space is used
		assertTrue(driveA.containsObject(object.getEntityId()));
		assertFalse(candidate.hasSpaceLeftFor(driveA.getCapacity() - 1024 * 4));

		//fail for object that's too big
		CdmiMetadata meta2 = new CdmiMetadata();
		CdmiDataObject bigObject = new CdmiDataObject(driveA.getCapacity(), meta2, "cloudfront.net", "");
		assertNull(candidate.saveBlob(bigObject));
	}
}
