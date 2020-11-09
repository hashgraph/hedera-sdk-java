package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.Function;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Submit a message for consensus.
 * <p>
 * Valid and authorized messages on valid topics will be ordered by the consensus service, gossipped to the
 * mirror net, and published (in order) to all subscribers (from the mirror net) on this topic.
 * <p>
 * The submitKey (if any) must sign this transaction.
 * <p>
 * On success, the resulting TransactionReceipt contains the topic's updated topicSequenceNumber and
 * topicRunningHash.
 */
public final class TopicMessageSubmitTransaction extends Transaction<TopicMessageSubmitTransaction> implements WithExecuteAll {
    private static final int CHUNK_SIZE = 4096;

    private final ConsensusSubmitMessageTransactionBody.Builder builder;

    private List<Transaction<SingleTopicMessageSubmitTransaction>> chunkTransactions = Collections.emptyList();

    /**
     * Maximum number of chunks this message will get broken up into when
     * its frozen.
     */
    private int maxChunks = 10;

    private ByteString message = ByteString.EMPTY;

    public TopicMessageSubmitTransaction() {
        super();

        builder = ConsensusSubmitMessageTransactionBody.newBuilder();
    }

    TopicMessageSubmitTransaction(HashMap<TransactionId, HashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) {
        super(txs.values().iterator().next());

        chunkTransactions = new ArrayList<>(txs.entrySet().size());

        for (var txEntry : txs.entrySet()) {
            var tx = new SingleTopicMessageSubmitTransaction(txEntry.getValue());
            message.concat(tx.bodyBuilder.getConsensusSubmitMessage().getMessage());
            chunkTransactions.add(tx);
        }

        builder = bodyBuilder.getConsensusSubmitMessage().toBuilder();
    }

    public byte[] toBytes() {
        if (!this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before calculating the hash will be stable, try calling `freeze`");
        }

        var buf = new ByteArrayOutputStream();

        for (var tx : chunkTransactions) {
            for (int i = 0; i < tx.transactions.size(); i++) {
                try {
                    tx.transactions.get(i).setSigMap(tx.signatures.get(i)).buildPartial().writeDelimitedTo(buf);
                } catch (IOException e) {
                    // Do nothing as this should never happen
                }
            }
        }

        return buf.toByteArray();
    }

    public Map<AccountId, byte[]> toBytesPerNode() {
        if (!this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before calculating the hash will be stable, try calling `freeze`");
        }

        var bufs = new HashMap<AccountId, ByteArrayOutputStream>();

        for (var tx : chunkTransactions) {
            for (int i = 0; i < tx.transactions.size(); i++) {
                var buf = bufs.computeIfAbsent(
                    tx.nodeIds.get(i),
                    k -> new ByteArrayOutputStream()
                );

                try {
                    transactions.get(i).setSigMap(signatures.get(0)).buildPartial().writeDelimitedTo(buf);
                } catch (IOException e) {
                    // Do nothing as this should never happen
                }
            }
        }

        var bytesMap = new HashMap<AccountId, byte[]>(bufs.size());
        for (var entry : bufs.entrySet()) {
            bytesMap.put(entry.getKey(), entry.getValue().toByteArray());
        }

        return bytesMap;
    }

    @Nullable
    public TopicId getTopicId() {
        return builder.hasTopicID() ? TopicId.fromProtobuf(builder.getTopicID()) : null;
    }

    public TopicMessageSubmitTransaction setTopicId(TopicId topicId) {
        requireNotFrozen();
        builder.setTopicID(topicId.toProtobuf());
        return this;
    }

    public ByteString getMessage() {
        return message;
    }

    public TopicMessageSubmitTransaction setMessage(ByteString message) {
        requireNotFrozen();
        this.message = message;
        return this;
    }

    public TopicMessageSubmitTransaction setMessage(byte[] message) {
        return setMessage(ByteString.copyFrom(message));
    }

    public TopicMessageSubmitTransaction setMessage(String message) {
        return setMessage(ByteString.copyFromUtf8(message));
    }

    public TopicMessageSubmitTransaction setMaxChunks(int maxChunks) {
        requireNotFrozen();
        this.maxChunks = maxChunks;
        return this;
    }

    @Override
    public CompletableFuture<com.hedera.hashgraph.sdk.TransactionResponse> executeAsync(Client client) {
        return executeAllAsync(client).thenApply(responses -> responses.get(0));
    }

    @Override
    @FunctionalExecutable(type = "java.util.List<TransactionResponse>")
    public CompletableFuture<List<com.hedera.hashgraph.sdk.TransactionResponse>> executeAllAsync(Client client) {
        if (!isFrozen()) {
            freezeWith(client);
        }

        var operatorId = client.getOperatorAccountId();

        if (operatorId != null && operatorId.equals(getTransactionId().accountId)) {
            // on execute, sign each transaction with the operator, if present
            // and we are signing a transaction that used the default transaction ID
            signWithOperator(client);
        }

        CompletableFuture<?>[] futures = new CompletableFuture[chunkTransactions.size()];

        for (var i = 0; i < chunkTransactions.size(); i++) {
            futures[i] = chunkTransactions.get(i).executeAsync(client);
        }

        return CompletableFuture.allOf(futures).thenApply(v -> {
            List<com.hedera.hashgraph.sdk.TransactionResponse> responses = new ArrayList<>(futures.length);

            for (var fut : futures) {
                responses.add((com.hedera.hashgraph.sdk.TransactionResponse) fut.join());
            }

            return responses;
        });
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ConsensusServiceGrpc.getSubmitMessageMethod();
    }

