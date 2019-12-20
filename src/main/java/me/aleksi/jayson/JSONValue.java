package me.aleksi.jayson;

import java.util.Objects;

/**
 * Represents any valid JSON value.
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public class JSONValue<T> implements JSONStringifyable {
    private final JSONType type;
    private final T value;

    /**
     * Create a new value of given type and value.
     *
     * <p>Use {@link JSONValue#from} methods instead to prevent mismatched type and value.</p>
     *
     * @param type  which type this value is
     * @param value value object
     */
    private JSONValue(JSONType type, T value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Create a new {@link JSONValue} from given String.
     *
     * <p>Strings are automatically escaped when outputting as JSON.</p>
     *
     * @param value String value
     * @return {@link JSONValue} object containing given String
     */
    public static JSONValue<?> from(String value) {
        if (value == null) return new JSONValue<>(JSONType.NULL, null);
        return new JSONValue<>(JSONType.STRING, value);
    }

    /**
     * Create a new {@link JSONValue} from given Number.
     *
     * @param value Number value
     * @return {@link JSONValue} object containing given Number
     */
    public static JSONValue<?> from(Number value) {
        if (value == null) return new JSONValue<>(JSONType.NULL, null);
        return new JSONValue<>(JSONType.NUMBER, value);
    }

    /**
     * Create a new {@link JSONValue} from given Boolean.
     *
     * @param value Boolean value
     * @return {@link JSONValue} object containing given Boolean
     */
    public static JSONValue<?> from(Boolean value) {
        if (value == null) return new JSONValue<>(JSONType.NULL, null);
        return new JSONValue<>(JSONType.BOOLEAN, value);
    }

    /**
     * Create a new {@link JSONValue} from given JSONObject.
     *
     * @param value a JSONObject
     * @return {@link JSONValue} object containing given JSONObject
     */
    public static JSONValue<?> from(JSONObject value) {
        if (value == null) return new JSONValue<>(JSONType.NULL, null);
        return new JSONValue<>(JSONType.OBJECT, value);
    }

    /**
     * Create a new {@link JSONValue} from given JSONArray.
     *
     * @param value a JSONArray
     * @return {@link JSONValue} object containing given JSONArray
     */
    public static JSONValue<?> from(JSONArray value) {
        if (value == null) return new JSONValue<>(JSONType.NULL, null);
        return new JSONValue<>(JSONType.ARRAY, value);
    }

    /**
     * Get type of this value.
     *
     * @return {@link me.aleksi.jayson.JSONType} of this value
     */
    public JSONType getType() {
        return type;
    }

    /**
     * Get the contained value.
     *
     * @return contained value
     */
    public T getValue() {
        return value;
    }

    /**
     * Get the contained value as a Number.
     *
     * <p>Throws if value is not actually a Number.</p>
     *
     * @return contained Number
     * @throws me.aleksi.jayson.JSONTypeException if contained value is not a Number
     */
    public Number getNumber() throws JSONTypeException {
        if (type != JSONType.NUMBER && type != JSONType.NULL) {
            throw new JSONTypeException(JSONType.NUMBER, type);
        }
        return (Number) value;
    }

    /**
     * Get the contained value as a Boolean.
     *
     * <p>Throws if value is not actually a Boolean.</p>
     *
     * @return contained Boolean
     * @throws me.aleksi.jayson.JSONTypeException if contained value is not a Boolean
     */
    public Boolean getBoolean() throws JSONTypeException {
        if (type != JSONType.BOOLEAN && type != JSONType.NULL) {
            throw new JSONTypeException(JSONType.BOOLEAN, type);
        }
        return (Boolean) value;
    }

    /**
     * Get the contained value as a String.
     *
     * <p>Throws if value is not actually a String.</p>
     *
     * @return contained String
     * @throws me.aleksi.jayson.JSONTypeException if contained value is not a String
     */
    public String getString() throws JSONTypeException {
        if (type != JSONType.STRING && type != JSONType.NULL) {
            throw new JSONTypeException(JSONType.STRING, type);
        }
        return (String) value;
    }

    /**
     * Get the contained value as a JSONObject.
     *
     * <p>Throws if value is not actually a JSONObject.</p>
     *
     * @return contained JSONObject
     * @throws me.aleksi.jayson.JSONTypeException if contained value is not a JSONObject
     */
    public JSONObject getObject() throws JSONTypeException {
        if (type != JSONType.OBJECT && type != JSONType.NULL) {
            throw new JSONTypeException(JSONType.OBJECT, type);
        }
        return (JSONObject) value;
    }

    /**
     * Get the contained value as a JSONArray.
     *
     * <p>Throws if value is not actually a JSONArray.</p>
     *
     * @return contained JSONArray
     * @throws me.aleksi.jayson.JSONTypeException if contained value is not a JSONArray
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
     * Return this value converted into a JSON string.
     *
     * @return JSON-converted value
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
        switch (type) {
            case OBJECT:
                return ((JSONObject) value).toJSONString();
            case ARRAY:
                return ((JSONArray) value).toJSONString();
            case STRING:
                return JSONUtils.quote((String) value);
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
