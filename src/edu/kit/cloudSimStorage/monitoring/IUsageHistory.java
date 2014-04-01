/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.monitoring;

import edu.kit.cloudSimStorage.cdmi.CdmiOperationVerbs;

/** @author Tobias Sturm, 6/19/13 2:38 PM */
public interface IUsageHistory extends TraceableResource {
	/**
	 * Call for every download operation
	 *
	 * @param size in byte
	 */
	void DownloadTraffic(long size);

	/**
	 * Call for every upload operation
	 *
	 * @param size in byte
	 */
	void UploadTraffic(long size);


	/**
	 * Process a query (like LIST, GET, ...)
	 *
	 * @param verb
	 */
	void query(CdmiOperationVerbs verb);

	/**
	 * Call this method whenever the currently used size has changed
	 *
	 * @param size
	 */
	void updateCurrentlyUsedSpace(long size);

	/** Lets a new accounting month begin */
	void endAccountingPeriod();

	/**
	 * The total debts int cents
	 *
	 * @return the debts in cents
	 */
	double getDebts();

	IUsageHistory clone(int userID);
}
