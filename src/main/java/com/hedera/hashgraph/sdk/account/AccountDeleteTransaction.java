package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.proto.CryptoDeleteTransactionBody;
import com.hedera.hashgraph.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionBuilder;

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

    /**
     * Set the ID of the account to delete.
     *
     * Note that this <b>MUST</b> be the same as the account ID in the transaction ID
     * or else getting the receipt will throw with {@code RECEIPT_NOT_FOUND}.
     *
     * You can ensure this is correct by calling
     * {@code setTransactionId(new TransactionId(deleteAccountId))}, however this does mean
     * that the account being deleted will also pay the fee for this transaction, which will be
     * deducted from the balance that will be transferred to the account set by
     * {@link #setTransferAccountId(AccountId)}.
     *
     * @param deleteAccountId the ID of the account to delete.
     * @return {@code this} for fluent usage.
     */
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
