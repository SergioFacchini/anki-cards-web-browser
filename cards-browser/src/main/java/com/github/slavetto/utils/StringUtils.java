package com.github.slavetto.utils;

/*
 * Created with ♥
 */
public class StringUtils {

    /**
     * Adds double curly brackets around the passed string.<br>
     * Ex: "Hello" -&gt; "{{Hello}}"
     * @param key the string that needs brackets
     * @return the curlified string
     */
    public static String curlyfy(String key) {
        return String.format("{{%s}}", key);
    }
}
