
package de.ifgi.airbase.feeder.io.sos.http.xml;

import java.util.Collection;
import java.util.LinkedList;

import net.opengis.gml.TimePeriodType;
import net.opengis.sensorML.x101.CapabilitiesDocument.Capabilities;
import net.opengis.sensorML.x101.ClassificationDocument.Classification;
import net.opengis.sensorML.x101.ClassificationDocument.Classification.ClassifierList;
import net.opengis.sensorML.x101.ClassificationDocument.Classification.ClassifierList.Classifier;
import net.opengis.sensorML.x101.ContactDocument.Contact;
import net.opengis.sensorML.x101.ContactInfoDocument.ContactInfo;
import net.opengis.sensorML.x101.ContactInfoDocument.ContactInfo.Address;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList.Identifier;
import net.opengis.sensorML.x101.InputsDocument.Inputs.InputList;
import net.opengis.sensorML.x101.IoComponentPropertyType;
import net.opengis.sensorML.x101.KeywordsDocument.Keywords.KeywordList;
import net.opengis.sensorML.x101.OutputsDocument.Outputs.OutputList;
import net.opengis.sensorML.x101.PositionDocument.Position;
import net.opengis.sensorML.x101.ResponsiblePartyDocument.ResponsibleParty;
import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sensorML.x101.SensorMLDocument.SensorML;
import net.opengis.sensorML.x101.SystemType;
import net.opengis.sensorML.x101.TermDocument.Term;
import net.opengis.swe.x101.AbstractDataRecordType;
import net.opengis.swe.x101.DataComponentPropertyType;
import net.opengis.swe.x101.DataRecordType;
import net.opengis.swe.x101.EnvelopeType;
import net.opengis.swe.x101.ObservablePropertyDocument.ObservableProperty;
import net.opengis.swe.x101.PositionType;
import net.opengis.swe.x101.QuantityDocument.Quantity;
import net.opengis.swe.x101.TextDocument.Text;
import net.opengis.swe.x101.VectorPropertyType;
import net.opengis.swe.x101.VectorType;
import net.opengis.swe.x101.VectorType.Coordinate;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.joda.time.DateTime;

import de.ifgi.airbase.feeder.data.EEAConfiguration;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.util.SOSNamespaceUtils;
import de.ifgi.airbase.feeder.util.Utils;

/**
 * Class to build a {@code RegisterSensorDocument} for an {@code EEAStation}.
 * 
 * @author Christian Autermann, Daniel Nüst
 * 
 */
