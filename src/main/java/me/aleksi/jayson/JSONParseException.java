package me.aleksi.jayson;

/**
 * JSON Parser exception.
 * <p>
 * Thrown when JSONReader is unable to parse something it was given.
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public class JSONParseException extends Exception {
    /**
     * Create a new JSONParseException with given parse error message.
     *
     * @param message parse error message
     */
    public JSONParseException(String message) {
        super("Error while parsing JSON: " + message);
    }

    /**
     * Create a new JSONParseException with given parse error message and exception cause.
     *
     * @param message parse error message
     * @param cause   exception that caused this JSONParseException
     */
    public JSONParseException(String message, Throwable cause) {
        super("Error while parsing JSON: " + message, cause);
    }
}
