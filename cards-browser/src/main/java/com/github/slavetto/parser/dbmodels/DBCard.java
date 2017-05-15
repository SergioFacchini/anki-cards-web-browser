package com.github.slavetto.parser.dbmodels;

/*
 * Created with ♥
 */

import com.github.slavetto.parser.models.CardTemplate;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "cards")
@SuppressWarnings("ALL")
public class DBCard {

    /**
     * Unique identifier of the card
     */
    @DatabaseField(columnName = "id", id = true)
    private int id;

    /**
     * ID of the note that generated this card
     */
    @DatabaseField(columnName = "nid", canBeNull = false, foreign = true)
    private DBNote note;

    /**
     * ID identifier of the deck where this card is contained
     */
    @DatabaseField(columnName = "did")
    private int deckId;

    /**
     * The position of the template of the model; it matches {@link CardTemplate#ord} field.
     */
    @DatabaseField(columnName = "ord")
    private int ord;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DBNote getNote() {
        return note;
    }

    public void setNote(DBNote note) {
        this.note = note;
    }

    public int getDeckId() {
        return deckId;
    }

    public void setDeckId(int deckId) {
        this.deckId = deckId;
    }

    public int getOrd() {
        return ord;
    }

    public void setOrd(int ord) {
        this.ord = ord;
    }
}
