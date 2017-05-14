package com.github.slavetto.parser.exceptions;

/*
 * Created with ♥
 */
public class DatabaseInconsistentException extends RuntimeException {

    public DatabaseInconsistentException() {
    }

    public DatabaseInconsistentException(String message) {
        super(message);
    }

    public DatabaseInconsistentException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseInconsistentException(Throwable cause) {
        super(cause);
    }

    public DatabaseInconsistentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
