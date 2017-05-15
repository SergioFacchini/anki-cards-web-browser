package com.github.slavetto.parser.dbmodels;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/*
 * Created with ♥
 */

/**
 * A database model for the table that contains information about note models and decks
 */
@SuppressWarnings("ALL")
@DatabaseTable(tableName = "col")
public class DBConfig {

    @DatabaseField(columnName = "models", canBeNull = false)
    private String modelsJsonStr;

    @DatabaseField(columnName = "decks", canBeNull = false)
    private String decksJsonStr;

    public DBConfig() {

    }

    public String getModelsJsonStr() {
        return modelsJsonStr;
    }

    public void setModelsJsonStr(String modelsJsonStr) {
        this.modelsJsonStr = modelsJsonStr;
    }

    public String getDecksJsonStr() {
        return decksJsonStr;
    }

    public void setDecksJsonStr(String decksJsonStr) {
        this.decksJsonStr = decksJsonStr;
    }
}
