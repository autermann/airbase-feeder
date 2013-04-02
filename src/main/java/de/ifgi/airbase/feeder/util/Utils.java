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
package de.ifgi.airbase.feeder.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ifgi.airbase.feeder.Configuration;
import de.ifgi.airbase.feeder.io.filter.ComponentFileFilter;
import de.ifgi.airbase.feeder.io.filter.CompositeFileFilter;
import de.ifgi.airbase.feeder.io.filter.MeasurementTypeFileFilter;
import de.ifgi.airbase.feeder.io.filter.StationFileFilter;
import de.ifgi.airbase.feeder.io.filter.TimeRangeFilter;

/**
 * Utility class.
 * 
 * @author Christian Autermann
 * 
 */
public class Utils {

	public static final DateTimeFormatter NORMAL_DATE_FORMAT = DateTimeFormat.forPattern("dd-MM-yyyy");
	public static final DateTimeFormatter REVERSE_DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");
	public static final DateTimeFormatter ISO8601_DATETIME_FORMAT = ISODateTimeFormat.dateTime();


	private static final String FILE_NAME = "/config.properties";
	private static final Logger log = LoggerFactory.getLogger(Utils.class);
    
	private static final Properties props = new Properties() {
        private static final long serialVersionUID = 1L;
        {
            log.info("Loading Properties");
            try {
                InputStream is = Utils.class.getResourceAsStream(FILE_NAME);
                if (is == null) {
                    throw new FileNotFoundException();
                }
                load(is);
                log.debug("LoadedProperties");
                for (String key : stringPropertyNames()) {
                    log.debug("LoadedProperty: {} => {}", key, get(key));
                }
            } catch (IOException e) {
                log.error("Failed to load properties", e);
            }
        }
    };

    private static final String AIRBASE_TEMP_FOLDER = "AirBase_Feeder_TEMP";
    private static final String EEA_TEMP_DIR_KEY = "eea.download.directory";
    private static String tempDir = null;
    private static File failedRequest = null;

    static {
        DateTimeZone.setDefault(DateTimeZone.UTC);
    }

    /**
     * Formats the time elapsed since
     *
     * @ start}
     *
     * @param start the start point
     *
     * @return a {@link String} describing the elapsed time
     */
    public static String timeElapsed(long start) {
        double sec = ((double) (System.currentTimeMillis() - start)) / 1000;
        return (sec < 1) ? ((int) (sec * 1000) + " ms")
                : (sec < 60) ? ((int) sec + " s")
                : (sec > 3600) ? ((int) (sec / 3600) + " h")
                : ((int) Math.floor(sec / 60) + " m " + (int) (sec % 60) + " s");
    }

    /**
     * Loads a configuration property.
     *
     * @param key the property key
     *
     * @return the property
     */
    public static String get(String key) {
        return props.getProperty(key);
    }

	

	public static FileFilter getRawDataFileFilter() {
		CompositeFileFilter cff = new CompositeFileFilter();
		if (Configuration.getInstance().getStationsToParse() != null) {
			cff.addFilter(new StationFileFilter(Configuration.getInstance().getStationsToParse()));
		}
        
		if (Configuration.getInstance().getTypesToParse() != null) {
			cff.addFilter(new MeasurementTypeFileFilter(Configuration.getInstance().getTypesToParse()));
		}
        
		if (Configuration.getInstance().getComponentsToParse() != null) {
			cff.addFilter(new ComponentFileFilter(Configuration.getInstance().getComponentsToParse()));
		}
        
        TimeRangeFilter trfilter = Configuration.getInstance().getTimeRangeFilter();
		if (trfilter != null) {
			cff.addFilter(trfilter);
		}
		return cff;
	}

	public static DateTime parseDate(String date) {
		return NORMAL_DATE_FORMAT.parseDateTime(date);
	}

	public static DateTime parseDateReverse(String date) {
		return REVERSE_DATE_FORMAT.parseDateTime(date.split(" ")[0]);
	}

    public static String createTempDir() throws IOException {
        if (tempDir == null) {
            String dir = get(EEA_TEMP_DIR_KEY);

            if (dir == null) {
                dir = System.getProperty("java.io.tmpdir") + File.separator + AIRBASE_TEMP_FOLDER;
            }
            File temp = new File(dir);

            if (temp.exists()) {
                log.info("Temp dir already exists, reusing " + temp.getAbsolutePath());
            } else {
                temp.mkdir();
                log.info("Created temp directory " + temp.getAbsolutePath());
            }
            tempDir = temp.getAbsolutePath();
        }
        return tempDir;
    }

    public static String getFailedRequestPrintPath() {
        if (failedRequest == null) {
            if (tempDir == null) {
                try {
                    createTempDir();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            failedRequest = new File(tempDir + File.separator + "FailedRequests");
            if (!failedRequest.exists()) {
                failedRequest.mkdir();
            }
        }
        return failedRequest.getAbsolutePath();
    }
}
