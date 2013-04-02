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
package de.ifgi.airbase.feeder.data;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.joda.time.DateTime;

/**
 * @author Christian Autermann
 * 
 */
public class EEARawDataFile implements Iterable<EEAMeasurement> {

	private DateTime startDate;
	private DateTime endDate;
	private int measurementEuropeanGroupCode;
	private EEAStation station;
	private EEAMeasurementType type;
	private EEAConfiguration configuration;
	private ArrayList<EEAMeasurement> measurements = new ArrayList<EEAMeasurement>();
	private String fileName;

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return this.fileName;
	}

	/**
	 * @param line
	 *            the measurement series
	 */
	public void addMeasurement(EEAMeasurement line) {
		this.measurements.add(line);
	}

	/**
	 * @return the measurement series
	 */
	public List<EEAMeasurement> getMeasurements() {
		return this.measurements;
	}

	/**
	 * @return the start date
	 */
	public DateTime getStartDate() {
		return this.startDate;
	}

	/**
	 * @param startDate
	 *            the start date
	 */
	public void setStartDate(DateTime startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the end date
	 */
	public DateTime getEndDate() {
		return this.endDate;
	}

	/**
	 * @param endDate
	 *            the end date
	 */
	public void setEndDate(DateTime endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the station
	 */
	public EEAStation getStation() {
		return this.station;
	}

	/**
	 * @param station
	 *            the station
	 */
	public void setStation(EEAStation station) {
		this.station = station;
	}

	/**
	 * @return the european group code
	 */
	public int getMeasurementEuropeanGroupCode() {
		return this.measurementEuropeanGroupCode;
	}

	/**
	 * @param measurementEuropeanGroupCode the european group code
	 */
	public void setMeasurementEuropeanGroupCode(int measurementEuropeanGroupCode) {
		this.measurementEuropeanGroupCode = measurementEuropeanGroupCode;
	}

	/**
	 * @return the type
	 */
	public EEAMeasurementType getType() {
		return this.type;
	}

	/**
	 * @param type the type
	 */
	public void setType(EEAMeasurementType type) {
		this.type = type;
	}

	/**
	 * @param configuration
	 *            the configuration to set
	 */
	public void setConfiguration(EEAConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * @return the configuration
	 */
	public EEAConfiguration getConfiguration() {
		return this.configuration;
	}

	@Override
	public Iterator<EEAMeasurement> iterator() {
		return this.measurements.iterator();
	}
}