public class SensorDescriptionBuilder extends AbstractXmlBuilder<SensorMLDocument> {
    //private static final String COORD_NAME_LAT = "latitude";
    //private static final String COORD_NAME_LON = "longitude";
    private static final String COORD_NAME_LAT = "easting";
    private static final String COORD_NAME_LON = "northing";
    private static final String QUANTITY_AXIS_ID_LAT = "y";
    private static final String QUANTITY_AXIS_ID_LON = "x";
    private static final String QUANTITY_AXIS_ID_ALTITUDE = "z";
    private static final String UOM_CODE_LATLON = "degree";
    private static final String COORD_NAME_ALTITUDE = "altitude";
    private static final String CLASSIFIER_NAME_SENSOR_TYPE = "sensorType";
    private static final String CLASSIFIER_DEFINITION_SENSOR_TYPE = Utils.get("eea.def.classifier.sensorType");
    private static final String CLASSIFIER_NAME_TYPE_OF_SENSOR = "typeOfSensor";
    private static final String CLASSIFIER_DEFINITION_TYPE_OF_SENSOR = Utils.get("eea.def.classifier.typeOfSensor");
    private static final String CLASSIFIER_NAME_INTENDED_APPLICATION = "intendedApplication";
    private static final String CLASSIFIER_DEFINITION_INTENDED_APPLICATION = Utils.get("eea.def.classifier.application");
    private static final String CLASSIFIER_VALUE_INTENDED_APPLICATION = Utils.get("eea.sensor.intendedApplication");
    private static final String CLASSIFIER_NAME_OZONE = "stationOzoneType";
    private static final String CLASSIFIER_DEFINITION_OZONE = Utils.get("eea.def.classifier.ozone");
    private static final String CLASSIFIER_NAME_AREA_TYPE = "stationAreaType";
    private static final String CLASSIFIER_DEFINITION_AREA_TYPE = Utils.get("eea.def.classifier.area");
    private static final String CLASSIFIER_NAME_SUBCAT_RURAL = "stationSubCatRural";
    private static final String CLASSIFIER_DEFINITION_SUBCAT_RURAL = Utils.get("eea.def.classifier.rural");
    private static final String CLASSIFIER_NAME_STREET_TYPE = "streetType";
    private static final String CLASSIFIER_DEFINITION_STREET = Utils.get("eea.def.classifier.street");
    
    
    
    
    private static final String BBOX_FIELD_NAME = "observedBBOX";
    private static final String BBOX_DEFINITION = Utils.get("eea.def.bbox");
    private static final String STATUS_FIELD_NAME = "status";
    private static final String STATUS_DEFINITION = Utils.get("eea.def.status");
    private static final String EMEP_STATION_FIELD_NAME = "EMEP_station";
    private static final String EMEP_STATION_DEFINITION = Utils.get("eea.def.emepstation");
    private static final String STATION_LOCAL_CODE_FIELD_NAME = "station_local_code";
    private static final String STATION_LOCAL_CODE_DEFINITION = Utils.get("eea.def.station_local_code");
    private static final String LAU_LEVEL1_CODE_FIELD_NAME = "lau_level1_code";
    private static final String LAU_LEVEL1_CODE_DEFINITION = Utils.get("eea.def.lau_level1_code");
    private static final String LAU_LEVEL2_CODE_FIELD_NAME = "lau_level2_code";
    private static final String LAU_LEVEL2_CODE_DEFINITION = Utils.get("eea.def.lau_level2_code");
    private static final String LAU_LEVEL2_NAME_FIELD_NAME = "lau_level2_name";
    private static final String LAU_LEVEL2_NAME_DEFINITION = Utils.get("eea.def.lau_level2_name");
    
    private static final String CAPABILITIES_DEFINITION = Utils.get("eea.def.caps");
    private static final String[] ADDITIONAL_KEYWORDS = Utils.get("eea.keywords").split(";");
    private static final String SENSOR_DESCRIPTION = Utils.get("eea.sensor.description");
    private static final String CONTACT_INDIVIDUAL_NAME = Utils.get("eea.sensor.contact.individualName");
    private static final String CONTACT_ORGANIZATION_NAME = Utils.get("eea.sensor.contact.organizationName");
    private static final String CONTACT_CITY = Utils.get("eea.sensor.contact.city");
    private static final String CONTACT_POSTAL_CODE = Utils.get("eea.sensor.contact.postalCode");
    private static final String CONTACT_COUNTRY = Utils.get("eea.sensor.contact.country");
    private static final String CONTACT_EMAIL_ADDRESS = Utils.get("eea.sensor.contact.email");
    private static final String CONTACT_DELIVERY_POINT = Utils.get("eea.sensor.contact.deliveryPoint");
    private static final String CONTACT_IDENTIFIER = Utils.get("eea.sensor.contact.id");

    private EEAStation station;
    
    protected EEAStation getStation() {
        return station;
    }
    
    public SensorDescriptionBuilder setStation(EEAStation station) {
        this.station = station;
        return this;
    }
    
    @Override
    public SensorMLDocument build() throws NoValidInputsOrOutputsException {
        SensorMLDocument smlDocument = SensorMLDocument.Factory.newInstance();
        SensorML sml = smlDocument.addNewSensorML();
        sml.setVersion(SOSNamespaceUtils.SML_1_0_1_NAMESPACE_SCHEMA_VERSION);
        SystemType systemType = (SystemType) sml.addNewMember().addNewProcess()
                .substitute(SOSNamespaceUtils.QN_SML_1_0_1_SYSTEM, SystemType.type);
        buildDescription(systemType);
        buildKeywords(systemType);
        buildIdentification(systemType);
        buildClassification(systemType);
        buildValidTime(systemType);
        buildCapabilities(systemType);
        buildContact(systemType);
        buildLatLonPosition(systemType);
        buildInputOutputLists(systemType);
        buildComponents(systemType);
  
        LinkedList<XmlError> errors = new LinkedList<XmlError>();
        if (!smlDocument.validate(new XmlOptions().setErrorListener(errors))) {
//            log.warn("Created invalid document for station " + station);
            for (XmlError e : errors) {
                log.warn("ValidationError: {}", e.toString());
            }
            System.out.println(smlDocument.xmlText(new XmlOptions().setSaveAggressiveNamespaces().setSavePrettyPrintIndent(2)));
            throw new RuntimeException("Created invalid document for station " + getStation().getEuropeanCode());
        }
        return smlDocument;
    }

