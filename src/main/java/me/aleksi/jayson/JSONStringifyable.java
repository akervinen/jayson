package me.aleksi.jayson;

/**
 * Interface for objects that can be turned into JSON-format strings.
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
interface JSONStringifyable {
    /**
     * Convert this object into a JSON-format string.
     *
     * @param options writing options as a {@link JSONWriterOptions} object
     * @return JSON-format string
     */
    String toJSONString(JSONWriterOptions options);
}
