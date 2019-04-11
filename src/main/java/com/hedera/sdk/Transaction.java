package com.hedera.sdk;

import com.google.protobuf.ByteString;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.sdk.crypto.ed25519.Ed25519Signature;
import com.hedera.sdk.proto.ResponseCodeEnum;
import com.hedera.sdk.proto.Signature;
import com.hedera.sdk.proto.SignatureList;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.*;

import java.util.Objects;
import javax.annotation.Nullable;

public final class Transaction extends TransactionCall {
    private final io.grpc.MethodDescriptor<com.hedera.sdk.proto.Transaction, com.hedera.sdk.proto.TransactionResponse> methodDescriptor;

    @Nullable
    private final Channel rpcChannel;

    @Nullable
    private byte[] bodyBytes;

    Transaction(
        @Nullable Channel channel,
        com.hedera.sdk.proto.Transaction.Builder inner,
        MethodDescriptor<com.hedera.sdk.proto.Transaction, TransactionResponse> methodDescriptor
    ) {
        super(inner);
        this.rpcChannel = channel;
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

    public final void validate() {
        require(inner.hasSigs() && inner.getSigsBuilder().getSigsCount() != 0, "Transaction must be signed");
        require(inner.getBodyBuilder().hasTransactionID(), "Transaction needs an ID");
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
        Objects.requireNonNull(rpcChannel, "Transaction.rpcChannel must be non-null in regular use");
        return rpcChannel;
    }
}
