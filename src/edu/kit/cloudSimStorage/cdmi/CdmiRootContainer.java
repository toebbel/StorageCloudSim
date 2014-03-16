/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.cdmi;

import edu.kit.cloudSimStorage.helper.FileSizeHelper;
import edu.kit.cloudSimStorage.exceptions.EntityNameException;
import edu.kit.cloudSimStorage.helper.TupleSequence;
import edu.kit.cloudSimStorage.monitoring.StorageUsageHistory;
import edu.kit.cloudSimStorage.monitoring.TrackableResource;
import edu.kit.cloudSimStorage.monitoring.sampleSequenceOperators.SampleCombinator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * User: Tobias Sturm
 * Date: 4/26/13
 * Time: 1:07 PM
 */
public class CdmiRootContainer extends CdmiContainer<CdmiObjectContainer> {
	protected HashMap<CdmiId, CdmiObjectContainer> objectContainerMapping;
	protected int user;

	/**
	 * Creates an entity within the given rootURI and creates a uniqueID ({@link edu.kit.cloudSimStorage.cdmi.CdmiId#generateId(String)}
	 *
	 * @param rootURI the URI of the StorageCloud
	 */
	public CdmiRootContainer(String rootURI, int user) {
		super(rootURI);
		objectContainerMapping = new HashMap<>();
		virtualStorageHistory = new StorageUsageHistory("virtual storage of " + rootURI + " " + user, FileSizeHelper.Magnitude.BYTE);
		virtualStorageHistory.setAvailableStorage(Long.MAX_VALUE);
	}

	@Override
	protected void init() {
		setEntityName("root " + user);
		super.init();
	}

	@Override
	public void renameChild(CdmiId id, String newName) {
		if (newName.trim().isEmpty())
			throw new EntityNameException("Can't create containers, with empty name");
		super.renameChild(id, newName);    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override
	public void Destroy() {
		for (CdmiEntity child : children.values())
			child.Destroy();
	}

	public void updateIdToObjectContainerMapping(CdmiId id, CdmiObjectContainer container) {
		objectContainerMapping.put(id, container);
	}

	public void deleteIdToObjectContainerMapping(CdmiId id) {
		objectContainerMapping.remove(id);
	}

	public boolean hasIdToContainerMapping(CdmiId id) {
		return objectContainerMapping.containsKey(id);
	}

	public CdmiObjectContainer getContainerOfObject(CdmiId id) {
		return objectContainerMapping.get(id);
	}

	@Override
	public TupleSequence<Double> getSamples(String key) {
		List<TupleSequence<Double>> sampleStreams = new ArrayList<>();
		switch (key) {
			case AVAILABLE_STORAGE_PHYICAL:
			case USED_STORAGE_PHYSICAL_ABS:
			case USED_STORAGE_VIRTUAL_ABS:
				for(TrackableResource c : getChildren().values())
					sampleStreams.add(c.getSamples(key));
				return SampleCombinator.sum(sampleStreams);
			case AVAILABLE_STORAGE_VIRTUAL:
				for(TrackableResource c : getChildren().values())
					sampleStreams.add(c.getSamples(key));
				sampleStreams.add(SampleCombinator.sum(sampleStreams));
				sampleStreams.add(virtualStorageHistory.getSamples(AVAILABLE_STORAGE));
				return SampleCombinator.min(sampleStreams);
		}
		return null;
	}

	@Override
	public String[] getAvailableTrackingKeys() {
		return new String[] {AVAILABLE_STORAGE_PHYICAL, USED_STORAGE_PHYSICAL_ABS, USED_STORAGE_VIRTUAL_ABS}; //TODO AVAILABLE_STORAGE_VIRTUAL
	}
}
