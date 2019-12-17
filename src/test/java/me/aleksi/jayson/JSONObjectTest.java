package me.aleksi.jayson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JSONObjectTest {
    @Test
    void toJSONString() {
        assertEquals("{}", new JSONObject().toJSONString(), "empty object");
        assertEquals("{\"foo\":\"bar\"}", new JSONObject().put("foo", "bar").toJSONString(), "object with string");

        assertEquals("{\"foo\":\"bar\",\"num\":1.5,\"bool\":true,\"null\":null}", new JSONObject()
            .put("foo", "bar")
            .put("num", 1.5f)
            .put("bool", true)
            .put("null", JSONObject.NULL)
            .toJSONString(), "object with various values");

        assertEquals("{\"obj1\":{\"foo\":\"bar\"}}", new JSONObject()
            .put("obj1", new JSONObject()
                .put("foo", "bar"))
            .toJSONString(), "object inside object");
    }
}
