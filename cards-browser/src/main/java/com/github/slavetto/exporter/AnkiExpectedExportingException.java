package com.github.slavetto.exporter;

/*
 * Created with ♥
 */

/**
 * A runtime lambda-friendly exception that signals error that generally happen during exporting (io errors, etc..)
 */
public class AnkiExpectedExportingException extends RuntimeException {

    public AnkiExpectedExportingException(String message) {
        super(message);
    }

}
