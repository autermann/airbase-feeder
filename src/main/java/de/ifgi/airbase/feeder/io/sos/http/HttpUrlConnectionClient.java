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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ifgi.airbase.feeder.util.Utils;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class HttpUrlConnectionClient {

    private static final Logger log = LoggerFactory.getLogger(HttpUrlConnectionClient.class);
    private static final String CONNECTION_TIMEOUT_PROPERTY = "sun.net.client.defaultConnectTimeout";
    private static final String READ_TIMEOUT_PROPERTY = "sun.net.client.defaultReadTimeout";
    private static final String SEND_DATA_PROPERTY = "eea.sendData";
    private static final String MIME_TYPE_HTTP_HEADER = "Content-Type";
    private static final String XML_MIME_TYPE = "application/xml";
    private static final String HTTP_METHOD = "POST";
    private static final int CONNECTION_TIMEOUT = getInteger(CONNECTION_TIMEOUT_PROPERTY);
    private static final int READ_TIMEOUT = getInteger(READ_TIMEOUT_PROPERTY);
    private boolean sendingData = getBoolean(SEND_DATA_PROPERTY);
    private final URL url;

    public HttpUrlConnectionClient(URL url) {
        this.url = url;
        if (!this.sendingData) {
            log.warn("Not sending any data, just printing it out!");
        }
    }

    public URL getURL() {
        return this.url;
    }

    private static int getInteger(String key) {
        String value = Utils.get(key);
        int val = Integer.MAX_VALUE;
        if (value != null) {
            try {
                val = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                log.error("Could not parse key.", e);
            }
        }
        return val;
    }

    private static boolean getBoolean(String key) {
        String value = Utils.get(key);
        boolean val = true;
        if (value != null) {
            val = Boolean.parseBoolean(value);
        }
        return val;
    }

    public InputStream post(String xml) throws IOException, SocketTimeoutException {
        if (!this.sendingData) {
            log.warn("Not sending any data!");
            System.out.println(xml);
            throw new RuntimeException("Debugging, not sending any data!");
        }

        OutputStreamWriter wr = null;
        Properties systemProperties = System.getProperties();
        systemProperties.setProperty(CONNECTION_TIMEOUT_PROPERTY, String.valueOf(CONNECTION_TIMEOUT));
        systemProperties.setProperty(READ_TIMEOUT_PROPERTY, String.valueOf(READ_TIMEOUT));
        try {
            HttpURLConnection conn = (HttpURLConnection) this.getURL().openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod(HTTP_METHOD);
            conn.setRequestProperty(MIME_TYPE_HTTP_HEADER, XML_MIME_TYPE);
            log.debug("Sending Request...");
            wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(xml);
            wr.flush();
            return conn.getInputStream();
        } catch (SocketTimeoutException e) {
            return null;
        } finally {
            if (wr != null) {
                wr.close();
            }
            System.getProperties().remove(CONNECTION_TIMEOUT_PROPERTY);
            System.getProperties().remove(READ_TIMEOUT_PROPERTY);
        }
    }
}
