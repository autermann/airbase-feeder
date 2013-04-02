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
package de.ifgi.airbase.feeder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

import net.opengis.sos.x10.InsertObservationDocument;
import net.opengis.sos.x10.RegisterSensorDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import de.ifgi.airbase.feeder.io.filter.CompositeFileFilter;
import de.ifgi.airbase.feeder.io.filter.FileExtensionFilter;
import de.ifgi.airbase.feeder.io.filter.PrefixFileFilter;
import de.ifgi.airbase.feeder.io.sos.http.SosException;
import de.ifgi.airbase.feeder.io.sos.http.TransactionalSosClient;

public class StaticFileFeeder extends TransactionalSosClient implements Runnable {
	
	private List<File> files = null;
	
	public StaticFileFeeder(List<File> files) {
		this.files = files;
	}

	@Override
	public void run() {
		for (File f : this.files) {
			try {
				XmlObject xo = XmlObject.Factory.parse(f);
				String req = xo.toString();
				if (xo instanceof RegisterSensorDocument) {
					log.info("Posting RegisterSensorDocument: {}", f.getName());
				} else if (xo instanceof InsertObservationDocument) {
					log.info("Posting InsertObservationDocument: {}", f.getName());
				}
				InputStream in;
				while ((in = post(req)) == null) {
                    log.warn("Connection failed. Trying again.");
                }
                SosException processResponse = processResponse(in);
                if (processResponse != null && processResponse.isFatal()) {
                    f.delete();
                }
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XmlException e) {
				e.printStackTrace();
			}
		}
		log.info("Done.");
	}
	
	public static void processDirectory(File dir) {
		if (dir == null) {
			throw new NullPointerException();
		}
		if (!dir.exists() || !dir.isDirectory() || !dir.canRead()) {
			throw new InvalidParameterException("Not a readable directory: " + dir.getAbsolutePath());
		}
		LinkedList<File> filesToProcess = new LinkedList<File>();
		for (File file :  dir.listFiles(new CompositeFileFilter(new FileExtensionFilter("xml"), new  PrefixFileFilter("RegisterSensor")))) {
			filesToProcess.add(file);
		}
		for (File file : dir.listFiles(new CompositeFileFilter(new FileExtensionFilter("xml"), new PrefixFileFilter("InsertObservation")))) {
			filesToProcess.add(file);
		}
		new Thread(new StaticFileFeeder(filesToProcess)).start();
	}
	
	public static void main(String[] args) {
		File directory = new File("/tmp/AirBase_Feeder_TEMP/FailedRequests");
		processDirectory(directory);
	}
}
