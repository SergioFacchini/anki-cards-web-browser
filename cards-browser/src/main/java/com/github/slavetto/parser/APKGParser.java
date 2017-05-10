package com.github.slavetto.parser;

import com.github.slavetto.parser.exceptions.AnkiDatabaseNotFoundException;
import com.google.common.io.Files;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created with ♥
 */
public class APKGParser {

    private File apgkFilePath;

    /**
     * The folder where the contents of the *.apkg file were unzipped. It's null if nothing has been unzipped.
     */
    private File unzippedToFolder = null;

    private AnkiDatabase database = null;

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
    private void initDeckDatabaseIfNeeded() throws AnkiDatabaseNotFoundException, SQLException {
        if (database == null) {
            database = new AnkiDatabase(calculateDatabaseFile());
        }
    }

    /**
     *
     * @return the file that points to the database file
     * @throws AnkiDatabaseNotFoundException
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
    private void unzipIfNeeded() throws ZipException {
        if (unzippedToFolder == null) {
            File tempDir = Files.createTempDir();
            ZipFile zipFile = new ZipFile(apgkFilePath);
            zipFile.extractAll(tempDir.getAbsolutePath());

            unzippedToFolder = tempDir;
        }
    }

    /**
     * Tries to "open" the apkg file specified in the constructor. By "opening" the file we means unzipping it,
     * establishing a connection to the database and reading the media file.
     */
    public void tryOpenFile() throws ZipException, SQLException, AnkiDatabaseNotFoundException {
        unzipIfNeeded();
        initDeckDatabaseIfNeeded();
    }

    /**
     * Closes the connection to the database and removes all the temporary files
     */
    public void clear() {
        //TODO:
    }

    public int getNumCards() throws SQLException {
        return database.getNumCards();
    }
}
