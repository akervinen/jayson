package me.aleksi.jayson;

/**
 * Utility class.
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public class JSONUtils {
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
     * Quote given string and escape all needed characters in it.
     *
     * @param str string to quote and escape
     * @return escaped string
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
}
