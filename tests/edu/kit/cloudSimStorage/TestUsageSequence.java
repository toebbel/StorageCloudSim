/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage;

import edu.kit.cloudSimStorage.ObjectStorageSLAs.StorageCloudSLARequirements;
import edu.kit.cloudSimStorage.cloudBroker.UserRequest;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static edu.kit.cloudSimStorage.cloudBroker.UserRequest.blocking;
import static org.junit.Assert.*;

/** @author Tobias Sturm, 8/5/13 5:32 PM */
public class TestUsageSequence {
	@Test
	public void testSerializeDeserializeEmptySequence() throws Exception {
		UsageSequence candidate = new UsageSequence(0, new StorageCloudSLARequirements(), Collections.EMPTY_LIST);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		candidate.serialize(outputStream);

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toString().getBytes());
		outputStream.writeTo(System.out);

		UsageSequence deserialized = UsageSequence.deserialize(inputStream);
		assertEquals(deserialized, candidate);
	}

	@Test
	public void testSerializeDeserialize() throws Exception {
		StorageCloudSLARequirements sla = new StorageCloudSLARequirements();
		sla.rateByPrice();
		sla.hasNoObjectSizeLimit();
		sla.canCreateContainers();
		sla.canDeleteContainers();
		sla.locationIs("de");

		List<UserRequest> requests = new ArrayList<>();
		requests.add(UserRequest.idle(1));
		requests.add(UserRequest.putContainer("logs"));
		requests.add(blocking(UserRequest.putObject("logs", "httpd.log", 50)));
		requests.add(blocking(UserRequest.putObject("logs", "dovecot.log", 21)));
		requests.add(UserRequest.idle(1000 * 60));
		requests.add(UserRequest.downloadObject("logs", "httpd.log"));
		requests.add(blocking(UserRequest.downloadObject("logs", "dovecot.log")));

		UsageSequence candidate = new UsageSequence(0, sla, requests);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		candidate.serialize(outputStream);

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toString().getBytes());
		outputStream.writeTo(System.out);

		UsageSequence deserialized = UsageSequence.deserialize(inputStream);
		assertEquals(deserialized, candidate);
	}
}
