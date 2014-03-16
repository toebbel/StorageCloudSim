/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.helper;

/** @author Tobias Sturm, 6/19/13 9:12 AM */
public class FileSizeHelper {

	public static long toBytes(double val, Magnitude m) {
		switch (m) {
			case BYTE:
				return (long) val;
			case KILO_BYTE:
				return (long) (val * 1024);
			case MEGA_BYTE:
				return (long) (val * 1024 * 1024);
			case GIGA_BYTE:
				return (long) (val * 1024 * 1024 * 1024);
			case TERA_BYTE:
				return (long) (val * 1024 * 1024 * 1024 * 1024);
			case PETA_BYTE:
				return (long) (val * 1024 * 1024 * 1024 * 1024 * 1024);
		}
		return 0;
	}

	public static String toHumanReadable(long val) {
		return toHumanReadable(val, Magnitude.BYTE, 3);
	}

	public static String toHumanReadable(long val, Magnitude m, int precision) {
		double tmp = val;
		while (hasNext(m) && ((long) tmp) / 1024 > 0) {
			m = next(m);
			tmp = tmp / 1024;
		}
		java.text.NumberFormat format = java.text.NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(precision);
		return format.format(tmp) + toString(m);
	}

	private static String toString(Magnitude m) {
		switch (m) {
			case BYTE:
				return "B";
			case KILO_BYTE:
				return "KB";
			case MEGA_BYTE:
				return "MB";
			case GIGA_BYTE:
				return "GB";
			case TERA_BYTE:
				return "TB";
			case PETA_BYTE:
				return "PB";
		}
		return "?";
	}

	private static Magnitude next(Magnitude m) {
		switch (m) {
			case BYTE:
				return Magnitude.KILO_BYTE;
			case KILO_BYTE:
				return Magnitude.MEGA_BYTE;
			case MEGA_BYTE:
				return Magnitude.GIGA_BYTE;
			case GIGA_BYTE:
				return Magnitude.TERA_BYTE;
			case TERA_BYTE:
				return Magnitude.PETA_BYTE;
		}
		assert false;
		return null;
	}

	private static boolean hasNext(Magnitude m) {
		return m != Magnitude.PETA_BYTE;
	}

	public static double fromBytes(long size, Magnitude m) {
		switch (m) {
			case BYTE:
				return size;
			case KILO_BYTE:
				return size / 1024.0;
			case MEGA_BYTE:
				return size / 1024.0 / 1024.0;
			case GIGA_BYTE:
				return size / 1024.0 / 1024.0 / 1024.0;
			case TERA_BYTE:
				return size / 1024.0 / 1024.0 / 1024.0 / 1024.0;
			case PETA_BYTE:
				return size / 1024.0 / 1024.0 / 1024.0 / 1024.0;
		}
		return -1;
	}


	public static enum Magnitude {
		BYTE,
		KILO_BYTE,
		MEGA_BYTE,
		GIGA_BYTE,
		TERA_BYTE,
		PETA_BYTE
	}
}
