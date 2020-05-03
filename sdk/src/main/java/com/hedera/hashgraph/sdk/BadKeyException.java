package com.hedera.hashgraph.sdk;

/**
 * Signals that a key could not be realized from the given input.
 * <p>
 * This exception can be raised by any of the {@code from} methods
 * on {@link PrivateKey} or {@link PublicKey}.
 */
public final class BadKeyException extends IllegalArgumentException {
    BadKeyException(String message) {
        super(message);
    }

    BadKeyException(Throwable cause) {
        super(cause);
    }
}
