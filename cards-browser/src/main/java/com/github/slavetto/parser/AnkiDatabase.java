package com.github.slavetto.parser;

import com.github.slavetto.parser.dbmodels.DBCard;
import com.github.slavetto.parser.dbmodels.DBConfig;
import com.github.slavetto.parser.dbmodels.DBNote;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * Created with ♥
 */
class AnkiDatabase {

    private final ConnectionSource connectionSource;
    private final Dao<DBNote, Integer> notesDAO;
    private final Dao<DBCard, Integer> cardsDAO;
    private final Dao<DBConfig, Void> configDAO;

    AnkiDatabase(File databaseFile) throws SQLException {
        try {
            //Loading the sqlite drivers
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            //Should never happen
            throw new SQLException(e);
        }

        //Creating a connection to the database:
        connectionSource = new JdbcConnectionSource("jdbc:sqlite:" + databaseFile.getAbsolutePath());

        //Creating DAOs
        notesDAO  = DaoManager.createDao(connectionSource, DBNote.class);
        cardsDAO  = DaoManager.createDao(connectionSource, DBCard.class);
        configDAO = DaoManager.createDao(connectionSource, DBConfig.class);
    }

    long getNumCardsInAllDecks() throws SQLException {
        return cardsDAO.countOf();
    }

    /**
     * Fetches the JSON containing information about the card models
     * @throws SQLException in case of error
     */
    String fetchCardModelsJson() throws SQLException {
        return fetchDbConfigRow().getModelsJsonStr();
    }

    private DBConfig fetchDbConfigRow() throws SQLException {
        //The "col" table always contains only one row
        return configDAO.queryForAll().get(0);
    }

    /**
     * Fetches the JSON containing information about the decks
     * @throws SQLException in case of error
     */
    String fetchDecksInfos() throws SQLException {
        return fetchDbConfigRow().getDecksJsonStr();
    }

    /**
     * @param deckId the id of the deck
     * @return how many cards there are in the deck having the given id
     */
    long getNumCardsInDeck(long deckId) throws SQLException {
        return cardsDAO.queryBuilder()
                .where()
                .eq("did", deckId)
                .countOf();
    }

    List<String> getTagsOfDeck(long deckId) throws SQLException {
        return getAllNotesOfDeck(deckId)
                .stream()
                .map(DBNote::getTags)
                .distinct()
                .collect(Collectors.toList()
        );
    }

    private List<DBNote> getAllNotesOfDeck(long deckId) throws SQLException {
        QueryBuilder<DBCard, Integer> cqb = cardsDAO.queryBuilder();
        cqb.where().eq("did", deckId);

        QueryBuilder<DBNote, Integer> nqb = notesDAO.queryBuilder()
                .join(cqb)
                .distinct();

        return nqb.query();
    }

    /**
     * Calculates how many cards in the deck have the specific tags
     * @param deckId the id of the deck
     * @param tags the tags to look for
     * @return how many cards in the deck have the specific tags
     */
    long getNumCardsHavingTagInDeck(long deckId, String tags) throws SQLException {
        QueryBuilder<DBCard, Integer> cqb = cardsDAO.queryBuilder();
        cqb.where().eq("did", deckId);

        return notesDAO.queryBuilder()
                .join(cqb)
                .where()
                    .eq("tags", tags)
                .countOf();

    }
}
