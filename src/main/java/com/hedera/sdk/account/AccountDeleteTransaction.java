package com.hedera.sdk.account;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.Client;
import com.hedera.sdk.TransactionBuilder;
import com.hedera.sdk.proto.CryptoDeleteTransactionBody;
import com.hedera.sdk.proto.CryptoServiceGrpc;
import com.hedera.sdk.proto.Transaction;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

// `CryptoDeleteTransaction`
public class AccountDeleteTransaction extends TransactionBuilder<AccountDeleteTransaction> {
    private final CryptoDeleteTransactionBody.Builder builder = inner.getBodyBuilder()
        .getCryptoDeleteBuilder();

    public AccountDeleteTransaction(Client client) {
        super(client);
    }

    AccountDeleteTransaction() {
        super(null);
    }

    public AccountDeleteTransaction setTransferAccountId(AccountId transferAccountId) {
        builder.setTransferAccountID(transferAccountId.toProto());
        return this;
    }

    public AccountDeleteTransaction setDeleteAccountId(AccountId deleteAccountId) {
        builder.setDeleteAccountID(deleteAccountId.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasTransferAccountID(), ".setTransferAccountId() required");
        require(builder.hasDeleteAccountID(), ".setDeleteAccountId() required");
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return CryptoServiceGrpc.getCryptoDeleteMethod();
    }
}
