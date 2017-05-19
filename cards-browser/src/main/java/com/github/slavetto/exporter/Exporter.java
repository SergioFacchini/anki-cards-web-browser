package com.github.slavetto.exporter;

import com.github.slavetto.gui.viewmodels.DecksWithTags;
import com.github.slavetto.parser.APKGParser;
import com.github.slavetto.parser.models.CardModel;
import com.github.slavetto.parser.models.DeckInfo;
import com.github.slavetto.utils.FileUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.json.JSONArray;
import org.json.JSONObject;
import res.Resources;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Created with ♥
 */

/**
 * The class that actually performs the exporting of the cards
 */
public class Exporter {

    private final APKGParser parser;
    private final File destinationFolder;
    private final List<DecksWithTags> tagsToExport;
    private final ExportOptions options;

    public Exporter(APKGParser parser, File destinationFolder, List<DecksWithTags> tagsToExport, ExportOptions options) {
        this.parser = parser;
        this.destinationFolder = destinationFolder;
        this.tagsToExport = tagsToExport;
        this.options = options;
    }

    public void tryExporting() throws SQLException, IOException, AnkiExpectedExportingException {
        writeJsonFile(generateDataJson());
        moveAndRenameImageFiles();
        addStaticFiles();
    }

    /**
     * Adds the index and the relative javascript files to the folder.
     */
    private void addStaticFiles() throws IOException {
        URL resourceFolderURL = Resources.class.getResource("data/");
        try {
            File resourceFolder = new File(resourceFolderURL.toURI());
            FileUtils.copyAllRecursively(resourceFolder, destinationFolder, true);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Cannot retrieve the folder of the static files!");
        }
    }

    private void moveAndRenameImageFiles() throws AnkiExpectedExportingException {
        HashMap<String, String> imageNamesDictionary = parser.getImageNamesDictionary();

        File imagesFolder = new File(destinationFolder, "anki-images");
        //noinspection ResultOfMethodCallIgnored
        imagesFolder.mkdirs();

        imageNamesDictionary.forEach((currentName, targetName) -> {
            File originalFile    = new File(parser.getUnzippedToFolder(), currentName);
            File destinationFile = new File(imagesFolder, targetName);

            try {
                Files.copy(originalFile, destinationFile);
            } catch (IOException e) {
                throw new AnkiExpectedExportingException("Cannot move "+originalFile+" to "+destinationFile);
            }
        });
    }

    private void writeJsonFile(JSONObject json) throws IOException {
        File file = new File(destinationFolder, "decks.json");

        //We want the json to be hand-editable, so we set the indent factor to 2
        Files.write(json.toString(2), file, Charsets.UTF_8);
    }

    private JSONObject generateDataJson() throws SQLException {
        JSONObject root = new JSONObject();

        JSONArray decksJson = new JSONArray();
        for (DeckInfo deckInfo : parser.getDeckInfos()) {
            GeneratedDeck generatedDeck = generateDeck(deckInfo, parser);
            if(!generatedDeck.isEmpty()) {
                decksJson.put(generatedDeck.toJSON(options));
            }
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

        //credits
        root.put("sidebarNotes", options.sidebarNotes);

        return root;
    }

    private GeneratedDeck generateDeck(DeckInfo deckInfo, APKGParser parser) throws SQLException {
        GeneratedDeck deck;

        List<String> tagsToExport = getTagsToExportForDeck(deckInfo);
        if(tagsToExport.isEmpty()){
            //We filtered out all the tags for this deck (including the default one). This is an empty deck.
            deck = new EmptyDeck(deckInfo);
        } else if(tagsToExport.size() == 1){ //No tags or only the default one
            deck = new DeckWithoutCategories(deckInfo, tagsToExport.get(0));
        } else {
            deck = new DeckWithCategories(deckInfo, tagsToExport);
        }

        deck.generate(parser);

        return deck;
    }

    private List<String> getTagsToExportForDeck(DeckInfo deckInfo) {
        return tagsToExport
            .stream()
            .filter(deckWithTags -> deckWithTags.getDeckId() == deckInfo.getId())
            .map(DecksWithTags::getTags)
            .collect(Collectors.toList());
    }

}
