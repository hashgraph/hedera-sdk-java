package com.hedera.hashgraph.sdk;

import io.grpc.StatusRuntimeException;

/**
 * Wrapper for all transport-related exceptions. Unchecked but should appear in `throws` clauses for documentation.
 */
public class HederaNetworkException extends RuntimeException implements HederaThrowable {
    final StatusRuntimeException cause;

    HederaNetworkException(StatusRuntimeException cause) {
        super("transport error occurred while accessing the Hedera network", cause);
        this.cause = cause;
    }
}
