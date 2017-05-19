package com.github.slavetto.exporter;

/*
 * Created with ♥
 */

/**
 * A data-class containing the options that it's possible to specify to the exporter from the gui.
 */
public class ExportOptions {

    /**
     * The name(s) of the creator of the cards.
     */
    public String cardsAuthors = "";

    /**
     * The notes that will be shown below
     */
    public String sidebarNotes = "";

    /**
     * Whenever the cards have to be shuffled before export.
     */
    public boolean shuffleCards = false;
}
