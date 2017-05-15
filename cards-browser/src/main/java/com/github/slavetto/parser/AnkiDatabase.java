package com.github.slavetto.parser;

import com.github.slavetto.parser.dbmodels.DBCard;
import com.github.slavetto.parser.dbmodels.DBConfig;
import com.github.slavetto.parser.dbmodels.DBNote;
import com.github.slavetto.parser.models.CardReference;
import com.github.slavetto.utils.NaturalOrderComparator;
import com.google.common.collect.Lists;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RawRowObjectMapper;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.j256.ormlite.field.DataType.*;

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
                .sorted(NaturalOrderComparator.INSTANCE::compare)
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

    /**
     * Retrieves all the cards from a deck having the given tags.
     * @param deckId the id of the deck to pull the cards from
     * @param tags the tags that the cards must have
     * @return a list of cards.
     */
    ArrayList<CardReference> fetchCards(long deckId, String tags) throws SQLException {
        String query =
                "SELECT C.id, N.mid, C.ord, N.flds " +
                "FROM cards C " +
                "  JOIN notes N " +
                "    ON C.nid = N.id " +
                "WHERE C.did = ? AND N.tags = ? ";

        DataType[] columnTypes = {LONG, LONG, INTEGER, STRING};
        String[] params = {String.valueOf(deckId), tags};

        return Lists.newArrayList(cardsDAO.queryRaw(query, columnTypes, getCardRawRowObjectMapper(), params));
    }

    /**
     * Retrieves and generates all the cards that belong to the given deck and having the give category
     * @param deckId id of the deck
     * @return a list containing all the cards
     */
    ArrayList<CardReference> fetchCards(long deckId) throws SQLException {
        String query =
                "SELECT C.id, N.mid, C.ord, N.flds " +
                "FROM cards C " +
                "  JOIN notes N " +
                "    ON C.nid = N.id " +
                "WHERE C.did = ? ";

        DataType[] columnTypes = {LONG, LONG, INTEGER, STRING};
        String[] params = {String.valueOf(deckId)};

        return Lists.newArrayList(cardsDAO.queryRaw(query, columnTypes, getCardRawRowObjectMapper(), params));
    }

    private RawRowObjectMapper<CardReference> getCardRawRowObjectMapper() {
        return (columnNames, dataTypes, resultColumns) -> {
            //The fields in database are separated by the "\u001F" character
            String fieldsToSplit = (String) resultColumns[3]; //N.flds
            String[] fieldValues = fieldsToSplit.split("\u001F");

            return new CardReference(
                    (Long)    resultColumns[0], //C.id
                    (Long)    resultColumns[1], //N.mid
                    (Integer) resultColumns[2], //C.ord
                    fieldValues
            );
        };
    }

    /**
     * Closes the connection to the database. Does not throws any exception if something goes wrong.
     */
    void close() {
        try {
            connectionSource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
