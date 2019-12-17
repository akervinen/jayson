package me.aleksi.jayson;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class JSONReaderTest {
    @Test
    void readInvalidJSON() {
        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse(" "),
            "should throw with just whitespace");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse(new byte[]{(byte) 255}),
            "should throw with invalid UTF8");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("[\u00FF]"),
            "should throw with invalid UTF8");
    }

    @Test
    void readEmptyObject() throws JSONParseException {
        assertEquals(new JSONObject(),
            new JSONReader().parse("{}").getValue(),
            "empty object");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("{"),
            "starting brace without matching end should throw");
    }

    @Test
    void readSimpleObject() throws JSONParseException {
        assertEquals(new JSONObject().put("test", "foo"),
            new JSONReader().parse("{\"test\": \"foo\"}").getValue(),
            "object with a single key-value pair");

        assertEquals(new JSONObject().put("test", false).put("foo", "bar"),
            new JSONReader().parse("{\"test\": false, \"foo\": \"bar\"}").getValue(),
            "object with multiple key-value pairs");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("{\"foo\": \"bar\""),
            "should throw if ending brace is missing");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("{\"foo\"}"),
            "should throw if given just key without colon or value");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("{\"foo\":}"),
            "should throw if given just key+colon without value");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("{\"foo\":"),
            "should throw if missing value and ending brace");
    }

    @Test
    void readNestedObjects() throws JSONParseException {
        assertEquals(new JSONObject().put("test", false).put("moo", new JSONObject().put("bar", true)),
            new JSONReader().parse("{\"test\": false, \"moo\": {\"bar\": true}}").getValue(),
            "nested objects");
    }

    @Test
    void readEmptyArray() throws JSONParseException {
        assertEquals(new JSONArray(),
            new JSONReader().parse("[]").getValue(),
            "empty array");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("["),
            "should throw if ending bracket is missing");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("[,"),
            "should throw if ending bracket is missing, trailing comma");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("[\"\\"),
            "should throw if ending bracket is missing, unclosed escape");
    }

    @Test
    void readSimpleArray() throws JSONParseException {
        assertEquals(new JSONArray().add("foo").add("bar"),
            new JSONReader().parse("[\"foo\", \"bar\"]").getValue(),
            "array with multiple values");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("[\"foo\", \"bar\""),
            "should throw if ending brackey is missing");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("[1:2]"),
            "should throw when parsing an array with colon");
    }

    @Test
    void readNestedArrays() throws JSONParseException {
        assertEquals(new JSONArray().add("boo").add(new JSONArray().add("far").add(false)),
            new JSONReader().parse("[\"boo\", [\"far\", false]]").getValue(),
            "nested arrays");
    }

    @Test
    void readComplicatedObject() throws JSONParseException {
        var json = "{\"menu\": {\n" +
            "  \"id\": \"file\",\n" +
            "  \"value\": \"File\",\n" +
            "  \"popup\": {\n" +
            "    \"menuitem\": [\n" +
            "      {\"value\": \"New\", \"onclick\": \"CreateNewDoc()\"},\n" +
            "      {\"value\": \"Open\", \"onclick\": \"OpenDoc()\"},\n" +
            "      {\"value\": \"Close\", \"onclick\": \"CloseDoc()\"}\n" +
            "    ]\n" +
            "  }\n" +
            "}}";

        var result = new JSONObject()
            .put("menu", new JSONObject()
                .put("id", "file")
                .put("value", "File")
                .put("popup", new JSONObject()
                    .put("menuitem", new JSONArray()
                        .add(new JSONObject()
                            .put("value", "New")
                            .put("onclick", "CreateNewDoc()"))
                        .add(new JSONObject()
                            .put("value", "Open")
                            .put("onclick", "OpenDoc()"))
                        .add(new JSONObject()
                            .put("value", "Close")
                            .put("onclick", "CloseDoc()")))));

        assertEquals(result, new JSONReader().parse(json).getValue(), "complicated JSON object");
    }

    @Test
    void readInteger() throws JSONParseException, JSONTypeException {
        assertEquals(0,
            new JSONReader().parse("0").getNumber().intValue(),
            "zero reading");

        assertEquals(1,
            new JSONReader().parse("1").getNumber().intValue(),
            "short integer reading");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("+1"),
            "number with positive sign should throw");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("-"),
            "negative sign with no number should throw");


        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("01"),
            "number with leading zero should throw");

        assertEquals(-5,
            new JSONReader().parse("-5").getNumber().intValue(),
            "negative integer reading");

        assertEquals(1234567890L,
            new JSONReader().parse("1234567890").getNumber().longValue(),
            "longish integer reading");

        var opts = new JSONReader.ReadOptions();
        opts.readNumbersAsBigDecimal = true;
        assertEquals(1234567890123456789L,
            new JSONReader(opts).parse("1234567890123456789").getNumber().longValue(),
            "very long integer reading");
    }

    @Test
    void readFloatingNumber() throws JSONParseException, JSONTypeException {
        assertEquals(1.0,
            new JSONReader().parse("1.0").getNumber().doubleValue(),
            "short float");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("1."),
            "float with decimal separator but no decimals should throw");

        assertEquals(-1.23456,
            new JSONReader().parse("-1.23456").getNumber().doubleValue(),
            "negative float");

        assertEquals(3.141592653589793238,
            new JSONReader().parse("3.141592653589793238").getNumber().doubleValue(),
            "too long float");

        var opts = new JSONReader.ReadOptions();
        opts.readNumbersAsBigDecimal = true;
        assertEquals(new BigDecimal("3.141592653589793238"),
            new JSONReader(opts).parse("3.141592653589793238").getNumber(),
            "very long decimal");
    }

    @Test
    void readNumberWithExponent() throws JSONParseException, JSONTypeException {
        assertEquals(0e0,
            new JSONReader().parse("0e0").getNumber().doubleValue(),
            "zero exponent");

        assertEquals(100e100,
            new JSONReader().parse("100E100").getNumber().doubleValue(),
            "upper-case exponent sign should work");

        assertThrows(Exception.class,
            () -> new JSONReader().parse("1e"),
            "exponent sign without digits should throw");

        assertEquals(100e100,
            new JSONReader().parse("100e100").getNumber().doubleValue(),
            "normal exponent");

        assertEquals(1.234e100,
            new JSONReader().parse("1.234E100").getNumber().doubleValue(),
            "decimal with exponent");

        assertEquals(1.234e100,
            new JSONReader().parse("1.234E+100").getNumber().doubleValue(),
            "exponent sign with positive sign should work");

        assertEquals(1.234e-100,
            new JSONReader().parse("1.234E-100").getNumber().doubleValue(),
            "exponent sign with negative sign should work");
    }

    @Test
    void readString() throws JSONParseException, JSONTypeException {
        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("\""),
            "should throw if ending quote  is missing");

        assertEquals("foo bar",
            new JSONReader().parse("\"foo bar\"").getString(),
            "string reading");

        assertEquals("€\uD834\uDD1E",
            new JSONReader().parse("\"€\uD834\uDD1E\"").getString(),
            "string with unicode characters");

        System.out.println(Arrays.toString(new JSONReader().parse(new String(new char[]{'"', '\u00E9', '"'})).getString().getBytes()));
        assertEquals("\u00E9",
            new JSONReader().parse(new String(new char[]{'"', '\u00E9', '"'})).getString(),
            "string with latin-1 encoded character");
    }

    @Test
    void readEscapeSequence() throws JSONParseException, JSONTypeException {
        assertEquals("foo\"bar",
            new JSONReader().parse("\"foo\\\"bar\"").getString(),
            "string reading with escaped quote");

        assertEquals("foo\\bar",
            new JSONReader().parse("\"foo\\\\bar\"").getString(),
            "string reading with escaped backslash");

        assertEquals("foo\nbar",
            new JSONReader().parse("\"foo\\nbar\"").getString(),
            "string reading with escaped newline");

        assertEquals("\uA66D",
            new JSONReader().parse("\"\\uA66D\"").getString(),
            "string reading with valid unicode escape sequence");

        assertEquals("a\uA66Da",
            new JSONReader().parse("\"a\\uA66Da\"").getString(),
            "string reading with valid unicode escape sequence surrounded with other characters");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("\"\\uA66\""),
            "should throw when unicode sequence is too short");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("\"foo\n\""),
            "should throw when encountering control character while parsing string");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("\"foo\\\""),
            "should throw when encountering a lonely backslash while parsing string");

        assertEquals("foo\"bar",
            new JSONReader().parse("\"foo\\\"bar\"").getString(),
            "string reading with escaped quote");
    }

    @Test
    void testReadIdentifier() throws JSONParseException {
        assertEquals(true,
            new JSONReader().parse("true").getValue(),
            "boolean reading");

        assertEquals(false,
            new JSONReader().parse("false").getValue(),
            "boolean reading");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("tru"),
            "should throw on unexpected input");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("true true"),
            "should throw on unexpected input");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("true,"),
            "should throw on unexpected input");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("true, true"),
            "should throw on unexpected input");

        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("truee"),
            "should throw on unexpected input");
        assertThrows(JSONParseException.class,
            () -> new JSONReader().parse("fals"),
            "should throw on unexpected input");

        assertNull(new JSONReader().parse("null").getValue(),
            "null reading");
    }

