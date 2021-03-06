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
package de.ifgi.airbase.feeder.io.file;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.gml.geometry.GmlGeometryFactory;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Point;
import de.ifgi.airbase.feeder.Configuration;

import de.ifgi.airbase.feeder.data.EEAMeasurement;
import de.ifgi.airbase.feeder.data.EEARawDataFile;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.io.filter.TimeRangeFilter;
import de.ifgi.airbase.feeder.io.sos.http.xml.AbstractXmlBuilder;
import de.ifgi.airbase.feeder.util.Utils;

/**
 * writer that can be used to write EEA Measurements to XML output file
 * 
 * @author staschc
 * 
 */
public class OMFileWriter {

	/**
	 * writes the Observations to an output file
	 * 
	 * @param outputFile
	 *            file to which the observations should be written
	 * @param file
	 * 
	 */
	public void writeObservations2File(File outputFile, EEARawDataFile file) {
		StaxObservationEncoder encoder = new StaxObservationEncoder();
		IObservationCollection obsCol = convertEEAFile2UWObsCol(file);
		try {
			if (obsCol != null){
				if (!outputFile.exists()){
					 outputFile.createNewFile();
				 }
				encoder.encodeObservationCollection(obsCol, outputFile);
			}
			else {
				outputFile.delete();
			}
		} catch (OMEncodingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * writes the Observations to an output file
	 * 
	 * @param outputFile
	 *            file to which the observations should be written
	 * @param inputFiles
	 * 			array of input files read from EEA input
	 * 
	 */
	public void writeObservations2File(File outputFile, List<EEARawDataFile> inputFiles) {
		StaxObservationEncoder encoder = new StaxObservationEncoder();
		IObservationCollection obsCollAll = new MeasurementCollection();
		for (EEARawDataFile inputFile : inputFiles){
			IObservationCollection obsCol = convertEEAFile2UWObsCol(inputFile);
			obsCollAll.addObservationCollection(obsCol);
		}
		try {
			encoder.encodeObservationCollection(obsCollAll,outputFile);
		} catch (OMEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * converts observations of EEA file into observation collection
	 * 
	 * @param file
	 * 			file containing the observations
	 * @return returns UncertWeb observation collection
     */
    private IObservationCollection convertEEAFile2UWObsCol(EEARawDataFile file) {
        int index = 0;
        TimeRangeFilter trf = Configuration.getInstance().getTimeRangeFilter();
        List<EEAMeasurement> allMeasurements = file.getMeasurements();
        Collection<Measurement> measCol = new ArrayList<Measurement>(allMeasurements.size());
        while (index < allMeasurements.size()) {
            Measurement meas;
            while (index < allMeasurements.size()) {
                EEAMeasurement e = allMeasurements.get(index);
                if (e.isValid() && (trf == null || trf.accept(e))) {
                    try {
                        SpatialSamplingFeature foi = createSF4EEAStation(file.getStation());
                        TimeObject resultTime = new TimeObject(Utils.ISO8601_DATETIME_FORMAT.print(e.getTime()));
                        String procedureID = AbstractXmlBuilder.getStationId(file.getStation());
                        String obsProp = AbstractXmlBuilder.getPhenomenonId(file.getConfiguration().getComponentCode());
                        IUncertainty[] uncertainty = {createRandomNormalDistribution(0, 1, 0)};
                        String uom = file.getConfiguration().getMeasurementUnit();
                        double obsResult = e.getValue();
                        meas = new Measurement(resultTime, resultTime, new URI(procedureID), new URI(obsProp), foi, new MeasureResult(obsResult, uom));
                        DQ_UncertaintyResult[] uncArray = {new DQ_UncertaintyResult(uncertainty, uom)};
                        meas.setResultQuality(uncArray);
                        measCol.add(meas);
                    } catch (Exception e1) {
                        throw new RuntimeException(e1);
                    }
                }
                index++;
            }
        }
        return new MeasurementCollection(new LinkedList<Measurement>(measCol));
    }

	/**
	 * 
	 * creates a normal distribution around mean by adding variance as random number between 0 and 1 multiplied by factor and added to
	 * varianceSummand
	 * 
	 * @param mean
	 * 			mean of the normal distribution
	 * @param factor
	 * 			factor of variance which is generated between 0 and 1
	 * @param summand
	 * 			summand of variance which is generated between 0 and 1
	 * @return
	 */
	private IUncertainty createRandomNormalDistribution(int mean, double varianceFactor, double varianceSummand) {
		double variance = varianceSummand+varianceFactor*Math.random();
		return new NormalDistribution(mean,variance);
	}

	/**
	 * creates SpatialSamplingFeature from station description
	 * 
	 * @param station
	 * 			station for which sampling feature should be created
	 * @return sampling feature created from station
	 * @throws IllegalArgumentException
	 * @throws URISyntaxException
	 */
	private SpatialSamplingFeature createSF4EEAStation(EEAStation station)
			throws IllegalArgumentException, URISyntaxException {
		String id = station.getName();
		String sampledCity = station.getCity();
		double lat = new Double(station.getLatitude()).doubleValue();
		double lon = new Double(station.getLongitude()).doubleValue();
		Point p = new GmlGeometryFactory().createPoint(lat, lon, 4326);
		SpatialSamplingFeature sf = new SpatialSamplingFeature(new Identifier(
				new URI("http://www.uncertweb.org"), id), sampledCity, p);
		return sf;
	}

}
