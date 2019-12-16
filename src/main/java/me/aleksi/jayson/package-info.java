/**
 * <p>Reading a JSON string is done using {@link me.aleksi.jayson.JSONReader#parse(java.lang.String)}.</p>
 * <p>
 * Example of use:
 * <pre>
 * var res = new JSONReader().parse(Files.readAllBytes(Paths.get(args[0])));
 * System.out.println(res);
 * </pre>
 */
package me.aleksi.jayson;
