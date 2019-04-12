package com.hedera.sdk;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.sdk.crypto.ed25519.Ed25519Signature;
import com.hedera.sdk.proto.Signature;
import com.hedera.sdk.proto.SignatureList;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.*;

import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public final class Transaction extends HederaCall<com.hedera.sdk.proto.Transaction, TransactionResponse, TransactionId> {
    private final io.grpc.MethodDescriptor<com.hedera.sdk.proto.Transaction, com.hedera.sdk.proto.TransactionResponse> methodDescriptor;
    private final com.hedera.sdk.proto.Transaction.Builder inner;

    @Nullable
    private final ChannelHolder channel;

    @Nullable
    private byte[] bodyBytes;

    Transaction(
        @Nullable ChannelHolder channel,
        com.hedera.sdk.proto.Transaction.Builder inner,
        MethodDescriptor<com.hedera.sdk.proto.Transaction, TransactionResponse> methodDescriptor
    ) {
        super();
        this.inner = inner;
        this.channel = channel;
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

    @Override
    public final void validate() {
        require(
            inner.hasSigs() && inner.getSigsBuilder()
                .getSigsCount() != 0,
            "Transaction must be signed"
        );
        require(
            inner.getBodyBuilder()
                .hasTransactionID(),
            "Transaction needs an ID"
        );
    }

    @Override
    public com.hedera.sdk.proto.Transaction toProto() {
        validate();
        return inner.build();
    }

    private byte[] getBodyBytes() {
        if (bodyBytes == null) {
            bodyBytes = inner.getBody()
                .toByteArray();
        }

        return bodyBytes;
    }

    @Override
    protected MethodDescriptor<com.hedera.sdk.proto.Transaction, TransactionResponse> getMethod() {
        return methodDescriptor;
    }

    @Override
    protected Channel getChannel() {
        Objects.requireNonNull(channel, "Transaction.rpcChannel must be non-null in regular use");
        return channel.getChannel();
    }

    /** Execute the transaction and immediately get its receipt which may not be available yet. */
    public final TransactionReceipt executeForReceipt() throws HederaException {
        var txId = execute();
        return new TransactionReceiptQuery(channel).setTransaction(txId)
            .execute();
    }

    public void executeForReceiptAsync(Consumer<TransactionReceipt> onSuccess, Consumer<Throwable> onFailure) {
        executeAsync(
            txId -> new TransactionReceiptQuery(channel).setTransaction(txId)
                .executeAsync(onSuccess, onFailure),
            onFailure
        );
    }

    public ListenableFuture<TransactionReceipt> executeForReceiptFuture() {
        return Futures.transformAsync(executeFuture(), txId -> {
            Objects.requireNonNull(txId, "null transaction ID");
            return new TransactionReceiptQuery(channel).setTransaction(txId)
                .executeFuture();
        }, MoreExecutors.directExecutor());

    }

    public TransactionRecord executeForRecord() throws HederaException {
        var txId = execute();
        return new TransactionFastRecordQuery(channel).setTransaction(txId)
            .execute();
    }

    public void executeForRecordAsync(Consumer<TransactionRecord> onSuccess, Consumer<Throwable> onFailure) {
        executeAsync(
            txId -> new TransactionFastRecordQuery(channel).setTransaction(txId)
                .executeAsync(onSuccess, onFailure),
            onFailure
        );
    }

    public ListenableFuture<TransactionRecord> executeForRecordFuture() {
        return Futures.transformAsync(executeFuture(), txId -> {
            Objects.requireNonNull(txId, "null tranasction ID");
            return new TransactionFastRecordQuery(channel).setTransaction(txId)
                .executeFuture();
        }, MoreExecutors.directExecutor());
    }

    @Override
    protected TransactionId mapResponse(TransactionResponse response) throws HederaException {
        HederaException.throwIfExceptional(response.getNodeTransactionPrecheckCode());
        return new TransactionId(
                inner.getBody()
                    .getTransactionIDOrBuilder()
        );
    }
}
