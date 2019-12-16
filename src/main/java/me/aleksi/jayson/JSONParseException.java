package me.aleksi.jayson;

/**
 * <p>JSONParseException class.</p>
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public class JSONParseException extends Exception {
    /**
     * <p>Constructor for JSONParseException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public JSONParseException(String message) {
        super("Error while parsing JSON: " + message);
    }

    /**
     * <p>Constructor for JSONParseException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause   a {@link java.lang.Throwable} object.
     */
    public JSONParseException(String message, Throwable cause) {
        super("Error while parsing JSON: " + message, cause);
    }
}
