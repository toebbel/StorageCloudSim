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

import edu.kit.cloudSimStorage.helper.TimeHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

import static edu.kit.cloudSimStorage.helper.TimeHelper.timeToString;

/** @author Tobias Sturm, 6/30/13 8:16 PM */
public class LogDeflector {
	Logger master = Logger.getGlobal();
	private List<Logger> knownLogger;
	File rootDir;
	private Level level;


	public LogDeflector(File rootDir) {
		level = Level.ALL;
		this.rootDir = rootDir;
		knownLogger = new ArrayList<>();
		knownLogger.add(master);

		Logger.getAnonymousLogger().setParent(master);

		Handler masterHandler = new ConsoleHandler();
		masterHandler.setFormatter(new FileFormater());
		master.addHandler(masterHandler);

		logToFile(master);
	}

	public LogDeflector add(ILoggable objectOfInterest) {
		return add(objectOfInterest.getLogger());
	}

	public LogDeflector add(Logger objectOfInterest) {
		if(objectOfInterest == null || knownLogger.contains(objectOfInterest))
			return this;

		objectOfInterest.setLevel(level);

		knownLogger.add(objectOfInterest);

		logToFile(objectOfInterest);

		return this;
	}

	private void logToFile(Logger objectOfInterest) {
		//write to file
		try {
			File fileName = new File(rootDir, objectOfInterest.getName() + ".log");
			if(fileName.exists())
				fileName.delete();
			FileHandler handler = new FileHandler(fileName.getAbsolutePath());
			handler.setLevel(level);
			handler.setFormatter(new FileFormater());
			objectOfInterest.addHandler(handler);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setLevel(Level level) {
		this.level = level;
		for(Logger l : knownLogger)
			l.setLevel(level);
	}
}

class FileFormater extends Formatter {

	@Override
	public String format(LogRecord record) {
	 	return record.getLevel() + "\t" + record.getLoggerName() + "\t" + timeToString(TimeHelper.getInstance().now()) + "\t" + record.getMessage() + "\n";
	}
}