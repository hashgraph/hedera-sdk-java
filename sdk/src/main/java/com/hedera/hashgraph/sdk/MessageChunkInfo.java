package com.hedera.hashgraph.sdk;

final class MessageChunkInfo {
    public TransactionId initialTransactionId;

    public int total;

    public int number;

    public MessageChunkInfo(TransactionId initialTransactionId, int total, int number) {
        this.initialTransactionId = initialTransactionId;
        this.total = total;
        this.number = number;
    }
}

