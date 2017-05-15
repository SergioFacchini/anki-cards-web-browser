package com.github.slavetto.exporter;

import com.github.slavetto.parser.APKGParser;
import com.github.slavetto.parser.models.DeckInfo;
import org.json.JSONObject;

import java.sql.SQLException;

/*
 * Created with ♥
 */

/**
 * Simulates a completely empty deck. This is a shorthand class that makes {@link Exporter#generateDataJson()}
 * immediately recognize the deck and filter it out.
 */
public class EmptyDeck extends GeneratedDeck {

    EmptyDeck(DeckInfo deckInfo) {
        super(deckInfo);
    }

    @Override
    protected void addCardsToJson(JSONObject json, boolean randomizeCardsPositions) {
        throw new IllegalStateException("An empty deck must not be added to JSON!");
    }

    @Override
    protected boolean hasCategories() {
        return false;
    }

    @Override
    public void generate(APKGParser parser) throws SQLException {

    }

    @Override
    boolean isEmpty() {
        return true;
    }
}
