package me.aleksi.jayson;

import java.util.Objects;

/**
 * <p>JSONValue class.</p>
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public class JSONValue<T> implements JSONStringifyable {
    private JSONType type;
    private T value;

    private JSONValue(JSONType type, T value) {
        this.type = type;
        this.value = value;
    }

    private static boolean containsControlChars(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) <= 0x1F) {
                return true;
            }
        }

        return false;
    }

    private static String escapeControlChars(String str) {
        var sb = new StringBuilder(str);

        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);

            if (c <= 0x1F) {
                // Escape control chars
                sb.deleteCharAt(i);
                sb.insert(i, String.format("\\u00%02X", (int) c));
            }
        }

        return sb.toString();
    }

    /**
     * <p>quote.</p>
     *
     * @param str a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String quote(String str) {
        // Common control characters have their own escape characters
        str = str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\b", "\\b")
            .replace("\r", "\\r")
            .replace("\f", "\\f");

        // Escape rest of the control characters if needed
        if (containsControlChars(str)) {
            str = escapeControlChars(str);
        }

        return "\"" + str + "\"";
    }

    /**
     * <p>from.</p>
     *
     * @param value a {@link java.lang.String} object.
     * @return a {@link me.aleksi.jayson.JSONValue} object.
     */
    public static JSONValue<?> from(String value) {
        if (value == null) return new JSONValue<>(JSONType.NULL, null);
        return new JSONValue<>(JSONType.STRING, value);
    }

    /**
     * <p>from.</p>
     *
     * @param value a {@link java.lang.Number} object.
     * @return a {@link me.aleksi.jayson.JSONValue} object.
     */
    public static JSONValue<?> from(Number value) {
        if (value == null) return new JSONValue<>(JSONType.NULL, null);
        return new JSONValue<>(JSONType.NUMBER, value);
    }

    /**
     * <p>from.</p>
     *
     * @param value a {@link java.lang.Boolean} object.
     * @return a {@link me.aleksi.jayson.JSONValue} object.
     */
    public static JSONValue<?> from(Boolean value) {
        if (value == null) return new JSONValue<>(JSONType.NULL, null);
        return new JSONValue<>(JSONType.BOOLEAN, value);
    }

    /**
     * <p>from.</p>
     *
     * @param value a {@link me.aleksi.jayson.JSONObject} object.
     * @return a {@link me.aleksi.jayson.JSONValue} object.
     */
    public static JSONValue<?> from(JSONObject value) {
        if (value == null) return new JSONValue<>(JSONType.NULL, null);
        return new JSONValue<>(JSONType.OBJECT, value);
    }

    /**
     * <p>from.</p>
     *
     * @param value a {@link me.aleksi.jayson.JSONArray} object.
     * @return a {@link me.aleksi.jayson.JSONValue} object.
     */
    public static JSONValue<?> from(JSONArray value) {
        if (value == null) return new JSONValue<>(JSONType.NULL, null);
        return new JSONValue<>(JSONType.ARRAY, value);
    }

    /**
     * <p>Getter for the field <code>type</code>.</p>
     *
     * @return a {@link me.aleksi.jayson.JSONType} object.
     */
    public JSONType getType() {
        return type;
    }

    /**
     * <p>Setter for the field <code>type</code>.</p>
     *
     * @param type a {@link me.aleksi.jayson.JSONType} object.
     */
    public void setType(JSONType type) {
        this.type = type;
    }

    /**
     * <p>Getter for the field <code>value</code>.</p>
     *
     * @return a T object.
     */
    public T getValue() {
        return value;
    }

    /**
     * <p>Setter for the field <code>value</code>.</p>
     *
     * @param value a T object.
     */
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * <p>getNumber.</p>
     *
     * @return a {@link java.lang.Number} object.
     * @throws me.aleksi.jayson.JSONTypeException if any.
     */
    public Number getNumber() throws JSONTypeException {
        if (type != JSONType.NUMBER && type != JSONType.NULL) {
            throw new JSONTypeException(JSONType.NUMBER, type);
        }
        return (Number) value;
    }

    /**
     * <p>getString.</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws me.aleksi.jayson.JSONTypeException if any.
     */
    public String getString() throws JSONTypeException {
        if (type != JSONType.STRING && type != JSONType.NULL) {
            throw new JSONTypeException(JSONType.STRING, type);
        }
        return (String) value;
    }

    /**
     * <p>getObject.</p>
     *
     * @return a {@link me.aleksi.jayson.JSONObject} object.
     * @throws me.aleksi.jayson.JSONTypeException if any.
     */
    public JSONObject getObject() throws JSONTypeException {
        if (type != JSONType.OBJECT && type != JSONType.NULL) {
            throw new JSONTypeException(JSONType.OBJECT, type);
        }
        return (JSONObject) value;
    }

    /**
     * <p>getArray.</p>
     *
     * @return a {@link me.aleksi.jayson.JSONArray} object.
     * @throws me.aleksi.jayson.JSONTypeException if any.
     */
    public JSONArray getArray() throws JSONTypeException {
        if (type != JSONType.ARRAY && type != JSONType.NULL) {
            throw new JSONTypeException(JSONType.ARRAY, type);
        }
        return (JSONArray) value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JSONValue<?> jsonValue = (JSONValue<?>) o;
        return type == jsonValue.type &&
            Objects.equals(value, jsonValue.value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toJSONString(new JSONWriterOptions());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toJSONString(JSONWriterOptions options) {
        switch (type) {
            case OBJECT:
                return ((JSONObject) value).toJSONString(options);
            case ARRAY:
                return ((JSONArray) value).toJSONString(options);
            case STRING:
                return quote((String) value);
            case NULL:
                return "null";
            case NUMBER:
                if (value instanceof Float) {
                    if (Float.isNaN((Float) value)) {
                        return "0";
                    }
                    if (Float.isInfinite((Float) value)) {
                        return "0";
                    }
                } else if (value instanceof Double) {
                    if (Double.isNaN((Double) value)) {
                        return "0";
                    }
                    if (Double.isInfinite((Double) value)) {
                        return "0";
                    }
                }
                break;
        }

        return value.toString();
    }
}
