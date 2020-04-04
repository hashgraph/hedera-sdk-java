package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;
import java8.util.concurrent.CompletableFuture;

import static com.hedera.hashgraph.sdk.FutureConverter.toCompletableFuture;

public final class Transaction {
    // A SDK [Transaction] is composed of multiple, raw protobuf transactions. These should be functionally identical,
    // with the exception of pointing to different nodes. When retrying a transaction after a network error or
    // retry-able status response, we try a different transaction and thus a different node.
    private final com.hedera.hashgraph.sdk.proto.Transaction[] raw;

    Transaction(com.hedera.hashgraph.sdk.proto.Transaction[] raw) {
        this.raw = raw;
    }

    // TODO: Return <TransactionId>
    public TransactionResponse execute() {
        return executeAsync().join();
    }

    // TODO: Return <TransactionId>
    public CompletableFuture<TransactionResponse> executeAsync() {
        // TODO: Move to <Client>
        ManagedChannel chan = ManagedChannelBuilder.forTarget("0.testnet.hedera.com:50211")
            .usePlaintext()
            // TODO: Inject project version
            .userAgent("hedera-sdk-java/2.0.0-SNAPSHOT")
            .build();

        MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> method = CryptoServiceGrpc.getCreateAccountMethod();
        ClientCall<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> call = chan.newCall(method, CallOptions.DEFAULT);

        return toCompletableFuture(ClientCalls.futureUnaryCall(call, raw[0]));
    }
}
