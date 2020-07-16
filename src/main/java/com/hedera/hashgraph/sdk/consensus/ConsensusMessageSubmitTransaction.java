package com.hedera.hashgraph.sdk.consensus;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.*;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConsensusMessageSubmitTransaction extends TransactionBuilder<List<TransactionId>, TransactionList, ConsensusMessageSubmitTransaction> {
    private static final int CHUNK_SIZE = 4096;

    @Nullable
    private ConsensusTopicId topicId;

    @Nullable
    private ConsensusMessageChunkInfo chunkInfo;

    private ByteString message = ByteString.EMPTY;

    private int maxChunks = 10;

    public ConsensusMessageSubmitTransaction() {
        super();
    }

    public ConsensusMessageSubmitTransaction setTopicId(ConsensusTopicId topicId) {
        this.topicId = topicId;
        return this;
    }

    public ConsensusMessageSubmitTransaction setMaxChunks(int maxChunks) {
        this.maxChunks = maxChunks;
        return this;
    }

    public ConsensusMessageSubmitTransaction setChunkInfo(
        TransactionId initialTransactionId,
        int totalNumber,
        int number)
    {
        this.chunkInfo = ConsensusMessageChunkInfo.newBuilder()
            .setInitialTransactionID(initialTransactionId.toProto())
            .setNumber(number)
            .setTotal(totalNumber)
            .build();

        return this;
    }

    public ConsensusMessageSubmitTransaction setMessage(byte[] message) {
        this.message = ByteString.copyFrom(message);
        return this;
    }

    public ConsensusMessageSubmitTransaction setMessage(String message) {
        this.message = ByteString.copyFromUtf8(message);
        return this;
    }

    @Override
    protected void doValidate() {
        require(topicId != null, "setTopicId() required");
    }

    @Override
    public List<TransactionId> execute(Client client) throws HederaStatusException, HederaNetworkException {
        return build(client).execute(client);
    }

    @Override
    public TransactionList build(@Nullable Client client) throws LocalValidationException {
        if (chunkInfo != null) {
            // This is duplicated from SingleTransactionBuilder
            // FIXME: De-duplicate it

            SingleConsensusMessageSubmitTransaction singleTransaction = new SingleConsensusMessageSubmitTransaction(
                bodyBuilder.buildPartial(),
                topicId,
                chunkInfo,
                message);

            return new TransactionList(Collections.singleton(singleTransaction.build(client)));
        }

        // lock into a transaction ID
        TransactionId initialTransactionId;
        if (!bodyBuilder.hasTransactionID()) {
            if (client == null || client.getOperatorId() == null) {
                throw new IllegalStateException("client must have an operator or set a transaction ID to build a consensus message transaction");
            }

            initialTransactionId = new TransactionId(client.getOperatorId());
        } else {
            initialTransactionId = new TransactionId(bodyBuilder.getTransactionID());
        }

        long totalMessageSize = this.message.size();
        long requiredChunks = (totalMessageSize + (CHUNK_SIZE - 1)) / CHUNK_SIZE;

        if (requiredChunks > maxChunks) {
            throw new IllegalArgumentException(
                "message of " + totalMessageSize + " bytes requires " + requiredChunks
                    + " chunks but the maximum allowed chunks is " + maxChunks + ", try using setMaxChunks");
        }

        ArrayList<com.hedera.hashgraph.sdk.Transaction> txs = new ArrayList<>();
        TransactionId nextTransactionId = initialTransactionId;

        for (int i = 0; i < requiredChunks; i += 1) {
            int startIndex = i * CHUNK_SIZE;
            int endIndex = startIndex + CHUNK_SIZE;

            if (endIndex > totalMessageSize) {
                endIndex = (int) totalMessageSize;
            }

            ByteString chunkMessage = message.substring(startIndex, endIndex);

            bodyBuilder.setTransactionID(nextTransactionId.toProto());

            txs.add(new SingleConsensusMessageSubmitTransaction(
                bodyBuilder.buildPartial(),
                topicId,
                ConsensusMessageChunkInfo.newBuilder()
                    .setInitialTransactionID(initialTransactionId.toProto())
                    .setTotal((int) requiredChunks)
                    .setNumber(i)
                    .build(),
                chunkMessage).build(client));

            // add 1 ns to make cascading transaction IDs
            nextTransactionId = TransactionId.withValidStart(nextTransactionId.accountId, nextTransactionId.validStart.plusNanos(1));
        }

        return new TransactionList(txs);
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return ConsensusServiceGrpc.getSubmitMessageMethod();
    }

    @Override
    public Transaction toProto() {
        throw new IllegalStateException("do not call toProto() on ConsensusMessageSubmitTransaction; call build() first");
    }

    @Override
    protected List<TransactionId> mapResponse(TransactionResponse response) throws HederaStatusException {
        throw new IllegalStateException("do not call mapResponse() on ConsensusMessageSubmitTransaction; call build() first");
    }

    static class SingleConsensusMessageSubmitTransaction extends SingleTransactionBuilder<SingleConsensusMessageSubmitTransaction> {
        SingleConsensusMessageSubmitTransaction(
            TransactionBody bodyBuilder,
            @Nullable ConsensusTopicId topicId,
            ConsensusMessageChunkInfo chunkInfo,
            ByteString message)
        {
            this.bodyBuilder.mergeFrom(bodyBuilder);

            ConsensusSubmitMessageTransactionBody.Builder builder = this.bodyBuilder.getConsensusSubmitMessageBuilder();

            if (topicId != null) {
                builder.setTopicID(topicId.toProto());
            }

            builder.setChunkInfo(chunkInfo);

            builder.setMessage(message);
        }

        @Override
        protected void doValidate() {
            // validation happens in parent class
        }

        @Override
        protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
            return ConsensusServiceGrpc.getSubmitMessageMethod();
        }
    }
}
