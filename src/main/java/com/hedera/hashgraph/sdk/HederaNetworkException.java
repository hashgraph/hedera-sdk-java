package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;
import io.grpc.StatusRuntimeException;

import javax.annotation.Nullable;

/**
 * Wrapper for all transport-related exceptions. Unchecked but should appear in `throws` clauses for documentation.
 */
public class HederaNetworkException extends RuntimeException implements HederaThrowable {
    @Nullable
    final StatusRuntimeException cause;

    /**
     * The ID of the node which returned the network error.
     */
    public final AccountId nodeId;

    HederaNetworkException(AccountId nodeId, StatusRuntimeException cause) {
        super("transport error occurred while accessing the Hedera network using node " + nodeId, cause);
        this.nodeId = nodeId;
        this.cause = cause;
    }

    HederaNetworkException(AccountId nodeId, String message) {
        super(message);
        this.nodeId = nodeId;
        cause = null;
    }
}
