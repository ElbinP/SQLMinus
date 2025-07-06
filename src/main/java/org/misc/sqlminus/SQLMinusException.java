package org.misc.sqlminus;

public class SQLMinusException extends Exception {

    public SQLMinusException() {
        super();
    }

    public SQLMinusException(String message) {
        super(message);
    }

    public SQLMinusException(String message, Throwable cause) {
        super(message, cause);
    }

    public SQLMinusException(Throwable cause) {
        super(cause);
    }
}

