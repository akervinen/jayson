package me.aleksi.jayson;

/**
 * <p>JSONTypeException class.</p>
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public class JSONTypeException extends Exception {
    /**
     * <p>Constructor for JSONTypeException.</p>
     *
     * @param expected a {@link me.aleksi.jayson.JSONType} object.
     * @param actual   a {@link me.aleksi.jayson.JSONType} object.
     */
    public JSONTypeException(JSONType expected, JSONType actual) {
        super("Error getting JSON value: expected '" + expected + "', got '" + actual + "'");
    }
}
