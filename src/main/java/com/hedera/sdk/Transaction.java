package com.hedera.sdk;

import com.google.protobuf.ByteString;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.sdk.crypto.ed25519.Ed25519Signature;
import com.hedera.sdk.proto.*;
import io.grpc.*;

import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;

public final class Transaction extends HederaCall<com.hedera.sdk.proto.Transaction, TransactionResponse, TransactionId> {
    private final int MAX_ATTEMPTS = 10;

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

    // FIXME: This doesn't actually do anything.
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

    @Override
    protected TransactionId mapResponse(TransactionResponse response) throws HederaException {
        HederaException.throwIfExceptional(response.getNodeTransactionPrecheckCode());
        return new TransactionId(
                inner.getBody()
                    .getTransactionIDOrBuilder()
        );
    }

    private <T> T executeAndWaitFor(CheckedFunction<TransactionId, T, HederaException> execute, Function<T, TransactionReceipt> mapReceipt)
            throws HederaException {
        var id = execute();
        T response = null;
        ResponseCodeEnum receiptStatus = ResponseCodeEnum.UNRECOGNIZED;

        for (int attempt = 1; attempt < MAX_ATTEMPTS; attempt++) {
            response = execute.apply(id);

            receiptStatus = mapReceipt.apply(response)
                .getStatus();

            if (receiptStatus == ResponseCodeEnum.UNKNOWN) {
                try {
                    Thread.sleep(500 * attempt);
                    continue;
                } catch (InterruptedException e) {
                    break;
                }
            }

            HederaException.throwIfExceptional(receiptStatus);

            break;
        }

        return response;
    }

    public final TransactionReceipt executeForReceipt() throws HederaException {
        return executeAndWaitFor(
            id -> new TransactionReceiptQuery(channel).setTransactionId(id)
                .execute(),
            res -> res
        );
    }
}
