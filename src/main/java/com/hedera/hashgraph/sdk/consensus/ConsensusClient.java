package com.hedera.hashgraph.sdk.consensus;

import com.google.common.collect.Iterators;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.mirror.api.proto.ConsensusServiceGrpc;
import com.hedera.mirror.api.proto.ConsensusTopicQuery;
import com.hedera.mirror.api.proto.ConsensusTopicResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

public class ConsensusClient implements AutoCloseable {
    private final ManagedChannel channel;

    private final HashMap<ConsensusTopicId, Subscription> subscriptions = new HashMap<>();

    public ConsensusClient(String endpoint) {
        channel = ManagedChannelBuilder.forTarget(endpoint)
            .usePlaintext()
            .build();
    }

    public void subscribe(ConsensusTopicId topic, TopicListener listener) {
        subscriptions.computeIfAbsent(topic, _topic -> new Subscription(startStreamingCall(topic)))
            .listeners
            .add(listener);
    }

    private ClientCall<ConsensusTopicQuery, ConsensusTopicResponse> startStreamingCall(ConsensusTopicId topic) {
        final ClientCall<ConsensusTopicQuery, ConsensusTopicResponse> call =
            channel.newCall(ConsensusServiceGrpc.getSubscribeTopicMethod(), CallOptions.DEFAULT);

        final ConsensusTopicQuery topicQuery = ConsensusTopicQuery.newBuilder()
            .setTopicID(topic.toProto())
            .build();

        ClientCalls.asyncServerStreamingCall(call, topicQuery, new StreamObserver<ConsensusTopicResponse>() {
            @Override
            public void onNext(ConsensusTopicResponse value) {
                subscriptions.get(topic)
                    .listeners
                    .forEach(listener -> listener.onMessage(new ConsensusMessage(topic, value)));
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });

        return call;
    }

    public Iterator<ConsensusMessage> messagesForPeriod(ConsensusTopicId topic, Instant startTime, Instant endTime) {
        final ConsensusTopicQuery topicQuery = ConsensusTopicQuery.newBuilder()
            .setTopicID(topic.toProto())
            .setConsensusStartTime(TimestampHelper.timestampFrom(startTime))
            .setConsensusEndTime(TimestampHelper.timestampFrom(endTime))
            .build();

        final Iterator<ConsensusTopicResponse> iter = ClientCalls.blockingServerStreamingCall(
            channel,
            ConsensusServiceGrpc.getSubscribeTopicMethod(),
            CallOptions.DEFAULT,
            topicQuery);

        return Iterators.transform(iter, message -> new ConsensusMessage(topic, Objects.requireNonNull(message)));
    }

    public void unsubscribe(ConsensusTopicId topicId, TopicListener listener) {
        subscriptions.computeIfPresent(topicId, (_topic, sub) -> {
            sub.listeners.remove(listener);

            if (sub.listeners.isEmpty()) {
                sub.call.cancel("no more listeners for topic", null);
                return null;
            }

            return sub;
        });
    }

    private void unsubscribeAll() {
        final Iterator<Subscription> iterator = subscriptions.values().iterator();

        while (iterator.hasNext()) {
            final Subscription sub = iterator.next();

            sub.call.cancel("ConsensusClient.unsubscribeAll() called", null);
            iterator.remove();
        }
    }

    @Override
    public void close() throws InterruptedException {
        unsubscribeAll();
        channel.shutdown();
        channel.awaitTermination(5, TimeUnit.SECONDS);
    }

    public boolean close(long timeout, TimeUnit timeoutUnit) throws InterruptedException {
        unsubscribeAll();
        channel.shutdown();
        return channel.awaitTermination(timeout, timeoutUnit);
    }

    private static final class Subscription {
        final ArrayList<TopicListener> listeners = new ArrayList<>();
        final ClientCall<ConsensusTopicQuery, ConsensusTopicResponse> call;

        private Subscription(ClientCall<ConsensusTopicQuery, ConsensusTopicResponse> call) {
            this.call = call;
        }
    }
}
