package com.github.slavetto.exporter;

import com.github.slavetto.parser.APKGParser;
import com.github.slavetto.parser.models.DeckInfo;
import com.github.slavetto.parser.models.RenderedCard;
import org.json.JSONObject;

import java.sql.SQLException;

/*
 * Created with ♥
 */

/**
 * A deck that can generate it's cards an prepare them for export in the JSON file.
 */
public abstract class GeneratedDeck {

    final DeckInfo deckInfo;

    GeneratedDeck(DeckInfo deckInfo) {
        this.deckInfo = deckInfo;
    }

    JSONObject toJSON(){
        /*
        {
          "name": "Calcolatori",
          "lastUpdated": 123456789,
          "hasCategories": true,
          "categories": [
            {
              "categoryName": "Capitolo 1",
              "cards": [
                {
                  "id": 12345678,
                  "typeId": 1493040141981,
                  "front": "Che cos'è un assemblatore?",
                  "rear": "Che cos'è un assemblatore?\n<hr id=answer>\nTraduce il linguaggio assembly in sequenze di bit"
                }, {
                  "id": 123456783,
                  "typeId": 1493040141981,
                  "front": "Che cos'è un assemblatore?",
                  "rear": "Che cos'è un assemblatore?\n<hr id=answer>\nTraduce il linguaggio assembly in sequenze di bit"
                }]
            }
        }
        */

        JSONObject json = new JSONObject();
        json.put("name", deckInfo.getName());
        json.put("hasCategories", hasCategories());

        addCardsToJson(json);

        return json;
    }

    /**
     * Adds to the json the information about the cards that have to be contained
     * @param json the json to add cards to
     */
    protected abstract void addCardsToJson(JSONObject json);

    JSONObject calculateCardJSON(RenderedCard card) {
        JSONObject json = new JSONObject();
        json.put("id",     card.getCardId());
        json.put("typeId", card.getCardModelId());
        json.put("front",  card.getFrontHTML());
        json.put("rear",   card.getRearHTML());
        return json;
    }

    /**
     * @return true if this deck has categories (it contains tags), false otherwise.
     */
    protected abstract boolean hasCategories();

    /**
     * Fetches all the elements needed to build the JSON of the deck.
     * @param parser the deck's parser
     *
     */
    public abstract void generate(APKGParser parser) throws SQLException;

    /**
     * @return true if this deck has no cards, false otherwise
     */
    abstract boolean isEmpty();
}
