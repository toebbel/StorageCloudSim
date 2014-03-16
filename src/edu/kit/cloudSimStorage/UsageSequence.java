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

import edu.kit.cloudSimStorage.ObjectStorageSLAs.StorageCloudSLARequest;
import edu.kit.cloudSimStorage.cdmi.CdmiOperationVerbs;
import edu.kit.cloudSimStorage.cloudBroker.StorageBroker;
import edu.kit.cloudSimStorage.cloudBroker.UserRequest;
import edu.kit.cloudSimStorage.cloudOperations.CloudRequest;
import edu.kit.cloudSimStorage.helper.TupleSequence;
import edu.kit.cloudSimStorage.monitoring.ILoggable;
import edu.kit.cloudSimStorage.monitoring.TrackableResource;
import edu.kit.cloudSimStorage.helper.Tuple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.simpleframework.xml.*;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/** @author Tobias Sturm, 6/30/13 3:18 PM */
@Root
public class UsageSequence implements ILoggable {
	@Element(name="SLA")
	private StorageCloudSLARequest SLA;

	@ElementList(name="requests")
	private List<UserRequest> requests;

	@Attribute(name="sequenceID")
	private int id;

	private Logger logger;

	private static final String LOGGER_PREFIX = "USAGE_SEQUENCE_LOGGER";

	public Logger getLoggerFor(int sequenceID) {
		return Logger.getLogger(LOGGER_PREFIX + sequenceID);
	}

	public UsageSequence(@Attribute(name="sequenceID") int sequenceID, @Element(name="SLA") StorageCloudSLARequest SLA, @ElementList(name="requests") List<UserRequest> userRequests) {
		this.SLA = SLA;
		this.id = sequenceID;
		this.requests = userRequests;
		logger = getLoggerFor(this.id);
	}


	public StorageCloudSLARequest getSLA() {
		return SLA;
	}

	public int getId() {
		return id;
	}


	public List<UserRequest> getRequests() {
		return requests;
	}

	@Override
	public Logger getLogger() {
		return logger;
	}


	@Override
	public String toString() {
		return "UsageSequence" + id;
	}


	public void serialize(OutputStream out) throws Exception {
		Serializer serializer = new Persister();
		serializer.write(this, out);
	}

	public static UsageSequence deserialize(InputStream in) throws Exception {
		Serializer serializer = new Persister();
		return serializer.read(UsageSequence.class, in);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj != null && obj.getClass() == this.getClass() && (
				(
						((UsageSequence) obj).getSLA() == null && ((UsageSequence) obj).getSLA() == getSLA())
							||
						(((UsageSequence) obj).getSLA().equals(getSLA()))
				) && (
						(((UsageSequence) obj).getRequests() == null && requests == null)
							||
						(((UsageSequence) obj).getRequests() != null &&
						requests != null &&
						((UsageSequence) obj).getRequests().size() == requests.size()))))
								return false;

		if(requests == null)
			return true;

		for(UserRequest r : ((UsageSequence) obj).getRequests())
			if(!requests.contains(r)) {
				return false;
			}

		return true;
	}
}
