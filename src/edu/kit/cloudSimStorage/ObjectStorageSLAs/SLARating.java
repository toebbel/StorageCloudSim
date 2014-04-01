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
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * A SLARating assigns a score to a given {@link edu.kit.cloudSimStorage.CdmiCloudCharacteristics} instance.
 *
 * If a {@link edu.kit.cloudSimStorage.cloudBroker.StorageMetaBroker} must choose one {@link edu.kit.cloudSimStorage.StorageCloud} amongst different Clouds, and more than one cloud matches all hard SLAs ({@link edu.kit.cloudSimStorage.ObjectStorageSLAs.matchingSLA.SLARequirement}), one Cloud must be chosen amongst them. Therefore a score is calculated for every cloud, using one or more instances of this class. The cloud with the higest score is then used to dispatch the {@link edu.kit.cloudSimStorage.UsageSequence}.
 */
@Root
public abstract class SLARating {

	@Element(name = "description", required = false)
	protected String description;

	/**
	 * Calculates a score for a candidate, based on a it's characteristics
	 *
	 * @param candidate the candidate to evaluate
	 * @return score for the candidate.
	 */
	public abstract double score(CdmiCloudCharacteristics candidate);

	@Override
	public String toString() {
		return description;
	}
}
