package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
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
    @Nullable
    private AccountId accountId = null;
    @Nullable
    private AccountId transferAccountId = null;


    public AccountDeleteTransaction() {
    }

    AccountDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    AccountDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
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
        Objects.requireNonNull(deleteAccountId);
        requireNotFrozen();
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
    void validateChecksums(Client client) throws BadEntityIdException {
        if (accountId != null) {
            accountId.validateChecksum(client);
        }

        if (transferAccountId != null) {
            transferAccountId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getCryptoDeleteMethod();
    }

    CryptoDeleteTransactionBody.Builder build() {
        var builder = CryptoDeleteTransactionBody.newBuilder();

        if (accountId != null) {
            builder.setDeleteAccountID(accountId.toProtobuf());
        }

        if (transferAccountId != null) {
            builder.setTransferAccountID(transferAccountId.toProtobuf());
        }

        return builder;
    }

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getCryptoDelete();
        if (body.hasDeleteAccountID()) {
            accountId = AccountId.fromProtobuf(body.getDeleteAccountID());
        }

        if (body.hasTransferAccountID()) {
            transferAccountId = AccountId.fromProtobuf(body.getTransferAccountID());
        }
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoDelete(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setCryptoDelete(build());
    }
}
