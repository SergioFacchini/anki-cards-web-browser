package com.github.slavetto.parser.exceptions;

/*
 * Created with ♥
 */
public class APKGFileException extends Exception {
    APKGFileException() {
    }

    public APKGFileException(String message) {
        super(message);
    }

    public APKGFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public APKGFileException(Throwable cause) {
        super(cause);
    }

    public APKGFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