    protected void buildIdentification(SystemType system) {
        IdentifierList idenList = system.addNewIdentification().addNewIdentifierList();

        Identifier ident = idenList.addNewIdentifier();
        Term term = ident.addNewTerm();
        term.setDefinition(UNIQUE_ID_DEFINITION);
        term.setValue(getStationId(getStation()));

        ident = idenList.addNewIdentifier();
        term = ident.addNewTerm();
        term.setDefinition(LONG_NAME_DEFINITION);
        term.setValue(escapeCharacters(getStation().getName()));

        ident = idenList.addNewIdentifier();
        term = ident.addNewTerm();
        term.setDefinition(SHORT_NAME_DEFINITION);
        term.setValue(getStation().getName());
        
        ident = idenList.addNewIdentifier();
        term = ident.addNewTerm();
        term.setDefinition(OFFERING_DEFINITION);
        term.setValue(getOfferingIdentifier(getStation()));
    }

    /**
     * @param systemType
     */
    protected void buildContact(SystemType systemType) {
        Contact contact = systemType.addNewContact();

        ResponsibleParty rp = contact.addNewResponsibleParty();
        rp.setId(CONTACT_IDENTIFIER);
        rp.setIndividualName(CONTACT_INDIVIDUAL_NAME);
        rp.setOrganizationName(CONTACT_ORGANIZATION_NAME);
        ContactInfo ci = rp.addNewContactInfo();
        Address a = ci.addNewAddress();
        a.setCity(CONTACT_CITY);
        a.setPostalCode(CONTACT_POSTAL_CODE);
        a.setCountry(CONTACT_COUNTRY);
        a.setElectronicMailAddress(CONTACT_EMAIL_ADDRESS);
        a.setDeliveryPointArray(new String[] { CONTACT_DELIVERY_POINT });
    }

    /**
     * @param systemType
     */
    protected void buildClassification(SystemType systemType) {
        Classification classification = systemType.addNewClassification();
        ClassifierList classifierList = classification.addNewClassifierList();

        // application
        Classifier classifierIA = classifierList.addNewClassifier();
        classifierIA.setName(CLASSIFIER_NAME_INTENDED_APPLICATION);
        Term termIA = classifierIA.addNewTerm();
        termIA.setDefinition(CLASSIFIER_DEFINITION_INTENDED_APPLICATION);
        termIA.setValue(CLASSIFIER_VALUE_INTENDED_APPLICATION);

        // sensor type (OGC)
        Classifier classifierST = classifierList.addNewClassifier();
        classifierST.setName(CLASSIFIER_NAME_SENSOR_TYPE);
        Term termST = classifierST.addNewTerm();
        termST.setDefinition(CLASSIFIER_DEFINITION_SENSOR_TYPE);
        termST.setValue(getStation().getType());

        // type of sensor (EEA)
        Classifier classifierTS = classifierList.addNewClassifier();
        classifierTS.setName(CLASSIFIER_NAME_TYPE_OF_SENSOR);
        Term termTS = classifierTS.addNewTerm();
        termTS.setDefinition(CLASSIFIER_DEFINITION_TYPE_OF_SENSOR);
        termTS.setValue(getStation().getType());
        
        // ozone classification
        if ( !getStation().getOzoneClassification().trim().isEmpty()) {
            Classifier classifierOzone = classifierList.addNewClassifier();
            classifierOzone.setName(CLASSIFIER_NAME_OZONE);
            Term termOzone = classifierOzone.addNewTerm();
            termOzone.setDefinition(CLASSIFIER_DEFINITION_OZONE);
            termOzone.setValue(getStation().getOzoneClassification().trim());
        }

        // area type
        if ( !getStation().getTypeOfArea().trim().isEmpty()) {
            Classifier classifier = classifierList.addNewClassifier();
            classifier.setName(CLASSIFIER_NAME_AREA_TYPE);
            Term term = classifier.addNewTerm();
            term.setDefinition(CLASSIFIER_DEFINITION_AREA_TYPE);
            term.setValue(getStation().getTypeOfArea().trim());
        }

        // station subcategory type
        if ( !getStation().getSubcatRuralBack().trim().isEmpty()) {
            Classifier classifier = classifierList.addNewClassifier();
            classifier.setName(CLASSIFIER_NAME_SUBCAT_RURAL);
            Term term = classifier.addNewTerm();
            term.setDefinition(CLASSIFIER_DEFINITION_SUBCAT_RURAL);
            term.setValue(getStation().getSubcatRuralBack().trim());
        }

        // street type
        if ( !getStation().getStreetType().trim().isEmpty()) {
            Classifier classifier = classifierList.addNewClassifier();
            classifier.setName(CLASSIFIER_NAME_STREET_TYPE);
            Term term = classifier.addNewTerm();
            term.setDefinition(CLASSIFIER_DEFINITION_STREET);
            term.setValue(escapeCharacters(getStation().getStreetType().trim()));
        }
    }

