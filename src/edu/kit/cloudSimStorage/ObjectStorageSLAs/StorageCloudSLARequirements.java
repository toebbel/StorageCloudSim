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

import edu.kit.cloudSimStorage.CdmiCloudCharacteristics;
import edu.kit.cloudSimStorage.ObjectStorageSLAs.matchingSLA.*;
import edu.kit.cloudSimStorage.ObjectStorageSLAs.ratingSLA.RakingSum;
import edu.kit.cloudSimStorage.ObjectStorageSLAs.ratingSLA.RateByExportCapabilities;
import edu.kit.cloudSimStorage.ObjectStorageSLAs.ratingSLA.RateByPrice;
import edu.kit.cloudSimStorage.ObjectStorageSLAs.ratingSLA.SLARating;
import edu.kit.cloudSimStorage.monitoring.ILoggable;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static edu.kit.cloudSimStorage.cdmi.CdmiMetadata.LOCATION;
import static edu.kit.cloudSimStorage.cdmi.CdmiMetadata.MAX_OBJECT_SIZE;

/**
 * Models a set of {@link edu.kit.cloudSimStorage.ObjectStorageSLAs.matchingSLA.SLARequirement}s and {@link edu.kit.cloudSimStorage.ObjectStorageSLAs.ratingSLA.SLARating}. The requirements are then attached to a {@link edu.kit.cloudSimStorage.UsageSequence} to let the {@link edu.kit.cloudSimStorage.cloudBroker.StorageMetaBroker} determine the {@link edu.kit.cloudSimStorage.StorageCloud} that suits the request the best.
 *
 * This class implements the Builder design pattern.
 *
 * @author Tobias Sturm, 6/26/13 5:05 PM */
@Root
public class StorageCloudSLARequirements implements ILoggable {
	@Element(required=false)
	SLARequirement requirements;

	@Element(required=false)
	SLARating ratings;
	Logger logger;

	public StorageCloudSLARequirements() {
		requirements = null;
		ratings = null;
		logger = Logger.getLogger("SLARequest" + java.util.UUID.randomUUID().toString());
	}

	/**
	 * Adds a custom requirement.
	 * @param req requirement that is checked against candidates when calling {@link #getMatches(java.util.List)}
	 * @return the instance itself
	 */
	public StorageCloudSLARequirements addRequirement(SLARequirement req) {
		if(requirements == null)
			requirements = req;
		else
			requirements = new SLARequirementAND(requirements, req);

		return this;
	}

	/**
	 * Adds a custom rating policy
	 * @param rater rating policy that is used to calculate a score for every candidate when calling {@link #getMatches(java.util.List)}.
	 *
	 * If there are other rating policies, the sum of all with be taken into account.
	 *
	 * @return the instance itself.
	 */
	public StorageCloudSLARequirements addRating(SLARating rater) {
		if(ratings == null)
			ratings = rater;
		else
			ratings = new RakingSum(rater, ratings);

		return this;
	}

	/**
	 * Returns all characteristics that mached all {@link edu.kit.cloudSimStorage.ObjectStorageSLAs.matchingSLA.SLARequirement}s. The list is sorted by the score provided by all  given {@link edu.kit.cloudSimStorage.ObjectStorageSLAs.ratingSLA.SLARating} descending.
	 * @param in all candidates
	 * @return all matched candidates, sorted desc by their score
	 */
	public List<CdmiCloudCharacteristics> getMatches(List<CdmiCloudCharacteristics> in) {
		logger.fine("matching characteristics against requirement: " + requirements);
		//create a list <Rating, Chara> for all characteristics, that match all requirements
		List<RatingEntry> ranking = new ArrayList<>();
		for(CdmiCloudCharacteristics c : in) {
			if(requirements == null || requirements.match(c))
				ranking.add(new RatingEntry(0, c));
		}

		logger.fine("raking characteristics against ratings: " + ratings);
		//rank all remaining characteristics
		if(ratings != null) {
			for(RatingEntry rankEntry : ranking) {
				rankEntry.score += ratings.score(rankEntry.characteristics);
			}
		}

		//sort them according to their score
		Collections.sort(ranking);
		Collections.reverse(ranking);

		//unpack characteristics
		int counter = 1;
		List<CdmiCloudCharacteristics> tmp = new ArrayList<>();
		for(RatingEntry entry : ranking) {
			logger.fine(counter + ") " + entry.characteristics.get(CdmiCloudCharacteristics.PROVIDER_NAME));
			tmp.add(entry.characteristics);
		}
		return tmp;
	}

