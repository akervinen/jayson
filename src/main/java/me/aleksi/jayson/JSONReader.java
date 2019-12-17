package me.aleksi.jayson;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Stack;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * A low-level JSON reader (parser).
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public class JSONReader {
    private static final Pattern jsonNumberPattern = Pattern.compile("(-?)(0|[1-9]\\d*)(\\.\\d+)?([eE][+-]?\\d+)?");
    private ReadOptions options = new ReadOptions();

    /**
     * JSON string currently being read.
     */
    private String jsonString;

    /**
     * Length of the current JSON string.
     */
    private int jsonLength;

    /**
     * Current position in {@link #jsonString}.
     */
    private int currentIndex = 0;

    /**
     * Stack of parse states to keep track of whether we currently parsing an object or array.
     *
     * <p>Top of the stack is the current state.</p>
     *
     * <p>Stack is empty if parsing the top-level value.</p>
     */
    private Stack<State> stateStack = new Stack<>();

    /**
     * Stack of JSON objects or arrays corresponding to {@link #stateStack} so values to in right objects.
     *
     * <p>Top of the stack is the currently active object or array.</p>
     *
     * <p>Stack is empty if parsing the top-level value.</p>
     */
    private Stack<JSONValue<?>> valueStack = new Stack<>();

    /**
     * Create a JSONReader with default {@link ReadOptions}.
     */
    public JSONReader() {
    }

    /**
     * Create a JSONReader with given {@link ReadOptions}.
     *
     * @param options {@link me.aleksi.jayson.JSONReader.ReadOptions} to use for reading
     */
    public JSONReader(ReadOptions options) {
        this.options = options;
    }

    /**
     * Check if given character is JSON-spec whitespace.
     *
     * @param c character to check
     * @return true if whitespace according to JSON spec
     */
    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\n' || c == '\t' || c == '\r';
    }

    /**
     * Check if given character is a control character.
     *
     * @param c character to check
     * @return true if character is a control character
     */
    private static boolean isControlCharacter(char c) {
        return c <= 0x1F;
    }

    /**
     * Check if character is between 'a' and 'z', lowercase ASCII.
     *
     * <p>Used for identifier reading (true, false, null).</p>
     *
     * @param c character to check
     * @return true if character is 'a'..'z'
     */
    private static boolean isAZ(char c) {
        return c >= 'a' && c <= 'z';
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
        return parse(new String(jsonBytes, StandardCharsets.UTF_8));
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
    public JSONValue<?> parse(final String jsonString) throws JSONParseException {
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

    /**
     * Read any JSON value at current position.
     *
     * @return the root object as a {@link JSONValue}
     * @throws JSONParseException if an error happens while parsing
     */
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

    /**
     * Read key-value pairs of the current object.
     *
     * <p>Expects object's opening brace to have been parsed already (i.e. position = opening brace + 1).</p>
     *
     * @throws JSONParseException if an error happens while parsing
     */
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

    /**
     * Read elements of the current array.
     *
     * <p>Expects arrays opening bracket to have been parsed already (i.e. position = opening bracket + 1).</p>
     *
     * @throws JSONParseException if an error happens while parsing
     */
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

    /**
     * Read a number from {@link #jsonString}.
     *
     * <p>Doesn't actually do parsing itself, just validates the current number-ish string
     * with a regex pattern and passes it to {@link BigDecimal} or {@link Double#valueOf(String)}.</p>
     *
     * @throws JSONParseException if an error happens while parsing
     */
    private JSONValue<?> readNumber() throws JSONParseException {
        var valStr = readWhile(c -> c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E' || (c >= '0' & c <= '9'));

        if (jsonNumberPattern.matcher(valStr).matches()) {
            try {
                return JSONValue.from(options.readNumbersAsBigDecimal ? new BigDecimal(valStr) : Double.valueOf(valStr));
            } catch (NumberFormatException e) {
                throw new JSONParseException("invalid number: '" + valStr + "'", e);
            }
        } else {
            throw new JSONParseException("invalid number '" + valStr + "'");
        }
    }

    /**
     * Read an identifier consisting of ASCII a-z characters.
     *
     * @return {@link JSONValue} of boolean or null type
     * @throws JSONParseException if an error happens while parsing
     */
    private JSONValue<?> readIdentifier() throws JSONParseException {
        skipWhitespace();
        var valStr = readUntil(c -> !isAZ(c));

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

    /**
     * Read forward in {@link #jsonString} until given function returns false.
     *
     * @param whileFunc function that takes a character and returns boolean
     * @return the read string
     */
    private String readWhile(final Function<Character, Boolean> whileFunc) {
        return readUntil(c -> !whileFunc.apply(c));
    }

    /**
     * Read forward in {@link #jsonString} until given function returns true.
     *
     * @param untilFunc function that takes a character and returns boolean
     * @return the read string
     */
    private String readUntil(final Function<Character, Boolean> untilFunc) {
        var sb = new StringBuilder();

        while (currentIndex < jsonLength) {
            char c = currentChar();

            if (untilFunc.apply(c)) {
                break;
            }
            sb.append(c);
            currentIndex++;
        }

        return sb.toString();
    }

    /**
     * Expect one of the given strings to be in {@link #jsonString} at current position.
     *
     * <p>If one of the strings was not found, throws an exception.</p>
     *
     * @param expectedList list of strings to look for
     * @return string that was found
     * @throws JSONParseException if none of the given strings match
     */
    private String expect(final String... expectedList) throws JSONParseException {
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

    /**
     * Expect one of the given characters to be in {@link #jsonString} at current position.
     *
     * <p>If one of the characters was not found, throws an exception.</p>
     *
     * @param expected list of characters to look for
     * @return character that was found
     * @throws JSONParseException if none of the given characters match
     */
    private char expect(final Character... expected) throws JSONParseException {
        return expect(Arrays.stream(expected).map(String::valueOf).toArray(String[]::new)).charAt(0);
    }

    private void expectEOF() throws JSONParseException {
        if (!isEOF()) {
            throw new JSONParseException("expected EOF, got '" + currentChar() + "'");
        }
    }

    private void expectNotEOF(final String expected) throws JSONParseException {
        if (isEOF()) {
            throw new JSONParseException("unexpected EOF, was expecting " + expected);
        }
    }

    /**
     * Skip forward in position until a non-whitespace character is found.
     */
    private void skipWhitespace() {
        while (!isEOF() && isWhitespace(currentChar())) {
            currentIndex++;
        }
    }

    private boolean isEOF() {
        return currentIndex >= jsonLength;
    }

    /**
     * Read a string escape sequence at current position.
     *
     * <p>Handles all JSON-specified escape sequences: {@code \" \\ \/ \b \f \n \r \t \\uXXXX}.</p>
     *
     * @return the escaped sequence
     * @throws JSONParseException if an invalid sequence was found
     */
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


    /**
     * Read a four hex digit unicode escape sequence from current position.
     *
     * @return the escaped unicode character
     * @throws JSONParseException if an invalid sequence was found
     */
    private char readUnicodeSequence() throws JSONParseException {
        if (currentIndex + 3 >= jsonLength) {
            throw new JSONParseException("expected 4-hex digit unicode sequence, got EOF");
        }

        var hexStr = jsonString.substring(currentIndex, currentIndex + 4);
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

    /**
     * Reading options.
     */
    public static class ReadOptions {
        /**
         * Whether all numbers should be read as BigDecimal instead of Double.
         *
         * <p>Recommended to turn this on if preserving precision is important.</p>
         */
        public boolean readNumbersAsBigDecimal = false;
    }
}
