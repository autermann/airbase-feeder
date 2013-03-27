package de.ifgi.airbase.feeder.io.sos.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sos.x20.InsertResultTemplateDocument;
import net.opengis.swes.x20.InsertSensorDocument;
import net.opengis.swes.x20.UpdateSensorDescriptionDocument;

import de.ifgi.airbase.feeder.Configuration;
import de.ifgi.airbase.feeder.data.EEAConfiguration;
import de.ifgi.airbase.feeder.data.EEAMeasurement;
import de.ifgi.airbase.feeder.data.EEARawDataFile;
import de.ifgi.airbase.feeder.data.EEAStation;
import de.ifgi.airbase.feeder.io.sos.http.xml.EncodingException;
import de.ifgi.airbase.feeder.io.sos.http.xml.InsertResultRequestBuilder;
import de.ifgi.airbase.feeder.io.sos.http.xml.InsertResultTemplateRequestBuilder;
import de.ifgi.airbase.feeder.io.sos.http.xml.InsertSensorRequestBuilder;
import de.ifgi.airbase.feeder.io.sos.http.xml.SensorDescriptionBuilder;
import de.ifgi.airbase.feeder.io.sos.http.xml.SoapRequestBuilder;
import de.ifgi.airbase.feeder.io.sos.http.xml.UpdateSensorDescriptionRequestBuilder;
import de.ifgi.airbase.feeder.util.Utils;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class TransactionalSosV2Client extends AbstractTransactionalSosClient {
    
    private static final String INSERT_SENSOR_REQUEST_FILE_PREFIX = "InsertSensor-";
    private static final String INSERT_RESULT_REQUEST_FILE_PREFIX = "InsertResult-";
    private static final String INSERT_RESULT_TEMPLATE_REQUEST_FILE_PREFIX = "InsertResultTemplate-";
    private static final String UPDATE_SENSOR_DESCRIPTION_FILE_PREFIX = "UpdateSensorDescription-";
    
    @Override
    public void registerStation(EEAStation station) throws IOException, RequestFailedException {
        insertSensor(station);
        insertResultTemplates(station);
    }

    private void insertResultTemplates(EEAStation station) throws IOException, RequestFailedException {
        Set<Integer> componentCodes = new HashSet<Integer>(Configuration.getInstance().getComponentsToParse().size());
        for (EEAConfiguration configuration : station.getConfigurations()) {
            final int componentCode = configuration.getComponentCode(); 
            if (!Configuration.getInstance().shouldBeIgnored(componentCode) && !componentCodes.contains(componentCode)) {
                insertResultTemplate(station, configuration);
                componentCodes.add(componentCode);
            }
        }
    }

    private void insertResultTemplate(EEAStation station, EEAConfiguration configuration) throws IOException, RequestFailedException {
        try {
            long start = System.currentTimeMillis();
            
            InsertResultTemplateDocument doc = new InsertResultTemplateRequestBuilder()
                    .setConfiguration(configuration).setStation(station).build();
            String req = new SoapRequestBuilder().setBody(doc).asString();
            String buildTime = Utils.timeElapsed(start);
            InputStream in = request(req);
            log.info("Build InsertResulteTemplateRequest for {} in {}; SOS processed it in {}.", new Object[]{
                        station.getEuropeanCode(), buildTime, Utils.timeElapsed(start)});
            SosException processResponse = processResponse(in);

            if (processResponse != null && processResponse.isFatal()) {
                String file = printRequest(req,
                        getFailedPath(),
                        INSERT_RESULT_TEMPLATE_REQUEST_FILE_PREFIX,
                        station.getEuropeanCode(),"-",
                        String.valueOf(configuration.getComponentCode()),
                        ".xml");
                throw new RequestFailedException("Request '" + file + "'  failed.");
            }
        } catch (EncodingException ex) {
            log.error("Could not encoder InsertResultTemplate request", ex);
        }
    }

    private void insertSensor(EEAStation station) throws IOException, RequestFailedException {
        try {
            long start = System.currentTimeMillis();
            SensorMLDocument build;
            InsertSensorDocument insertSensorDocument;
            try {
                build = new SensorDescriptionBuilder().setStation(station).build();
                insertSensorDocument = new InsertSensorRequestBuilder().setStation(station)
                        .setDescription(build).build();
            } catch (EncodingException e) {
                log.info("Station {} has no valid input/outputs.", station.getEuropeanCode());
                return;
            }
            String req = new SoapRequestBuilder().setBody(insertSensorDocument).asString();
            printRequest(req, getPath(), INSERT_SENSOR_REQUEST_FILE_PREFIX, station.getEuropeanCode(), ".xml");
            String buildTime = Utils.timeElapsed(start);
            InputStream in = request(req);
            log.info("Build RegisterSensor for {} in {}; SOS processed it in {}.", new Object[]{
                station.getEuropeanCode(), buildTime, Utils.timeElapsed(start)});
            SosException processResponse = processResponse(in);
            if (processResponse != null && processResponse.isFatal()) {
                String file = printRequest(req, getFailedPath(), INSERT_SENSOR_REQUEST_FILE_PREFIX,
                        station.getEuropeanCode(), ".xml");
                throw new RequestFailedException("Request '" + file + "'  failed.");
            } else if (processResponse != null && processResponse == SosException.SENSOR_ALREADY_REGISTERED) {
                updateSensor(station, build);
            }
        } catch (EncodingException ex) {
            log.error("Could not encode InsertSensor request", ex);
        }
    }

    @Override
    protected void insertObservations(int reqNo, EEARawDataFile file, List<EEAMeasurement> valuesToInsert) throws IOException, RequestFailedException {
        try {
            String req = new SoapRequestBuilder().setBody(
                    new InsertResultRequestBuilder().setValues(valuesToInsert)
                            .setFile(file).build()).asString();
            printRequest(req, getPath(), INSERT_RESULT_REQUEST_FILE_PREFIX, file.getStation()
                    .getEuropeanCode(), "-", String.valueOf(file.getConfiguration().getComponent()
                    .getCode()), "-", String.valueOf(reqNo), ".xml");
            long requestStart = System.currentTimeMillis();
            InputStream in = request(req);
            
            log.debug("Build InsertResult {} for Station {}; SOS processed it in {}.",
                      new Object[] {Integer.valueOf(reqNo), 
                                    file.getStation().getEuropeanCode(),
                                    Utils.timeElapsed(requestStart)});
            SosException processResponse = processResponse(in);

            if (processResponse != null && processResponse.isFatal()) {
                String f = printRequest(req, getFailedPath(), INSERT_RESULT_REQUEST_FILE_PREFIX,
                             file.getStation().getEuropeanCode(), "-",
                             String.valueOf(file.getConfiguration().getComponent().getCode()),
                             "-", String.valueOf(reqNo), ".xml");
                throw new RequestFailedException("Request '" + f + "'  failed.");
            }
        } catch (EncodingException ex) {
            log.error("Could not encode InsertResult request", ex);
        }
    }

    private void updateSensor(EEAStation station, SensorMLDocument build) throws IOException, RequestFailedException {
        try {
            long start = System.currentTimeMillis();
            UpdateSensorDescriptionDocument update =
                                            new UpdateSensorDescriptionRequestBuilder().setDescription(build).setStation(station).build();
            String req = new SoapRequestBuilder().setBody(update).asString();
            printRequest(req, getPath(), UPDATE_SENSOR_DESCRIPTION_FILE_PREFIX, station.getEuropeanCode(), ".xml");
            String buildTime = Utils.timeElapsed(start);
            InputStream in = request(req);
            log.info("Build UpdateSensor for {} in {}; SOS processed it in {}.", new Object[] {
                station.getEuropeanCode(), buildTime, Utils.timeElapsed(start) });
            SosException processResponse = processResponse(in);
            if (processResponse != null && processResponse.isFatal()) {
                String file = printRequest(req, getFailedPath(), UPDATE_SENSOR_DESCRIPTION_FILE_PREFIX,
                                           station.getEuropeanCode(), ".xml");
                throw new RequestFailedException("Request '" + file + "'  failed.");
            }
        } catch (EncodingException ex) {
            log.error("Could not encode InsertSensor request", ex);
        }
    }
}
