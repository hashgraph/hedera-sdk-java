package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoTransferTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

public final class CryptoTransferTransaction extends TransactionBuilder<CryptoTransferTransaction> {
    private final CryptoTransferTransactionBody.Builder builder;

    public CryptoTransferTransaction() {
        builder = CryptoTransferTransactionBody.newBuilder();
    }

    @Override
    protected void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoTransfer(builder);
    }
}
