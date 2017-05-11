package com.github.slavetto.parser.models;

/*
 * Created with ♥
 */

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

class CardModel {

    private long id;
    private String css;

    /**
     * The fields that the cards implementing this model must specify.
     */
    private ArrayList<String> fields;

    /**
     * The templates of the structure of the cards
     */
    private ArrayList<CardTemplate> templates;

    static CardModel fromJSON(JSONObject modelJson) {
        JSONArray fieldsJson    = modelJson.getJSONArray("flds");
        JSONArray templatesJson = modelJson.getJSONArray("tmpls");

        CardModel model = new CardModel();
        model.id  = modelJson.getLong("id");
        model.css = modelJson.getString("css");

        model.fields = new ArrayList<>(fieldsJson.length());
        for (Object object : fieldsJson) {
            JSONObject fieldObject = (JSONObject) object;
            model.fields.add(fieldObject.getString("name"));
        }

        model.templates = new ArrayList<>(templatesJson.length());
        for (Object object : templatesJson) {
            model.templates.add(CardTemplate.fromJSON((JSONObject) object));
        }

        return model;
    }

}
