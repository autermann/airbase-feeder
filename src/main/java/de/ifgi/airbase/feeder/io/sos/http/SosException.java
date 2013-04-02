/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ifgi.airbase.feeder.io.sos.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public enum SosException {
    SENSOR_ALREADY_REGISTERED("Station already registered.", false,
                              ".*The offering with the identifier '(.*)' still exists in this service and it is not allowed to insert more than one procedure to an offering!.*",
                              ".*Sensor with ID: .* is already registered at this SOS!",
                              "The requested offering identifier (.*) is already provided by this server!.*",
                              ".*The offering with the identifier '.*' still exists in this service.*"),
    OBSERVATION_ALREADY_REGISTERED("Observation already registered.", false,
                                   ".*FEHLER: doppelter Schlüsselwert verletzt Unique-Constraint.*"),
    RESULT_TEMPLATE_ALREADY_REGISTERED("ResultTemplate already registered.", false,
                                       ".*The requested template identifier (.*) still contains in this service!.*",
                                       ".*The requested resultTemplate identifier (.*) is already registered at this service.*"),
    UNKNOWN("Unknown", true);
    private List<Pattern> pattern;
    private String message;
    private boolean shouldFail;

    private SosException(String message, boolean shouldFail, String... regex) {
        this.shouldFail = shouldFail;
        this.message = message;
        this.pattern = new ArrayList<Pattern>(regex.length);
        for (String s : regex) {
            this.pattern.add(Pattern.compile(s, Pattern.DOTALL));
        }
    }

    public boolean matches(String exception) {
        for (Pattern r : getPattern()) {
            if (r.matcher(exception).matches()) {
                return true;
            }
        }
        return false;
    }

    public String getMessage() {
        return message;
    }

    public List<Pattern> getPattern() {
        return Collections.unmodifiableList(pattern);
    }
    
    public boolean isFatal() {
        return shouldFail;
    }

    public static SosException fromErrorMessage(String message) {
        if (message != null) {
            for (SosException ke : values()) {
                if (ke.matches(message)) {
                    return ke;
                }
            }
        }
        return UNKNOWN;
    }
    
}