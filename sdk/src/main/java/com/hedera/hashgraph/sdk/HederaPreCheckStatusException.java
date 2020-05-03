package com.hedera.hashgraph.sdk;

import javax.annotation.Nullable;

public final class HederaPreCheckStatusException extends Exception {
    /** The status of the failing transaction */
    public final Status status;

    /**
     * The ID of the transaction that failed.
     *
     * This can be `null` if a query fails pre-check without an
     * associated payment transaction.
     */
    @Nullable
    public final TransactionId transactionId;

    HederaPreCheckStatusException(Status status, @Nullable TransactionId transactionId) {
        this.status = status;
        this.transactionId = transactionId;
    }

    @Override
    public String getMessage() {
        var stringBuilder = new StringBuilder();

        if (transactionId != null) {
            stringBuilder.append("Hedera transaction `").append(transactionId).append("` ");
        }

        stringBuilder.append("failed pre-check with the status `").append(status).append("`");

        return stringBuilder.toString();
    }
}
