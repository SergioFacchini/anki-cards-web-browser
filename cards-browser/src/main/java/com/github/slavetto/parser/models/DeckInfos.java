package com.github.slavetto.parser.models;

import org.json.JSONObject;

import java.util.ArrayList;

/*
 * Created with ♥
 */
public class DeckInfos extends ArrayList<DeckInfo> {

    public void addFromJson(String decksInfosJson) {
        JSONObject modelsObject = new JSONObject(decksInfosJson);
        modelsObject.keys().forEachRemaining(
                key -> add(DeckInfo.fromJSON(modelsObject.getJSONObject(key)))
        );
    }

}
