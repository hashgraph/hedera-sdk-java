package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.mirror.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.mirror.MirrorNetworkService;
import com.hedera.hashgraph.sdk.proto.mirror.NetworkServiceGrpc;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.Deadline;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import java8.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;
import org.w3c.dom.Node;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class AddressBookQuery extends MirrorQuery<AddressBookQuery, com.hedera.hashgraph.sdk.proto.mirror.AddressBookQuery, com.hedera.hashgraph.sdk.proto.NodeAddress, NodeAddress, NodeAddressBook> {
    @Nullable
    private FileId fileId = null;

    @Nullable
    private Integer limit = null;

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

    @Override
    protected MethodDescriptor<com.hedera.hashgraph.sdk.proto.mirror.AddressBookQuery, com.hedera.hashgraph.sdk.proto.NodeAddress> getMethodDescriptor() {
        return NetworkServiceGrpc.getGetNodesMethod();
    }

    protected com.hedera.hashgraph.sdk.proto.mirror.AddressBookQuery makeRequest() {
        var builder = com.hedera.hashgraph.sdk.proto.mirror.AddressBookQuery.newBuilder();
        if (fileId != null) {
            builder.setFileId(fileId.toProtobuf());
        }
        if (limit != null) {
            builder.setLimit(limit);
        }
        return builder.build();
    }

    @Nullable
    @Override
    protected NodeAddress mapResponse(com.hedera.hashgraph.sdk.proto.NodeAddress protoResponse) {
        return NodeAddress.fromProtobuf(protoResponse);
    }

    protected void logError(Throwable error) {
        var delay = Math.min(500 * (long) Math.pow(2, attempt.get()), maxBackoff.toMillis());
        LOGGER.warn("Error fetching address book at FileId {} during attempt #{}. Waiting {} ms before next attempt: {}",
            fileId, attempt, delay, error.getMessage());
    }

    @Override
    protected NodeAddressBook mapExecuteResponse() {
        return new NodeAddressBook().setNodeAddresses(responses);
    }
}
