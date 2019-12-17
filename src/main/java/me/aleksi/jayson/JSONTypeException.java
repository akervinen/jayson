package me.aleksi.jayson;

/**
 * JSON type exception.
 * <p>
 * Thrown when trying to get a JSON value with incorrect type,
 * e.g. using getString when actual value is a number.
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public class JSONTypeException extends Exception {
    /**
     * Create a new JSONTypeException with a message using this expected and actual type.
     *
     * @param expected {@link me.aleksi.jayson.JSONType} that was expected
     * @param actual   actual {@link me.aleksi.jayson.JSONType}
     */
    public JSONTypeException(JSONType expected, JSONType actual) {
        super("Error getting JSON value: expected '" + expected + "', got '" + actual + "'");
    }
}
