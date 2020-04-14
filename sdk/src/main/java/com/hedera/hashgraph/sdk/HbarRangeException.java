package com.hedera.hashgraph.sdk;

/**
 * Thrown by constructors/factory methods of {@link Hbar} when a value is out of range.
 */
public class HbarRangeException extends IllegalArgumentException {
    HbarRangeException(String message) {
        super(message);
    }

    HbarRangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
