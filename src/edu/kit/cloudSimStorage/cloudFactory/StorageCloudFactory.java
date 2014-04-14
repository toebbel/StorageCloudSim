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
import edu.kit.cloudSimStorage.StorageCloud;
import edu.kit.cloudSimStorage.cdmi.CdmiMetadata;
import edu.kit.cloudSimStorage.helper.FileSizeHelper;
import edu.kit.cloudSimStorage.pricing.SimplePricing;
import edu.kit.cloudSimStorage.storageModel.IObjectStorageDrive;
import edu.kit.cloudSimStorage.storageModel.ObjectStorageServer;
import edu.kit.cloudSimStorage.storageModel.resourceUtilization.FirstFitAllocation;
import edu.kit.cloudSimStorage.storageModel.resourceUtilization.UnlimitedResource;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/** @author Tobias Sturm, 6/7/13 1:28 PM */
public class StorageCloudFactory {

	/**
	 * Creates a {@link edu.kit.cloudSimStorage.StorageCloud instance from a model}
	 * @param model existing cloud model
	 * @return deep copy of the model
	 */
	public static StorageCloud createCloud(CloudModel model) {
		assert model.characteristics != null;
		assert model.cloudIOLimits != null;
		assert model.pricingPolicy != null;
		assert model.servers != null && model.servers.size() > 0;

		StorageCloud cloud = new StorageCloud(model.name, model.characteristics, model.location, model.rootUrl, model.pricingPolicy, model.cloudIOLimits);

		for(ObjectStorageServerModel server : model.servers) {
			ObjectStorageServer currentServer = new ObjectStorageServer(model.rootUrl, server.name, server.ioLimitations);
			for(ObjectStorageDiskModel disk : server.disks) {
				currentServer.installHarddrive(disk.drive.clone(currentServer, disk.name));
			}
			cloud.installServer(currentServer);
		}
		return cloud;
	}

	/**
	 * Creates a model from the given parameters.
	 * @param cloudName name of the cloud
	 * @param rootUrl rootUrl of the cloud
	 * @param location location of the cloud (eg "de" or "us-east1")
	 * @param numServers number of servers in the cloud
	 * @param numHddsPerServer number of disks per server
	 * @param exampleDrive instance of a drive that will be used in each server
	 * @param upCost costs per uploaded GB in $
	 * @param downCost costs per downloaded GB in $
	 * @param storeCost costs per stored GB $ per time frame
	 * @param MBperSecondThroughputPerServer throughput of each server in MB/s (inter-cloud connectivity)
	 * @return model instance
	 */
	public static CloudModel createModel(String cloudName, String rootUrl, String location, int numServers, int numHddsPerServer, IObjectStorageDrive exampleDrive, double upCost, double downCost, double storeCost, int MBperSecondThroughputPerServer) {

		CdmiCloudCharacteristics cloudCharacteristics = CdmiCloudCharacteristics.getDefault();
		cloudCharacteristics.set(CdmiMetadata.LOCATION, location);
		cloudCharacteristics.setDownloadCosts(downCost).setUploadCosts(upCost).setStorageCosts(storeCost);

		CloudModel model = new CloudModel();
		model.rootUrl = rootUrl;
		model.name = cloudName;
		model.location = location;
		model.characteristics = cloudCharacteristics;
		model.pricingPolicy = new SimplePricing(upCost, downCost, storeCost);
		model.cloudIOLimits = new UnlimitedResource();
		model.servers = new ArrayList<>();

		for (int numServer = 0; numServer < numServers; numServer++) {
			ObjectStorageServerModel currentServer = new ObjectStorageServerModel();
			currentServer.ioLimitations = FirstFitAllocation.create(MBperSecondThroughputPerServer, FileSizeHelper.Magnitude.MEGA_BYTE);
			currentServer.name = "server" + numServer;
			currentServer.disks = new ArrayList<>();

			for (int numDrive = 0; numDrive < numHddsPerServer; numDrive++) {
				ObjectStorageDiskModel diskModel = new ObjectStorageDiskModel();
				diskModel.name = "/dev/sda" + numDrive;
				diskModel.drive = exampleDrive.clone(null, diskModel.name);
				currentServer.disks.add(diskModel);
			}
			model.servers.add(currentServer);
		}

		return model;
	}

	public static void serializeCloudModel(CloudModel m, OutputStream out) throws Exception {
		Serializer serializer = new Persister();
		serializer.write(m, out);
	}

	public static CloudModel deserializeCloudModel(InputStream in) throws Exception {
		Serializer serializer = new Persister();
		return serializer.read(CloudModel.class, in);
	}

}
