package com.hedera.hashgraph.tck.exception;

/**
 * Thrown when the server cannot parse the given parameters
 */
public class InvalidJSONRPC2ParamsException extends RuntimeException {

    public InvalidJSONRPC2ParamsException() {
        super();
    }

    public InvalidJSONRPC2ParamsException(String message) {
        super(message);
    }

    public InvalidJSONRPC2ParamsException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidJSONRPC2ParamsException(Throwable cause) {
        super(cause);
    }
}
