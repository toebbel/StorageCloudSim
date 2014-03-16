/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.exceptions;

/**
 * @author Tobias Sturm
 *         created at 5/6/13, 4:48 PM
 */
public class EntityAlreadyExistsException extends RuntimeException {
	public EntityAlreadyExistsException(String msg) {
		super(msg);
	}
}
