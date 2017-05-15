package com.github.slavetto.parser.dbmodels;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/*
 * Created with ♥
 */

/**
 * A database model for the column
 */
@SuppressWarnings("ALL")
@DatabaseTable(tableName = "notes")
public class DBNote {

    public DBNote() {

    }

    @DatabaseField(id = true)
    private int id;

    @DatabaseField(columnName = "flds", canBeNull = false)
    private String fields;

    @DatabaseField(columnName = "mid", canBeNull = false)
    private String modelId;

    @DatabaseField(columnName = "tags", canBeNull = false)
    private String tags;

    public int getId() {
        return id;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
