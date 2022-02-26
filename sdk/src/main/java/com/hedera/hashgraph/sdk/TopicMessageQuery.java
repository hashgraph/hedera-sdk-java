package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.proto.Timestamp;
import com.hedera.hashgraph.sdk.proto.TransactionID;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusTopicQuery;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusTopicResponse;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java8.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class TopicMessageQuery extends MirrorQuery<TopicMessageQuery, ConsensusTopicQuery, ConsensusTopicResponse, TopicMessage, List<TopicMessage>> {
    @Nullable
    private TopicId topicId;

    @Nullable
    private Instant startTime;

    @Nullable
    private Instant endTime;

    private long limit = 0;

    HashMap<TransactionID, ArrayList<ConsensusTopicResponse>> pendingMessages = new HashMap<>();

    public TopicMessageQuery() {
    }

    public TopicMessageQuery setTopicId(TopicId topicId) {
        this.topicId = Objects.requireNonNull(topicId, "topicId must not be null");
        return this;
    }

    public TopicMessageQuery setStartTime(Instant startTime) {
        this.startTime = Objects.requireNonNull(startTime, "startTime must not be null");
        return this;
    }

    public TopicMessageQuery setEndTime(Instant endTime) {
        this.endTime = Objects.requireNonNull(endTime, "endTime must not be null");
        return this;
    }

    public TopicMessageQuery setLimit(long limit) {
        this.limit = limit;
        return this;
    }

    protected void onComplete() {
        LOGGER.info("Subscription to topic {} complete", topicId);
    }

    @Override
    protected MethodDescriptor<ConsensusTopicQuery, ConsensusTopicResponse> getMethodDescriptor() {
        return ConsensusServiceGrpc.getSubscribeTopicMethod();
    }

    @Override
    protected ConsensusTopicQuery makeRequest() {
        @Var
        var builder = ConsensusTopicQuery.newBuilder()
                .setTopicID(topicId.toProtobuf())
                .setLimit(limit);

        if (startTime != null) {
            builder.setConsensusStartTime(InstantConverter.toProtobuf(startTime));
        }

        if (endTime != null) {
            builder.setConsensusEndTime(InstantConverter.toProtobuf(endTime));
        }

        return builder.build();
    }

    @Override
    protected TopicMessage mapResponse(ConsensusTopicResponse protoResponse) {
        if (limit != 0) {
            limit = limit - counter.get();
        }

        startTime = InstantConverter.fromProtobuf(
            Timestamp.newBuilder(protoResponse.getConsensusTimestamp())
                .setNanos(protoResponse.getConsensusTimestamp().getNanos() + 1)
                .build()
        );

        // Short circuit for no chunks or 1/1 chunks
        if (!protoResponse.hasChunkInfo() || protoResponse.getChunkInfo().getTotal() == 1) {
            return TopicMessage.ofSingle(protoResponse);
        }

        // get the list of chunks for this pending message
        var initialTransactionID = protoResponse.getChunkInfo().getInitialTransactionID();

        // Can't use `HashMap.putIfAbsent()` since that method is not available on Android
        if (!pendingMessages.containsKey(initialTransactionID)) {
            pendingMessages.put(initialTransactionID, new ArrayList<>());
        }

        ArrayList<ConsensusTopicResponse> chunks = pendingMessages.get(initialTransactionID);

        // not possible as we do [putIfAbsent]
        // add our response to the pending chunk list
        chunks.add(protoResponse);

        // if we now have enough chunks, emit
        if (chunks.size() == protoResponse.getChunkInfo().getTotal()) {
            return TopicMessage.ofMany(chunks);
        }

        return null;
    }

    /**
     * This method will retry the following scenarios:
     * <p>
     * NOT_FOUND: Can occur when a client creates a topic and attempts to subscribe to it immediately before it
     * is available in the mirror node.
     *
     * @param throwable the potentially retryable exception
     * @return if the request should be retried or not
     */
    protected boolean shouldRetry(Throwable throwable) {
        if (throwable instanceof StatusRuntimeException) {
            var retry = super.shouldRetry(throwable);
            if (retry) {
                return true;
            }

            var statusRuntimeException = (StatusRuntimeException) throwable;
            var code = statusRuntimeException.getStatus().getCode();

            return code == Status.Code.NOT_FOUND;
        }

        return false;
    }

    public SubscriptionHandle subscribe(Client client, Consumer<TopicMessage> onNext) {
        setListener(onNext);
        return super.subscribe(client);
    }

    protected void logError(Throwable t) {
        LOGGER.warn("Error subscribing to topic {} during attempt #{}. Waiting {} before next attempt: {}",
                topicId, attempt, currentBackoff.toString(), t.getMessage());
    }

    @Override
    protected List<TopicMessage> mapExecuteResponse() {
        return responses;
    }
}
