package com.github.slavetto.parser.models;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
 * Created with ♥
 */
public class CardTemplateTest {

    @Test
    public void updateImagesPathTest() throws Exception {
        String sampleText = "Che cos'è un latch SR?\n<hr id=answer>\n<div>Un circuito elettronico capace di memorizzare informazioni</div><img src=\"paste-9921374453761.jpg\" /> oppure <img src=\"paste-9921374453761.jpg\" />";
        String expectedText = "Che cos'è un latch SR?\n<hr id=answer>\n<div>Un circuito elettronico capace di memorizzare informazioni</div><img src=\"anki-images/paste-9921374453761.jpg\" /> oppure <img src=\"anki-images/paste-9921374453761.jpg\" />";

//        int numMatches = 0;
//        Matcher matcher = Pattern.compile("<img[^(src)]+src=\"[^\"]+\"").matcher(sampleText);
//        while (matcher.find()) {
//            numMatches++;
//        }

        String result = sampleText.replaceAll("(<img[^(src)]+src=\")([^\"]+)\"", "$1anki-images/$2\"");

        Assert.assertEquals(null, expectedText, result);

    }

}