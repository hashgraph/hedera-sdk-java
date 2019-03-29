package com.hedera.sdk.account;

import com.google.protobuf.ByteString;
import com.hedera.sdk.AccountId;
import com.hedera.sdk.TransactionBuilder;
import com.hedera.sdk.proto.CryptoDeleteClaimTransactionBody;
import com.hedera.sdk.proto.CryptoServiceGrpc;
import com.hedera.sdk.proto.Transaction;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

// `CryptoDeleteClaimTransaction`
public class AccountDeleteClaimTransaction
        extends TransactionBuilder<AccountDeleteClaimTransaction> {
    private final CryptoDeleteClaimTransactionBody.Builder builder;

    public AccountDeleteClaimTransaction() {
        builder = inner.getBodyBuilder().getCryptoDeleteClaimBuilder();
    }

    public AccountDeleteClaimTransaction setAccountToDeleteFrom(AccountId accountId) {
        builder.setAccountIDToDeleteFrom(accountId.toProto());
        return this;
    }

    public AccountDeleteClaimTransaction setHashToDelete(byte[] hashToDelete) {
        builder.setHashToDelete(ByteString.copyFrom(hashToDelete));
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.getAccountIDToDeleteFromOrBuilder(), ".setAccountToDeleteFrom() required");
        require(builder.getHashToDelete(), ".setHashToDelete() required");
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return CryptoServiceGrpc.getDeleteClaimMethod();
    }
}
