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

import static de.ifgi.airbase.feeder.io.sos.http.xml.AbstractXmlBuilder.getFeatureOfInterestId;

import net.opengis.gml.x32.CodeWithAuthorityType;
import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.PointType;
import net.opengis.om.x20.OMObservationType;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureDocument;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureType;
import net.opengis.samplingSpatial.x20.ShapeType;
import net.opengis.sos.x20.InsertResultTemplateDocument;
import net.opengis.sos.x20.InsertResultTemplateType;
import net.opengis.sos.x20.ResultTemplateType;
import net.opengis.sos.x20.ResultTemplateType.ObservationTemplate;
import net.opengis.swe.x20.DataRecordDocument;
import net.opengis.swe.x20.DataRecordType;
import net.opengis.swe.x20.DataRecordType.Field;
import net.opengis.swe.x20.QuantityDocument;
import net.opengis.swe.x20.QuantityType;
import net.opengis.swe.x20.TextEncodingDocument;
import net.opengis.swe.x20.TextEncodingType;
import net.opengis.swe.x20.TimeDocument;
import net.opengis.swe.x20.TimeType;

import de.ifgi.airbase.feeder.data.EEAConfiguration;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.util.SOSNamespaceUtils;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class InsertResultTemplateRequestBuilder extends AbstractXmlBuilder<InsertResultTemplateDocument> {
    private EEAStation station;
    private EEAConfiguration configuration;

    public InsertResultTemplateRequestBuilder setStation(EEAStation station) {
        this.station = station;
        return this;
    }

    public InsertResultTemplateRequestBuilder setConfiguration(EEAConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }
    
    protected EEAStation getStation() {
        return station;
    }

    protected EEAConfiguration getConfiguration() {
        return configuration;
    }
    
    @Override
    public InsertResultTemplateDocument build() {
        InsertResultTemplateDocument insertResultTemplateDocument = InsertResultTemplateDocument.Factory.newInstance();
        InsertResultTemplateType insertResultTemplateType = insertResultTemplateDocument.addNewInsertResultTemplate();
        insertResultTemplateType.setVersion(SOS_V2_SERVICE_VERSION);
        insertResultTemplateType.setService(SOS_SERVICE_NAME);
        InsertResultTemplateType.ProposedTemplate proposedTemplate = insertResultTemplateType.addNewProposedTemplate();
        ResultTemplateType resultTemplateType = proposedTemplate.addNewResultTemplate();
        resultTemplateType.setOffering(getOfferingIdentifier(getStation()));
        resultTemplateType.setIdentifier(getResultTemplateIdentifier(getStation(), getConfiguration()));
        buildObservationTemplate(resultTemplateType);
        buildResultEncoding(resultTemplateType);
        buildResultStructure(resultTemplateType);
        SOSNamespaceUtils.insertSchemaLocations(insertResultTemplateDocument);
        return insertResultTemplateDocument;
    }

    protected void buildObservationTemplate(ResultTemplateType resultTemplateType) {
        ObservationTemplate observationTemplate = resultTemplateType.addNewObservationTemplate();
        OMObservationType observationType = observationTemplate.addNewOMObservation();
        observationType.setId(TEMPLATE_NIL_REASON);
        observationType.addNewType().setHref(OM_2_MEASUREMENT_OBSERVATION);
        observationType.addNewPhenomenonTime().setNilReason(TEMPLATE_NIL_REASON);
        observationType.addNewResultTime().setNilReason(TEMPLATE_NIL_REASON);
        observationType.addNewProcedure().setHref(getStationId(getStation()));
        observationType.addNewObservedProperty().setHref(getPhenomenonId(getConfiguration().getComponentCode()));
        buildFeatureOfInterest(observationType);
        observationType.addNewResult();
    }

    protected void buildResultEncoding(ResultTemplateType resultTemplateType) {
        TextEncodingDocument textEncodingDocument = TextEncodingDocument.Factory.newInstance();
        TextEncodingType textEncodingType = textEncodingDocument.addNewTextEncoding();
        textEncodingType.setBlockSeparator(SWE_DATA_ARRAY_BLOCK_SEPERATOR);
        textEncodingType.setDecimalSeparator(SWE_DATA_ARRAY_DECIMAL_SEPERATOR);
        textEncodingType.setTokenSeparator(SWE_DATA_ARRAY_TOKEN_SEPERATOR);
        resultTemplateType.addNewResultEncoding().set(textEncodingDocument);
    }
   
    protected void buildResultStructure(ResultTemplateType resultTemplateType) {
        DataRecordDocument dataRecordDocument = DataRecordDocument.Factory.newInstance();
        DataRecordType dataRecordType = dataRecordDocument.addNewDataRecord();
        TimeDocument timeDocument = TimeDocument.Factory.newInstance();
        TimeType timeType = timeDocument.addNewTime();
        timeType.setDefinition(PHENOMENON_TIME_DEFINITION);
        timeType.addNewUom().setHref(ISO8601_GREGORIAN_UOM);
        Field timeField = dataRecordType.addNewField();
        timeField.set(timeDocument);
        timeField.setName(PHENOMENON_TIME_FIELD);
        
        QuantityDocument quantityDocument = QuantityDocument.Factory.newInstance();
        QuantityType quantityType = quantityDocument.addNewQuantity();
        quantityType.addNewUom().setCode(getConfiguration().getMeasurementUnit());
        quantityType.setDefinition(getPhenomenonId(getConfiguration().getComponentCode()));
        Field quantityField = dataRecordType.addNewField();
        quantityField.set(quantityDocument);
        quantityField.setName(getPhenomenonFieldName());
        
        resultTemplateType.addNewResultStructure().set(dataRecordDocument);
    }
    
    protected void buildFeatureOfInterest(OMObservationType observationType) {
        SFSpatialSamplingFeatureDocument spatialSamplingFeatureDocument = SFSpatialSamplingFeatureDocument.Factory.newInstance();
        SFSpatialSamplingFeatureType spatialSamplingFeatureType = spatialSamplingFeatureDocument
                .addNewSFSpatialSamplingFeature();
        spatialSamplingFeatureType.setId(getSFSpatialSamplingPointIdentifier());
        CodeWithAuthorityType codeWithAuthorityType = spatialSamplingFeatureType.addNewIdentifier();
        codeWithAuthorityType.setCodeSpace(FEATURE_CODE_SPACE);
        codeWithAuthorityType.setStringValue(getFeatureOfInterestId(getStation()));
        spatialSamplingFeatureType.addNewType().setHref(SF_SAMPLING_POINT);
        spatialSamplingFeatureType.addNewSampledFeature().setHref(OGC_UNKNOWN);
        ShapeType shapeType = spatialSamplingFeatureType.addNewShape();
        PointType pointType = (PointType) shapeType.addNewAbstractGeometry()
                .substitute(SOSNamespaceUtils.QN_GML_3_2_POINT, PointType.type);
        pointType.setId(getPointId());
        DirectPositionType directPositionType = pointType.addNewPos();
        directPositionType.setSrsName(EPSG_4326_REFERENCE_SYSTEM_DEFINITION);
        directPositionType.setStringValue(buildPosString(getStation()));
        observationType.addNewFeatureOfInterest().set(spatialSamplingFeatureDocument);
    }

    private String getSFSpatialSamplingPointIdentifier() {
        return String.format("sf_%s", getStation().getEuropeanCode());
    }

    private String getPointId() {
        return String.format("p_%s", getStation().getEuropeanCode());
    }

    protected String getPhenomenonFieldName() {
        return String.format("phen_%s", getNameForComponent(getConfiguration().getComponentCode()));
    }
}
