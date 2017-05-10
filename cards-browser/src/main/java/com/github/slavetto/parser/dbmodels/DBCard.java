package com.github.slavetto.parser.dbmodels;

/*
 * Created with ♥
 */

import com.github.slavetto.parser.models.CardTemplate;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "cards")
public class DBCard {

    /**
     * Unique identifier of the card
     */
    @DatabaseField(columnName = "id", id = true)
    private int id;

    /**
     * ID of the note that generated this card
     */
    @DatabaseField(columnName = "nid")
    private int noteId;

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

}
