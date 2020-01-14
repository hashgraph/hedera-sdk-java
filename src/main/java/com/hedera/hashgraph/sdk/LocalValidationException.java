package com.hedera.hashgraph.sdk;

/**
 * Thrown from {@link TransactionBuilder#build(Client)} and {@link QueryBuilder#execute(Client)}
 * if the built transaction or query fails local sanity checks.
 */
public class LocalValidationException extends IllegalStateException {
    LocalValidationException(String message) {
        super(message);
    }
}
