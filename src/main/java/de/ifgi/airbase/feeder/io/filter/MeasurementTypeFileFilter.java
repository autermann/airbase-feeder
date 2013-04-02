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
/**
 * 
 */
package de.ifgi.airbase.feeder.io.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;

import de.ifgi.airbase.feeder.data.EEAMeasurementType;

/**
 * {@link FileFilter} to filter all for specific {@link EEAMeasurementType}s.
 * 
 * @author Christian Autermann
 * 
 */
public class MeasurementTypeFileFilter implements FileFilter {

	private Collection<EEAMeasurementType> types;

	/**
	 * Creates a new {@code FileFilter} for the specified
	 * {@code EEAMeasurementType}s.
	 * 
	 * @param types
	 *            the {@code EEAMeasurementType}s
	 */
	public MeasurementTypeFileFilter(Collection<EEAMeasurementType> types) {
		this.types = types;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean accept(File pathname) {
		if (pathname.isDirectory()) {
            return false;
        }
        return this.types.contains(EEAMeasurementType.getValue(pathname
        		.getName().split("\\.")[0].substring(17)));
	}

}