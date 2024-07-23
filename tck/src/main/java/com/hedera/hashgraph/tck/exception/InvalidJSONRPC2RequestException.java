package com.hedera.hashgraph.tck.exception;

/**
 * Thrown when the server cannot process the request
 */
public class InvalidJSONRPC2RequestException extends Exception {

    public InvalidJSONRPC2RequestException(String message) {
        super(message);
    }
}
