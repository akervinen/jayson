package me.aleksi.jayson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JSONArrayTest {
    @Test
    void toJSONString() {
        var opts = new JSONWriterOptions();
        assertEquals("[]", new JSONArray().toJSONString(opts), "empty array");

        assertEquals("[\"foo\"]", new JSONArray()
            .add("foo")
            .toJSONString(opts), "single-element array");

        assertEquals("[\"foo\",\"bar\",\"baz\"]", new JSONArray()
            .add("foo").add("bar").add("baz")
            .toJSONString(opts), "multi-element array");
    }
}
