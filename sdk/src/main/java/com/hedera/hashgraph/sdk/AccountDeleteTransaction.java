package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Marks an account as deleted, moving all its current hbars to another account.
 * <p>
 * It will remain in the ledger, marked as deleted, until it expires.
 * Transfers into it a deleted account fail. But a deleted account can still have its
 * expiration extended in the normal way.
 */
public final class AccountDeleteTransaction extends Transaction<AccountDeleteTransaction> {
    private final CryptoDeleteTransactionBody.Builder builder;

    public AccountDeleteTransaction() {
        builder = CryptoDeleteTransactionBody.newBuilder();
    }

    AccountDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getCryptoDelete().toBuilder();
    }

    AccountDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) throws InvalidProtocolBufferException {
        super(txBody);

        builder = bodyBuilder.getCryptoDelete().toBuilder();
    }

    @Nullable
    public AccountId getAccountId() {
        return builder.hasDeleteAccountID() ? AccountId.fromProtobuf(builder.getDeleteAccountID()) : null;
    }

    /**
     * Sets the account ID which should be deleted.
     *
     * @param deleteAccountId The AccountId to be set
     * @return {@code this}
     */
    public AccountDeleteTransaction setAccountId(AccountId deleteAccountId) {
        requireNotFrozen();
        builder.setDeleteAccountID(deleteAccountId.toProtobuf());
        return this;
    }

    @Nullable
    public AccountId getTransferAccountId() {
        return builder.hasTransferAccountID() ? AccountId.fromProtobuf(builder.getTransferAccountID()) : null;
    }

    /**
     * Sets the account ID which will receive all remaining hbars.
     *
     * @param transferAccountId The AccountId to be set
     * @return {@code this}
     */
    public AccountDeleteTransaction setTransferAccountId(AccountId transferAccountId) {
        requireNotFrozen();
        builder.setTransferAccountID(transferAccountId.toProtobuf());
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getCryptoDeleteMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoDelete(builder);
        return true;
    }
}
