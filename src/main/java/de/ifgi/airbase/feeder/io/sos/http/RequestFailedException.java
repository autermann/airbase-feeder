/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ifgi.airbase.feeder.io.sos.http;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class RequestFailedException extends Exception {
    private static final long serialVersionUID = 6255358204173302981L;

    public RequestFailedException(String message) {
        super(message);
    }
    
}
