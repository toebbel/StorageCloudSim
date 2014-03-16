/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.cloudFactory.harddrives;

import edu.kit.cloudSimStorage.helper.FileSizeHelper;
import edu.kit.cloudSimStorage.storageModel.IObjectStorageDrive;
import edu.kit.cloudSimStorage.storageModel.ObjectStorageDrive;
import edu.kit.cloudSimStorage.storageModel.ObjectStorageServer;
import edu.kit.cloudSimStorage.storageModel.resourceUtilization.FirstFitAllocation;
import edu.kit.cloudSimStorage.storageModel.resourceUtilization.TimeawareResourceLimitation;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import static edu.kit.cloudSimStorage.helper.FileSizeHelper.Magnitude.MEGA_BYTE;
import static edu.kit.cloudSimStorage.helper.FileSizeHelper.toBytes;

/** @author Tobias Sturm, 6/7/13 1:31 PM */
@Root
public class SeagateST2000DM001_2TB extends ObjectStorageDrive {

	public SeagateST2000DM001_2TB(@Attribute(name = "name") String name) {
		super(name);
		init();
	}

	public SeagateST2000DM001_2TB(String rootUrl, ObjectStorageServer location, String name) {
		super(rootUrl, location, name);

		this.rootUrl = rootUrl;
		init();
	}

	public void init() {
		capacity = toBytes(2, FileSizeHelper.Magnitude.TERA_BYTE);
		readRate = writeRate = toBytes(156, MEGA_BYTE);
		readLatency = 8.5;
		writeLatency = 9.5;
		ioLimitation = FirstFitAllocation.create(210, MEGA_BYTE);
	}

	@Override
	public IObjectStorageDrive clone(ObjectStorageServer location, String name) {
		return new SeagateST2000DM001_2TB(rootUrl, location, name);
	}


}
