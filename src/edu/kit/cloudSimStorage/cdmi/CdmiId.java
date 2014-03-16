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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

/**
 * Represents a ID, that is used in CDMI-systems.
 * <p/>
 * IDs are unique within one rootURI. RootURIs are not case-sensitive, but are just treated as Strings in this class (due to laziness):
 * {@code 'http://domain.com' != 'http://domain.com/'}
 * <p/>
 * IDs can be generated. This class ensures, that generated IDs are unique within one rootURI.
 * IDs can be built from Strings, but these instances are not managed, which means, that collisions with generated IDs are
 * possible.
 * <p/>
 * The {@link CdmiId#UNKNOWN} instance can be used to indicate, that some {@link CdmiEntity} does not have an ID yet.
 * <p/>
 * Two IDs are equal, if their string-representation (case-sensitive) are equal.
 * <p/>
 * @author Tobias Sturm
 * Date: 4/26/13
 * Time: 12:51 PM
 */
public class CdmiId {
	/** max. number of tries to generate a unique ID within a rootURI */
	private static final int MAX_TRIES = 1000;

	/** the ID, which indicates that the ID is unknown (-> use name of {@link CdmiEntity}) */
	public static final CdmiId UNKNOWN = new CdmiId("UNKNOWN");

	/** All used IDs per rootUri */
	private static Hashtable<String, List<String>> usedIds = new Hashtable<>();

	/** Allowed characters in IDs */
	private final static String validCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	/** length of IDs to be generated */
	private final static int ID_LENGTH = 10;

	private String id;

	/**
	 * Generate an ID from string.
	 * <p/>
	 * The ID can't be empty, must contain {@link CdmiId#validCharacters} only and must not be the {@link CdmiId#UNKNOWN}.
	 * <p/>
	 * !This ID is not managed by the system. Collisions are possible.
	 *
	 * @param id value of the ID, non-empty. Contains only valid characters, does not represent UNKNOWN id
	 */
	public CdmiId(String id) {
		if (id == null || id.trim().isEmpty())
			throw new IllegalArgumentException("id is empty");
		for (char c : id.toCharArray())
			if (!validCharacters.contains(String.valueOf(c)))
				throw new IllegalArgumentException("id contains illegal character: '" + c + "'");
		if (UNKNOWN != null && UNKNOWN.toString().equals(id))
			throw new IllegalArgumentException("Can't create the UNKNOWN id");
		this.id = id;
	}


	/**
	 * Generates an ID which is unique inside the given rootURI.
	 *
	 * @param rootURI rootURI (not case-sensitive)
	 * @return a unique ID
	 */
	public static CdmiId generateId(String rootURI) {
		if (!usedIds.containsKey(rootURI.toLowerCase()))
			usedIds.put(rootURI.toLowerCase(), new ArrayList<String>());
		String candidate = generateRandomString();
		int tries = 0;
		while (usedIds.get(rootURI.toLowerCase()).contains(candidate)) {
			candidate = generateRandomString();
			if (tries++ > MAX_TRIES)
				throw new IllegalStateException("Can't find a random ID for rootURI " + rootURI);
		}
		return new CdmiId(candidate);
	}

	private static String generateRandomString() {
		StringBuilder b = new StringBuilder(ID_LENGTH);
		Random rnd = new Random();
		for (int i = 0; i < ID_LENGTH; i++)
			b.append(validCharacters.charAt(rnd.nextInt(ID_LENGTH)));
		return b.toString();
	}

	public String toString() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(this.getClass()))
			return false;
		return obj.toString().equals(toString());
	}
}
