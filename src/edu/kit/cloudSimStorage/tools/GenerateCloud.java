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
						"-n  | --name           name of the cloud [default-cloud]\n" +
						"-r  | --rootUrl        [cloud.org/]\n" +
						"-l  | --location       [us]\n" +
						"-s  | --servers        number of servers [1]\n" +
						"-d  | --disks          disks per server [3]\n" +
						"-up | --uploadprice    cents/GB [0.1]\n" +
						"-dp | --downloadprice   cents/GB [0.1]\n" +
						"-pp | --storageprice    cents/GB/period [0.1]\n" +
						"-b  | --serverbandwidth of servers in MB/s [128]\n" +
						"-f  | --file            output file\n" +
						"    | --std             use std output\n");
				return;
			}
		}

		if(out == null) {
			System.err.println("no output specified. Will use './cloudname'");
			out = new FileOutputStream(cloudName + CLOUD_FILE_EXTENTION);
		}

		GenericDrive drive = new GenericDrive("generic");
		drive.init(toBytes(2, TERA_BYTE), toBytes(156, MEGA_BYTE), toBytes(156, MEGA_BYTE), 9.5, 8.5, FirstFitAllocation.create(210, MEGA_BYTE));

		CloudModel m = StorageCloudFactory.createModel(cloudName, rootUrl, location, servers, harddrivesPerServer, drive, uploadPrice,downloadPrice,storagePrice, serverBandwidth);

		StorageCloudFactory.serialize(m, out);
	}
}
