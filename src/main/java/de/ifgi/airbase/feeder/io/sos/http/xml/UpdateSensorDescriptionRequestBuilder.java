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

import net.opengis.swes.x20.UpdateSensorDescriptionDocument;
import net.opengis.swes.x20.UpdateSensorDescriptionType;

import org.apache.xmlbeans.XmlObject;

import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.util.SOSNamespaceUtils;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class UpdateSensorDescriptionRequestBuilder extends AbstractXmlBuilder<UpdateSensorDescriptionDocument> {
    private XmlObject description;
    private EEAStation station;

    public UpdateSensorDescriptionRequestBuilder setDescription(XmlObject description) {
        this.description = description;
        return this;
    }

    public UpdateSensorDescriptionRequestBuilder setStation(EEAStation station) {
        this.station = station;
        return this;
    }

    protected XmlObject getDescription() {
        return this.description;
    }

    protected EEAStation getStation() {
        return this.station;
    }

    /**
     * Creates a {@code RegisterSensorDocument} that registers a {@code EEAStation} at a SOS.
     *
     * @return the RegisterSensor request
     */
    @Override
    public UpdateSensorDescriptionDocument build() {
        UpdateSensorDescriptionDocument updateSensorDoc = UpdateSensorDescriptionDocument.Factory.newInstance();
        UpdateSensorDescriptionType updateSensor = updateSensorDoc.addNewUpdateSensorDescription();
        updateSensor.setService(SOS_SERVICE_NAME);
        updateSensor.setVersion(SOS_V2_SERVICE_VERSION);
        updateSensor.setProcedureDescriptionFormat(SOSNamespaceUtils.SML_1_0_1_PROCEDURE_DESCRIPTION_FORMAT);
        updateSensor.setProcedure(getStationId(getStation()));
        updateSensor.addNewDescription().addNewSensorDescription().addNewData().set(getDescription());
        SOSNamespaceUtils.insertSchemaLocations(updateSensor);
        return updateSensorDoc;
    }


}
