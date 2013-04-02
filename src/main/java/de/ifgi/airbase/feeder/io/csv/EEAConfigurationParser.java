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
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import de.ifgi.airbase.feeder.Configuration;
import de.ifgi.airbase.feeder.data.EEAConfiguration;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.util.Utils;


class EEAConfigurationParser {
	private static final Logger log = LoggerFactory.getLogger(EEAConfigurationParser.class);

	static void parse(Map<String, EEAStation> stations, File file) throws IOException {
		CSVReader reader = null;
		try {
			long start = System.currentTimeMillis();
			reader = new CSVReader(new FileReader(
					file), '\t', '"', 1);
			String[] line = null;
			int count = 0;
			while ((line = reader.readNext()) != null) {
				EEAConfiguration config = parseConfigurationLine(line);
				
				if (Configuration.getInstance().isAcceptableStation(config.getStationCode())) {
					EEAStation station = stations.get(config.getStationCode());
					config.setStation(station);
					station.addConfiguration(config);
					count++;
				}
			}
			log.info("Parsed {} measurment configurations in {}.", Integer.valueOf(count),
					Utils.timeElapsed(start));
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	private static EEAConfiguration parseConfigurationLine(String[] line) {
		EEAConfiguration config = new EEAConfiguration();
		config.setStationCode(line[0]);
		config.setComponentCode(Integer.parseInt(line[1]));
		config.setComponentCaption(line[2]);
		config.setComponentName(line[3]);
		config.setComponentFwd(line[4].equalsIgnoreCase("yes"));
		config.setMeasurementEuropeanGroupCode(Integer.parseInt(line[5]));
		config.setMeasurementUnit(line[6]);
		config.setMeasurementGroupStartDate(line[7]);
		config.setMeasurementGroupEndDate(line[8]);
		config.setMeasurementLatestAIRBASE(line[9]);
		config.setMeasurementEuropeanCode(line[10]);
		config.setMeasurementStartDate(line[11]);
		config.setMeasurementEndDate(line[12]);
		config.setMeasurementAutomatic(line[13]);
		config.setMeasurementTechniquePrinciple(line[14]);
		config.setMeasurementEquipment(line[15]);
		config.setIntegrationTimeFrequency(line[16]);
		config.setCalibrationUnit(line[17]);
		config.setHeightSamplingPoint(line[18]);
		config.setLengthSamplingLine(line[19]);
		config.setLocationSamplingPoint(line[20]);
		config.setSamplingTime(line[21]);
		config.setSamplingTimeUnit(line[22]);
		config.setCalibrationFrequency(line[23]);
		config.setIntegrationTimeUnit(line[24]);
		config.setCalibrationMethod(line[25]);
		config.setCalibrationDescription(line[26]);
		return config;
	}
}
