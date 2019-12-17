package me.aleksi.jayson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JSONArrayTest {
    @Test
    void toJSONString() {
        assertEquals("[]", new JSONArray().toJSONString(), "empty array");

        assertEquals("[\"foo\"]", new JSONArray()
            .add("foo")
            .toJSONString(), "single-element array");

        assertEquals("[\"foo\",\"bar\",\"baz\"]", new JSONArray()
            .add("foo").add("bar").add("baz")
            .toJSONString(), "multi-element array");
    }
}
