package com.hedera.hashgraph.sdk;

public class TransactionResponse {
    public final AccountId nodeId;

    public final byte[] transactionHash;

    public final TransactionId transactionId;

    TransactionResponse(AccountId nodeId, TransactionId transactionId) {
        this.nodeId = nodeId;
        this.transactionId = transactionId;
    }
}
