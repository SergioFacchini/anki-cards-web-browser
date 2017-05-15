package com.github.slavetto.parser;

import com.github.slavetto.parser.exceptions.AnkiDatabaseNotFoundException;
import com.github.slavetto.parser.exceptions.DatabaseInconsistentException;
import com.github.slavetto.parser.models.*;
import com.google.common.io.Files;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 * Created with ♥
 */
public class APKGParser {

    private File apgkFilePath;

    /**
     * The folder where the contents of the *.apkg file were unzipped. It's null if nothing has been unzipped.
     */
    private File unzippedToFolder = null;
    private AnkiDatabase database = null;

    private CardModels cardModels;
    private DeckInfos deckInfos;

    /**
     * Keeps track of the compressed images' names and their corresponding original names. For "original names" we mean
     * the names that are used in the cards.
     */
    private HashMap<String, String> imageNamesDictionary;

    public APKGParser(File apgkFilePath) {
        this.apgkFilePath = apgkFilePath;
    }

    public CardModels getCardModels() {
        return cardModels;
    }

    /**
     * Creates a new connection to the database. Can be called only when the anki file is unzipped.
     */
    private void initDeckDatabase() throws AnkiDatabaseNotFoundException, SQLException {
        database = new AnkiDatabase(calculateDatabaseFile());
    }

    /**
     *
     * @return the file that points to the database file
     * @throws AnkiDatabaseNotFoundException if the anki database was not found (probably invalid zip file)
     */
    private File calculateDatabaseFile() throws AnkiDatabaseNotFoundException {
        if (unzippedToFolder == null) {
            throw new IllegalStateException("Cannot calculate the database name when the *.apkg is not unzipped!");
        }

        File dbFile = new File(unzippedToFolder, "collection.anki2");
        if (!dbFile.exists()) {
            throw new AnkiDatabaseNotFoundException();
        }

        return dbFile;
    }


    /**
     * Unzips the .dpkg if it's not already unzipped.
     * @throws ZipException if an error happens when unzipping
     */
    private void unzip() throws ZipException {
        File tempDir = Files.createTempDir();
        ZipFile zipFile = new ZipFile(apgkFilePath);
        zipFile.extractAll(tempDir.getAbsolutePath());

        unzippedToFolder = tempDir;
    }

    /**
     * Tries to "open" the apkg file specified in the constructor. By "opening" the file we means unzipping it,
     * establishing a connection to the database and reading the media file.
     */
    public void tryOpenFile() throws ZipException, SQLException, AnkiDatabaseNotFoundException, IOException {
        unzip();
        initDeckDatabase();
        fetchDecksInfos();
        fetchCardModels();
        readMediaDictionaryJson();
    }

    private void readMediaDictionaryJson() throws IOException {
        //Reading file
        File mediaFile = new File(unzippedToFolder, "media");
        String mediaStr = Files.toString(mediaFile, Charset.forName("UTF-8"));

        //Parsing JSON
        //The JSON is something like this:
        //{
        // "10": "paste-61641370632193.jpg",
        // "4": "paste-81522774245377.jpg",
        // "1": "latex-0cc8b5131ccb25b20258394ebcf13773bb8b2d19.png"
        //}
        imageNamesDictionary = new HashMap<>();

        JSONObject mediaObject = new JSONObject(mediaStr);
        mediaObject.toMap()
                .forEach((key, value) -> imageNamesDictionary.put(key, (String) value));
    }

    private void fetchDecksInfos() throws SQLException {
        deckInfos = new DeckInfos();
        deckInfos.addFromJson(database.fetchDecksInfos());
    }

    private void fetchCardModels() throws SQLException {
        cardModels = new CardModels();
        cardModels.addFromJson(database.fetchCardModelsJson());
    }

    /**
     * Closes the connection to the database and removes all the temporary files
     */
    public void clear() {
        //TODO:
    }

    public long getNumCardsInAllDecks() throws SQLException {
        return database.getNumCardsInAllDecks();
    }

    public DeckInfos getDeckInfos() {
        return deckInfos;
    }

    public long getNumCardsInDeck(long deckId) throws SQLException {
        return database.getNumCardsInDeck(deckId);
    }

    /**
     * @return a list of tags that the current deck has associated with it. For notes that don't have any tag returns a
     * "(no tag)" string.
     */
    public List<String> fetchTagsOfDeck(long deckId) throws SQLException {
        return database.getTagsOfDeck(deckId);
    }

    /**
     * Calculates how many cards in the deck have the specific tags
     * @param deckId the id of the deck
     * @param tags the tags to look for
     * @return how many cards in the deck have the specific tags
     */
    public long getNumCardsHavingTagInDeck(long deckId, String tags) throws SQLException {
        return database.getNumCardsHavingTagInDeck(deckId, tags);
    }

    /**
     * Retrieves and generates all the cards that belong to the given deck and having the give category
     * @param deckId id of the deck
     * @param tags the tags that the note must have
     * @return a list containing all the cards
     */
    public ArrayList<RenderedCard> generateCardsOfDeck(long deckId, String tags) throws SQLException {
        ArrayList<RenderedCard> renderedCards = new ArrayList<>();
        for (CardReference cardReference : database.fetchCards(deckId, tags)) {
            CardModel model = getCardModel(cardReference.getCardModelId());
            renderedCards.add(model.render(cardReference));
        }

        return renderedCards;
    }

    private CardModel getCardModel(long cardModelId) {
        return cardModels.stream()
                .filter(cardModel -> cardModel.getId() == cardModelId)
                .findFirst()
                .orElseThrow(() -> new DatabaseInconsistentException("Unknown card model id: "+cardModelId));

    }

    /**
     * @return the folder where the apkg file has been temporarily unzipped.
     */
    public File getUnzippedToFolder() {
        return unzippedToFolder;
    }

    /**
     * @return the association between the name of the unzipped file and the name of the image that will be used in
     * cards.
     */
    public HashMap<String, String> getImageNamesDictionary() {
        return imageNamesDictionary;
    }
}
