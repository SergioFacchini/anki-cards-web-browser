package com.github.slavetto.parser.dbmodels;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/*
 * Created with ♥
 */

/**
 * A database model for the column
 */
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

    public void setId(int id) {
        this.id = id;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
