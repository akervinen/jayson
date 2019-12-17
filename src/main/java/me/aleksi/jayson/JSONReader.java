package me.aleksi.jayson;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Stack;

/**
 * <p>JSONReader class.</p>
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public class JSONReader {
    private ReadOptions options = new ReadOptions();
    private String jsonString;
    private int jsonLength;
    private int currentIndex = 0;
    private Stack<State> stateStack = new Stack<>();
    private Stack<JSONValue<?>> valueStack = new Stack<>();

    /**
     * <p>Constructor for JSONReader.</p>
     */
    public JSONReader() {
    }

    /**
     * <p>Constructor for JSONReader.</p>
     *
     * @param options a {@link me.aleksi.jayson.JSONReader.ReadOptions} object.
     */
    public JSONReader(ReadOptions options) {
        this.options = options;
    }

    private static boolean isBoundary(char c) {
        return isWhitespace(c) || c == ',' || c == '}' || c == ']' || c == ':';
    }

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\n' || c == '\t' || c == '\r';
    }

    private static boolean isControlCharacter(char c) {
        return c <= 0x1F;
    }

    private char currentChar() {
        return jsonString.charAt(currentIndex);
    }

    /**
     * Parse given JSON-formatted byte array and return the root as {@link JSONValue}.
     *
     * <p>Root value can be any valid JSON value.</p>
     *
     * @param jsonBytes a {@link byte} array containing JSON
     * @return the root object as {@link JSONValue}
     * @throws JSONParseException if an error happens while parsing
     */
    public JSONValue<?> parse(byte[] jsonBytes) throws JSONParseException {
        return parse(new String(jsonBytes));
    }

    /**
     * Parse given JSON-formatted string and return the root as {@link JSONValue}.
     *
     * <p>Root value can be any valid JSON value.</p>
     *
     * @param jsonString a {@link String} containing JSON
     * @return the root object as {@link JSONValue}
     * @throws JSONParseException if an error happens while parsing
     */
    public JSONValue<?> parse(String jsonString) throws JSONParseException {
        this.jsonString = jsonString;
        jsonLength = jsonString.length();
        currentIndex = 0;

        JSONValue<?> root = null;

        while (true) {
            skipWhitespace();

            if (stateStack.empty()) {
                root = readValue();
            } else if (stateStack.peek() == State.IN_OBJECT) {
                readObjectKeys();
            } else if (stateStack.peek() == State.IN_ARRAY) {
                readArrayElements();
            }

            if (stateStack.empty()) {
                skipWhitespace();
                expectEOF();
                break;
            }
        }

        return root;
    }

    private JSONValue<?> readValue() throws JSONParseException {
        skipWhitespace();
        expectNotEOF("JSON value");

        char currentChar = currentChar();
        JSONValue<?> currentValue;

        if (currentChar == '{') {
            currentIndex++;
            stateStack.push(State.IN_OBJECT);
            currentValue = JSONValue.from(new JSONObject());
            valueStack.push(currentValue);
            return currentValue;
        } else if (currentChar == '[') {
            currentIndex++;
            stateStack.push(State.IN_ARRAY);
            currentValue = JSONValue.from(new JSONArray());
            valueStack.push(currentValue);
            return currentValue;
        } else if (currentChar == '"') {
            return JSONValue.from(readString());
        } else if (currentChar == '-' || (currentChar >= '0' && currentChar <= '9')) {
            return readNumber();
        } else {
            return readIdentifier();
        }
    }

    private void readObjectKeys() throws JSONParseException {
        assert (stateStack.peek() == State.IN_OBJECT);
        var currentObject = (JSONObject) valueStack.peek().getValue();

        char actual;

        skipWhitespace();

        if (currentObject.isEmpty()) {
            actual = expect('"', '}');
        } else {
            actual = expect(',', '}');

            if (actual == ',') {
                skipWhitespace();
                actual = expect('"');
            }
        }

        if (actual == '}') {
            stateStack.pop();
            valueStack.pop();
            return;
        }

        var key = readString(false);
        skipWhitespace();
        expect(':');
        skipWhitespace();
        var value = readValue();
        skipWhitespace();

        currentObject.put(key, value);
    }

    private void readArrayElements() throws JSONParseException {
        assert (stateStack.peek() == State.IN_ARRAY);
        var currentArray = (JSONArray) valueStack.peek().getValue();

        char actual;

        skipWhitespace();

        if (!currentArray.isEmpty()) {
            actual = expect(',', ']');

            if (actual == ',') {
                skipWhitespace();
            }
        } else {
            expectNotEOF("JSON value");
            actual = currentChar();
            if (actual == ']') {
                // Since we're manually checking for closing array bracket instead of using `expect`, we have to
                // advance index ourselves.
                currentIndex++;
            }
        }

        if (actual == ']') {
            stateStack.pop();
            valueStack.pop();
            return;
        }

        currentArray.add(readValue());
    }

    private JSONValue<?> readNumber() throws JSONParseException {
        var valStr = readUntilBoundary().toUpperCase();

        var symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');

        var format = new DecimalFormat("0.0", symbols);
        format.setParseBigDecimal(true);

        try {
            return JSONValue.from(format.parse(valStr));
        } catch (ParseException e) {
            throw new JSONParseException("invalid number \"" + valStr + "\"", e);
        }
    }

    private JSONValue<?> readIdentifier() throws JSONParseException {
        var valStr = readUntilBoundary();

        switch (valStr) {
            case "true":
                return JSONValue.from(true);
            case "false":
                return JSONValue.from(false);
            case "null":
                return JSONValue.from(JSONObject.NULL);
        }

        throw new JSONParseException("unexpected identifier '" + valStr + "'");
    }

    private String readString() throws JSONParseException {
        return readString(true);
    }

    private String readString(boolean withFirstQuote) throws JSONParseException {
        var sb = new StringBuilder();

        // Skip opening quote
        if (withFirstQuote) {
            expect('"');
        }

        expectNotEOF("string value or closing quote");

        char currentChar = currentChar();

        while (currentChar != '"') {
            if (isControlCharacter(currentChar)) {
                throw new JSONParseException("illegal control character in string");
            }

            if (currentChar == '\\') {
                sb.append(readEscapeSequence());
            } else {
                sb.append(currentChar);
                currentIndex++;
            }

            if (currentIndex >= jsonLength) {
                throw new JSONParseException("unexpected EOF while reading string");
            }

            currentChar = currentChar();
        }

        // Skip last quote
        expect('"');

        return sb.toString();
    }

    private String readUntilBoundary() {
        var sb = new StringBuilder();

        while (currentIndex < jsonLength) {
            char c = currentChar();

            if (isBoundary(c)) {
                break;
            }
            sb.append(c);
            currentIndex++;
        }

        return sb.toString();
    }

    String expect(String... expectedList) throws JSONParseException {
        var exceptionMsg = "";
        if (expectedList.length == 1) {
            exceptionMsg = String.format("expected '%s', got ",
                expectedList[0]);
        } else {
            exceptionMsg = String.format("expected one of ['%s'], got ",
                String.join("', '", expectedList));
        }

        String actual = "";
        for (var expected : expectedList) {
            if (expected.length() == 0) {
                throw new IllegalArgumentException("expected String length cannot be zero");
            }

            var lastIdx = currentIndex + expected.length() - 1;

            if (lastIdx >= jsonLength) {
                continue;
            }

            actual = jsonString.substring(currentIndex, lastIdx + 1);

            if (actual.equals(expected)) {
                currentIndex = lastIdx + 1;
                return actual;
            }
        }

        if (actual.isEmpty() && currentIndex < jsonLength) {
            actual = jsonString.substring(currentIndex);
        } else if (currentIndex >= jsonLength) {
            actual = "EOF";
        }

        throw new JSONParseException(exceptionMsg + '\'' + actual + '\'');
    }

    char expect(Character... expected) throws JSONParseException {
        return expect(Arrays.stream(expected).map(String::valueOf).toArray(String[]::new)).charAt(0);
    }

    void expectEOF() throws JSONParseException {
        if (!isEOF()) {
            throw new JSONParseException("expected EOF, got '" + currentChar() + "'");
        }
    }

    void expectNotEOF(String expected) throws JSONParseException {
        if (isEOF()) {
            throw new JSONParseException("unexpected EOF, was expecting " + expected);
        }
    }

    private void skipWhitespace() {
        while (!isEOF() && isWhitespace(currentChar())) {
            currentIndex++;
        }
    }

    private boolean isEOF() {
        return currentIndex >= jsonLength;
    }

    private char readEscapeSequence() throws JSONParseException {
        // Skip backslash
        currentIndex++;

        expectNotEOF("escape sequence");
        var escaped = jsonString.charAt(currentIndex++);

        switch (escaped) {
            case '"':
            case '\\':
            case '/':
                return escaped;
            case 'b':
                return '\b';
            case 'f':
                return '\f';
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 't':
                return '\t';
            case 'u':
                return readUnicodeSequence();
        }

        throw new JSONParseException("invalid escape sequence \\" + escaped);
    }

    private char readUnicodeSequence() throws JSONParseException {
        if (currentIndex + 3 >= jsonLength) {
            throw new JSONParseException("expected 4-hex digit unicode sequence, got EOF");
        }

        var hexStr = jsonString.substring(currentIndex, currentIndex + 4);
        char result;
        try {
            var res = (char) Integer.parseInt(hexStr, 16);
            currentIndex += 4;
            return res;
        } catch (NumberFormatException e) {
            throw new JSONParseException("expected 4-hex digit unicode sequence, got '" + hexStr + "'", e);
        }
    }

    private enum State {
        IN_OBJECT,
        IN_ARRAY
    }

    public static class ReadOptions {
        public boolean readNumbersAsBigDecimal = true;
    }
}
