package me.aleksi.jayson;

import java.util.*;
import java.util.function.Consumer;

/**
 * Represents a JSON array containing a list of any JSON values.
 *
 * <p>Implements {@link Iterable} but not {@link List}.
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public class JSONArray implements JSONStringifyable, Iterable<JSONValue<?>> {
    private List<JSONValue<?>> list = new LinkedList<>();

    /**
     * Returns size of this array.
     *
     * @return size of this array
     */
    public int size() {
        return list.size();
    }

    /**
     * Whether this array is empty.
     *
     * @return true if this array is empty
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * Add a value to this array.
     *
     * <p>Prevents adding an array into itself to prevent loops.</p>
     *
     * @param value a {@link me.aleksi.jayson.JSONValue} object to add
     * @return this array for chaining
     */
    public JSONArray add(JSONValue<?> value) {
        if (value.getType() == JSONType.ARRAY && value.getValue() == this) {
            throw new IllegalArgumentException("cannot place JSONArray inside itself");
        }

        list.add(value);
        return this;
    }

    /**
     * Add a {@link String} to this array.
     *
     * @param value a {@link String} object to add
     * @return this array for chaining
     */
    public JSONArray add(String value) {
        return add(JSONValue.from(value));
    }

    /**
     * Add a {@link Number} to this array.
     *
     * @param value a {@link Number} object to add
     * @return this array for chaining
     */
    public JSONArray add(Number value) {
        return add(JSONValue.from(value));
    }

    /**
     * Add a {@link Boolean} to this array.
     *
     * @param value a {@link Boolean} object to add
     * @return this array for chaining
     */
    public JSONArray add(Boolean value) {
        return add(JSONValue.from(value));
    }

    /**
     * Add a {@link JSONObject} to this array.
     *
     * @param value a {@link JSONObject} object to add
     * @return this array for chaining
     */
    public JSONArray add(JSONObject value) {
        return add(JSONValue.from(value));
    }

    /**
     * Add another {@link JSONArray} to this array.
     *
     * @param value a {@link JSONArray} object to add
     * @return this array for chaining
     */
    public JSONArray add(JSONArray value) {
        return add(JSONValue.from(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JSONArray that = (JSONArray) o;
        return list.equals(that.list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(list);
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
        var sb = new StringBuilder("[");

        forEach((e) -> {
            if (sb.length() > 1) {
                sb.append(",");
            }
            sb.append(e.toJSONString(options));
        });

        return sb.append("]").toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<JSONValue<?>> iterator() {
        return list.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forEach(Consumer<? super JSONValue<?>> action) {
        list.forEach(action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Spliterator<JSONValue<?>> spliterator() {
        return list.spliterator();
    }
}
