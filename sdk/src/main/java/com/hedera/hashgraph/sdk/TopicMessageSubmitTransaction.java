package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java8.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
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

    TopicMessageSubmitTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getConsensusSubmitMessage().toBuilder();

        for (var i = 0; i < signedTransactions.size(); i += nodeIds.size()) {
            message = message.concat(
                TransactionBody.parseFrom(signedTransactions.get(i).getBodyBytes())
                    .getConsensusSubmitMessage().getMessage()
            );
        }
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
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ConsensusServiceGrpc.getSubmitMessageMethod();
    }

    public byte[] getTransactionHash() {
        if (transactions.size() > nodeIds.size()) {
            throw new IllegalStateException("a single transaction hash can not be calculated for a chunked transaction, try calling `getAllTransactionHashesPerNode`");
        }

        return super.getTransactionHash();
    }

    @Override
    public Map<AccountId, byte[]> getTransactionHashPerNode() {
        if (transactions.size() > nodeIds.size()) {
            throw new IllegalStateException("a single transaction hash can not be calculated for a chunked transaction, try calling `getAllTransactionHashesPerNode`");
        }

        return super.getTransactionHashPerNode();
    }

    public final List<Map<AccountId, byte[]>> getAllTransactionHashesPerNode() {
        if (!this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before calculating the hash will be stable, try calling `freeze`");
        }

        var size = signedTransactions.size() / nodeIds.size();
        var transactionHashes = new ArrayList<Map<AccountId, byte[]>>(transactions.size() / nodeIds.size());

        for (var group = 0; group < size; ++group) {
            var hashes = new HashMap<AccountId, byte[]>();

            for (var i = group * size; i < (group + 1) * size; ++i) {
                hashes.put(nodeIds.get(group), hash(transactions.get(i).getSignedTransactionBytes().toByteArray()));
            }

            transactionHashes.add(hashes);
        }

        return transactionHashes;
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        return true;
    }

    @Override
    public TopicMessageSubmitTransaction freezeWith(@Nullable Client client) {
        super.freezeWith(client);

        var initialTransactionId = Objects.requireNonNull(transactionIds.get(0)).toProtobuf();
        var requiredChunks = (this.message.size() + (CHUNK_SIZE - 1)) / CHUNK_SIZE;

        if (requiredChunks == 1) {
            return this;
        }

        if (requiredChunks > maxChunks) {
            throw new IllegalArgumentException(
                "message of " + this.message.size() + " bytes requires " + requiredChunks
                    + " chunks but the maximum allowed chunks is " + maxChunks + ", try using setMaxChunks");
        }

        signatures = new ArrayList<>(requiredChunks * nodeIds.size());
        transactions = new ArrayList<>(requiredChunks * nodeIds.size());
        signedTransactions = new ArrayList<>(requiredChunks * nodeIds.size());
        transactionIds = new ArrayList<>(requiredChunks);

        @Var var nextTransactionId = initialTransactionId.toBuilder();

        for (int i = 0; i < requiredChunks; i++) {
            @Var var startIndex = i * CHUNK_SIZE;
            @Var var endIndex = startIndex + CHUNK_SIZE;

            if (endIndex > this.message.size()) {
                endIndex = this.message.size();
            }

            transactionIds.add(TransactionId.fromProtobuf(nextTransactionId.build()));

            bodyBuilder
                .setTransactionID(nextTransactionId.build())
                .setConsensusSubmitMessage(
                    builder
                        .setMessage(message.substring(startIndex, endIndex))
                        .setChunkInfo(ConsensusMessageChunkInfo.newBuilder()
                            .setInitialTransactionID(initialTransactionId)
                            .setTotal(requiredChunks)
                            .setNumber(i + 1) // 1..=total
                            .build()
                        ).build()
                );

            // For each node we add a transaction with that node
            for (var nodeId : nodeIds) {
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

        if (operatorId != null && operatorId.equals(Objects.requireNonNull(getTransactionId()).accountId)) {
            // on execute, sign each transaction with the operator, if present
            // and we are signing a transaction that used the default transaction ID
            signWithOperator(client);
        }

        CompletableFuture<List<com.hedera.hashgraph.sdk.TransactionResponse>> future =
            CompletableFuture.supplyAsync(() -> new ArrayList<>(transactionIds.size()));

        for (var i = 0; i < transactionIds.size(); i++) {
            future = future.thenCompose(list -> super.executeAsync(client).thenApply(response -> {
                    list.add(response);
                    return list;
                }).thenCompose(CompletableFuture::completedFuture)
            );
        }

        return future;
    }
}
