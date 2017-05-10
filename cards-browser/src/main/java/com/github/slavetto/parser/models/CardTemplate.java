package com.github.slavetto.parser.models;

import org.json.JSONObject;

/*
 * Created with ♥
 */
public class CardTemplate {

    private String frontTemplate;
    private String rearsTemplate;

    /**
     * Position of the template in the template array. This is used by the cards to identify which of the templates of
     * the {@link CardModel} generated it.
     */
    private int ord;

    static CardTemplate fromJSON(JSONObject templateJson) {
        CardTemplate cardTemplate = new CardTemplate();

        cardTemplate.frontTemplate = templateJson.getString("qfmt");
        cardTemplate.rearsTemplate = templateJson.getString("afmt");

        cardTemplate.ord = templateJson.getInt("ord");

        return cardTemplate;
    }

    public String getFrontTemplate() {
        return frontTemplate;
    }

    public String getRearsTemplate() {
        return rearsTemplate;
    }

    /**
     * @return position of the template in the template array. This is used by the cards to identify which of the
     * templates of the {@link CardModel} generated it.
     */
    public int getOrd() {
        return ord;
    }
}