    /**
     * @param systemType
     */
    protected void buildValidTime(SystemType systemType) {
        String start = getStation().getStartDate();
        String end = getStation().getEndDate();

        if (end.isEmpty()) {
            // set the end data to today
        	end = Utils.REVERSE_DATE_FORMAT.print(new DateTime());
        }
        TimePeriodType validTime = systemType.addNewValidTime().addNewTimePeriod();
        validTime.addNewBeginPosition().setStringValue(start);
        validTime.addNewEndPosition().setStringValue(end);
    }

    protected void buildInputOutputLists(SystemType system) throws NoValidInputsOrOutputsException {
        InputList inputList = system.addNewInputs().addNewInputList();
        OutputList outputList = system.addNewOutputs().addNewOutputList();
        
        Collection<EEAConfiguration> uniqueConfigs = getUniqueConfigurations(getStation());
        
        for (EEAConfiguration configuration : uniqueConfigs) {
        	
            String name = getNameForComponent(configuration.getComponentCode());

            /* inputs */
            IoComponentPropertyType ioComp = inputList.addNewInput();
            ioComp.setName(configuration.getComponentName());
            ObservableProperty obsProp = ioComp.addNewObservableProperty();
            obsProp.setDefinition(getComponentId(configuration.getComponentCode()));

            /* outputs */
            IoComponentPropertyType output = outputList.addNewOutput();
            output.setName(name);
            Quantity quantity = output.addNewQuantity();
            quantity.setDefinition(getPhenomenonId(configuration.getComponentCode()));
            quantity.addNewDescription().setStringValue(configuration.getComponentName());
            quantity.addNewUom().setCode(configuration.getMeasurementUnit().replace(' ', '_'));
        }
    }

