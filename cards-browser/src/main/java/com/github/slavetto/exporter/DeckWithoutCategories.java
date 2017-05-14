package com.github.slavetto.exporter;

import com.github.slavetto.parser.APKGParser;
import com.github.slavetto.parser.models.DeckInfo;
import com.github.slavetto.parser.models.RenderedCard;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;

/*
 * Created with ♥
 */

/**
 * Represents a deck that has tags for filtering.
 */
public class DeckWithoutCategories extends GeneratedDeck {

    private ArrayList<RenderedCard> cards;

    DeckWithoutCategories(DeckInfo deckInfo) {
        super(deckInfo);
    }

    @Override
    protected void addCardsToJson(JSONObject json) {
        JSONArray cardsJson = new JSONArray();
        cards.forEach(card -> cardsJson.put(calculateCardJSON(card)));

        json.put("cards", cardsJson);
    }

    @Override
    protected boolean hasCategories() {
        return false;
    }

    @Override
    public void generate(APKGParser parser) throws SQLException {
        cards = parser.generateCardsOfDeck(deckInfo.getId());
    }

}
