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
package de.ifgi.airbase.feeder.io.sos.http;
// <editor-fold defaultstate="collapsed" desc="Imports">
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.opengis.ows.x11.ExceptionDocument;
import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.ows.x11.ExceptionType;
import net.opengis.sos.x10.InsertObservationResponseDocument;
import net.opengis.sos.x10.RegisterSensorResponseDocument;
import net.opengis.sos.x20.InsertResultResponseDocument;
import net.opengis.sos.x20.InsertResultTemplateResponseDocument;
import net.opengis.swes.x20.InsertSensorResponseDocument;
import net.opengis.swes.x20.UpdateSensorDescriptionResponseDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.w3.x2003.x05.soapEnvelope.Body;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;
import org.w3.x2003.x05.soapEnvelope.FaultDocument;
import org.w3c.dom.Node;

import de.ifgi.airbase.feeder.Configuration;
import de.ifgi.airbase.feeder.data.EEAMeasurement;
import de.ifgi.airbase.feeder.data.EEARawDataFile;
import de.ifgi.airbase.feeder.io.filter.TimeRangeFilter;
import de.ifgi.airbase.feeder.io.sos.SosClient;
import de.ifgi.airbase.feeder.util.Utils;
// </editor-fold>
/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class AbstractTransactionalSosClient extends SosClient {
    private static final String SOS_PRINT_PATH_PROPERTY = "eea.sosRequestPrintPath";
    private static final String SOS_URL_PROPERTY = "eea.sosTransactionalUrl";
    private static final String VALUES_PRO_REQUEST_PROPERTY = "eea.valuesProRequest";
    private static final int VALUES_PER_REQUEST = Integer.parseInt(Utils.get(VALUES_PRO_REQUEST_PROPERTY));
    private URL url = null;
    private String path = null;
    private String failedPath = Utils.getFailedRequestPrintPath();
    private HttpUrlConnectionClient client;
    
    protected SosException processInvalidXml(InputStream response) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(response));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append("\n").append(line);
            }
            log.warn("Unable to read Response: {}", sb.toString());
        } finally {
            if (br != null) {
                br.close();
            }
        }
        return SosException.UNKNOWN;
    }

    protected SosException processExceptionDocument(XmlObject xml) {
        ExceptionType exceptionType;
        if (xml instanceof ExceptionDocument) {
            exceptionType = ((ExceptionDocument) xml).getException();
        } else {
            exceptionType = (ExceptionType) xml;
        }
        StringBuilder sb = new StringBuilder();
        for (String s : exceptionType.getExceptionTextArray()) {
            SosException ke = SosException.fromErrorMessage(s);
            if (ke != null) {
                log.info(ke.getMessage());
                return ke;
            }
            sb.append("\n\t").append(s.trim());
        }
        log.warn("Request failed: {}", sb.toString());
        return SosException.UNKNOWN;
    }

    protected SosException processExceptionReportDocument(XmlObject xml) throws IOException {
        for (ExceptionType exceptionReport : ((ExceptionReportDocument) xml).getExceptionReport().getExceptionArray()) {
            return processExceptionDocument(exceptionReport);
        }
        return SosException.UNKNOWN;
    }

    protected SosException processInsertObservationResponseDocument(XmlObject xml) {
        log.info("Inserted Observation; assigned Id: {}",
                ((InsertObservationResponseDocument) xml).getInsertObservationResponse().getAssignedObservationId());
        return null;
    }

    protected SosException processRegisterSensorResponseDocument(XmlObject xml) {
        log.info("Registered station; assigned Id: {}",
                ((RegisterSensorResponseDocument) xml).getRegisterSensorResponse().getAssignedSensorId());
        return null;
    }

    protected SosException processInsertResultTemplateResponseDocument(XmlObject xml) {
        log.info("Inserted ResultTemplate; assigned Id: {}", ((InsertResultTemplateResponseDocument) xml)
                .getInsertResultTemplateResponse().getAcceptedTemplate());
        return null;
    }

    protected SosException processInsertResultResponseDocument(XmlObject xml) {
        log.info("Inserted Result");
        return null;
    }

    protected SosException processInsertSensorResponseDocument(XmlObject xml) {
        InsertSensorResponseDocument insertSensorResponseDocument = ((InsertSensorResponseDocument) xml);
        log.info("Registered station; assigned Id: {}, Offering: {}",
                insertSensorResponseDocument.getInsertSensorResponse().getAssignedProcedure(),
                insertSensorResponseDocument.getInsertSensorResponse().getAssignedOffering());
        return null;
    }

    protected SosException processUpdateSensorDescriptionResponseDocument(XmlObject xml) {
        UpdateSensorDescriptionResponseDocument usdr = ((UpdateSensorDescriptionResponseDocument) xml);
        log.info("Updated station; id: {}", usdr.getUpdateSensorDescriptionResponse().getUpdatedProcedure());
        return null;
    }


    protected SosException processFaultDocument(XmlObject xml) throws IOException {
        return processResponse(nodeToInputStream(((FaultDocument)xml).getFault().getDetail().getDomNode().getFirstChild()));
    }

    protected SosException processEnvelopeDocument(XmlObject xml) throws IOException, RuntimeException {
        try {
            Body body = ((EnvelopeDocument) xml).getEnvelope().getBody();
            SOAPMessage soapMessage = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL)
                    .createMessage(new MimeHeaders(), xml.newInputStream());
            return processResponse(nodeToInputStream(soapMessage.getSOAPBody().extractContentAsDocument()));
        } catch (SOAPException se) {
            throw new RuntimeException(se);
        }
    }
    
    protected InputStream post(String xml) throws IOException {
        if (client == null) {
            client = new HttpUrlConnectionClient(getUrl());
        }
        return client.post(xml);
    }
    
    protected URL getUrl() {
        if (this.url == null) {
            String urlProp = Utils.get(SOS_URL_PROPERTY);
            if (urlProp == null) {
                throw new NullPointerException();
            }
            try {
                this.url = new URL(urlProp);
            } catch (MalformedURLException e) {
                throw new Error(e);
            }
        }
        return this.url;
    }
    
    protected InputStream request(String req) throws IOException {
        InputStream in;
        while ((in = post(req)) == null) {
            log.warn("Connection failed. Trying again.");
        }
        return in;
    }    
    
    protected String getPath() {
        if (this.path != null) {
            String p = Utils.get(SOS_PRINT_PATH_PROPERTY);
            if (p != null) {
                File f = new File(p);
                if (!f.exists()) {
                    f.mkdirs();
                    this.path = p;
                }
            }
        }
        return this.path;
    }
    
    protected String printRequest(String string, String p, String... filenameComponents) throws IOException {
        if (p != null) {
            BufferedWriter bw = null;
            try {
                StringBuilder sb = new StringBuilder().append(p).append(File.separator);
                for (String s : filenameComponents) {
                    sb.append(s);
                }
                String fileName = sb.toString();
                log.info("Writing request to file {}", fileName);
                bw = new BufferedWriter(new FileWriter(new File(fileName)));
                bw.append(string);
                return fileName;
            }
            catch (IOException e) {
                log.error("Error saving file", e);
                throw e;
            }
            finally {
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e) { }
                }
            }
        } else {
            return null;
        }
    }

    public String getFailedPath() {
        return failedPath;
    }
    
    public InputStream nodeToInputStream(Node node) {
        StringWriter sw = null;
        try {
            sw = new StringWriter();
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            t.transform(new DOMSource(node), new StreamResult(sw));
            return new ByteArrayInputStream(sw.toString().getBytes());
        } catch (TransformerException te) {
            throw new RuntimeException(te);
        } finally {
            try {
                if (sw != null) {
                    sw.close();
                }
            } catch (IOException ioe) { }
        }
    }

    protected SosException processResponse(InputStream response) throws IOException {
        try {
            XmlObject xml = XmlObject.Factory.parse(response, new XmlOptions()
                    .setLoadStripWhitespace().setLoadStripComments().setLoadStripProcinsts());
            if (xml instanceof EnvelopeDocument) {
                return processEnvelopeDocument(xml);
            } else if (xml instanceof FaultDocument) {
                return processFaultDocument(xml);
            } else if (xml instanceof InsertSensorResponseDocument) {
                return processInsertSensorResponseDocument(xml);
            } else if (xml instanceof UpdateSensorDescriptionResponseDocument) {
                return processUpdateSensorDescriptionResponseDocument(xml);
            } else if (xml instanceof InsertResultResponseDocument) {
                return processInsertResultResponseDocument(xml);
            } else if (xml instanceof InsertResultTemplateResponseDocument) {
                return processInsertResultTemplateResponseDocument(xml);
            } else if (xml instanceof RegisterSensorResponseDocument) {
                return processRegisterSensorResponseDocument(xml);
            } else if (xml instanceof InsertObservationResponseDocument) {
                return processInsertObservationResponseDocument(xml);
            } else if (xml instanceof ExceptionReportDocument) {
                return processExceptionReportDocument(xml);
            } else if (xml instanceof ExceptionDocument) {
                return processExceptionDocument(xml);
            } else {
                log.warn("Unable to read Response {}:\n{}",xml.getClass(), xml);
                return SosException.UNKNOWN;
            }
        } catch (XmlException ex) {
            return processInvalidXml(response);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
    
     /*
     * (non-Javadoc)
     * 
     * @see de.ifgi.airbase.feeder.io.sos.http.SosClient#insertObservations(de.ifgi
     * .airbase.feeder.data.EEARawDataFile)
     */
    @Override
    public void insertObservations(EEARawDataFile file) throws IOException, RequestFailedException {
        long start = System.currentTimeMillis();
        int index = 0, requestCount = 0;
        TimeRangeFilter trf = Configuration.getInstance().getTimeRangeFilter();
        List<EEAMeasurement> allMeasurements = file.getMeasurements();
        while (index < allMeasurements.size()) {
            List<EEAMeasurement> valuesToInsert = new LinkedList<EEAMeasurement>();
            while (valuesToInsert.size() < VALUES_PER_REQUEST && index < allMeasurements.size()) {
                EEAMeasurement e = allMeasurements.get(index);
                if (e.isValid() && (trf == null || trf.accept(e)) && !isDstDuplicate(e)) {
                    valuesToInsert.add(e);
                }
                index++;
            }
            if (!valuesToInsert.isEmpty()) {
                insertObservations(requestCount, file, valuesToInsert);
            }
            requestCount++;
        }
        log.info("Processed {} in {}", file.getFileName(), Utils.timeElapsed(start));
    }
    
    protected abstract void insertObservations(int req, EEARawDataFile file, List<EEAMeasurement> valuesToInsert) throws IOException, RequestFailedException;

    private boolean isDstDuplicate(EEAMeasurement e) {
        DateTime d = new DateTime(DateTimeZone.UTC)
                .withYear(e.getTime().getYear())
                .withMonthOfYear(DateTimeConstants.OCTOBER)
                .withDayOfMonth(getLastSundayOfOctober(e.getTime().getYear()))
                .withHourOfDay(1)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);
        return d.equals(e.getTime());
    }

    protected int getLastSundayOfOctober(int year) {
        LocalDate ld = new LocalDate(year, DateTimeConstants.OCTOBER, 1)
                .dayOfMonth()
                .withMaximumValue()
                .dayOfWeek()
                .setCopy(DateTimeConstants.SUNDAY);
        if (ld.getMonthOfYear() != DateTimeConstants.OCTOBER) {
            return ld.minus(Period.days(7)).getDayOfMonth();
        } else {
            return ld.getDayOfMonth();
        }
    }
}
