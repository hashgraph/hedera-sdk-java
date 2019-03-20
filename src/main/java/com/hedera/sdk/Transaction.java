package com.hedera.sdk;

import com.google.protobuf.ByteString;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.sdk.proto.Signature;
import com.hedera.sdk.proto.SignatureList;
import javax.annotation.Nullable;

public class Transaction {
    private final com.hedera.sdk.proto.Transaction.Builder inner;

    @Nullable private byte[] bodyBytes;

    Transaction(com.hedera.sdk.proto.Transaction.Builder inner) {
        this.inner = inner;
    }

    public Transaction sign(Ed25519PrivateKey privateKey) {
        var signature = privateKey.sign(getBodyBytes());

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
                                                                .setEd25519(
                                                                        ByteString.copyFrom(
                                                                                signature)))));

        return this;
    }

    public final com.hedera.sdk.proto.Transaction build() {
        return inner.build();
    }

    private byte[] getBodyBytes() {
        if (bodyBytes == null) {
            bodyBytes = inner.getBody().toByteArray();
        }

        return bodyBytes;
    }
}
