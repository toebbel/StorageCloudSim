/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.monitoring.report;

import edu.kit.cloudSimStorage.StorageCloud;
import edu.kit.cloudSimStorage.UsageSequence;
import edu.kit.cloudSimStorage.helper.TimeHelper;
import edu.kit.cloudSimStorage.helper.TupleSequence;
import edu.kit.cloudSimStorage.monitoring.TrackableResource;
import edu.kit.cloudSimStorage.helper.Tuple;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/** @author Tobias Sturm, 7/1/13 2:05 PM */
public class GraphGenerator extends ReportGenerator{

	@Override
	public void generate(File rootDir) throws IOException {
		StringBuilder texMake = new StringBuilder();
		File texDir = new File(rootDir, "tex-src/");
		if(!texDir.exists())
			texDir.mkdirs();

		for(StorageCloud cloud : this.clouds) {
			for(String key : cloud.getAvailableTrackingKeys()) {
				try{
					plotTrackableResourceSamples(cloud, key, texMake, texDir);
				}catch(Exception e) {}
			}
		}


		writeToFile(rootDir, "buildTex.sh", texMake.toString());
		(new File("buildTex.sh")).setExecutable(true);
	}

	private static void plotTrackableResourceSamples(TrackableResource resource, String key, StringBuilder texMake, File texDir) throws IOException {
		TupleSequence<Double> samples = resource.getSamples(key);
		if(samples == null || samples.isEmpty())
			return;

		Tuple<String, TupleSequence<String>> formatedData = format(samples, key);
		String filename = plotTrackableResourceSamples(formatedData.y, formatedData.x, texDir, resource.toString() + formatedData.x);

		filename = filename.replace(" ", "\\ ");
		String epsFilename = filename.substring(0, filename.length() - 3) + "eps";
		String pdfFilename = filename.substring(0, filename.length() - 3) + "pdf";
		String pngFilename = filename.substring(0, filename.length() - 3) + "png";
		String auxFilename = filename.substring(0, filename.length() - 3) + "aux";
		String logFilename = filename.substring(0, filename.length() - 3) + "log";
		texMake.append("pdflatex --output-dir ").append("./").append(" tex-src/").append(filename).append(" >/dev/null").append("\n");
		texMake.append("pdftops -eps ").append(pdfFilename).append(" ").append(epsFilename).append("\n");
		texMake.append("convert -density 300 ").append(epsFilename).append(" ").append(pngFilename).append("\n");
		texMake.append("rm ").append(pdfFilename).append("\n");
		texMake.append("rm ").append(logFilename).append("\n");
		texMake.append("rm ").append(auxFilename).append("\n");
	}

	private static String plotTrackableResourceSamples(TupleSequence<String> data, String axisLabel, File root, String name) throws IOException {
		List<String> xTicks = getFullHours(data);

		StringBuilder b = new StringBuilder();
		b.append(tkizHead());
		b.append("\\begin{axis}[compat=1.5.1,\n").
			append("date coordinates in=x,\n").
			append("date coordinates in=x,\n").
			append("date ZERO=").append(xTicks.get(0)).append(",\n").
			append("xticklabel=\\hour:\\minute:\\second,\n").
			append("xticklabel style= {rotate=45,anchor=north east},\n");

		b.append("xtick={\n");
		for(String tick : xTicks)
			b.append("\t{").append(tick).append("},\n");
		b.append("\t{").append(timeToString(roundUp(data.get(data.size() - 1).x + 10 * 60 * 1000))).append("}\n");
		b.append("},\n");

		b.append("xmin={").append(timeToString(data.get(0).x)).append("},\n")
			.append("xmax ={").append(timeToString(data.get(data.size() - 1).x)).append("},\n")
			.append("clip = false,\n")
			.append("xlabel=Timestamp,\n")
			.append("ylabel=").append(axisLabel.replace("$", "\\$").replace("%", "\\%").replace("#", "\\#")).append("\n]\n");

		b.append("\\addplot[only marks, mark=x, mark options={fill=white}] coordinates {\n");
		for(Tuple<Long, String> t : data) {
			b.append("\t(").append(timeToString(t.x)).append(',').append(t.y).append(")\n");
		}
		b.append("};\n");
		b.append("\\end{axis}\n");

		b.append(tkizFooter());
		String filename = (name + ".tex").replace("%", "").replace("$","").replace("(","").replace(")","").replace(" ","");
		writeToFile(root, filename, b.toString());
		return filename;
	}

	private static String timeToString(Long timestamp) {
		return TimeHelper.timeToString(timestamp, new SimpleDateFormat("MM-dd-yyyy hh:mm:ss"));
	}

	private static List<String> getFullHours(TupleSequence<?> data) {
		List<String> result = new ArrayList<>();
		if(data.isEmpty())
			return result;

		//add x axis
		long lastValue = roundUp(data.get(0).x);
		result.add(timeToString(lastValue));
		for(Tuple<Long, ?> t : data) {
			if(roundUp(t.x) != lastValue) {
				lastValue = roundUp(t.x);
				result.add(timeToString(lastValue));
			}
		}

		return result;
	}

	private static long roundUp(long ticks) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(ticks);
		c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) / 10 * 10);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}

	private static String tkizHead() {
		return "\\documentclass{article}\n" +
				"\\usepackage{tikz,pgfplots}\n" +
				"\\usepgfplotslibrary{dateplot}\n" +
				"\\begin{document}\n" +
				"\\resizebox {\\hsize} {!} {" +
				"\\begin{tikzpicture}";
	}

	private static String tkizFooter() {
		return "\\end{tikzpicture}\n}\n" +
				"\\end{document}";
	}

}
