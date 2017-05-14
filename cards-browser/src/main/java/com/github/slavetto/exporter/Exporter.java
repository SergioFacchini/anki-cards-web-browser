package com.github.slavetto.exporter;

import com.github.slavetto.gui.viewmodels.DecksWithTags;
import com.github.slavetto.parser.APKGParser;
import com.github.slavetto.parser.models.CardModel;
import com.github.slavetto.parser.models.DeckInfo;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/*
 * Created with ♥
 */

/**
 * The class that actually performs the exporting of the cards
 */
public class Exporter {

    private final APKGParser parser;
    private final File destinationFolder;
    private final JList<DecksWithTags> tagsToExport;

    public Exporter(APKGParser parser, File destinationFolder, JList<DecksWithTags> tagsToExport) {
        this.parser = parser;
        this.destinationFolder = destinationFolder;
        this.tagsToExport = tagsToExport;
    }

    public void tryExporting() throws SQLException, IOException {
        //For now, we just generate the data-json.
        writeJsonFile(generateDataJson());
    }

    public void writeJsonFile(JSONObject json) throws IOException {
        File file = new File(destinationFolder, "data.json");
        Files.write(json.toString(2), file, Charsets.UTF_8);
    }

    private JSONObject generateDataJson() throws SQLException {
        JSONObject root = new JSONObject();

        JSONArray decksJson = new JSONArray();
        for (DeckInfo deckInfo : parser.getDeckInfos()) {
            decksJson.put(generateDeck(deckInfo, parser).toJSON());
        }
        root.put("decks", decksJson);

        //cardTypes
        JSONObject cardModelsJson = new JSONObject();
        for (CardModel model : parser.getCardModels()) {
            JSONObject modelJson = new JSONObject();
            modelJson.put("css", model.getCss());

            cardModelsJson.put(String.valueOf(model.getId()), modelJson);
        }
        root.put("cardTypes", cardModelsJson);
        return root;
    }

    private GeneratedDeck generateDeck(DeckInfo deckInfo, APKGParser parser) throws SQLException {
        GeneratedDeck deck;

        List<String> tags = parser.fetchTagsOfDeck(deckInfo.getId());
        if(tags.isEmpty()){
            deck = new DeckWithoutCategories(deckInfo);
        } else {
            deck = new DeckWithCategories(deckInfo, tags);
        }

        deck.generate(parser);

        return deck;
    }

}