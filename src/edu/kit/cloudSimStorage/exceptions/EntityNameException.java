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
 * Used to indicate, if a a name of an entity is invalid (illegal characters, duplicate, empty where it should not be, ...)
 *
 * @author Tobias Sturm
 *         created at 5/8/13, 10:24 AM
 */
public class EntityNameException extends RuntimeException {
	public EntityNameException(String s) {

	}
}
