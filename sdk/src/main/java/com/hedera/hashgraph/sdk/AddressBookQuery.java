package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.mirror.NetworkServiceGrpc;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.Deadline;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;

public class AddressBookQuery {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddressBookQuery.class);

    @Nullable
    private FileId fileId = null;
    @Nullable
    private Integer limit = null;
    private int maxAttempts = 10;
    private Duration maxBackoff = Duration.ofSeconds(8L);

    public AddressBookQuery() {
    }

    public AddressBookQuery setFileId(FileId fileId) {
        this.fileId = fileId;
        return this;
    }

    @Nullable
    public FileId getFileId() {
        return fileId;
    }

    public AddressBookQuery setLimit(@Nullable @Nonnegative Integer limit) {
        this.limit = limit;
        return this;
    }

    @Nullable
    public Integer getLimit() {
        return limit;
    }

    public AddressBookQuery setMaxAttempts(@Nonnegative int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public AddressBookQuery setMaxBackoff(Duration maxBackoff) {
        Objects.requireNonNull(maxBackoff);
        if (maxBackoff.toMillis() < 500L) {
            throw new IllegalArgumentException("maxBackoff must be at least 500 ms");
        }
        this.maxBackoff = maxBackoff;
        return this;
    }

    public NodeAddressBook execute(Client client) {
        return execute(client, client.getRequestTimeout());
    }

    public NodeAddressBook execute(Client client, Duration timeout) {
        var deadline = Deadline.after(timeout.toMillis(), TimeUnit.MILLISECONDS);
        for (int attempt = 1; true; attempt++) {
            try {
                var addressProtoIter = ClientCalls.blockingServerStreamingCall(
                    buildCall(client, deadline),
                    buildQuery()
                );
                List<NodeAddress> addresses = new ArrayList<>();
                while (addressProtoIter.hasNext()) {
                    addresses.add(NodeAddress.fromProtobuf(addressProtoIter.next()));
                }
                return new NodeAddressBook().setNodeAddresses(addresses);
            } catch (Throwable error) {
                if (!shouldRetry(error) || attempt >= maxAttempts) {
                    LOGGER.error("Error attempting to get address book at FileId {}", fileId, error);
                    throw error;
                }
                warnAndDelay(attempt, error);
            }
        }
    }

    public CompletableFuture<NodeAddressBook> executeAsync(Client client) {
        return executeAsync(client, client.getRequestTimeout());
    }

    public CompletableFuture<NodeAddressBook> executeAsync(Client client, Duration timeout) {
        var deadline = Deadline.after(timeout.toMillis(), TimeUnit.MILLISECONDS);
        CompletableFuture<NodeAddressBook> returnFuture = new CompletableFuture<>();
        executeAsync(client, deadline, returnFuture, 1);
        return returnFuture;
    }

    void executeAsync(Client client, Deadline deadline, CompletableFuture<NodeAddressBook> returnFuture, int attempt) {
        List<NodeAddress> addresses = new ArrayList<>();
        ClientCalls.asyncServerStreamingCall(
            buildCall(client, deadline),
            buildQuery(),
            new StreamObserver<com.hedera.hashgraph.sdk.proto.NodeAddress>() {
                @Override
                public void onNext(com.hedera.hashgraph.sdk.proto.NodeAddress addressProto) {
                    addresses.add(NodeAddress.fromProtobuf(addressProto));
                }

                @Override
                public void onError(Throwable error) {
                    if (attempt >= maxAttempts || !shouldRetry(error)) {
                        LOGGER.error("Error attempting to get address book at FileId {}", fileId, error);
                        returnFuture.completeExceptionally(error);
                        return;
                    }
                    warnAndDelay(attempt, error);
                    executeAsync(client, deadline, returnFuture, attempt + 1);
                }

                @Override
                public void onCompleted() {
                    returnFuture.complete(new NodeAddressBook().setNodeAddresses(addresses));
                }
            });
    }

    com.hedera.hashgraph.sdk.proto.mirror.AddressBookQuery buildQuery() {
        var builder = com.hedera.hashgraph.sdk.proto.mirror.AddressBookQuery.newBuilder();
        if (fileId != null) {
            builder.setFileId(fileId.toProtobuf());
        }
        if (limit != null) {
            builder.setLimit(limit);
        }
        return builder.build();
    }

    private ClientCall<com.hedera.hashgraph.sdk.proto.mirror.AddressBookQuery, com.hedera.hashgraph.sdk.proto.NodeAddress>
    buildCall(Client client, Deadline deadline) {
        try {
            return client.mirrorNetwork.getNextMirrorNode().getChannel().newCall(
                NetworkServiceGrpc.getGetNodesMethod(),
                CallOptions.DEFAULT.withDeadline(deadline)
            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean shouldRetry(Throwable throwable) {
        if (throwable instanceof StatusRuntimeException) {
            var statusRuntimeException = (StatusRuntimeException) throwable;
            var code = statusRuntimeException.getStatus().getCode();
            var description = statusRuntimeException.getStatus().getDescription();

            return (code == io.grpc.Status.Code.UNAVAILABLE) ||
                (code == io.grpc.Status.Code.RESOURCE_EXHAUSTED) ||
                (code == Status.Code.INTERNAL && description != null && Executable.RST_STREAM.matcher(description).matches());
        }

        return false;
    }

    private void warnAndDelay(int attempt, Throwable error) {
        var delay = Math.min(500 * (long) Math.pow(2, attempt), maxBackoff.toMillis());
        LOGGER.warn("Error fetching address book at FileId {} during attempt #{}. Waiting {} ms before next attempt: {}",
            fileId, attempt, delay, error.getMessage());

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