	/**
	 * Requires cloud candidates to have a bandwidth that is at least X.
	 * @param minBandwidth minimal accepted bandwidth
	 * @return the instance itself
	 */
	public StorageCloudSLARequirements minBandiwdth(double minBandwidth) {
		addRequirement(new MinimumCharactersisticValue(CdmiCloudCharacteristics.MIN_BANDWIDTH, minBandwidth));
		return this;
	}

	/**
	 * Requires clouds' latency (in ms) to be lower than X
	 * @param maxLatency max. allowed latency
	 * @return the instance itself
	 */
	public StorageCloudSLARequirements maxLatency(double maxLatency) {
		addRequirement(new MaximumCharacteristicsValue(CdmiCloudCharacteristics.MIN_BANDWIDTH, maxLatency, true));
		return this;
	}

	/**
	 * Requires clouds' prices per stored GB (in cents) to be lower than X
	 * @param maxStorageCost max accepted price per stored GB in X
	 * @return the instance itself
	 */
	public StorageCloudSLARequirements maxStorageCost(double maxStorageCost) {
		addRequirement(new MaximumCharacteristicsValue(CdmiCloudCharacteristics.MAX_LATENCY, maxStorageCost, true));
		return this;
	}

	/**
	 * Requires clouds' prices per uploaded GB (in cents) to be lower than X
	 * @param maxStorageCost max accepted price per uploaded GB in X
	 * @return the instance itself
	 */
	public StorageCloudSLARequirements maxUploadCost(double maxStorageCost) {
		addRequirement(new MaximumCharacteristicsValue(CdmiCloudCharacteristics.UPLOAD_COSTS, maxStorageCost, true));
		return this;
	}

	/**
	 * Requires clouds' prices per downloaded GB (in cents) to be lower than X
	 * @param maxStorageCost max accepted price per downloaded GB in X
	 * @return the instance itself.
	 */
	public StorageCloudSLARequirements maxDownloadCost(double maxStorageCost) {
		addRequirement(new MaximumCharacteristicsValue(CdmiCloudCharacteristics.DOWNLOAD_COSTS, maxStorageCost, true));
		return this;
	}

	/**
	 * Requires the clouds to have at least the given amount of capacity available.
	 *
	 * @param capacity required capacity (in bytes)
	 * @return the instance itself.
	 */
	public StorageCloudSLARequirements minCapacity(long capacity) {
		addRequirement(new MinimumCharactersisticValue(CdmiCloudCharacteristics.AVAILABLE_CAPACITY, capacity));
		return this;
	}

	/**
	 * Requires the cloud to either have no container size limitation or have a limit that is greater than X
	 * @param capacity the minimum accepted container size limitation
	 * @return the instance itself
	 */
	public StorageCloudSLARequirements maxContainerSizeAtLeast(long capacity) {
		addRequirement(new SLARequirementOR(
				new DoesNotSupportCapability(CdmiCloudCharacteristics.MAX_CONTAINER_SIZE),
				new MinimumCharactersisticValue(CdmiCloudCharacteristics.MAX_CONTAINER_SIZE, capacity)
		));
		return this;
	}

	/**
	 * Requires the cloud to have no limitation on container sizes
	 * @return the instance itself.
	 */
	public StorageCloudSLARequirements hasNoContainerSizeLimit() {
		return maxContainerSizeAtLeast(Long.MAX_VALUE);
	}

	/**
	 * Requires the cloud to either have no object size limitation or have a limit that is greater than X
	 * @param capacity the minimum accepted object size limitation
	 * @return the instance itself
	 */
	public StorageCloudSLARequirements maxObjectSizeAtLeast(long capacity) {
		addRequirement(new SLARequirementOR(
				new DoesNotSupportCapability(MAX_OBJECT_SIZE),
				new MinimumCharactersisticValue(MAX_OBJECT_SIZE, capacity)
		));
		return this;
	}

	/**
	 * Requires the cloud to be located in the given location.
	 * @param location some location (country code eG)
	 * @return the instance itself
	 */
	public StorageCloudSLARequirements locationIs(String location) {
		addRequirement(new CharacteristicMatchesString(LOCATION, location));
		return this;
	}

	/**
	 * Requires the cloud to be located in one of the given locations
	 * @param locations some locations (country code eG)
	 * @return the instance itself.
	 */
	public StorageCloudSLARequirements locationIsIn(List<String> locations) {
		if(locations.size() == 1)
			return locationIs(locations.get(0));
		addRequirement(locationIsIn(locations.subList(1, locations.size() - 1), new CharacteristicMatchesString(LOCATION, locations.get(0))));
		return this;
	}

