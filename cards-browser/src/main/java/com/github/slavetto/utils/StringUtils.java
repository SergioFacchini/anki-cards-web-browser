package com.github.slavetto.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created with ♥
 */
public class StringUtils {

    private static final Pattern stylePattern = Pattern.compile("(?s)<style.*?>.*?</style>");
    private static final Pattern scriptPattern = Pattern.compile("(?s)<script.*?>.*?</script>");
    private static final Pattern tagPattern = Pattern.compile("<.*?>");

    private static final Pattern htmlEntitiesPattern = Pattern.compile("&#?\\w+;");

    /**
     * Adds double curly brackets around the passed string.<br>
     * Ex: "Hello" -&gt; "{{Hello}}"
     * @param key the string that needs brackets
     * @return the curlified string
     */
    public static String curlyfy(String key) {
        return String.format("{{%s}}", key);
    }

    /**
     * SHA1 checksum.
     * Equivalent to python sha1.hexdigest()
     *
     * @param data the string to generate hash from
     * @return A string of length 40 containing the hexadecimal representation of the MD5 checksum of data.
     */
    public static String checksum(String data) {
        String result = "";
        if (data != null) {

            MessageDigest md;
            byte[] digest = null;

            try {
                md = MessageDigest.getInstance("SHA1");
                digest = md.digest(data.getBytes("UTF-8"));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            //noinspection ConstantConditions
            BigInteger biginteger = new BigInteger(1, digest);
            result = biginteger.toString(16);

            // pad with zeros to length of 40 This method used to pad
            // to the length of 32. As it turns out, sha1 has a digest
            // size of 160 bits, leading to a hex digest size of 40,
            // not 32.
            if (result.length() < 40) {
                String zeroes = "0000000000000000000000000000000000000000";
                result = zeroes.substring(0, zeroes.length() - result.length()) + result;
            }
        }
        return result;
    }

    /**
     * Strips a text from <style>...</style>, <script>...</script> and <_any_tag_> HTML tags.
     * @param s The HTML text to be cleaned.
     * @return The text without the aforementioned tags.
     */
    public static String stripHTML(String s) {
        Matcher htmlMatcher = stylePattern.matcher(s);
        s = htmlMatcher.replaceAll("");
        htmlMatcher = scriptPattern.matcher(s);
        s = htmlMatcher.replaceAll("");
        htmlMatcher = tagPattern.matcher(s);
        s = htmlMatcher.replaceAll("");
        return entsToTxt(s);
    }

    /**
     * Takes a string and replaces all the HTML symbols in it with their unescaped representation.
     * This should only affect substrings of the form &something; and not tags.
     * Internet rumour says that Html.fromHtml() doesn't cover all cases, but it doesn't get less
     * vague than that.
     * @param html The HTML escaped text
     * @return The text with its HTML entities unescaped.
     */
    private static String entsToTxt(String html) {
        Matcher htmlEntities = htmlEntitiesPattern.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (htmlEntities.find()) {
            htmlEntities.appendReplacement(sb, htmlEntities.group());
        }
        htmlEntities.appendTail(sb);
        return sb.toString();
    }



}