    protected void buildLatLonPosition(SystemType systemType) {
        Position position = systemType.addNewPosition();
        position.setName(getStation().getEuropeanCode());

        PositionType positionType = (PositionType) position.addNewProcess().substitute(SOSNamespaceUtils.QN_SWE_1_0_1_POSITION,
                                                                                       PositionType.type);
        positionType.setReferenceFrame(EPSG_4326_REFERENCE_SYSTEM_DEFINITION);
        positionType.setFixed(true);
        VectorType vector = positionType.addNewLocation().addNewVector();

        /* Latitude */
        Coordinate coordLatitude = vector.addNewCoordinate();
        coordLatitude.setName(COORD_NAME_LAT);
        Quantity quantityLatitude = coordLatitude.addNewQuantity();
        quantityLatitude.setAxisID(QUANTITY_AXIS_ID_LAT);
        quantityLatitude.addNewUom().setCode(COORDINATE_UOM);
        quantityLatitude.setValue(Double.parseDouble(getStation().getLatitude()));

        /* Longitude */
        Coordinate coordLongitude = vector.addNewCoordinate();
        coordLongitude.setName(COORD_NAME_LON);
        Quantity quantityLongitude = coordLongitude.addNewQuantity();
        quantityLongitude.setAxisID(QUANTITY_AXIS_ID_LON);
        quantityLongitude.addNewUom().setCode(COORDINATE_UOM);
        quantityLongitude.setValue(Double.parseDouble(getStation().getLongitude()));

        /* Altitude */
        Coordinate coordAltitude = vector.addNewCoordinate();
        coordAltitude.setName(COORD_NAME_ALTITUDE);
        Quantity quantityAltitude = coordAltitude.addNewQuantity();
        quantityAltitude.setAxisID(QUANTITY_AXIS_ID_ALTITUDE);
        quantityAltitude.addNewUom().setCode(METER_UOM);
        quantityAltitude.setValue(Double.parseDouble(getStation().getAltitude()));
    }

    protected void buildDescription(SystemType systemType) {
        systemType.addNewDescription().setStringValue(String.format(SENSOR_DESCRIPTION,
                                                                    getStation().getEuropeanCode(),
                                                                    getStation().getCountryName()));
    }

    protected void buildKeywords(SystemType systemType) {
        KeywordList keywordList = systemType.addNewKeywords().addNewKeywordList();

        if (getStation().getCity() != null && !getStation().getCity().isEmpty()) {
            keywordList.addKeyword(getStation().getCity());
        }
        keywordList.addKeyword(getStation().getCountryName());
        keywordList.addKeyword(getStation().getCountryIsoCode());
        
        for (String s : ADDITIONAL_KEYWORDS) {
        	keywordList.addKeyword(escapeCharacters(s));
        }
    }

    protected void buildComponents(SystemType systemType) {
        // Components components = systemType.addNewComponents();
    }

