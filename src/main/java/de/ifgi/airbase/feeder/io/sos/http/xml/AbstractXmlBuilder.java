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
package de.ifgi.airbase.feeder.io.sos.http.xml;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ifgi.airbase.feeder.Configuration;
import de.ifgi.airbase.feeder.data.EEAConfiguration;
import de.ifgi.airbase.feeder.data.EEAMeasurement;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.util.SOSNamespaceUtils;
import de.ifgi.airbase.feeder.util.Utils;

/**
 * @param <T> 
 * @author Christian Autermann
 */
public abstract class AbstractXmlBuilder<T extends XmlObject> {
    protected static final String OM_2_MEASUREMENT_OBSERVATION = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement";
    protected static final String SF_SAMPLING_POINT = "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint";
    protected static final String OGC_UNKNOWN = "http://www.opengis.net/def/nil/OGC/0/unknown";
	
    protected static final String SOS_SERVICE_NAME = "SOS";
	protected static final String SOS_V1_SERVICE_VERSION = "1.0.0";
    protected static final String SOS_V2_SERVICE_VERSION = "2.0.0";
    
    protected static final String UNIQUE_ID_DEFINITION = Utils.get("eea.def.uniqueId");
    protected static final String LONG_NAME_DEFINITION = Utils.get("eea.def.longName");
    protected static final String SHORT_NAME_DEFINITION = Utils.get("eea.def.shortName");
    protected static final String OFFERING_DEFINITION = Utils.get("eea.def.offering");
    protected static final String FEATURE_DEFINITION = Utils.get("eea.def.feature");
    protected static final String EPSG_4326_REFERENCE_SYSTEM_DEFINITION = Utils.get("eea.def.epsg.4326");
    protected static final String ISO8601_TIME_FORMAT_DEFINITION = Utils.get("eea.def.iso.8601");
    protected static final String PHENOMENON_TIME_DEFINITION = Utils.get("eea.uri.def.phenomenonTime");
    protected static final String STATION_IDENTIFIER = Utils.get("eea.identifier.station");
    protected static final String FEATURE_OF_INTEREST_IDENTIFIER = Utils.get("eea.identifier.featureOfInterest");
    protected static final String DOMAIN_FEATURE_IDENTIFIER = Utils.get("eea.identifier.domainFeature");
    protected static final String COMPONENT_IDENTIFIER = Utils.get("eea.identifier.component");
    protected static final String PHENOMENON_IDENTIFIER = Utils.get("eea.identifier.phenomenon");
	protected static final String COORDINATE_UOM = "degree";
	protected static final String METER_UOM = "m";
	protected static final String SWE_DATA_ARRAY_BLOCK_SEPERATOR = ";";
	protected static final String SWE_DATA_ARRAY_TOKEN_SEPERATOR = ",";
	protected static final String SWE_DATA_ARRAY_DECIMAL_SEPERATOR = ".";

    protected static final NumberFormat MEASUREMENT_VALUE_FORMAT = new DecimalFormat(
            "0.########",  new DecimalFormatSymbols(Locale.US));
    
	protected static final Logger log = LoggerFactory.getLogger(AbstractXmlBuilder.class);
	
	private static HashMap<Integer,String> phenomenonsCache = new HashMap<Integer, String>();
    protected static final String ISO8601_GREGORIAN_UOM = "http://www.opengis.net/def/uom/ISO-8601/0/Gregorian";
    
    protected static final String PHENOMENON_TIME_FIELD = "phenomenonTime";
    protected static final String TEMPLATE_NIL_REASON = "template";

    public static final String FEATURE_CODE_SPACE = Utils.get("eea.codeSpace.feature");

    public static String getPhenomenonId(int componentCode) {
        return String.format(PHENOMENON_IDENTIFIER, componentCode);
	}

    public static String getComponentId(int componentCode) {
        return String.format(COMPONENT_IDENTIFIER, componentCode);
    }

    public static String getNameForComponent(int component) {
        Integer code = new Integer(component);
        String name = phenomenonsCache.get(code);
        if (name == null) {
            phenomenonsCache.put(code, name = Utils.get(String.format("eea.phenomenon.name.%d", code)));
        }
        return name;
    }
	
    public static String getOfferingIdentifier(EEAStation station) {
        /* SOS does not accept URN's as offering identifieres.............. */
        return String.format("Offering_%s", station.getEuropeanCode());
    }

    public static String getOfferingName(EEAStation station) {
        return station.getEuropeanCode();
    }

	public static String getStationId(EEAStation station) {
        return String.format(STATION_IDENTIFIER, station.getEuropeanCode());
	}

	public static String getFeatureOfInterestId(EEAStation station) {
        return String.format(FEATURE_OF_INTEREST_IDENTIFIER, station.getEuropeanCode());
	}

	public static String getDomainFeatureId(EEAStation station) {
        return String.format(DOMAIN_FEATURE_IDENTIFIER, station.getEuropeanCode());
	}

	protected String buildPosString(EEAStation station) {
		return new StringBuilder()
				.append(new BigDecimal(station.getLatitude())).append(" ")
				.append(new BigDecimal(station.getLongitude())).append(" ")
				.append(new BigDecimal(station.getAltitude())).toString();
	}
    
    protected List<EEAMeasurement> sortMeasurements(Collection<EEAMeasurement> eeams) {
        List<EEAMeasurement> sortedMeasurements = new LinkedList<EEAMeasurement>(
				eeams);
		Collections.sort(sortedMeasurements, new Comparator<EEAMeasurement>() {
			@Override public int compare(EEAMeasurement o1, EEAMeasurement o2) {
				return o1.getTime().compareTo(o2.getTime());
			}
		});
        return sortedMeasurements;
    }

    protected String getResultTemplateIdentifier(EEAStation station, EEAConfiguration configuration) {
        return String.format("ResultTemplate_%s_%s",
                             station.getEuropeanCode(),
                             getNameForComponent(configuration.getComponentCode()));
    }
    
    
    protected Collection<EEAConfiguration> getUniqueConfigurations(EEAStation station) throws NoValidInputsOrOutputsException {
        HashMap<Integer, EEAConfiguration> uniqueConfigs = new HashMap<Integer, EEAConfiguration>(Configuration.getInstance().getComponentsToParse().size());
        for (EEAConfiguration config : station.getConfigurations()) {
            if (!Configuration.getInstance().shouldBeIgnored(config.getComponentCode())) {
                uniqueConfigs.put(Integer.valueOf(config.getComponentCode()), config);
            }
        }
        if (uniqueConfigs.isEmpty()) {
            throw new NoValidInputsOrOutputsException();        	
        }
        return uniqueConfigs.values();
    }

    public String asString() throws EncodingException {
        return SOSNamespaceUtils.toString(build());
    } 
    
    protected abstract T build() throws EncodingException;
    
}
