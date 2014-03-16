/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.pricing;

import edu.kit.cloudSimStorage.helper.FileSizeHelper;
import edu.kit.cloudSimStorage.cdmi.CdmiOperationVerbs;
import edu.kit.cloudSimStorage.monitoring.IUsageHistory;
import edu.kit.cloudSimStorage.monitoring.UsageHistory;
import org.simpleframework.xml.Root;

import static edu.kit.cloudSimStorage.helper.FileSizeHelper.Magnitude.*;
import static edu.kit.cloudSimStorage.helper.FileSizeHelper.fromBytes;
import static edu.kit.cloudSimStorage.helper.FileSizeHelper.toBytes;

/** @author Tobias Sturm, 6/19/13 2:56 PM */
@Root
public class AmazonUS extends UsageHistory {
	private long downloadedData = 0;
	private int remainingGETRequests = 0;
	private int remainingOtherRequests = 0;
	private long usedSpace;
	private boolean forceNextUsedSpace;


	public AmazonUS() {super();};

	protected AmazonUS(int userID) {
		super(userID);
	}

	@Override
	public void DownloadTraffic(long size) {
		downloadedData += size;
		if (downloadedData > toBytes(1, TERA_BYTE) && downloadedData < toBytes(10, TERA_BYTE)) {
			debts += fromBytes(size, GIGA_BYTE) * 0.12;
		} else if (downloadedData >= toBytes(10, TERA_BYTE) && downloadedData < toBytes(50, TERA_BYTE)) {
			debts += fromBytes(size, GIGA_BYTE) * 0.09;
		} else if (downloadedData >= toBytes(50, TERA_BYTE) && downloadedData < toBytes(150, TERA_BYTE)) {
			debts += fromBytes(size, GIGA_BYTE) * 0.07;
		} else if (downloadedData >= toBytes(150, TERA_BYTE)) {
			debts += fromBytes(size, GIGA_BYTE) * 0.05;
		}
	}

	@Override
	public void UploadTraffic(long size) {
		//for free
		super.UploadTraffic(size);
	}

	@Override
	public void query(CdmiOperationVerbs verb) {
		switch (verb) {
			case GET:
				if (remainingGETRequests <= 0) {
					remainingGETRequests += 10000;
					debts += 0.004;
				}
				break;
			case DELETE:
				break; //for free
			default:
				if (remainingOtherRequests <= 0) {
					remainingOtherRequests += 1000;
					debts += 0.005;
				}
		}
		super.query(verb);
	}

	@Override
	public void updateCurrentlyUsedSpace(long size) {
		if (usedSpace < size || forceNextUsedSpace)
			usedSpace = size;
		super.updateCurrentlyUsedSpace(size);
	}

	@Override
	public void endAccountingPeriod() {
		forceNextUsedSpace = true;
		debts = 0;
		super.endAccountingPeriod();
	}

	@Override
	public double getDebts() {
		return (int) (debts + priceForStorage(usedSpace));
	}

	@Override
	public IUsageHistory clone(int userID) {
		return new AmazonUS(userID);
	}

	private double priceForStorage(long usedSpace) {
		long[] cuts = {0, FileSizeHelper.toBytes(1, TERA_BYTE), FileSizeHelper.toBytes(50, TERA_BYTE), FileSizeHelper.toBytes(500, TERA_BYTE), FileSizeHelper.toBytes(1, PETA_BYTE), FileSizeHelper.toBytes(4, PETA_BYTE)};
		double[] prices = {0, 0.095, 0.08, 0.07, 0.065, 0.6};

		int i = 1;
		double result = 0;
		long calculatedVolume;

		while (usedSpace > cuts[i - 1]) {
			calculatedVolume = Math.min(usedSpace, cuts[i]);
			result += prices[i] * calculatedVolume;
			i++;
		}

		return result;
	}
}
