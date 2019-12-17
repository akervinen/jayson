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
     * @return JSON-format string
     */
    String toJSONString();
}
