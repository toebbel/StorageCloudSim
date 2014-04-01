/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.ObjectStorageSLAs;

import edu.kit.cloudSimStorage.ObjectStorageSLAs.ratingSLA.RateCharacteristicsWithInverse;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static edu.kit.cloudSimStorage.helper.FileSizeHelper.*;
import static edu.kit.cloudSimStorage.helper.FileSizeHelper.Magnitude.*;
import static org.junit.Assert.*;

/** @author Tobias Sturm, 8/5/13 4:17 PM */
public class TestStorageCloudSLARequest {

	@Test
	public void testSerializeDeserializeEmptySLA() throws Exception {
		StorageCloudSLARequirements candidate = new StorageCloudSLARequirements();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		candidate.serialize(outputStream);

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toString().getBytes());
		outputStream.writeTo(System.out);

		StorageCloudSLARequirements deserialized = StorageCloudSLARequirements.deserializer(inputStream);
		assertEquals(deserialized, candidate);
	}

	@Test
	public void testSerializeDeserializeOneRequirementOneRating() throws Exception {
		StorageCloudSLARequirements candidate = new StorageCloudSLARequirements();
		candidate.canCreateContainers().rateByPrice();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		candidate.serialize(outputStream);

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toString().getBytes());
		outputStream.writeTo(System.out);

		StorageCloudSLARequirements deserialized = StorageCloudSLARequirements.deserializer(inputStream);
		assertEquals(deserialized, candidate);
	}

	@Test
	public void testSerializeDeserializeAllSLAs() throws Exception {
		StorageCloudSLARequirements candidate = new StorageCloudSLARequirements();
		candidate.rateByPrice().canCreateContainers().maxObjectSizeAtLeast(1).maxDownloadCost(1).canDeleteContainers().canModifyMetadata().hasNoContainerSizeLimit().hasNoObjectSizeLimit().locationIs("de").maxContainerSizeAtLeast((long) fromBytes(2, GIGA_BYTE)).maxStorageCost(0.1).rateByExportCapabilities().addRating(new RateCharacteristicsWithInverse("some key", "some descritption", 1, 4));

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		candidate.serialize(outputStream);

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toString().getBytes());
		outputStream.writeTo(System.out);

		StorageCloudSLARequirements deserialized = StorageCloudSLARequirements.deserializer(inputStream);
		assertEquals(deserialized, candidate);
	}


}
