package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.mirror.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.mirror.MirrorNetworkService;
import com.hedera.hashgraph.sdk.proto.mirror.NetworkServiceGrpc;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import java8.util.concurrent.CompletableFuture;
import org.threeten.bp.Duration;
import org.w3c.dom.Node;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class AddressBookQuery {
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

    public NodeAddressBook execute(Client client) {
        return execute(client, client.getRequestTimeout());
    }

    public NodeAddressBook execute(Client client, Duration timeout) {
        var addressProtoIter = ClientCalls.blockingServerStreamingCall(
            buildCall(client, timeout),
            buildQuery()
        );
        List<NodeAddress> addresses = new ArrayList<>();
        while (addressProtoIter.hasNext()) {
            addresses.add(NodeAddress.fromProtobuf(addressProtoIter.next()));
        }
        return new NodeAddressBook().setNodeAddresses(addresses);
    }

    public CompletableFuture<NodeAddressBook> executeAsync(Client client) {
        return executeAsync(client, client.getRequestTimeout());
    }

    public CompletableFuture<NodeAddressBook> executeAsync(Client client, Duration timeout) {
        CompletableFuture<NodeAddressBook> returnFuture = new CompletableFuture<>();
        List<NodeAddress> addresses = new ArrayList<>();
        ClientCalls.asyncServerStreamingCall(buildCall(client, timeout), buildQuery(), new StreamObserver<com.hedera.hashgraph.sdk.proto.NodeAddress>() {
            @Override
            public void onNext(com.hedera.hashgraph.sdk.proto.NodeAddress addressProto) {
                addresses.add(NodeAddress.fromProtobuf(addressProto));
            }

            @Override
            public void onError(Throwable error) {
                returnFuture.completeExceptionally(error);
            }

            @Override
            public void onCompleted() {
                returnFuture.complete(new NodeAddressBook().setNodeAddresses(addresses));
            }
        });
        return returnFuture;
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
    buildCall(Client client, Duration timeout) {
        try {
            return client.mirrorNetwork.getNextMirrorNode().getChannel().newCall(
                NetworkServiceGrpc.getGetNodesMethod(),
                CallOptions.DEFAULT.withDeadlineAfter(timeout.toMillis(), TimeUnit.MILLISECONDS)
            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
