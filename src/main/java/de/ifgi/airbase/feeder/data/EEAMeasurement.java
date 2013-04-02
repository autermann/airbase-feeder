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

import org.joda.time.DateTime;

/**
 * @author Christian Autermann
 */
public class EEAMeasurement {
	private double value;
	private int quality;
	private DateTime time;
	
	/**
	 * Constructs a new {@code EEAMeasurement}
	 * @param value the value
	 * @param quality the quality
	 * @param time the time
	 */
	public EEAMeasurement(double value, int quality, DateTime time) {
		this.value = value;
		this.quality = quality;
		this.time = time;
	}
	
	/**
	 * @return whether the quality is greater 0
	 */
	public boolean isValid(){
		return getQuality() > 0;
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return this.value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * @return the quality
	 */
	public int getQuality() {
		return this.quality;
	}

	/**
	 * @param quality
	 *            the quality to set
	 */
	public void setQuality(int quality) {
		this.quality = quality;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(DateTime time) {
		this.time = time;
	}

	/**
	 * @return the time
	 */
	public DateTime getTime() {
		return this.time;
	}

}