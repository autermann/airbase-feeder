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

import org.joda.time.Period;

/**
 * @author Christian Autermann
 */
public enum EEAMeasurementType {
	/***/
	THREE_HOURS, // undocumented...
	/***/
	HOUR,
	/***/
	EIGHT_HOURS,
	/***/
	DAY,
	/***/
	DAY_MAX,
	/***/
	WEEK,
	/***/
	TWO_WEEKS,
	/***/
	FOUR_WEEKS,
	/***/
	MONTH,
	/***/
	THREE_MONTH,
	/***/
	YEAR,
	/***/
	VAR;

	/**
	 * @param s
	 *            the string representation
	 * @return the {@link EEAMeasurementType}
	 */
	public static EEAMeasurementType getValue(String s) {
		if (s.equals("hour"))
			return HOUR;
		else if (s.equals("3hour"))
			return THREE_HOURS;
		else if (s.equals("hour8"))
			return EIGHT_HOURS;
		else if (s.equals("day"))
			return DAY;
		else if (s.equals("dymax"))
			return DAY_MAX;
		else if (s.equals("week"))
			return WEEK;
		else if (s.equals("2week"))
			return TWO_WEEKS;
		else if (s.equals("4week"))
			return FOUR_WEEKS;
		else if (s.equals("month"))
			return MONTH;
		else if (s.equals("3month"))
			return THREE_MONTH;
		else if (s.equals("year"))
			return YEAR;
		else if (s.equals("var"))
			return VAR;
		else
			throw new Error("Unknown DataType: " + s);
	}

	public Period getPeriod() {
		switch (this) {
		case HOUR:
			return Period.hours(1);
		case THREE_HOURS:
			return Period.hours(3);
		case EIGHT_HOURS:
			return Period.hours(8);
		case DAY:
		case DAY_MAX:
			return Period.days(1);
		case WEEK:
			return Period.weeks(1);
		case TWO_WEEKS:
			return Period.weeks(2);
		case FOUR_WEEKS:
			return Period.weeks(4);
		case MONTH:
			return Period.months(1);
		case THREE_MONTH:
			return Period.months(3);
		case YEAR:
			return Period.years(1);
		case VAR:
		default:
			throw new Error("Period not known");
		}
	}
}