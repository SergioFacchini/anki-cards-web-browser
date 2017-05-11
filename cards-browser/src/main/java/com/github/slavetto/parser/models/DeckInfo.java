package com.github.slavetto.parser.models;

import org.json.JSONObject;

/*
 * Created with ♥
 */
public class DeckInfo {

    private long id;
    private String name;

    static DeckInfo fromJSON(JSONObject deckJson) {
        DeckInfo deckInfo = new DeckInfo();
        deckInfo.id   = deckJson.getLong("id");
        deckInfo.name = deckJson.getString("name");

        return deckInfo;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
