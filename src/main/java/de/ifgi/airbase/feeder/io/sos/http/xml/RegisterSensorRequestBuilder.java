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
package de.ifgi.airbase.feeder.io.sos.http.xml;

import net.opengis.om.x10.MeasurementType;
import net.opengis.sos.x10.ObservationTemplateDocument;
import net.opengis.sos.x10.RegisterSensorDocument;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import de.ifgi.airbase.feeder.util.SOSNamespaceUtils;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class RegisterSensorRequestBuilder extends AbstractXmlBuilder<RegisterSensorDocument> {
    
    private static final String AN_UOM = "uom";
    private XmlObject description;
    
    public RegisterSensorRequestBuilder setDescription(XmlObject description) {
        this.description = description;
        return this;
    }
    
    protected XmlObject getDescription() {
        return this.description;
    }
    
    /**
     * Creates a {@code RegisterSensorDocument} that registers a {@code EEAStation} at a SOS.
     * 
     * @return the RegisterSensor request
     * @throws NoValidInputsOrOutputsException 
     */
    @Override
    public RegisterSensorDocument build() throws NoValidInputsOrOutputsException {
        RegisterSensorDocument regSensorDoc = RegisterSensorDocument.Factory.newInstance();
        RegisterSensorDocument.RegisterSensor regSensor = regSensorDoc.addNewRegisterSensor();
        regSensor.setService(SOS_SERVICE_NAME);
        regSensor.setVersion(SOS_V1_SERVICE_VERSION);
        regSensor.addNewSensorDescription().set(getDescription());
        // buildDomainFeature(station, regSensor.addNewDomainFeature());
        buildObservationTemplate(regSensor.addNewObservationTemplate());
        SOSNamespaceUtils.insertSchemaLocations(regSensor);
        return regSensorDoc;
    }
    
    private void buildObservationTemplate(ObservationTemplateDocument.ObservationTemplate template) {
        MeasurementType measurementType = (MeasurementType) template.addNewObservation()
                .substitute(SOSNamespaceUtils.QN_OM_1_0_MEASUREMENT, MeasurementType.type);
        measurementType.addNewSamplingTime();
        measurementType.addNewProcedure();
        measurementType.addNewObservedProperty();
        measurementType.addNewFeatureOfInterest();
        XmlObject result = measurementType.addNewResult();
        XmlCursor resultCursor = result.newCursor();
        resultCursor.toNextToken();
        resultCursor.insertAttributeWithValue(AN_UOM, "");
        resultCursor.insertChars("0.0");
        resultCursor.dispose();
    }

}
