package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.proto.CryptoDeleteTransactionBody;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.proto.CryptoServiceGrpc;

import javax.annotation.Nullable;

import io.grpc.MethodDescriptor;

// `CryptoDeleteTransaction`
public class AccountDeleteTransaction extends TransactionBuilder<AccountDeleteTransaction> {
    private final CryptoDeleteTransactionBody.Builder builder = bodyBuilder.getCryptoDeleteBuilder();

    /**
     * @deprecated use the no-arg constructor and pass the client to {@link #build(Client)} instead.
     */
    @Deprecated
    public AccountDeleteTransaction(@Nullable Client client) {
        super(client);
    }

    public AccountDeleteTransaction() {
        super();
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
