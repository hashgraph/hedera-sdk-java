package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Marks an account as deleted, moving all its current hbars to another account.
 * <p>
 * It will remain in the ledger, marked as deleted, until it expires.
 * Transfers into it a deleted account fail. But a deleted account can still have its
 * expiration extended in the normal way.
 */
public final class AccountDeleteTransaction extends Transaction<AccountDeleteTransaction> {
    private final CryptoDeleteTransactionBody.Builder builder;

    AccountId accountId;
    AccountId transferAccountId;

    public AccountDeleteTransaction() {
        builder = CryptoDeleteTransactionBody.newBuilder();
    }

    AccountDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getCryptoDelete().toBuilder();

        if (builder.hasDeleteAccountID()) {
            accountId = AccountId.fromProtobuf(builder.getDeleteAccountID());
        }

        if (builder.hasTransferAccountID()) {
            transferAccountId = AccountId.fromProtobuf(builder.getTransferAccountID());
        }
    }

    AccountDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getCryptoDelete().toBuilder();

        if (builder.hasDeleteAccountID()) {
            accountId = AccountId.fromProtobuf(builder.getDeleteAccountID());
        }

        if (builder.hasTransferAccountID()) {
            transferAccountId = AccountId.fromProtobuf(builder.getTransferAccountID());
        }
    }

    @Nullable
    public AccountId getAccountId() {
        return accountId;
    }

    /**
     * Sets the account ID which should be deleted.
     *
     * @param deleteAccountId The AccountId to be set
     * @return {@code this}
     */
    public AccountDeleteTransaction setAccountId(AccountId deleteAccountId) {
        requireNotFrozen();
        Objects.requireNonNull(deleteAccountId);
        this.accountId = deleteAccountId;
        return this;
    }

    @Nullable
    public AccountId getTransferAccountId() {
        return transferAccountId;
    }

    /**
     * Sets the account ID which will receive all remaining hbars.
     *
     * @param transferAccountId The AccountId to be set
     * @return {@code this}
     */
    public AccountDeleteTransaction setTransferAccountId(AccountId transferAccountId) {
        requireNotFrozen();
        Objects.requireNonNull(transferAccountId);
        this.transferAccountId = transferAccountId;
        return this;
    }

    @Override
    void validateNetworkOnIds(@Nullable NetworkName networkName) {
        EntityIdHelper.validateNetworkOnIds(this.accountId, networkName);
        EntityIdHelper.validateNetworkOnIds(this.transferAccountId, networkName);
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getCryptoDeleteMethod();
    }

    CryptoDeleteTransactionBody.Builder build() {
        if (accountId != null) {
            builder.setDeleteAccountID(accountId.toProtobuf());
        }

        if (transferAccountId != null) {
            builder.setTransferAccountID(transferAccountId.toProtobuf());
        }

        return builder;
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoDelete(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setCryptoDelete(build());
    }
}
