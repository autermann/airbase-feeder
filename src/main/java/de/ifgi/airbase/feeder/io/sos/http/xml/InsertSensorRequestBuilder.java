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

import java.util.Collection;

import net.opengis.sos.x20.SosInsertionMetadataDocument;
import net.opengis.sos.x20.SosInsertionMetadataType;
import net.opengis.swes.x20.InsertSensorDocument;
import net.opengis.swes.x20.InsertSensorType;
import net.opengis.swes.x20.InsertSensorType.ProcedureDescription;

import org.apache.xmlbeans.XmlObject;

import de.ifgi.airbase.feeder.data.EEAConfiguration;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.util.SOSNamespaceUtils;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class InsertSensorRequestBuilder extends AbstractXmlBuilder<InsertSensorDocument> {
    private EEAStation station;
    private XmlObject description;
    
    public InsertSensorRequestBuilder setStation(EEAStation station) {
        this.station = station;
        return this;
    }
    
    protected EEAStation getStation() {
        return station;
    }
    
    public InsertSensorRequestBuilder setDescription(XmlObject description) {
        this.description = description;
        return this;
    }
    
    protected XmlObject getDescription() {
        return this.description;
    }

    @Override
    public InsertSensorDocument build() throws NoValidInputsOrOutputsException {
        InsertSensorDocument insertSensorDocument = InsertSensorDocument.Factory.newInstance();
        InsertSensorType insertSensorType = insertSensorDocument.addNewInsertSensor();
        insertSensorType.setService(SOS_SERVICE_NAME);
        insertSensorType.setVersion(SOS_V2_SERVICE_VERSION);
        insertSensorType.setProcedureDescriptionFormat(SOSNamespaceUtils.SML_1_0_1_PROCEDURE_DESCRIPTION_FORMAT);
        ProcedureDescription procedureDescription = insertSensorType.addNewProcedureDescription();
        procedureDescription.set(getDescription());
        setObservableProperties(insertSensorType);
        setInsertionMetadata(insertSensorType);
        SOSNamespaceUtils.insertSchemaLocations(insertSensorType);
        return insertSensorDocument;
    }

    private void setObservableProperties(InsertSensorType insertSensorType) throws NoValidInputsOrOutputsException {
        Collection<EEAConfiguration> configurations = getUniqueConfigurations(getStation());
        for (EEAConfiguration configuration : configurations) {
            insertSensorType.addObservableProperty(getPhenomenonId(configuration.getComponentCode()));
        }
    }
    
    private void setInsertionMetadata(InsertSensorType insertSensorType) {
        SosInsertionMetadataDocument sosInsertionMetadataDocument = SosInsertionMetadataDocument.Factory.newInstance();
        SosInsertionMetadataType sosInsertionMetadataType = sosInsertionMetadataDocument.addNewSosInsertionMetadata();
        sosInsertionMetadataType.addFeatureOfInterestType(SF_SAMPLING_POINT);
        sosInsertionMetadataType.addObservationType(OM_2_MEASUREMENT_OBSERVATION);
        insertSensorType.addNewMetadata().set(sosInsertionMetadataDocument);
    }

    
}
