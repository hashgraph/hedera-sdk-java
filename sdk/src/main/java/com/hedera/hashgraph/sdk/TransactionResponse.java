package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import java8.util.concurrent.CompletableFuture;
import org.bouncycastle.util.encoders.Hex;

import java.util.Collections;

public final class TransactionResponse implements WithGetReceipt, WithGetRecord {
    public final AccountId nodeId;

    public final byte[] transactionHash;

    public final TransactionId transactionId;

    TransactionResponse(AccountId nodeId, TransactionId transactionId, byte[] transactionHash) {
        this.nodeId = nodeId;
        this.transactionId = transactionId;
        this.transactionHash = transactionHash;
    }

    @Override
    public CompletableFuture<TransactionReceipt> getReceiptAsync(Client client) {
        return new TransactionReceiptQuery()
            .setTransactionId(transactionId)
            .setNodeAccountIds(Collections.singletonList(nodeId))
            .executeAsync(client);
    }

    @Override
    public CompletableFuture<TransactionRecord> getRecordAsync(Client client) {
        return getReceiptAsync(client).thenCompose((receipt) -> new TransactionRecordQuery()
                .setTransactionId(transactionId)
                .setNodeAccountIds(Collections.singletonList(nodeId))
                .executeAsync(client)
        );
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("nodeId", nodeId)
            .add("transactionHash", Hex.toHexString(transactionHash))
            .add("transactionId", transactionId)
            .toString();
    }
}
