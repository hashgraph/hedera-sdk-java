package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.SignatureMap;
import com.hedera.hashgraph.sdk.proto.SignaturePair;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.CallOptions;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.ClientCalls;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.BiConsumer;
import java8.util.function.Function;

import java.util.ArrayList;
import java.util.List;

import static com.hedera.hashgraph.sdk.FutureConverter.toCompletableFuture;

// TODO: Handle multiple raw transactions

public final class Transaction {
    // A SDK [Transaction] is composed of multiple, raw protobuf transactions. These should be functionally identical,
    // with the exception of pointing to different nodes. When retrying a transaction after a network error or
    // retry-able status response, we try a different transaction and thus a different node.
    private final com.hedera.hashgraph.sdk.proto.Transaction.Builder[] raw;
    private final List<SignatureMap.Builder> signatureMaps;

    Transaction(com.hedera.hashgraph.sdk.proto.Transaction.Builder[] raw) {
        signatureMaps = new ArrayList<>(raw.length);

        for (int i = 0; i < raw.length; i++) {
            signatureMaps.add(SignatureMap.newBuilder());
        }

        this.raw = raw;
    }

    public Transaction sign(PrivateKey privateKey) {
        return signWith(privateKey.getPublicKey(), privateKey::sign);
    }

    public Transaction signWith(PublicKey publicKey, Function<byte[], byte[]> transactionSigner) {
        var signatureBytes = transactionSigner.apply(raw[0].getBodyBytes().toByteArray());

        SignaturePair signature = publicKey.toSignaturePairProtobuf(signatureBytes);

        signatureMaps.get(0).addSigPair(signature);

        return this;
    }

    // TODO: Return <TransactionId>
    public TransactionResponse execute() {
        return executeAsync().join();
    }

    // TODO: Return <TransactionId>
    public CompletableFuture<TransactionResponse> executeAsync() {
        // TODO: Move to <Client>
        var chan = ManagedChannelBuilder.forTarget("0.testnet.hedera.com:50211")
            .usePlaintext()
            // TODO: Inject project version
            .userAgent("hedera-sdk-java/2.0.0-SNAPSHOT")
            .build();

        var method = CryptoServiceGrpc.getCreateAccountMethod();
        var call = chan.newCall(method, CallOptions.DEFAULT);

        return toCompletableFuture(ClientCalls.futureUnaryCall(call, toProtobuf()));
    }

    // TODO: Return <TransactionId>
    @SuppressWarnings("FutureReturnValueIgnored")
    public void executeAsync(BiConsumer<TransactionResponse, Throwable> callback) {
        executeAsync().whenComplete(callback);
    }

    private com.hedera.hashgraph.sdk.proto.Transaction toProtobuf() {
        raw[0].setSigMap(signatureMaps.get(0));
        return raw[0].build();
    }
}
