package com.hedera.hashgraph.sdk;

public final class HederaPreCheckStatusException extends Exception {
    /** The status of the failing transaction */
    public final Status status;

    /** The ID of the transaction that failed */
    public final TransactionId transactionId;

    HederaPreCheckStatusException(Status status, TransactionId transactionId) {
        this.status = status;
        this.transactionId = transactionId;
    }

    @Override
    public String getMessage() {
        return "Hedera transaction `"
                + transactionId
                + "` failed pre-check with the status `"
                + status
                + "`";
    }
}
