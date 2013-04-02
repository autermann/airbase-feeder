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
package de.ifgi.airbase.feeder.io.sos;

import de.ifgi.airbase.feeder.Configuration;
import de.ifgi.airbase.feeder.data.EEAMeasurement;
import de.ifgi.airbase.feeder.data.EEARawDataFile;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.io.filter.TimeRangeFilter;

public class Counter extends SosClient {
	private static long count = 0;
	
	@Override
	public void registerStation(EEAStation station) {}

	@Override
	public void insertObservations(EEARawDataFile file) {
		TimeRangeFilter trf = Configuration.getInstance().getTimeRangeFilter();
		for (EEAMeasurement m : file.getMeasurements()) {
			if (m.isValid() && (trf == null || trf.accept(m))) {
                count++;
            }
		}
		log.info("Values: {}", count);
	}
}
