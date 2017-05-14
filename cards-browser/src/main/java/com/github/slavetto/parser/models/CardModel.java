package com.github.slavetto.parser.models;

/*
 * Created with ♥
 */

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class CardModel {

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
            model.templates.add(createCardTemplateFromJSON((JSONObject) object));
        }

        return model;
    }

    private static CardTemplate createCardTemplateFromJSON(JSONObject object) {
        return new CardTemplate(
                object.getString("qfmt"),
                object.getString("afmt"),
                object.getInt("ord")
        );
    }

    public long getId() {
        return id;
    }

    public String getCss() {
        return css;
    }

    public ArrayList<String> getFields() {
        return fields;
    }

    public ArrayList<CardTemplate> getTemplates() {
        return templates;
    }

    public RenderedCard render(CardReference cardReference) {
        CardTemplate templateToUse = templates.get(cardReference.getTemplateOrd());
        String frontHTML = templateToUse.renderFront(cardReference, fields);
        String rearHTML  = templateToUse.renderRear(cardReference, fields);

        return new RenderedCard(frontHTML, rearHTML, cardReference.getId(), getId());
    }
}