    public byte[] getTransactionHash() {
        if (!this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before calculating the hash will be stable, try calling `freeze`");
        }

        if (chunkTransactions.size() > 1) {
            throw new IllegalStateException("a single transaction hash can not be calculated for a chunked transaction, try calling `getAllTransactionHashesPerNode`");
        }

        return chunkTransactions.get(0).getTransactionHash();
    }

    @Override
    public Map<AccountId, byte[]> getTransactionHashPerNode() {
        if (!this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before calculating the hash will be stable, try calling `freeze`");
        }

        if (chunkTransactions.size() > 1) {
            throw new IllegalStateException("a single transaction hash can not be calculated for a chunked transaction, try calling `getAllTransactionHashesPerNode`");
        }

        return chunkTransactions.get(0).getTransactionHashPerNode();
    }

    public final List<Map<AccountId, byte[]>> getAllTransactionHashesPerNode() {
        if (!this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before calculating the hash will be stable, try calling `freeze`");
        }

        var transactionHashes = new ArrayList<Map<AccountId, byte[]>>(chunkTransactions.size());

        for (var chunkTx : chunkTransactions) {
            transactionHashes.add(chunkTx.getTransactionHashPerNode());
        }

        return transactionHashes;
    }

    @Override
    public TopicMessageSubmitTransaction signWith(PublicKey publicKey, Function<byte[], byte[]> transactionSigner) {
        if (!isFrozen()) {
            freeze();
        }

        for (var chunkTx : chunkTransactions) {
            chunkTx.signWith(publicKey, transactionSigner);
        }

        return this;
    }

    @Override
    public TopicMessageSubmitTransaction signWithOperator(Client client) {
        if (!isFrozen()) {
            freezeWith(client);
        }

        if (client.getOperator() == null) {
            throw new IllegalStateException(
                "`client` must have an `operator` to sign with the operator");
        }

        signWith(client.getOperator().publicKey, client.getOperator().transactionSigner);

        return this;
    }

    @Override
    protected boolean isFrozen() {
        return !chunkTransactions.isEmpty();
    }

    @Override
    public TopicMessageSubmitTransaction freezeWith(@Nullable Client client) {
        if (isFrozen()) {
            return this;
        }

        if (client == null && nodeIds.size() == 0) {
            throw new IllegalStateException("`client` must be provided or `nodeId` must be set");
        }

        if (nodeIds.size() == 0) {
            nodeIds = client.network.getNodeAccountIdsForExecute();
        }

        super.freezeWith(client);

        for (var chunkTx : chunkTransactions) {
            chunkTx.freezeWith(client);
        }

        return this;
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        var initialTransactionId = bodyBuilder.getTransactionID();
        var message = this.message;
        var topicId = builder.getTopicID();
        var totalMessageSize = message.size();
        var requiredChunks = (totalMessageSize + (CHUNK_SIZE - 1)) / CHUNK_SIZE;

        if (requiredChunks > maxChunks) {
            throw new IllegalArgumentException(
                "message of " + totalMessageSize + " bytes requires " + requiredChunks
                    + " chunks but the maximum allowed chunks is " + maxChunks + ", try using setMaxChunks");
        }

        chunkTransactions = new ArrayList<>(requiredChunks);
        @Var var nextTransactionId = initialTransactionId.toBuilder();

        for (int i = 0; i < requiredChunks; i++) {
            @Var var startIndex = i * CHUNK_SIZE;
            @Var var endIndex = startIndex + CHUNK_SIZE;

            if (endIndex > totalMessageSize) {
                endIndex = totalMessageSize;
            }

            var chunkMessage = message.substring(startIndex, endIndex);

            bodyBuilder.setTransactionID(nextTransactionId.build());

            chunkTransactions.add(new SingleTopicMessageSubmitTransaction(
                nodeIds,
                bodyBuilder.clone(),
                topicId,
                ConsensusMessageChunkInfo.newBuilder()
                    .setInitialTransactionID(initialTransactionId)
                    .setTotal(requiredChunks)
                    .setNumber(i + 1) // 1..=total
                    .build(),
                chunkMessage));

            // add 1 ns to the validStart to make cascading transaction IDs
            var nextValidStart = nextTransactionId.getTransactionValidStart().toBuilder();
            nextValidStart.setNanos(nextValidStart.getNanos() + 1);

            nextTransactionId.setTransactionValidStart(nextValidStart);
        }

        // false means stop freezing, this is good enough
        return false;
    }
}
