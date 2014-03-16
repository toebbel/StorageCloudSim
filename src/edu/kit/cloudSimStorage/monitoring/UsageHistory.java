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
import edu.kit.cloudSimStorage.helper.TimeHelper;
import edu.kit.cloudSimStorage.helper.TupleSequence;
import edu.kit.cloudSimStorage.monitoring.sampleSequenceOperators.SampleCombinator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** @author Tobias Sturm, 6/24/13 6:16 PM */
public abstract class UsageHistory implements IUsageHistory {

	protected double debts;
	ResourceUsageHistory debtHistory;
	ResourceUsageHistory uploadTraffic;
	ResourceUsageHistory downloadTraffic;
	EventTracker<Integer> numGETQueries, numPUTQueries, numDELETEQueries, numLISTQueries;

	public UsageHistory(){};

	protected UsageHistory(int userID) {
		debtHistory = new ResourceUsageHistory(DEBTS, "Debts of user " + userID, "total debts", "CENTS");
		uploadTraffic = new ResourceUsageHistory(TRAFFIC, "upload traffic of user " + userID, "traffic in current month", "BYTE");
		downloadTraffic = new ResourceUsageHistory(TRAFFIC, "download traffic of user " + userID, "traffic in current month","BYTE");
		numGETQueries = new EventTracker<>("GET-Queries", "GET-Queries of user " + userID);
		numPUTQueries = new EventTracker<>("GET-Queries", "GET-Queries of user " + userID);
		numDELETEQueries = new EventTracker<>("GET-Queries", "GET-Queries of user " + userID);
		numLISTQueries = new EventTracker<>("GET-Queries", "GET-Queries of user " + userID);
		debts = 0;
	}

	@Override
	public TupleSequence<Double> getSamples(String key) {
		List<TupleSequence<Double>> sampleStreams = new ArrayList<>();
		switch (key) {
			case DEBTS:
				TupleSequence<Double> tmp = debtHistory.getSamples(key);
				Collections.sort(tmp);
				while(!tmp.isEmpty() && tmp.get(tmp.size() - 1).y < 0.00001)
					tmp.remove(tmp.size() - 1);
				return tmp;
			case TRAFFIC_DOWNLOAD:
				return downloadTraffic.getSamples();
			case TRAFFIC_UPLOAD:
				return uploadTraffic.getSamples();
			case TRAFFIC:
				sampleStreams.add(getSamples(TRAFFIC_DOWNLOAD));
				sampleStreams.add(getSamples(TRAFFIC_UPLOAD));
				return SampleCombinator.sum(sampleStreams);
			case NUM_REQUESTS:
				sampleStreams.add(numLISTQueries.getSamples());
			case NUM_REQUESTS_OTHER:
				sampleStreams.add(numPUTQueries.getSamples());
				sampleStreams.add(numGETQueries.getSamples());
				sampleStreams.add(numDELETEQueries.getSamples());
				return SampleCombinator.sum(sampleStreams);
			case NUM_REQUESTS_PER_MINUTE:
			case NUM_REQUESTS_PER_SECOND:
				sampleStreams.add(numLISTQueries.getSamples());
			case NUM_REQUESTS_OTHER_PER_MINUTE:
			case NUM_REQUESTS_OTHER_PER_SECOND:
				sampleStreams.add(numPUTQueries.getSamples());
				sampleStreams.add(numGETQueries.getSamples());
				sampleStreams.add(numDELETEQueries.getSamples());
				int time = (key == NUM_REQUESTS_OTHER_PER_MINUTE || key == NUM_REQUESTS_PER_MINUTE) ? 60 * 1000 : 1000;
				return SampleCombinator.samplesPerTime(time, SampleCombinator.flatten(sampleStreams));
			case NUM_REQUESTS_LIST:
				return numLISTQueries.getSamples();
			case NUM_REQUESTS_LIST_PER_MINUTE:
				return SampleCombinator.samplesPerTime(60 * 1000, numLISTQueries.getSamples());
			case NUM_REQUESTS_LIST_PER_SECOND:
				return SampleCombinator.samplesPerTime(1000, numLISTQueries.getSamples());
		}
		return null;
	}

	@Override
	public String[] getAvailableTrackingKeys() {
		return new String[] {DEBTS, TRAFFIC, TRAFFIC_DOWNLOAD, TRAFFIC_UPLOAD, NUM_REQUESTS_LIST, NUM_REQUESTS_OTHER, NUM_REQUESTS, NUM_REQUESTS_PER_MINUTE, NUM_REQUESTS_PER_SECOND, NUM_REQUESTS_OTHER_PER_MINUTE, NUM_REQUESTS_OTHER_PER_SECOND, NUM_REQUESTS_LIST_PER_MINUTE, NUM_REQUESTS_LIST_PER_SECOND};
	}


	@Override
	public void DownloadTraffic(long size) {
		debtHistory.addSample(TimeHelper.getInstance().now(), getDebts());
		downloadTraffic.addDiff(size);
	}

	@Override
	public void UploadTraffic(long size) {
		debtHistory.addSample(TimeHelper.getInstance().now(), getDebts());
		uploadTraffic.addDiff(size);
	}

	@Override
	public void query(CdmiOperationVerbs verb) {
		debtHistory.addSample(TimeHelper.getInstance().now(), getDebts());
		switch (verb) {
			case DELETE:numDELETEQueries.addEvent(1); break;
			case GET:numGETQueries.addEvent(1);break;
			case PUT:numPUTQueries.addEvent(1);break;
		}
	}

	@Override
	public void updateCurrentlyUsedSpace(long size) {
		debtHistory.addSample(TimeHelper.getInstance().now(), getDebts());
	}

	@Override
	public void endAccountingPeriod() {
		debtHistory.addSample(TimeHelper.getInstance().now(), getDebts());
	}
}
