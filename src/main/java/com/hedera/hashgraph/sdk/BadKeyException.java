package com.hedera.hashgraph.sdk;

public final class BadKeyException extends IllegalArgumentException {
    BadKeyException(String message) {
        super(message);
    }

    BadKeyException(Throwable cause) {
        super(cause);
    }
}
