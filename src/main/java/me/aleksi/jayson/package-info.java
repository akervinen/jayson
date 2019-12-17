/**
 * Simple and low-level JSON parser and writer.
 *
 * <p>Reading a JSON string is done using {@link me.aleksi.jayson.JSONReader#parse(java.lang.String)}.</p>
 * <p>
 * Example of use:
 * <pre>
 * var res = new JSONReader().parse(Files.readAllBytes(Paths.get(args[0])));
 * System.out.println(res);
 * </pre>
 *
 * <p>Writing a JSON string is done using {@link me.aleksi.jayson.JSONValue} and {@link me.aleksi.jayson.JSONValue#toJSONString()}.</p>
 * <p>
 * Example of use:
 * <pre>
 * String jsonOutput = new JSONObject()
 *     .put("obj1", new JSONObject()
 *         .put("foo", "bar"))
 *     .toJSONString();
 *
 * // jsonOutput =&gt; {"obj1":{"foo":"bar"}}
 * </pre>
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
package me.aleksi.jayson;