    protected void buildCapabilities(SystemType systemType) {
        Capabilities capabilities = systemType.addNewCapabilities();
        AbstractDataRecordType abstractDataRecord = capabilities.addNewAbstractDataRecord();
        DataRecordType dataRecord = (DataRecordType) abstractDataRecord.substitute(SOSNamespaceUtils.QN_SWE_1_0_1_DATA_RECORD,
                                                                                   DataRecordType.type);
        dataRecord.setDefinition(CAPABILITIES_DEFINITION);
        DataComponentPropertyType field_bbox = dataRecord.addNewField();
        field_bbox.setName(BBOX_FIELD_NAME);

        // status field
        DataComponentPropertyType statusField = dataRecord.addNewField();
        statusField.setName(STATUS_FIELD_NAME);
        net.opengis.swe.x101.BooleanDocument.Boolean  statusBoolean = statusField.addNewBoolean();
        statusBoolean.setDefinition(STATUS_DEFINITION);
        statusBoolean.setValue(getStation().getEndDate().trim().isEmpty());

        // emep field
        DataComponentPropertyType emepField = dataRecord.addNewField();
        emepField.setName(EMEP_STATION_FIELD_NAME);
        Text emepText = emepField.addNewText();
        emepText.setDefinition(EMEP_STATION_DEFINITION);
        emepText.setValue(getStation().getEMEPStation().trim());

        // local code field
        DataComponentPropertyType localCodeField = dataRecord.addNewField();
        localCodeField.setName(STATION_LOCAL_CODE_FIELD_NAME);
        Text localCodeText = localCodeField.addNewText();
        localCodeText.setDefinition(STATION_LOCAL_CODE_DEFINITION);
        localCodeText.setValue(getStation().getLocalCode().trim());

        // lau_level1_code
        if ( !getStation().getLauLevel1Code().trim().isEmpty()) {
            DataComponentPropertyType lau1CodeField = dataRecord.addNewField();
            lau1CodeField.setName(LAU_LEVEL1_CODE_FIELD_NAME);
            Text text = lau1CodeField.addNewText();
            text.setDefinition(LAU_LEVEL1_CODE_DEFINITION);
            text.setValue(getStation().getLauLevel1Code().trim());
        }

        // lau_level2_code
        if ( !getStation().getLauLevel2Code().trim().isEmpty()) {
            DataComponentPropertyType lau2CodeField = dataRecord.addNewField();
            lau2CodeField.setName(LAU_LEVEL2_CODE_FIELD_NAME);
            Text text = lau2CodeField.addNewText();
            text.setDefinition(LAU_LEVEL2_CODE_DEFINITION);
            text.setValue(getStation().getLauLevel2Code().trim());
        }

        // lau_level2_name
        if ( !getStation().getLauLevel2Name().trim().isEmpty()) {
            DataComponentPropertyType lau2NameField = dataRecord.addNewField();
            lau2NameField.setName(LAU_LEVEL2_NAME_FIELD_NAME);
            Text text = lau2NameField.addNewText();
            text.setDefinition(LAU_LEVEL2_NAME_DEFINITION);
            text.setValue(getStation().getLauLevel2Name().trim());
        }

        // envelope
        double[] lowerCornerPoint = new double[] {Double.parseDouble(getStation().getLatitude()),
                                                  Double.parseDouble(getStation().getLongitude())};
        double[] upperCornerPoint = new double[] {Double.parseDouble(getStation().getLatitude()),
                                                  Double.parseDouble(getStation().getLongitude())};

        EnvelopeType envelope = EnvelopeType.Factory.newInstance();
        envelope.setDefinition(BBOX_DEFINITION);
        envelope.setReferenceFrame(EPSG_4326_REFERENCE_SYSTEM_DEFINITION);
       
        VectorPropertyType lowerCorner = envelope.addNewLowerCorner();
        VectorType lCVector = lowerCorner.addNewVector();
        
        Coordinate lCLat = lCVector.addNewCoordinate();
        lCLat.setName(COORD_NAME_LAT);
        Quantity lCLatQuantity = lCLat.addNewQuantity();
        lCLatQuantity.setAxisID(QUANTITY_AXIS_ID_LAT);
        lCLatQuantity.addNewUom().setCode(UOM_CODE_LATLON);
        lCLatQuantity.setValue(lowerCornerPoint[0]);
        
        Coordinate lCLon = lCVector.addNewCoordinate();
        lCLon.setName(COORD_NAME_LON);
        Quantity lCLonQuantity = lCLon.addNewQuantity();
        lCLonQuantity.setAxisID(QUANTITY_AXIS_ID_LON);
        lCLonQuantity.addNewUom().setCode(UOM_CODE_LATLON);
        lCLonQuantity.setValue(lowerCornerPoint[1]);
        
        VectorPropertyType upperCorner = envelope.addNewUpperCorner();
        VectorType uCVector = upperCorner.addNewVector();
        
        Coordinate uCLat = uCVector.addNewCoordinate();
        uCLat.setName(COORD_NAME_LAT);
        Quantity uCLatQuantity = uCLat.addNewQuantity();
        uCLatQuantity.setAxisID(QUANTITY_AXIS_ID_LAT);
        uCLatQuantity.addNewUom().setCode(UOM_CODE_LATLON);
        uCLatQuantity.setValue(upperCornerPoint[0]);
        
        Coordinate uCLon = uCVector.addNewCoordinate();
        uCLon.setName(COORD_NAME_LON);
        Quantity uCLonQuantity = uCLon.addNewQuantity();
        uCLonQuantity.setAxisID(QUANTITY_AXIS_ID_LON);
        uCLonQuantity.addNewUom().setCode(UOM_CODE_LATLON);
        uCLonQuantity.setValue(upperCornerPoint[1]);

        AbstractDataRecordType bboxAbstractDataRec = field_bbox.addNewAbstractDataRecord();
        EnvelopeType env = (EnvelopeType) bboxAbstractDataRec.substitute(SOSNamespaceUtils.QN_SWE_1_0_1_ENVELOPE,
                                                                         EnvelopeType.type);
        env.set(envelope);
    }

    /**
     * @param s
     * @return
     */
    protected String escapeCharacters(String s) {
        String encoded = s.replace("<", "&lt;");
        encoded = encoded.replace(">", "&gt;");
        encoded = encoded.replace('\'', '_');
        return encoded;
    }
}
