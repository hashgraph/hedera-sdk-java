package org.hiero.tck.exception;

public class JSONRPCParseException extends RuntimeException {
    public JSONRPCParseException(String message) {
        super(message);
    }

    public JSONRPCParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
