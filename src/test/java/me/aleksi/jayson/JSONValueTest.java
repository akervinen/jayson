package me.aleksi.jayson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JSONValueTest {
    @Test
    void testFromString() {
        assertEquals("\"hello\"", JSONValue.from("hello").toString(), "basic string");
        assertEquals("\"foo bar\"", JSONValue.from("foo bar").toString(), "string with spaces");
    }

    @Test
    void testFromStringEscaped() {
        assertEquals("\"lorem \\\"ipsum\\\" dolor\"", JSONValue.from("lorem \"ipsum\" dolor").toString(), "quotes should be escaped");
        assertEquals("\"line\\nfeed\"", JSONValue.from("line\nfeed").toString(), "linefeed should be escaped");
        assertEquals("\"back\\bspace\"", JSONValue.from("back\bspace").toString(), "backspace should be escaped");
        assertEquals("\"form\\ffeed\"", JSONValue.from("form\ffeed").toString(), "formfeed should be escaped");
        assertEquals("\"carriage\\rreturn\"", JSONValue.from("carriage\rreturn").toString(), "carriage return should be escaped");

        assertEquals("\"\\\\\"", JSONValue.from("\\").toString(), "backslash should be escaped");
        assertEquals("\"\\u0000\\u001F\"", JSONValue.from("\u0000\u001F").toString(), "control characters should be escaped");
    }

    @Test
    void testFromNumber() {
        assertEquals("1", JSONValue.from(1).toString(), "positive integer");
        assertEquals("-5", JSONValue.from(-5).toString(), "negative integer");
        assertEquals("0", JSONValue.from(0).toString(), "zero");

        assertEquals("1.0", JSONValue.from(1.0f).toString(), "positive float");
        assertEquals("-5.0", JSONValue.from(-5.0f).toString(), "negative float");

        assertEquals("0", JSONValue.from(Float.NaN).toString(), "Float NaN should become 0");
        assertEquals("0", JSONValue.from(Double.NaN).toString(), "Double NaN should become 0");

        assertEquals("0", JSONValue.from(Float.POSITIVE_INFINITY).toString(), "Float +Infinity should become 0");
        assertEquals("0", JSONValue.from(Float.NEGATIVE_INFINITY).toString(), "Float -Infinity should become 0");
        assertEquals("0", JSONValue.from(Double.POSITIVE_INFINITY).toString(), "Double +Infinity should become 0");
        assertEquals("0", JSONValue.from(Double.NEGATIVE_INFINITY).toString(), "Double -Infinity should become 0");
    }

    @Test
    void testFromBoolean() {
        assertEquals("true", JSONValue.from(Boolean.TRUE).toString(), "boolean true");
        assertEquals("true", JSONValue.from(true).toString(), "boolean true");
        assertEquals("false", JSONValue.from(Boolean.FALSE).toString(), "boolean false");
        assertEquals("false", JSONValue.from(false).toString(), "boolean false");
    }

    @Test
    void testFromNull() {
        assertEquals("null", JSONValue.from((JSONObject) null).toString(), "null object");
    }

    @Test
    void testFromJSONObject() {
    }

    @Test
    void testFromJSONArray() {
    }
}
