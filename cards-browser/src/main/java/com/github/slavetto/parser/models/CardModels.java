package com.github.slavetto.parser.models;

import org.json.JSONObject;

import java.util.ArrayList;

/*
 * Created with ♥
 */
public class CardModels extends ArrayList<CardModel> {

    public void addFromJson(String decksInfosJson) {
        JSONObject modelsObject = new JSONObject(decksInfosJson);
        modelsObject.keys().forEachRemaining(
            key -> add(CardModel.fromJSON(modelsObject.getJSONObject(key)))
        );
    }
}
