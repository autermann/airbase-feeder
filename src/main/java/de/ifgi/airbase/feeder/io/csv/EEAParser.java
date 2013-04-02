/**
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package de.ifgi.airbase.feeder.io.csv;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.ifgi.airbase.feeder.data.EEAMeasurementType;
import de.ifgi.airbase.feeder.data.EEARawDataFile;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.data.EEAStatistic;

/**
 * Class to parse all sorts of EEA AirBase files.
 * 
 * @author Christian Autermann
 * 
 */
public class EEAParser {

	private Map<String, EEAStation> stations = new HashMap<String, EEAStation>();
	private EEARawDataParser parser;
	private File statisticsFile;
	
	
	/**
	 * Creates a new {@link EEAParser}.
	 * 
	 * @param stationsFile
	 *            the stations file
	 * @param rawDataDirectory
	 *            the directory containing the data files
	 * @param statisticsFile
	 *            the statistics file
	 * @param measurementConfigurationFile
	 *            the measurement configuration file
	 * @param types
	 *            the {@link EEAMeasurementType}s that should be parsed
	 * @throws IOException
	 *             if an IO error occurs.
	 */
	public EEAParser(File stationsFile, File rawDataDirectory,
			File statisticsFile, File measurementConfigurationFile) throws IOException {
		testFiles(stationsFile,rawDataDirectory,statisticsFile,measurementConfigurationFile);
		this.statisticsFile = statisticsFile;
		this.stations = EEAStationParser.parse(stationsFile);
		EEAConfigurationParser.parse(this.stations, measurementConfigurationFile);
		this.parser = new EEARawDataParser(rawDataDirectory);
	}
	
	/**
	 * @return the parsed {@link EEAStation}s
	 */
	public Collection<EEAStation> getStations() {
		return Collections.unmodifiableCollection(this.stations.values());
	}

	/**
	 * @param station
	 *            the station
	 * @return the parsed data files of the specified station
	 * @throws IOException
	 *             if an IO error occurs.
	 */
	public Collection<EEARawDataFile> getDataByStation(EEAStation station)
			throws IOException {
		return this.parser.parse(station);
	}

	/**
	 * @return the parsed {@link EEAStatistic}s
	 * @throws IOException
	 *             if an IO error occurs.
	 */
	public Collection<EEAStatistic> getStatistics() throws IOException {
		return EEAStatisticsParser.parse(this.statisticsFile);
	}

	private void testFiles(File stationsFile, File rawDataDirectory,
			File statisticsFileP, File measurementConfigurationFile) throws IOException {
		if (stationsFile == null) {
			throw new IOException("can not read stationsFile: "
					+ stationsFile);
		}
		if (statisticsFileP == null) {
			throw new IOException("can not read statisticsFile: "
					+ statisticsFileP);
		}
		if (rawDataDirectory == null) {
			throw new IOException("can not read rawDataDirectory: "
					+ rawDataDirectory);
		}
		if (measurementConfigurationFile == null) {
			throw new IOException("can not read measurementConfigurationFile: "
					+ measurementConfigurationFile);
		}

		if (!stationsFile.canRead()) {
			throw new IOException("can not read stationsFile: "
					+ stationsFile);
		}
		if (!statisticsFileP.canRead()) {
			throw new IOException("can not read statisticsFile: "
					+ statisticsFileP);
		}
		if (!rawDataDirectory.canRead()) {
			throw new IOException("can not read rawDataDirectory: "
					+ rawDataDirectory);
		}
		if (!measurementConfigurationFile.canRead()) {
			throw new IOException("can not read measurementConfigurationFile: "
					+ measurementConfigurationFile);
		}
		if (!stationsFile.isFile()) {
			throw new IOException("stationsFile is not a file: "
					+ stationsFile);
		}
		if (!statisticsFileP.isFile()) {
			throw new IOException("statisticsFile is not a file: "
					+ statisticsFileP);
		}
		if (!measurementConfigurationFile.isFile()) {
			throw new IOException(
					"measurementConfigurationFile is not a file: "
							+ measurementConfigurationFile);
		}
		if (!rawDataDirectory.isDirectory()) {
			throw new IOException("rawDataDirectory is not a directory: "
					+ rawDataDirectory);
		}
	}
}
