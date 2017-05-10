package com.github.slavetto.gui.viewmodels;

import com.github.slavetto.parser.models.DeckInfo;

/*
 * Created with ♥
 */
public class DeckWithCardNumber {
    private final String deckName;
    private final long deckId;
    private final long numCards;

    public DeckWithCardNumber(String deckName, long deckId, long numCards) {
        this.deckName = deckName;
        this.deckId = deckId;
        this.numCards = numCards;
    }

    public DeckWithCardNumber(DeckInfo deckInfo, long numCardsInDeck) {
        this(deckInfo.getName(), deckInfo.getId(), numCardsInDeck);
    }

    public String getDeckName() {
        return deckName;
    }

    public long getDeckId() {
        return deckId;
    }

    public long getNumCards() {
        return numCards;
    }

    @Override
    public String toString() {
        return String.format("%s (%d cards)", deckName, numCards);
    }
}