//    @Test
//    void testExpectChar() {
//        assertDoesNotThrow(() -> JSONReader.expect("foo", 0, 'f'),
//                "should work with correct parameters");
//
//        assertThrows(Exception.class,
//                () -> JSONReader.expect("foo", 0, 'g'),
//                "should throw when given character isn't the current one in string");
//
//        assertThrows(Exception.class,
//                () -> JSONReader.expect("foo", 3, 'o'),
//                "should throw when current index is out of bounds");
//    }
//
//    @Test
//    void testExpectString() {
//        assertDoesNotThrow(() -> JSONReader.expect("foo", 0, "foo"),
//                "should work with same length matching string");
//
//        assertDoesNotThrow(() -> JSONReader.expect("foo", 0, "fo"),
//                "should work with shorter matching string");
//        assertDoesNotThrow(() -> JSONReader.expect("foo", 1, "oo"),
//                "should work with shorter matching string");
//
//        assertThrows(Exception.class,
//                () -> JSONReader.expect("foo", 0, "goo"),
//                "should throw when expected String isn't the current one in string");
//
//        assertThrows(Exception.class,
//                () -> JSONReader.expect("foo", 0, "food"),
//                "should throw when expected String is longer than comparison");
//
//        assertThrows(Exception.class,
//                () -> JSONReader.expect("foo", 1, "ood"),
//                "should throw when expected String extends past end of string");
//    }
}
