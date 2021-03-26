package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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
public final class TopicMessageSubmitTransaction extends ChunkedTransaction<TopicMessageSubmitTransaction> {
    private final ConsensusSubmitMessageTransactionBody.Builder builder;

    public TopicMessageSubmitTransaction() {
        super();

        builder = ConsensusSubmitMessageTransactionBody.newBuilder();
    }

    TopicMessageSubmitTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getConsensusSubmitMessage().toBuilder();

        for (var i = 0; i < signedTransactions.size(); i += nodeAccountIds.isEmpty() ? 1 : nodeAccountIds.size()) {
            data = data.concat(
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
        return getData();
    }

    public TopicMessageSubmitTransaction setMessage(ByteString message) {
        return setData(message);
    }

    public TopicMessageSubmitTransaction setMessage(byte[] message) {
        return setData(message);
    }

    public TopicMessageSubmitTransaction setMessage(String message) {
        return setData(message);
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ConsensusServiceGrpc.getSubmitMessageMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setConsensusSubmitMessage(builder);
        return true;
    }

    @Override
    void onFreezeChunk(TransactionBody.Builder body, TransactionID initialTransactionId, int startIndex, int endIndex, int chunk, int total) {
        if (total == 1) {
            body.setConsensusSubmitMessage(builder.setMessage(data.substring(startIndex, endIndex)));
        } else {
            body.setConsensusSubmitMessage(builder.setMessage(data.substring(startIndex, endIndex))
                .setChunkInfo(ConsensusMessageChunkInfo.newBuilder()
                    .setInitialTransactionID(initialTransactionId)
                    .setNumber(chunk + 1)
                    .setTotal(total)
                )
            );
        }

    }
}
