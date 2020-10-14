package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.AccountID;
import com.hedera.hashgraph.sdk.proto.ConsensusMessageChunkInfo;
import com.hedera.hashgraph.sdk.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.ConsensusSubmitMessageTransactionBody;
import com.hedera.hashgraph.sdk.proto.SignatureMap;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionID;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.Function;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public TopicMessageSubmitTransaction() {
        super();

        builder = ConsensusSubmitMessageTransactionBody.newBuilder();
    }

    TopicMessageSubmitTransaction(TransactionBody body, SignatureMap signatureMap) {
        super(body);

        builder = body.getConsensusSubmitMessage().toBuilder();

        if (signatureMap.getSigPairCount() > 0) {
            // this transaction has been signed, we need to freeze it
            // which should inflate the right data structures
            freeze();

            var signatures = signatureMap.getSigPairList().iterator();
            var numSignatures = signatureMap.getSigPairCount() / chunkTransactions.size();

            for (var chunkTx : chunkTransactions) {
                for (var i = 0; i < numSignatures; i++) {
                    chunkTx.signatures.get(0).addSigPair(signatures.next());
                }
            }
        }
    }

    @Override
    public byte[] toBytes() {
        var signatureMap = SignatureMap.newBuilder();

        @Var
        @Nullable
        AccountID firstNodeId = null;

        @Var
        @Nullable
        TransactionID initialTransactionID = null;

        for (var chunkTx : chunkTransactions) {
            if (firstNodeId == null) {
                firstNodeId = chunkTx.getNodeAccountId(null).toProtobuf();
            }

            if (initialTransactionID == null) {
                initialTransactionID = chunkTx.bodyBuilder
                    .getConsensusSubmitMessage()
                    .getChunkInfo()
                    .getInitialTransactionID();
            }

            for (var sigPair : chunkTx.signatures.get(0).getSigPairList()) {
                signatureMap.addSigPair(sigPair);
            }
        }

        var outBodyBuilder = bodyBuilder.clone();

        if (initialTransactionID != null) {
            outBodyBuilder.setTransactionID(initialTransactionID);
        }

        if (firstNodeId != null) {
            outBodyBuilder.setNodeAccountID(firstNodeId);
        }

        return com.hedera.hashgraph.sdk.proto.Transaction.newBuilder()
            .setBodyBytes(outBodyBuilder
                .setConsensusSubmitMessage(builder)
                .buildPartial()
                .toByteString())
            .setSigMap(signatureMap)
            .buildPartial()
            .toByteArray();
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
        return builder.getMessage();
    }

    public TopicMessageSubmitTransaction setMessage(ByteString message) {
        requireNotFrozen();
        builder.setMessage(message);
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

    @Override
    public byte[] getTransactionHash() {
        if (!this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before calculating the hash will be stable, try calling `freeze`");
        }

        if (chunkTransactions.size() > 1) {
            throw new IllegalStateException("a single transaction hash can not be calculated for a chunked transaction, try calling `getAllTransactionHash`");
        }

        return chunkTransactions.get(0).getTransactionHash();
    }

    public final List<byte[]> getAllTransactionHash() {
        if (!this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before calculating the hash will be stable, try calling `freeze`");
        }

        var transactionHashes = new ArrayList<byte[]>(chunkTransactions.size());

        for (var chunkTx : chunkTransactions) {
            transactionHashes.add(chunkTx.getTransactionHash());
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

        for (var chunkTx : chunkTransactions) {
            chunkTx.signWithOperator(client);
        }

        return this;
    }

    @Override
    protected boolean isFrozen() {
        return !chunkTransactions.isEmpty();
    }

    @Override
    public TopicMessageSubmitTransaction freezeWith(@Nullable Client client) {
        var wasFrozen = isFrozen();

        if (!bodyBuilder.hasNodeAccountID()) {
            if (client != null) {
                // if there is no defined node ID, we need to pick a set of nodes
                // up front so each chunk's nodes are consistent
                var size = client.getNumberOfNodesForTransaction();
                nodeIds = new ArrayList<>(size);

                for (var i = 0; i < size; ++i) {
                    var nodeId = client.getNextNodeId();

                    nodeIds.add(nodeId);
                }
            } else {
                throw new IllegalStateException("`client` must be provided or `nodeId` must be set");
            }
        }

        super.freezeWith(client);

        if (!wasFrozen) {
            for (var chunkTx : chunkTransactions) {
                chunkTx.freezeWith(client);
            }
        }

        return this;
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        var initialTransactionId = bodyBuilder.getTransactionID();
        var message = builder.getMessage();
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
