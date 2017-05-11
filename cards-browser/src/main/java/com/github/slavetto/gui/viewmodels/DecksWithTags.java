package com.github.slavetto.gui.viewmodels;

/*
 * Created with ♥
 */

/**
 * Keeps track of the number of cards that have a specific tag in a deck.
 */
public class DecksWithTags {

    private final String deckName;
    private final String tags;
    private final long numCards;

    public DecksWithTags(String deckName, String tags, long numCards) {
        this.deckName = deckName;
        this.tags = tags;
        this.numCards = numCards;
    }

    public String getDeckName() {
        return deckName;
    }

    public String getTags() {
        return tags;
    }

    public long getNumCards() {
        return numCards;
    }

    @Override
    public String toString() {
        if (tags.isEmpty()) {
            return String.format("%s (%d)", deckName, numCards);
        } else {
            return String.format("%s [%s] (%d)", deckName, tags.trim(), numCards);
        }
    }
}
