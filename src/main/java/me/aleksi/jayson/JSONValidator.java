package me.aleksi.jayson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * JSON Validator to test if a .json file parses correctly.
 * <p>
 * Written for JSONTestSuite but could have other uses.
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public class JSONValidator {
    /**
     * Runs validator, expects {@code args[0]} to be file name.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Missing file name argument!");
            System.exit(2);
        }

        try {
            var res = new JSONReader().parse(Files.readAllBytes(Paths.get(args[0])));
            System.out.println(res);
            System.exit(0);
        } catch (JSONParseException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.out.println("not found");
            System.exit(2);
        }
    }
}
