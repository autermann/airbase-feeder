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
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import de.ifgi.airbase.feeder.Configuration;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.util.Utils;

class EEAStationParser {

	private static final Logger log = LoggerFactory.getLogger(EEAStationParser.class);

	static Map<String, EEAStation> parse(File file) throws IOException {
		CSVReader reader = null;
		Map<String, EEAStation> stations = new HashMap<String, EEAStation>();
		try {
			long start = System.currentTimeMillis();
			reader = new CSVReader(new FileReader(file), '\t', '"', 1);
			String[] line;
			while ((line = reader.readNext()) != null) {
				EEAStation s = parseStationLine(line);
				if (Configuration.getInstance().isAcceptableStation(s.getEuropeanCode())) {
					stations.put(s.getEuropeanCode(), s);
				}
			}
			log.info("Parsed {} stations in {}.", Integer.valueOf(stations.size()),
					Utils.timeElapsed(start));
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return stations;
	}

	private static EEAStation parseStationLine(String[] line) {
		EEAStation station = new EEAStation();
		station.setEuropeanCode(line[0]);
		station.setLocalCode(line[1]);
		station.setCountryIsoCode(line[2]);
		station.setCountryName(line[3]);
		station.setName(line[4]);
		station.setStartDate(line[5]);
		station.setEndDate(line[6]);
		station.setType(line[7]);
		station.setOzoneClassification(line[8]);
		station.setTypeOfArea(line[9]);
		station.setSubcatRuralBack(line[10]);
		station.setStreetType(line[11]);
		station.setLongitudeDeg(line[12]);
		station.setLatitudeDeg(line[13]);
		station.setAltitude(line[14]);
		station.setCity(line[15]);
		station.setLauLevel1Code(line[16]);
		station.setLauLevel2Code(line[17]);
		station.setLauLevel2Name(line[18]);
		station.setEMEPStation(line[19]);
		return station;
	}

    private EEAStationParser() {
    }
}
