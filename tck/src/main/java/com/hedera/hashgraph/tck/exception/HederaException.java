package com.hedera.hashgraph.tck.exception;

/**
 * Thrown when the SDK returns an error
 */
public class HederaException extends Exception {

    public HederaException() {
        super();
    }

    public HederaException(String message) {
        super(message);
    }

    public HederaException(String message, Throwable cause) {
        super(message, cause);
    }

    public HederaException(Throwable cause) {
        super(cause);
    }
}
