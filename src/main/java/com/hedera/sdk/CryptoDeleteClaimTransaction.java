package com.hedera.sdk;

import com.google.protobuf.ByteString;
import com.hedera.sdk.proto.CryptoDeleteClaimTransactionBody;
import com.hedera.sdk.proto.CryptoServiceGrpc;
import com.hedera.sdk.proto.Transaction;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

public class CryptoDeleteClaimTransaction extends TransactionBuilder<CryptoDeleteClaimTransaction> {
    private final CryptoDeleteClaimTransactionBody.Builder builder;

    public CryptoDeleteClaimTransaction() {
        builder = inner.getBodyBuilder().getCryptoDeleteClaimBuilder();
    }

    public CryptoDeleteClaimTransaction setAccountToDeleteFrom(AccountId accountId) {
        builder.setAccountIDToDeleteFrom(accountId.inner);
        return this;
    }

    public CryptoDeleteClaimTransaction setHashToDelete(byte[] hashToDelete) {
        builder.setHashToDelete(ByteString.copyFrom(hashToDelete));
        return this;
    }

    @Override
    MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return CryptoServiceGrpc.getDeleteClaimMethod();
    }
}
