package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SignatureMap;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionID;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import javax.annotation.Nullable;

abstract class ChunkedTransaction<T extends ChunkedTransaction<T>> extends Transaction<T> implements WithExecuteAll {
    private static final int CHUNK_SIZE = 4096;

    /**
     * Maximum number of chunks this message will get broken up into when
     * its frozen.
     */
    private int maxChunks = 10;

    protected ByteString data = ByteString.EMPTY;

    ChunkedTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
    }

    ChunkedTransaction() {
        super();
    }

    @Nullable
    ByteString getData() {
        return data;
    }

    T setData(byte[] data) {
        requireNotFrozen();
        this.data = ByteString.copyFrom(data);

        // noinspection unchecked
        return (T) this;
    }

    T setData(ByteString data) {
        requireNotFrozen();
        this.data = data;

        // noinspection unchecked
        return (T) this;
    }

    T setData(String text) {
        requireNotFrozen();
        this.data = ByteString.copyFromUtf8(text);

        // noinspection unchecked
        return (T) this;
    }

    public T setMaxChunks(int maxChunks) {
        requireNotFrozen();
        this.maxChunks = maxChunks;

        // noinspection unchecked
        return (T) this;
    }

    public int getMaxChunks() {
        return maxChunks;
    }

    public byte[] getTransactionHash() {
        if (transactions.size() > nodeAccountIds.size()) {
            throw new IllegalStateException("a single transaction hash can not be calculated for a chunked transaction, try calling `getAllTransactionHashesPerNode`");
        }

        return super.getTransactionHash();
    }

    @Override
    public Map<AccountId, byte[]> getTransactionHashPerNode() {
        if (transactions.size() > nodeAccountIds.size()) {
            throw new IllegalStateException("a single transaction hash can not be calculated for a chunked transaction, try calling `getAllTransactionHashesPerNode`");
        }

        return super.getTransactionHashPerNode();
    }

    public final List<Map<AccountId, byte[]>> getAllTransactionHashesPerNode() {
        if (!this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before calculating the hash will be stable, try calling `freeze`");
        }

        var size = signedTransactions.size() / nodeAccountIds.size();
        var transactionHashes = new ArrayList<Map<AccountId, byte[]>>(transactions.size() / nodeAccountIds.size());

        for (var group = 0; group < size; ++group) {
            var hashes = new HashMap<AccountId, byte[]>();

            for (var i = group * size; i < (group + 1) * size; ++i) {
                hashes.put(nodeAccountIds.get(group), hash(transactions.get(i).getSignedTransactionBytes().toByteArray()));
            }

            transactionHashes.add(hashes);
        }

        return transactionHashes;
    }

    @FunctionalExecutable(type = "java.util.List<TransactionResponse>")
    public CompletableFuture<List<com.hedera.hashgraph.sdk.TransactionResponse>> executeAllAsync(Client client) {
        if (!isFrozen()) {
            freezeWith(client);
        }

        var operatorId = client.getOperatorAccountId();

        if (operatorId != null && operatorId.equals(Objects.requireNonNull(getTransactionId()).accountId)) {
            // on execute, sign each transaction with the operator, if present
            // and we are signing a transaction that used the default transaction ID
            signWithOperator(client);
        }

        CompletableFuture<List<com.hedera.hashgraph.sdk.TransactionResponse>> future =
            CompletableFuture.supplyAsync(() -> new ArrayList<>(transactionIds.size()));

        for (var i = 0; i < transactionIds.size(); i++) {
            future = future.thenCompose(list -> {
                var responseFuture = super.executeAsync(client);

                Function<TransactionResponse, ? extends CompletionStage<TransactionResponse>> receiptFuture =
                    (TransactionResponse response) -> response.getReceiptAsync(client)
                        .thenApply(receipt -> response);

                Function<TransactionResponse, List<TransactionResponse>> addToList =
                    (response) -> {
                        list.add(response);
                        return list;
                    };

                if (shouldGetReceipt()) {
                    return responseFuture.thenCompose(receiptFuture).thenApply(addToList).thenCompose(CompletableFuture::completedFuture);
                } else {
                    return responseFuture.thenApply(addToList).thenCompose(CompletableFuture::completedFuture);
                }
            });
        }

        return future;
    }

    @Override
    public CompletableFuture<com.hedera.hashgraph.sdk.TransactionResponse> executeAsync(Client client) {
        return executeAllAsync(client).thenApply(responses -> responses.get(0));
    }

    public T freezeWith(@Nullable Client client) {
        super.freezeWith(client);

        var initialTransactionId = Objects.requireNonNull(transactionIds.get(0)).toProtobuf();
        @Var var requiredChunks = (this.data.size() + (CHUNK_SIZE - 1)) / CHUNK_SIZE;

        if (requiredChunks == 0) {
            requiredChunks = 1;
        }

        if (requiredChunks > maxChunks) {
            throw new IllegalArgumentException(
                "message of " + this.data.size() + " bytes requires " + requiredChunks
                    + " chunks but the maximum allowed chunks is " + maxChunks + ", try using setMaxChunks");
        }

        signatures = new ArrayList<>(requiredChunks * nodeAccountIds.size());
        transactions = new ArrayList<>(requiredChunks * nodeAccountIds.size());
        signedTransactions = new ArrayList<>(requiredChunks * nodeAccountIds.size());
        transactionIds = new ArrayList<>(requiredChunks);

        @Var var nextTransactionId = initialTransactionId.toBuilder();

        for (int i = 0; i < requiredChunks; i++) {
            @Var var startIndex = i * CHUNK_SIZE;
            @Var var endIndex = startIndex + CHUNK_SIZE;

            if (endIndex > this.data.size()) {
                endIndex = this.data.size();
            }

            transactionIds.add(TransactionId.fromProtobuf(nextTransactionId.build()));

            onFreezeChunk(
                bodyBuilder.setTransactionID(nextTransactionId.build()),
                initialTransactionId,
                startIndex,
                endIndex,
                i,
                requiredChunks
            );

            // For each node we add a transaction with that node
            for (var nodeId : nodeAccountIds) {
                signatures.add(SignatureMap.newBuilder());
                signedTransactions.add(com.hedera.hashgraph.sdk.proto.SignedTransaction.newBuilder()
                    .setBodyBytes(
                        bodyBuilder
                            .setNodeAccountID(nodeId.toProtobuf())
                            .build()
                            .toByteString()
                    )
                );
            }

            // add 1 ns to the validStart to make cascading transaction IDs
            var nextValidStart = nextTransactionId.getTransactionValidStart().toBuilder();
            nextValidStart.setNanos(nextValidStart.getNanos() + 1);

            nextTransactionId.setTransactionValidStart(nextValidStart);
        }

        // noinspection unchecked
        return (T) this;
    }

    abstract void onFreezeChunk(TransactionBody.Builder body, TransactionID initialTransactionId, int startIndex, int endIndex, int chunk, int total);

    boolean shouldGetReceipt() {
        return false;
    }
}
