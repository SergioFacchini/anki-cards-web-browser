package com.github.slavetto.exporter;

import com.github.slavetto.parser.APKGParser;
import com.github.slavetto.parser.models.DeckInfo;
import com.github.slavetto.parser.models.RenderedCard;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

/*
 * Created with ♥
 */

/**
 * Represents a deck that has tags for filtering.
 */
public class DeckWithoutCategories extends GeneratedDeck {

    private final String tagToLookFor;
    private ArrayList<RenderedCard> cards;

    DeckWithoutCategories(DeckInfo deckInfo, String tagToLookFor) {
        super(deckInfo);
        this.tagToLookFor = tagToLookFor;
    }

    @Override
    protected void addCardsToJson(JSONObject json, boolean randomizeCardsPositions) {
        JSONArray cardsJson = new JSONArray();

        if (randomizeCardsPositions) {
            Collections.shuffle(cards);
        }

        cards.forEach(card -> cardsJson.put(calculateCardJSON(card)));

        json.put("cards", cardsJson);
    }

    @Override
    protected boolean hasCategories() {
        return false;
    }

    @Override
    public void generate(APKGParser parser) throws SQLException {
        cards = parser.generateCardsOfDeck(deckInfo.getId(), tagToLookFor);
    }

    @Override
    boolean isEmpty() {
        return cards.isEmpty();
    }

}
