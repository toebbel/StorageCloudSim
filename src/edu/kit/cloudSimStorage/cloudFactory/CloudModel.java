/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.cloudFactory;

import edu.kit.cloudSimStorage.CdmiCloudCharacteristics;
import edu.kit.cloudSimStorage.monitoring.IUsageHistory;
import edu.kit.cloudSimStorage.storageModel.resourceUtilization.TimeawareResourceLimitation;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Default;

import java.util.List;

/**
 * Models a cloud that can be serialized to XML.
 *
 * Use {@link edu.kit.cloudSimStorage.cloudFactory.StorageCloudFactory} to generate a {@link edu.kit.cloudSimStorage.StorageCloud} instance from a given model or to generate the model itself.
 *
 * @author Tobias Sturm, 8/6/13 4:15 PM */
@Default
public class CloudModel {
	@Attribute
	public String name;

	@Attribute
	public String location;
	public CdmiCloudCharacteristics characteristics;

	@Attribute
	public String rootUrl;
	public IUsageHistory pricingPolicy;
	public List<ObjectStorageServerModel> servers;
	public TimeawareResourceLimitation cloudIOLimits;
}

