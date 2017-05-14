package com.github.slavetto.parser;

import com.github.slavetto.parser.exceptions.AnkiDatabaseNotFoundException;
import com.github.slavetto.parser.exceptions.DatabaseInconsistentException;
import com.github.slavetto.parser.models.*;
import com.google.common.io.Files;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
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
    public void tryOpenFile() throws ZipException, SQLException, AnkiDatabaseNotFoundException {
        unzip();
        initDeckDatabase();
        fetchDecksInfos();
        fetchCardModels();
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
}
