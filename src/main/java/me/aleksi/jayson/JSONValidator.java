package me.aleksi.jayson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * <p>JSONValidator class.</p>
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public class JSONValidator {
    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
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
