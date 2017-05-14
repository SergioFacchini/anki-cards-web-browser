package com.github.slavetto.exporter;

import com.github.slavetto.parser.APKGParser;
import com.github.slavetto.parser.models.DeckInfo;
import com.github.slavetto.parser.models.RenderedCard;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 * Created with ♥
 */

/**
 * Represents a deck that tags for filtering.
 */
class DeckWithCategories extends GeneratedDeck {

    private final HashMap<String, ArrayList<RenderedCard>> tagsCards;
    private final List<String> tags;

    DeckWithCategories(DeckInfo deckInfo, List<String> tags) {
        super(deckInfo);
        this.tags = tags;

        tagsCards = new HashMap<>();
    }

    @Override
    protected void addCardsToJson(JSONObject json) {
        JSONArray categoriesArray = new JSONArray();

        tagsCards.forEach((tag, cards) -> {
            JSONArray cardsOfCategory = new JSONArray();
            cards.forEach(card -> cardsOfCategory.put(calculateCardJSON(card)));

            JSONObject categoryObject = new JSONObject();
            categoryObject.put("categoryName", tag);
            categoryObject.put("cards", cardsOfCategory);

            categoriesArray.put(categoryObject);
        });

        json.put("categories", categoriesArray);
    }

    @Override
    protected boolean hasCategories() {
        return true;
    }

    @Override
    public void generate(APKGParser parser) throws SQLException {
        for (String category: tags) {
            ArrayList<RenderedCard> cards = parser.generateCardsOfDeck(deckInfo.getId(), category);
            tagsCards.put(category, cards);
        }
    }

}
