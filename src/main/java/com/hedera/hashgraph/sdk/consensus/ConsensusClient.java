package com.hedera.hashgraph.sdk.consensus;

import com.google.common.collect.Iterators;
import com.hedera.hashgraph.proto.mirror.ConsensusServiceGrpc;
import com.hedera.hashgraph.proto.mirror.ConsensusTopicQuery;
import com.hedera.hashgraph.proto.mirror.ConsensusTopicResponse;
import com.hedera.hashgraph.sdk.TimestampHelper;

import java.time.Instant;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

public class ConsensusClient implements AutoCloseable {
    private final ManagedChannel channel;

    @Nullable
    private Consumer<Throwable> errorHandler;

    public ConsensusClient(String endpoint) {
        channel = ManagedChannelBuilder.forTarget(endpoint)
            // https://github.com/hashgraph/hedera-sdk-java/issues/294
            .keepAliveTime(2, TimeUnit.MINUTES)
            .usePlaintext()
            .build();
    }

    // TODO: enumerate possible throwable types
    /**
     * Set a global error handler for all streams.
     *
     * @param errorHandler
     * @return
     */
    public ConsensusClient setErrorHandler(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    /**
     * Subscribe to a Consensus Service topic; the callback will receive messages
     * with consensus timestamps starting now and continuing indefinitely into the future.
     *
     * @param topicId
     * @param listener
     * @return a handle which you can use to cancel the subscription at any time.
     */
    public Subscription subscribe(ConsensusTopicId topicId, Consumer<ConsensusMessage> listener) {
        return startStreamingCall(topicId, null, listener);
    }

    /**
     * Subscribe to a Consensus Service topic; the callback will receive messages
     * with consensus timestamps falling on or after the given {@link Instant}
     * (which may be in the past or future) and continuing indefinitely afterwards.
     *
     * @param topicId
     * @param consensusStartTime the lower bound for timestamps (inclusive), may be in the past
     *                           or future.
     * @param listener
     * @return a handle which you can use to cancel the subscription at any time.
     */
    public Subscription subscribe(ConsensusTopicId topicId, Instant consensusStartTime, Consumer<ConsensusMessage> listener) {
        return startStreamingCall(topicId, consensusStartTime, listener);
    }

    private Subscription startStreamingCall(ConsensusTopicId topicId, @Nullable Instant startTime,
                                            Consumer<ConsensusMessage> listener) {
        final ClientCall<ConsensusTopicQuery, ConsensusTopicResponse> call =
            channel.newCall(ConsensusServiceGrpc.getSubscribeTopicMethod(), CallOptions.DEFAULT);

        final ConsensusTopicQuery.Builder topicQuery = ConsensusTopicQuery.newBuilder()
            .setTopicID(topicId.toProto());

        if (startTime != null) {
            topicQuery.setConsensusStartTime(TimestampHelper.timestampFrom(startTime));
        }

        final Subscription subscription = new Subscription(topicId, startTime, call);

        ClientCalls.asyncServerStreamingCall(call, topicQuery.build(), new StreamObserver<ConsensusTopicResponse>() {
            @Override
            public void onNext(ConsensusTopicResponse message) {
                listener.accept(new ConsensusMessage(topicId, message));
            }

            @Override
            public void onError(Throwable t) {
                if (errorHandler != null) {
                    errorHandler.accept(t);
                }
            }

            @Override
            public void onCompleted() {

            }
        });

        return subscription;
    }

    /**
     * Get a blocking iterator which returns messages for the given topic with consensus timestamps
     * between two {@link Instant}s.
     *
     * @param topicId
     * @param startTime the lower bound for timestamps (inclusive), may be in the past or future.
     * @param endTime the upper bound for timestamps (exclusive), may also be in the past or future.
     * @return
     */
    public Iterator<ConsensusMessage> getMessages(ConsensusTopicId topicId, Instant startTime, Instant endTime) {
        final ConsensusTopicQuery topicQuery = ConsensusTopicQuery.newBuilder()
            .setTopicID(topicId.toProto())
            .setConsensusStartTime(TimestampHelper.timestampFrom(startTime))
            .setConsensusEndTime(TimestampHelper.timestampFrom(endTime))
            .build();

        final Iterator<ConsensusTopicResponse> iter = ClientCalls.blockingServerStreamingCall(
            channel,
            ConsensusServiceGrpc.getSubscribeTopicMethod(),
            CallOptions.DEFAULT,
            topicQuery);

        return Iterators.transform(iter, message -> new ConsensusMessage(topicId, Objects.requireNonNull(message)));
    }

    /**
     * Get a blocking iterator which returns messages for the given topic with consensus timestamps
     * starting now and continuing until the given {@link Instant}.
     *
     * @param topicId
     * @param endTime the upper bound for timestamps (exclusive), may be in the past or future.
     * @return
     */
    public Iterator<ConsensusMessage> getMessagesUntil(ConsensusTopicId topicId, Instant endTime) {
        final ConsensusTopicQuery topicQuery = ConsensusTopicQuery.newBuilder()
            .setTopicID(topicId.toProto())
            .setConsensusEndTime(TimestampHelper.timestampFrom(endTime))
            .build();

        final Iterator<ConsensusTopicResponse> iter = ClientCalls.blockingServerStreamingCall(
            channel,
            ConsensusServiceGrpc.getSubscribeTopicMethod(),
            CallOptions.DEFAULT,
            topicQuery);

        return Iterators.transform(iter, message -> new ConsensusMessage(topicId, Objects.requireNonNull(message)));
    }

    @Override
    public void close() throws InterruptedException {
        close(5, TimeUnit.SECONDS);
    }

    public boolean close(long timeout, TimeUnit timeoutUnit) throws InterruptedException {
        // shutdownNow() is required because we have by-design infinitely running calls
        channel.shutdownNow();
        return channel.awaitTermination(timeout, timeoutUnit);
    }

    public static final class Subscription {
        private final ClientCall<ConsensusTopicQuery, ConsensusTopicResponse> call;

        public final ConsensusTopicId topicId;

        @Nullable
        public final Instant consensusStartTime;

        private Subscription(ConsensusTopicId topicId, @Nullable Instant startTime, ClientCall<ConsensusTopicQuery, ConsensusTopicResponse> call) {
            this.call = call;
            this.topicId = topicId;
            this.consensusStartTime = startTime;
        }

        public void unsubscribe() {
            call.cancel("unsubscribed from topic", null);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Subscription that = (Subscription) o;
            return topicId.equals(that.topicId)
                && Objects.equals(consensusStartTime, that.consensusStartTime);
        }

        @Override
        public int hashCode() {
            return Objects.hash(topicId, consensusStartTime);
        }
    }
}
