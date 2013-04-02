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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ifgi.airbase.feeder;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ifgi.airbase.feeder.data.EEAMeasurementType;
import de.ifgi.airbase.feeder.io.filter.TimeRangeFilter;
import de.ifgi.airbase.feeder.util.Utils;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class Configuration {

    private static final Logger log = LoggerFactory.getLogger(Configuration.class);
    private static final String STATIONS_TO_PARSE_PROPERTY = "eea.stationsToParse";
    private static final String TYPES_TO_PARSE_PROPERTY = "eea.typesToParse";
    private static final String TIMES_TO_PARSE_PROPERTY = "eea.timesToParse";
    private static final String COMPONENTS_TO_PARSE_PROPERTY = "eea.componentsToParse";
    private static Configuration instance;

    public static Configuration getInstance() {
        return (instance == null) ? instance = new Configuration() : instance;
    }
    private Set<EEAMeasurementType> typesToParse = null;
    private Set<String> stationsToParse = null;
    private Set<Integer> components = null;
    private TimeRangeFilter trf = null;

    private Configuration() {
    }

    public Set<String> getStationsToParse() {
        if (stationsToParse == null) {
            String prop = Utils.get(STATIONS_TO_PARSE_PROPERTY);
            if (prop != null) {
                final String[] splittedProperty = prop.split(",");
                stationsToParse = new HashSet<String>(splittedProperty.length);
                for (String s : splittedProperty) {
                    stationsToParse.add(s.trim());
                }
                log.info("Stations to parse: {}", stationsToParse);
            }
        }
        return stationsToParse == null ? null : Collections.unmodifiableSet(stationsToParse);
    }

    public Set<EEAMeasurementType> getTypesToParse() {
        if (typesToParse == null) {
            String prop = Utils.get(TYPES_TO_PARSE_PROPERTY);
            if (prop != null) {
                final String[] splittedProperty = prop.split(",");
                List<EEAMeasurementType> typeList = new LinkedList<EEAMeasurementType>();
                for (String type : splittedProperty) {
                    typeList.add(EEAMeasurementType.getValue(type.trim()));
                }
                typesToParse = EnumSet.copyOf(typeList);
            }
        }
        return typesToParse == null ? null : Collections.unmodifiableSet(typesToParse);
    }

    public Set<Integer> getComponentsToParse() {
        if (components == null) {
            String property = Utils.get(COMPONENTS_TO_PARSE_PROPERTY);
            if (property != null) {
                LinkedList<Integer> componentList = new LinkedList<Integer>();
                for (String comp : property.split(",")) {
                    if (comp.contains("-")) {
                        String[] split = comp.split("-");
                        int begin = Integer.valueOf(split[0]).intValue();
                        int end = Integer.valueOf(split[0]).intValue();
                        if (end > begin) {
                            throw new Error("Can not parse component range. First value has to be smaller than the second.");
                        }
                        for (int i = begin; i <= end; ++i) {
                            componentList.add(Integer.valueOf(i));
                        }
                    } else {
                        componentList.add(Integer.valueOf(comp));
                    }
                }
                components = new HashSet<Integer>(componentList);
            }
        }
        return components == null ? null : Collections.unmodifiableSet(components);
    }

    public TimeRangeFilter getTimeRangeFilter() {
        String NULL = "null";
        if (trf == null) {
            String property = Utils.get(TIMES_TO_PARSE_PROPERTY);
            if (property != null) {
                trf = new TimeRangeFilter();
                for (String range : property.split(";")) {
                    String[] split = range.split(",");
                    if (split[0].equalsIgnoreCase(NULL)) {
                        if (split[1].equalsIgnoreCase(NULL)) {
                            throw new NullPointerException(
                                    "Begin and end date are null");
                        }
                        DateTime end = Utils.ISO8601_DATETIME_FORMAT.parseDateTime(split[1]);
                        trf.addEnd(end);
                    } else {
                        if (split[1].equalsIgnoreCase(NULL)) {
                            DateTime begin = Utils.ISO8601_DATETIME_FORMAT.parseDateTime(split[0]);
                            trf.addStart(begin);
                        } else {
                            DateTime begin = Utils.ISO8601_DATETIME_FORMAT.parseDateTime(split[0]);
                            DateTime end = Utils.ISO8601_DATETIME_FORMAT.parseDateTime(split[1]);
                            trf.addStartEndRange(begin, end);
                        }
                    }
                }
            }
        }
        return trf == null ? null : trf;
    }

    public boolean shouldBeIgnored(int componentCode) {
        return !getComponentsToParse().contains(new Integer(componentCode));
    }
    
    public boolean isAcceptableStation(String stationId) {
		Set<String> stations = getStationsToParse();
		if (stations == null) {
			return true;
		}
		for (String s : stations) {
			if (stationId.matches(s)) {
                return true;
            }
		}
		return false;
	}
}
