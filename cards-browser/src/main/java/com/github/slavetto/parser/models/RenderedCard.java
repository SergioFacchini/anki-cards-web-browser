package com.github.slavetto.parser.models;

/*
 * Created with ♥
 */

/**
 * A card that was already "rendered". It tracks the HTML of the front and rear, card's identifier of the card model
 * that was used to generate it. Note that there are no reference to the template used to generate this card; we don't
 * track that because we don't need it during the export.
 */
public class RenderedCard {
    private String frontHTML;
    private String rearHTML;
    private long cardId;
    private long cardModelId;

    RenderedCard(String frontHTML, String rearHTML, long cardId, long cardModelId) {
        this.frontHTML = frontHTML;
        this.rearHTML = rearHTML;
        this.cardId = cardId;
        this.cardModelId = cardModelId;
    }


    public String getFrontHTML() {
        return frontHTML;
    }

    public void setFrontHTML(String frontHTML) {
        this.frontHTML = frontHTML;
    }

    public String getRearHTML() {
        return rearHTML;
    }

    public void setRearHTML(String rearHTML) {
        this.rearHTML = rearHTML;
    }

    public long getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public long getCardModelId() {
        return cardModelId;
    }

    public void setCardModelId(long cardModelId) {
        this.cardModelId = cardModelId;
    }
}
