package com.hedera.sdk;

import com.hedera.sdk.proto.CryptoDeleteTransactionBody;
import com.hedera.sdk.proto.CryptoServiceGrpc;
import com.hedera.sdk.proto.Transaction;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

public class CryptoDeleteTransaction
        extends TransactionBuilder<com.hedera.sdk.CryptoDeleteTransaction> {
    private final CryptoDeleteTransactionBody.Builder builder;

    public CryptoDeleteTransaction() {
        builder = inner.getBodyBuilder().getCryptoDeleteBuilder();
    }

    public CryptoDeleteTransaction setTransferAccountId(AccountId transferAccountId) {
        builder.setTransferAccountID(transferAccountId.inner);
        return this;
    }

    public CryptoDeleteTransaction setDeleteAccountId(AccountId deleteAccountId) {
        builder.setDeleteAccountID(deleteAccountId.inner);
        return this;
    }

    @Override
    MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return CryptoServiceGrpc.getCryptoDeleteMethod();
    }
}
