package com.github.slavetto.parser.models;

/*
 * Created with ♥
 */

/**
 * Keep track of all the information that are needed in order to create a {@link RenderedCard}. Used to hide the
 * database layer from other logic.
 */
public class CardReference {
    private final String[] fields;
    private final long id;
    private final int templateOrd;
    private final long cardModelId;

    public CardReference(long id, long cardModelId, int templateOrd, String[] fields) {
        this.id = id;
        this.cardModelId = cardModelId;
        this.templateOrd = templateOrd;
        this.fields = fields;
    }

    String[] getFields() {
        return fields;
    }

    int getTemplateOrd() {
        return templateOrd;
    }

    public long getId() {
        return id;
    }

    public long getCardModelId() {
        return cardModelId;
    }

}
