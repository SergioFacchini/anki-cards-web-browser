package com.github.slavetto.parser;

import java.io.File;
import java.sql.*;

/**
 * Created with ♥
 */
class AnkiDatabase {

    private final Connection connectionSource;

    AnkiDatabase(File databaseFile) throws SQLException {
        try {
            //Loading the sqlite drivers
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            //Should never happen
            throw new SQLException(e);
        }

        //Creating a connection to the database:
        connectionSource = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
    }


    public int getNumCards() throws SQLException {
        Statement statement = connectionSource.createStatement();

        try(ResultSet rs = statement.executeQuery("SELECT count(*) FROM cards")){
            rs.next();
            return rs.getInt(1);
        }

    }
}
