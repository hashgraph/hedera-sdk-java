package com.hedera.sdk;

import com.google.common.util.concurrent.Futures;
import com.google.protobuf.ByteString;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.sdk.crypto.ed25519.Ed25519Signature;
import com.hedera.sdk.proto.ResponseCodeEnum;
import com.hedera.sdk.proto.Signature;
import com.hedera.sdk.proto.SignatureList;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.Future;
import java.util.function.Function;
import javax.annotation.Nullable;

public class Transaction {
    private final com.hedera.sdk.proto.Transaction.Builder inner;
    private final io.grpc.MethodDescriptor<com.hedera.sdk.proto.Transaction, com.hedera.sdk.proto.TransactionResponse> methodDescriptor;
    private final Channel rpcChannel;

    @Nullable
    private byte[] bodyBytes;

    Transaction(
        Channel rpcChannel,
        com.hedera.sdk.proto.Transaction.Builder inner,
        MethodDescriptor<com.hedera.sdk.proto.Transaction, TransactionResponse> methodDescriptor
    ) {
        this.rpcChannel = rpcChannel;
        this.inner = inner;
        this.methodDescriptor = methodDescriptor;
    }

    public Transaction sign(Ed25519PrivateKey privateKey) {
        var signature = Ed25519Signature.forMessage(privateKey, getBodyBytes())
            .toBytes();

        // FIXME: This nested signature is only for account IDs < 1000
        // FIXME: spotless makes this.. lovely
        // FIXME: Is `ByteString.copyFrom` ideal here?
        inner.getSigsBuilder()
            .addSigs(
                Signature.newBuilder()
                    .setSignatureList(
                        SignatureList.newBuilder()
                            .addSigs(
                                Signature.newBuilder()
                                    .setEd25519(ByteString.copyFrom(signature))
                            )
                    )
            );

        return this;
    }

    public final com.hedera.sdk.proto.Transaction build() {
        if (inner.getSigsBuilder()
            .getSigsCount() == 0) {
            throw new IllegalStateException("Transaction is not signed");
        }

        if (!inner.getBodyBuilder()
            .hasTransactionID()) {
            throw new IllegalStateException("Transaction needs an ID");
        }

        return inner.build();
    }

    private byte[] getBodyBytes() {
        if (bodyBytes == null) {
            bodyBytes = inner.getBody()
                .toByteArray();
        }

        return bodyBytes;
    }

    private ClientCall<com.hedera.sdk.proto.Transaction, TransactionResponse> newClientCall() {
        return client.getChannel()
            .newCall(methodDescriptor, CallOptions.DEFAULT);
    }

    public final ResponseCodeEnum execute(Client client) {
        return ClientCalls.blockingUnaryCall(newClientCall(client), build())
            .getNodeTransactionPrecheckCode();
    }

    public final void executeAsync(Client client, Function<ResponseCodeEnum, Void> onResponse, Function<Throwable, Void> onError) {
        ClientCalls.asyncUnaryCall(newClientCall(client), build(), new ResponseObserver(onResponse, onError));
    }

    public Future<ResponseCodeEnum> executeFuture(Client client) {
        return Futures.lazyTransform(
            ClientCalls.futureUnaryCall(newClientCall(client), build()),
            TransactionResponse::getNodeTransactionPrecheckCode
        );
    }

    // inner class because anonymous classes can't reassign their captures so `callbackExecuted`
    // must be a field instead
    private static class ResponseObserver implements StreamObserver<TransactionResponse> {
        private final Function<ResponseCodeEnum, Void> onResponse;
        private final Function<Throwable, Void> onError;
        private boolean callbackExecuted = false;

        private ResponseObserver(Function<ResponseCodeEnum, Void> onResponse, Function<Throwable, Void> onError) {
            this.onResponse = onResponse;
            this.onError = onError;
        }

        @Override
        public void onNext(TransactionResponse response) {
            if (!callbackExecuted) {
                callbackExecuted = true;
                onResponse.apply(response.getNodeTransactionPrecheckCode());
            }
        }

        @Override
        public void onError(Throwable throwable) {
            if (!callbackExecuted) {
                callbackExecuted = true;
                onError.apply(throwable);
            }
        }

        @Override
        public void onCompleted() {}
    }
}