	private SLARequirement locationIsIn(List<String> locations, SLARequirement prev) {
		SLARequirement tmp = new SLARequirementOR(prev, new CharacteristicMatchesString(LOCATION, locations.get(0)));
		locations.remove(0);
		if(locations.isEmpty())
			return tmp;
		return locationIsIn(locations, tmp);
	}

	/**
	 * Requires the cloud to have no limitation on object sizes
	 * @return the instance itself
	 */
	public StorageCloudSLARequirements hasNoObjectSizeLimit() {
		addRequirement(new SLARequirementOR(
				new DoesNotSupportCapability(MAX_OBJECT_SIZE),
				new MinimumCharactersisticValue(MAX_OBJECT_SIZE, Long.MAX_VALUE)
		));
		return this;
	}

	/**
	 * Requires clouds to allow user to create containers
	 * @return the instance itself
	 */
	public StorageCloudSLARequirements canCreateContainers() {
		addRequirement(new SupportsCapability(CdmiCloudCharacteristics.CAPABILITY_CREATE_CONTAINER));
		return this;
	}

	/**
	 * Requires clouds to allow users to delete containers
	 * @return the instance itself
	 */
	public StorageCloudSLARequirements canDeleteContainers() {
		addRequirement(new SupportsCapability(CdmiCloudCharacteristics.CAPABILITY_DELETE_CONTAINER));
		return this;
	}

	/**
	 * Requires clouds to allow users to modify the user-part of metadata
	 * @return the instance itself
	 */
	public StorageCloudSLARequirements canModifyMetadata() {
		addRequirement(new SLARequirementAND(
				new SupportsCapability(CdmiCloudCharacteristics.CAPABILITY_READ_METADATA),
				new SupportsCapability(CdmiCloudCharacteristics.CAPABILITY_MOD_METADATA)
		));
		return this;
	}

	/**
	 * Rate Clouds by their prices.
	 *
	 * See {@link edu.kit.cloudSimStorage.ObjectStorageSLAs.ratingSLA.RateByPrice} for details.
	 * @return the instance itself
	 */
	public StorageCloudSLARequirements rateByPrice() {
		addRating(new RateByPrice());
		return this;
	}

	/**
	 * Rates clouds by their capabilities
	 *
	 * See {@link edu.kit.cloudSimStorage.ObjectStorageSLAs.ratingSLA.RateByExportCapabilities}
	 * @return
	 */
	public StorageCloudSLARequirements rateByExportCapabilities() {
		addRating(new RateByExportCapabilities());
		return this;
	}

	/**
	 * Saves the SLA requirements as XML file
	 * @param out output stream
	 * @throws Exception if something goes wrong :P
	 */
	public void serialize(OutputStream out) throws Exception {
		Serializer serializer = new Persister();
		serializer.write(this, out);
	}

	/**
	 * Creats the SLA requirements from XML file
	 * @param in the stream to red the XML from
	 * @return the instance that was created from XML
	 * @throws Exception if something goes wrong :P
	 */
	public static StorageCloudSLARequirements deserializer(InputStream in) throws Exception {
		Serializer serializer = new Persister();
		return serializer.read(StorageCloudSLARequirements.class, in);
	}

	@Override
	public String toString() {
		return "select (" + requirements + "), order by (" + ratings + ")";
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.toString().equals(toString());
		/*return obj.getClass() == this.getClass() &&
				(requirements == null && ((StorageCloudSLARequirements)obj).requirements == null ||
					requirements.equals(((StorageCloudSLARequirements)obj).requirements)) &&
				(ratings == null && ((StorageCloudSLARequirements)obj).ratings == null ||
						ratings.equals(((StorageCloudSLARequirements)obj).ratings));*/
	}

	/**
	 * Clones the SLA requirements
	 * @return a exact deep copy of the requirements
	 * @throws CloneNotSupportedException
	 */
	public StorageCloudSLARequirements clone() throws CloneNotSupportedException {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			this.serialize(outputStream);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toString().getBytes());
			return StorageCloudSLARequirements.deserializer(inputStream);
		} catch (Exception e) {
			throw new IllegalStateException("cloud not clone SLA request due to IO exception: " + e);
		}
	}
}

/**
 * Used to link characteristics with their score
 */
class RatingEntry implements Comparable<RatingEntry> {
	public int score;
	public CdmiCloudCharacteristics characteristics;

	RatingEntry(int score, CdmiCloudCharacteristics characteristics) {
		this.score = score;
		this.characteristics = characteristics;
	}

	@Override
	public int compareTo(RatingEntry o) {
		return score - o.score;
	}
}






