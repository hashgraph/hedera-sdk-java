package com.hedera.hashgraph.sdk;

import javax.annotation.Nullable;

public class TransactionResponse {
    public final AccountId nodeId;

    public final byte[] transactionHash;

    @Nullable
    public final TransactionId transactionId;

    TransactionResponse(AccountId nodeId,@Nullable TransactionId transactionId, byte[] transactionHash) {
        this.nodeId = nodeId;
        this.transactionId = transactionId;
        this.transactionHash = transactionHash;
    }
}

