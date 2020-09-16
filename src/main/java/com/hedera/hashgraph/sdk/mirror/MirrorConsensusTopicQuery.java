package com.hedera.hashgraph.sdk.mirror;

import com.hedera.hashgraph.proto.TransactionID;
import com.hedera.hashgraph.proto.mirror.ConsensusServiceGrpc;
import com.hedera.hashgraph.proto.mirror.ConsensusTopicQuery;
import com.hedera.hashgraph.proto.mirror.ConsensusTopicResponse;
import io.grpc.StatusException;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import io.grpc.Status;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class MirrorConsensusTopicQuery {
    private ConsensusTopicQuery.Builder builder;

    public MirrorConsensusTopicQuery() {
        builder = ConsensusTopicQuery.newBuilder();
    }

    public MirrorConsensusTopicQuery setTopicId(ConsensusTopicId topicId) {
        builder.setTopicID(topicId.toProto());
        return this;
    }

    public MirrorConsensusTopicQuery setStartTime(Instant startTime) {
        builder.setConsensusStartTime(TimestampHelper.timestampFrom(startTime));
        return this;
    }

    public MirrorConsensusTopicQuery setEndTime(Instant endTime) {
        builder.setConsensusEndTime(TimestampHelper.timestampFrom(endTime));
        return this;
    }

    public MirrorConsensusTopicQuery setLimit(long limit) {
        builder.setLimit(limit);
        return this;
    }

    public MirrorSubscriptionHandle subscribe(MirrorClient mirrorClient, Consumer<MirrorConsensusTopicResponse> onNext,
                                              Consumer<Throwable> onError)
    {
        final ClientCall<ConsensusTopicQuery, ConsensusTopicResponse> call = mirrorClient.channel
            .newCall(ConsensusServiceGrpc.getSubscribeTopicMethod(), CallOptions.DEFAULT);

        final MirrorSubscriptionHandle subscriptionHandle = new MirrorSubscriptionHandle(() -> {
            call.cancel("unsubscribed", null);
        });

        makeStreamingCall(call, builder.build(), onNext, onError, 0);

        return subscriptionHandle;
    }

    private static void makeStreamingCall(ClientCall<ConsensusTopicQuery, ConsensusTopicResponse> call,
                                          ConsensusTopicQuery query, Consumer<MirrorConsensusTopicResponse> onNext, Consumer<Throwable> onError,
                                          int attempt)
    {
        if (attempt > 10) {
            onError.accept(new Error("Failed to connect to mirror node"));
        }

        // From our testing `ClientCalls.asyncServerStreamingCall` does *not* call `onNext` before the previous call
        // to `onNext` finishes. Meaning, there is no use in having `ConcurrentHashmap` or `CopyOnWriteArrayList` since
        // the callback is never run concurrently.
        HashMap<TransactionID, Tuple<Instant, ArrayList<ConsensusTopicResponse>>> pendingMessages = new HashMap<>();
        Instant[] lastInstantChecked = new Instant[]{null};

        ClientCalls.asyncServerStreamingCall(call, query, new StreamObserver<ConsensusTopicResponse>() {
            private boolean shouldRetry = true;

            @Override
            public void onNext(ConsensusTopicResponse consensusTopicResponse) {
                shouldRetry = false;

                if (!consensusTopicResponse.hasChunkInfo()) {
                    // short circuit for no chunks
                    onNext.accept(MirrorConsensusTopicResponse.ofSingle(consensusTopicResponse));
                    return;
                }

                // get the list of chunks for this pending message
                TransactionID initialTransactionID = consensusTopicResponse.getChunkInfo().getInitialTransactionID();
                pendingMessages.putIfAbsent(initialTransactionID, new Tuple<>(Instant.now(), new ArrayList<>()));
                ArrayList<ConsensusTopicResponse> chunks = pendingMessages.get(initialTransactionID).second;

                // not possible to be null as we do [putIfAbsent]
                // add our response to the pending chunk list
                Objects.requireNonNull(chunks).add(consensusTopicResponse);

                // if we now have enough chunks, emit
                if (chunks.size() == consensusTopicResponse.getChunkInfo().getTotal()) {
                    // Remove message from pending
                    pendingMessages.remove(initialTransactionID);

                    // Send message to callback
                    onNext.accept(MirrorConsensusTopicResponse.ofMany(chunks));
                }

                // Remove transaction which have not been created for 5 minutes.
                ArrayList<TransactionID> toRemoveTransactions = new ArrayList<>();
                Instant now = Instant.now();

                if (lastInstantChecked[0] == null || Duration.between(lastInstantChecked[0], now).compareTo(Duration.ofSeconds(10)) > 0) {
                    lastInstantChecked[0] = now;

                    for (Map.Entry<TransactionID, Tuple<Instant, ArrayList<ConsensusTopicResponse>>> entry: pendingMessages.entrySet()) {
                        if (Duration.between(entry.getValue().first, now).compareTo(Duration.ofMinutes(5)) > 0) {
                            toRemoveTransactions.add(entry.getKey());
                        }
                    }
                }

                for (TransactionID id : toRemoveTransactions) {
                    pendingMessages.remove(id);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                if (shouldRetry) {
                    if (throwable instanceof StatusException) {
                        StatusException status = (StatusException) throwable;

                        if (status.getStatus().equals(Status.NOT_FOUND) || status.getStatus().equals(Status.UNAVAILABLE)) {

                            // Cannot use `CompletableFuture<U>` here since this future is never polled
                            try {
                                long delay = Math.min(250 * (long) Math.pow(2, attempt), 16000);
                                Thread.sleep(delay);
                            } catch (InterruptedException e) {
                                // Do nothing
                            }

                            makeStreamingCall(call, query, onNext, onError, attempt + 1);
                        }
                    }
                }

                onError.accept(throwable);
            }

            @Override
            public void onCompleted() {
                // Do nothing
            }
        });
    }

    static class Tuple<T1, T2> {
        T1 first;
        T2 second;

        Tuple(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }
    }
}
