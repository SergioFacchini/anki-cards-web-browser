package com.github.slavetto.parser;

import com.github.slavetto.parser.exceptions.AnkiDatabaseNotFoundException;
import com.github.slavetto.parser.models.CardModels;
import com.github.slavetto.parser.models.DeckInfos;
import com.google.common.io.Files;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

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

    public void parseDecksAndTags() throws IOException, ZipException, SQLException, AnkiDatabaseNotFoundException {
        //1) Unzip the *.apgk file
        //2) Fetch the decks' names and categories from the database
        //3) Fetch the tags for each deck



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

}
