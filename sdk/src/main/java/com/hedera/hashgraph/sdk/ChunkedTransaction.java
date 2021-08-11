package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SignatureMap;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionID;
import com.hedera.hashgraph.sdk.proto.SignedTransaction;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.*;

abstract class ChunkedTransaction<T extends ChunkedTransaction<T>> extends Transaction<T> implements WithExecuteAll {
    private static final int CHUNK_SIZE = 1024;

    /**
     * Maximum number of chunks this message will get broken up into when
     * its frozen.
     */
    private int maxChunks = 20;

    protected ByteString data = ByteString.EMPTY;

    ChunkedTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
    }

    ChunkedTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
    }

    ChunkedTransaction() {
        super();
    }

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

    @Override
    public byte[] getTransactionHash() {
        if (outerTransactions.size() > nodeAccountIds.size()) {
            throw new IllegalStateException("a single transaction hash can not be calculated for a chunked transaction, try calling `getAllTransactionHashesPerNode`");
        }

        return super.getTransactionHash();
    }

    @Override
    public Map<AccountId, byte[]> getTransactionHashPerNode() {
        if (outerTransactions.size() > nodeAccountIds.size()) {
            throw new IllegalStateException("a single transaction hash can not be calculated for a chunked transaction, try calling `getAllTransactionHashesPerNode`");
        }

        return super.getTransactionHashPerNode();
    }

    public final List<Map<AccountId, byte[]>> getAllTransactionHashesPerNode() {
        if (!this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before calculating the hash will be stable, try calling `freeze`");
        }

        buildAllTransactions();

        var txCount = transactionIds.size();
        var nodeCount = nodeAccountIds.size();
        var transactionHashes = new ArrayList<Map<AccountId, byte[]>>(txCount);

        for (var txIndex = 0; txIndex < txCount; ++txIndex) {
            var hashes = new HashMap<AccountId, byte[]>();
            var offset = txIndex*nodeCount;

            for (var nodeIndex = 0; nodeIndex < nodeCount; ++nodeIndex) {
                hashes.put(
                    nodeAccountIds.get(nodeIndex),
                    hash(outerTransactions.get(offset + nodeIndex).getSignedTransactionBytes().toByteArray()));
            }

            transactionHashes.add(hashes);
        }

        return transactionHashes;
    }

    @Override
    public T addSignature(PublicKey publicKey, byte[] signature) {
        if (data.size() > CHUNK_SIZE) {
            throw new IllegalStateException("Cannot manually add signature to chunked transaction with length greater than " + CHUNK_SIZE);
        }
        return super.addSignature(publicKey, signature);
    }

    @Override
    public Map<AccountId, Map<PublicKey, byte[]>> getSignatures() {
        if (data.size() > CHUNK_SIZE) {
            throw new IllegalStateException("Cannot call getSignatures() on a chunked transaction with length greater than " + CHUNK_SIZE);
        }
        return super.getSignatures();
    }

    public List<Map<AccountId, Map<PublicKey, byte[]>>> getAllSignatures() {
        if (publicKeys.isEmpty()) {
            return Collections.emptyList();
        }

        buildAllTransactions();

        var txCount = transactionIds.size();
        var nodeCount = nodeAccountIds.size();

        var retval = new ArrayList<Map<AccountId, Map<PublicKey, byte[]>>>(txCount);

        for(int i = 0; i < txCount; i++) {
            retval.add(getSignaturesAtOffset(i * nodeCount));
        }

        return retval;
    }

    @Override
    @FunctionalExecutable(type = "java.util.List<TransactionResponse>")
    public CompletableFuture<List<com.hedera.hashgraph.sdk.TransactionResponse>> executeAllAsync(Client client) {
        if (!isFrozen()) {
            freezeWith(client);
        }

        var operatorId = client.getOperatorAccountId();

        if (operatorId != null && operatorId.equals(Objects.requireNonNull(getTransactionId().accountId))) {
            // on execute, sign each transaction with the operator, if present
            // and we are signing a transaction that used the default transaction ID
            signWithOperator(client);
        }

        @Var
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
                    return responseFuture.thenCompose(receiptFuture).thenApply(addToList);
                } else {
                    return responseFuture.thenApply(addToList);
                }
            });
        }

        return future;
    }

    @Override
    public CompletableFuture<com.hedera.hashgraph.sdk.TransactionResponse> executeAsync(Client client) {
        return executeAllAsync(client).thenApply(responses -> responses.get(0));
    }

    @Override
    public ScheduleCreateTransaction schedule() {
        requireNotFrozen();
        if (!nodeAccountIds.isEmpty()) {
            throw new IllegalStateException(
                "The underlying transaction for a scheduled transaction cannot have node account IDs set"
            );
        }
        if (data.size() > CHUNK_SIZE) {
            throw new IllegalStateException("Cannot schedule a chunked transaction with length greater than " + CHUNK_SIZE);
        }

        var bodyBuilder = spawnBodyBuilder();

        onFreeze(bodyBuilder);

        onFreezeChunk(
            bodyBuilder,
            null,
            0,
            data.size(),
            1,
            1
        );

        return doSchedule(bodyBuilder);
    }

    @Override
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

        sigPairLists = new ArrayList<>(requiredChunks * nodeAccountIds.size());
        outerTransactions = new ArrayList<>(requiredChunks * nodeAccountIds.size());
        innerSignedTransactions = new ArrayList<>(requiredChunks * nodeAccountIds.size());
        transactionIds = new ArrayList<>(requiredChunks);

        var nextTransactionId = initialTransactionId.toBuilder();

        for (int i = 0; i < requiredChunks; i++) {
            var startIndex = i * CHUNK_SIZE;
            @Var var endIndex = startIndex + CHUNK_SIZE;

            if (endIndex > this.data.size()) {
                endIndex = this.data.size();
            }

            transactionIds.add(TransactionId.fromProtobuf(nextTransactionId.build()));

            onFreezeChunk(
                frozenBodyBuilder.setTransactionID(nextTransactionId.build()),
                initialTransactionId,
                startIndex,
                endIndex,
                i,
                requiredChunks
            );

            // For each node we add a transaction with that node
            for (var nodeId : nodeAccountIds) {
                sigPairLists.add(SignatureMap.newBuilder());
                innerSignedTransactions.add(SignedTransaction.newBuilder()
                    .setBodyBytes(
                        frozenBodyBuilder
                            .setNodeAccountID(nodeId.toProtobuf())
                            .build()
                            .toByteString()
                    )
                );
                outerTransactions.add(null);
            }

            // add 1 ns to the validStart to make cascading transaction IDs
            var nextValidStart = nextTransactionId.getTransactionValidStart().toBuilder();
            nextValidStart.setNanos(nextValidStart.getNanos() + 1);

            nextTransactionId.setTransactionValidStart(nextValidStart);
        }

        // noinspection unchecked
        return (T) this;
    }

    abstract void onFreezeChunk(TransactionBody.Builder body, @Nullable TransactionID initialTransactionId, int startIndex, int endIndex, int chunk, int total);

    boolean shouldGetReceipt() {
        return false;
    }
}
