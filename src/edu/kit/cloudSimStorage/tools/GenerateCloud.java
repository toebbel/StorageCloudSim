/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.tools;

import edu.kit.cloudSimStorage.cloudFactory.CloudModel;
import edu.kit.cloudSimStorage.cloudFactory.StorageCloudFactory;
import edu.kit.cloudSimStorage.cloudFactory.harddrives.GenericDrive;
import edu.kit.cloudSimStorage.storageModel.resourceUtilization.FirstFitAllocation;

import java.io.FileOutputStream;
import java.io.OutputStream;

import static edu.kit.cloudSimStorage.SharedConstants.*;
import static edu.kit.cloudSimStorage.helper.FileSizeHelper.Magnitude.*;
import static edu.kit.cloudSimStorage.helper.FileSizeHelper.toBytes;

/** @author Tobias Sturm, 8/6/13 6:58 PM */
public class GenerateCloud {
	public static void main(String[] args) throws Exception {
		OutputStream out = null;
		String cloudName = "default-cloud";
		String rootUrl = "cloud.org/";
		String location = "us";
		int servers = 1;
		int harddrivesPerServer = 3;
		int serverBandwidth = 1024 / 8; //Gbit/s
		double uploadPrice = 0.1;
		double downloadPrice = 0.1;
		double storagePrice = 0.1;

		double diskReadLacenty = 8.5;
		double diskWriteLacenty = 9.5;
		double diskWriteBandwidth = 210; //MB/s
		double diskReadBandwidth = 210; //MB/s
		double diskCapacity = 1024; //GB
		int diskTotalIOLimitation = 210; //in MB/s

		for(int i = 0; i < args.length; i++) {
			if((args.length >= i) && args[i].toLowerCase().equals("-n") || args[i].toLowerCase().equals("--name")) {
				cloudName = args[i+1];
				i++;
			} else if((args.length >= i) && args[i].toLowerCase().equals("-r") || args[i].toLowerCase().equals("--rooturl")) {
				rootUrl = args[i+1];
				i++;
			} else if((args.length >= i) && args[i].toLowerCase().equals("-l") || args[i].toLowerCase().equals("--location")) {
				location = args[i+1];
				i++;
			} else if((args.length >= i) && args[i].toLowerCase().equals("-s") || args[i].toLowerCase().equals("--servers")) {
				servers = Integer.parseInt(args[i+1]);
				i++;
			} else if((args.length >= i) && args[i].toLowerCase().equals("-d") || args[i].toLowerCase().equals("--disks")) {
				harddrivesPerServer = Integer.parseInt(args[i+1]);
				i++;
			} else if((args.length >= i) && args[i].toLowerCase().equals("-up") || args[i].toLowerCase().equals("--uploadprice")) {
				uploadPrice = Double.parseDouble(args[i + 1]);
				i++;
			} else if((args.length >= i) && args[i].toLowerCase().equals("-dp") || args[i].toLowerCase().equals("--downloadprice")) {
				downloadPrice = Double.parseDouble(args[i+1]);
				i++;
			} else if((args.length >= i) && args[i].toLowerCase().equals("-sp") || args[i].toLowerCase().equals("--storageprice")) {
				storagePrice = Double.parseDouble(args[i+1]);
				i++;
			} else if((args.length >= i) && args[i].toLowerCase().equals("-b") || args[i].toLowerCase().equals("--serverbandwidth")) {
				serverBandwidth = Integer.parseInt(args[i+1]);
				i++;
			} else if ((args.length >= i) && args[i].toLowerCase().equals("-hr") || args[i].toLowerCase().equals("--hddreadbandwidth")) {
				diskReadBandwidth = Integer.parseInt(args[i + 1]);
				i++;
			} else if ((args.length >= i) && args[i].toLowerCase().equals("-hw") || args[i].toLowerCase().equals("--hddwritebandwidth")) {
				diskWriteBandwidth = Integer.parseInt(args[i + 1]);
				i++;
			} else if ((args.length >= i) && args[i].toLowerCase().equals("-dc") || args[i].toLowerCase().equals("--diskcapacity")) {
				diskCapacity = Double.parseDouble(args[i + 1]);
				i++;
			} else if ((args.length >= i) && args[i].toLowerCase().equals("-drl") || args[i].toLowerCase().equals("--diskreadlatency")) {
				diskReadLacenty = Double.parseDouble(args[i + 1]);
				i++;
			} else if ((args.length >= i) && args[i].toLowerCase().equals("-dwl") || args[i].toLowerCase().equals("--diskwritelatency")) {
				diskWriteLacenty = Double.parseDouble(args[i + 1]);
				i++;
			} else if ((args.length >= i) && args[i].toLowerCase().equals("-dio") || args[i].toLowerCase().equals("--diskiolimit")) {
				diskTotalIOLimitation = Integer.parseInt(args[i + 1]);
				i++;
			} else if((args.length >= i) && args[i].toLowerCase().equals("-f") || args[i].toLowerCase().equals("--file")) {
				String filename = args[i + 1];
				if(!filename.endsWith(CLOUD_FILE_EXTENTION))
					filename += "." + CLOUD_FILE_EXTENTION;
				out = new FileOutputStream(filename);
				i++;
			} else if(args[i].toLowerCase().equals("--std")) {
				out = System.out;
			} else {
				System.err.println("Unrecognized parameter '" + args[i] + "'\n\n" +
						"Usage:\n" +
						"-n  | --name              name of the cloud [default-cloud]\n" +
						"-r  | --rootUrl           [cloud.org/]\n" +
						"-l  | --location          [us]\n" +
						"-s  | --servers           number of servers [1]\n" +
						"-d  | --disks             disks per server [3]\n" +
						"-up | --uploadprice       cents/GB [0.1]\n" +
						"-dp | --downloadprice     cents/GB [0.1]\n" +
						"-pp | --storageprice      cents/GB/period [0.1]\n" +
						"-b  | --serverbandwidth   of servers in MB/s [128]\n" +
						"-hr | --hddreadbandwidth  of disks in MB/s [210]\n" +
						"-hw | --hddwritebandwidth of disks in MB/s [210]\n" +
						"-dc | --diskcapacity      in GB [1024]\n" +
						"-drl| --diskreadlatency   in ms [8.5]\n" +
						"-dwl| --diskwritelatency  in ms [8.5]\n" +
						"-dio| --diskiolimit       in MB/s [210]\n" +
						"-f  | --file              output file\n" +
						"    | --std               use std output for Cloud XML\n");
				return;
			}
		}

		if(out == null) {
			System.err.println("no output specified. Will use './" + cloudName + "." + CLOUD_FILE_EXTENTION + "'");
			out = new FileOutputStream(cloudName + CLOUD_FILE_EXTENTION);
		}

		GenericDrive drive = new GenericDrive("generic");
		drive.init(toBytes(diskCapacity, GIGA_BYTE), toBytes(diskWriteBandwidth, MEGA_BYTE), toBytes(diskReadBandwidth, MEGA_BYTE), diskWriteLacenty, diskReadLacenty, FirstFitAllocation.create(diskTotalIOLimitation, MEGA_BYTE));

		CloudModel m = StorageCloudFactory.createModel(cloudName, rootUrl, location, servers, harddrivesPerServer, drive, uploadPrice,downloadPrice,storagePrice, serverBandwidth);

		StorageCloudFactory.serialize(m, out);
	}
}
