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
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import static edu.kit.cloudSimStorage.helper.FileSizeHelper.fromBytes;

/** @author Tobias Sturm, 6/19/13 2:48 PM */
@Root
public class SimplePricing extends UsageHistory {
	@Element(name = "centsPerUploadedGB")
	double centsPerGBUpload = 0.1;
	@Element(name = "centsPerDownloadedGB")
	double centsPerGBDownload = 0.2;
	@Element(name = "centsPerStoredGBperPeriod")
	double centsPerStoredGBPerPeriod = 0.095;

	long maxUsedSize = 0;
	private boolean forceNextSpaceUpdate;

	public SimplePricing() {super();};

	public SimplePricing(@Element(name = "centsPerUploadedGB")double up, @Element(name = "centsPerDownloadedGB")double down, @Element(name = "centsPerStoredGBperPeriod")double stored) {
		super();
		centsPerGBUpload = up;
		centsPerGBDownload = down;
		centsPerStoredGBPerPeriod = stored;
	}

	public SimplePricing(int userID, double up, double down, double stored) {
		super(userID);
		centsPerGBUpload = up;
		centsPerGBDownload = down;
		centsPerStoredGBPerPeriod = stored;
	}

	protected SimplePricing(int userID) {
		super(userID);
	}

	@Override
	public void DownloadTraffic(long size) {
		debts += centsPerGBDownload * fromBytes(size, FileSizeHelper.Magnitude.GIGA_BYTE);
		super.DownloadTraffic(size);
	}

	@Override
	public void UploadTraffic(long size) {
		debts += centsPerGBUpload * fromBytes(size, FileSizeHelper.Magnitude.GIGA_BYTE);
		super.UploadTraffic(size);
	}

	@Override
	public void query(CdmiOperationVerbs verb) {
		//for free!
		super.query(verb);
	}

	@Override
	public void updateCurrentlyUsedSpace(long size) {
		if (size > maxUsedSize || forceNextSpaceUpdate)
			maxUsedSize = size;
		super.updateCurrentlyUsedSpace(size);
	}

	@Override
	public void endAccountingPeriod() {
		forceNextSpaceUpdate = true;
		debts = 0;
		super.endAccountingPeriod();
	}

	@Override
	public double getDebts() {
		return debts + (fromBytes(maxUsedSize, FileSizeHelper.Magnitude.GIGA_BYTE) * centsPerStoredGBPerPeriod);
	}

	@Override
	public IUsageHistory clone(int userID) {
		return new SimplePricing(userID, centsPerGBUpload, centsPerGBDownload, centsPerStoredGBPerPeriod);
	}
}
