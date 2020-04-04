package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TransactionBody;

public abstract class TransactionBuilder {
    // Maximum number of characters that all memo fields share
    protected final static int MAX_MEMO_LENGTH = 100;

    protected final TransactionBody.Builder bodyBuilder;

    private final com.hedera.hashgraph.sdk.proto.Transaction.Builder builder;

    public TransactionBuilder() {
        builder = com.hedera.hashgraph.sdk.proto.Transaction.newBuilder();
        bodyBuilder = TransactionBody.newBuilder();
    }

    public final Transaction build() {
        // Emplace the body into the transaction wrapper
        // This wrapper object contains the bytes for the body and signatures of the body
        builder.setBodyBytes(bodyBuilder.build().toByteString());

        return new Transaction(new com.hedera.hashgraph.sdk.proto.Transaction[]{builder.build()});
    }
}
