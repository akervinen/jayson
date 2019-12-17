package me.aleksi.jayson;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Represents a JSON object containing key-value pairs.
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public class JSONObject implements JSONStringifyable {
    /**
     * Constant <code>NULL</code> to represent a JSON null value conveniently.
     */
    public static final JSONObject NULL = null;
    private Map<String, JSONValue<?>> kvPairs = new LinkedHashMap<>();

    /**
     * Returns size of this object, i.e. how-many key-value pairs it contains.
     *
     * @return size of this object
     */
    public int size() {
        return kvPairs.size();
    }

    /**
     * Whether this object is empty.
     *
     * @return true if this object is empty
     */
    public boolean isEmpty() {
        return kvPairs.isEmpty();
    }

    /**
     * Insert a key-value pair into this object.
     *
     * <p>Already existing key is overwritten.</p>
     *
     * @param key   {@link String} key of k-v to insert
     * @param value a {@link JSONValue} object to insert
     * @return this object for chaining
     */
    public JSONObject put(String key, JSONValue<?> value) {
        if (value.getType() == JSONType.OBJECT && value.getValue() == this) {
            throw new IllegalArgumentException("cannot place JSONObject inside itself");
        }

        kvPairs.put(key, value);
        return this;
    }

    /**
     * Insert a key-value pair into this object.
     *
     * <p>Already existing key is overwritten.</p>
     *
     * @param key   {@link String} key of k-v to insert
     * @param value a {@link String} object to insert
     * @return this object for chaining
     */
    public JSONObject put(String key, String value) {
        return put(key, JSONValue.from(value));
    }

    /**
     * Insert a key-value pair into this object.
     *
     * <p>Already existing key is overwritten.</p>
     *
     * @param key   {@link String} key of k-v to insert
     * @param value a {@link Number} object to insert
     * @return this object for chaining
     */
    public JSONObject put(String key, Number value) {
        return put(key, JSONValue.from(value));
    }

    /**
     * Insert a key-value pair into this object.
     *
     * <p>Already existing key is overwritten.</p>
     *
     * @param key   {@link String} key of k-v to insert
     * @param value a {@link Boolean} object to insert
     * @return this object for chaining
     */
    public JSONObject put(String key, Boolean value) {
        return put(key, JSONValue.from(value));
    }

    /**
     * Insert a key-value pair into this object.
     *
     * <p>Already existing key is overwritten and prevents adding the object into itself.</p>
     *
     * @param key   {@link String} key of k-v to insert
     * @param value a {@link JSONObject} object to insert
     * @return this object for chaining
     */
    public JSONObject put(String key, JSONObject value) {
        return put(key, JSONValue.from(value));
    }

    /**
     * Insert a key-value pair into this object.
     *
     * <p>Already existing key is overwritten.</p>
     *
     * @param key   {@link String} key of k-v to insert
     * @param value a {@link JSONArray} object to insert
     * @return this object for chaining
     */
    public JSONObject put(String key, JSONArray value) {
        return put(key, JSONValue.from(value));
    }

    /**
     * Get a value with given key, or null if it doesn't exist.
     *
     * <p>JSON null value has its own JSONValue type that can be used to distinguish non-existent vs. null values.</p>
     *
     * @param key key to get
     * @return {@link JSONValue} object at given key, or null
     */
    public JSONValue<?> get(String key) {
        return kvPairs.get(key);
    }

    /**
     * Call given consumer for each key-value pair.
     *
     * @param action a {@link java.util.function.BiConsumer} object to call
     */
    public void forEach(BiConsumer<? super String, ? super JSONValue<?>> action) {
        kvPairs.forEach(action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JSONObject that = (JSONObject) o;
        return kvPairs.equals(that.kvPairs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(kvPairs);
    }

    /**
     * Return this object converted into a JSON string.
     *
     * @return JSON-converted object
     */
    @Override
    public String toString() {
        return toJSONString();
    }

    /**
     * Convert this object into a JSON-format string.
     *
     * <p>Strings are automatically escaped.</p>
     * <p>Invalid number values like NaN or Infinite are written as '0'.</p>
     *
     * @return JSON-format string
     */
    @Override
    public String toJSONString() {
        var sb = new StringBuilder("{");

        forEach((k, v) -> {
            if (sb.length() > 1) {
                sb.append(",");
            }
            sb.append(JSONUtils.quote(k));
            sb.append(":");
            sb.append(v.toJSONString());
        });

        return sb.append("}").toString();
    }
}
