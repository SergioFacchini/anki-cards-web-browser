package com.github.slavetto.parser;

import com.github.slavetto.parser.dbmodels.DBCard;
import com.github.slavetto.parser.dbmodels.DBConfig;
import com.github.slavetto.parser.dbmodels.DBNote;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import java.io.File;
import java.sql.SQLException;

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
        return notesDAO.countOf();
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
}
