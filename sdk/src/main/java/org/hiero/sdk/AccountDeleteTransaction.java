// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.MethodDescriptor;
import java.util.LinkedHashMap;
import java.util.Objects;
import javax.annotation.Nullable;
import org.hiero.sdk.proto.CryptoDeleteTransactionBody;
import org.hiero.sdk.proto.CryptoServiceGrpc;
import org.hiero.sdk.proto.SchedulableTransactionBody;
import org.hiero.sdk.proto.TransactionBody;
import org.hiero.sdk.proto.TransactionResponse;

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

    /**
     * Constructor.
     */
    public AccountDeleteTransaction() {}

    /**
     * Constructor.
     *
     * @param txs                                   Compound list of transaction id's list of (AccountId, Transaction) records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    AccountDeleteTransaction(
            LinkedHashMap<TransactionId, LinkedHashMap<AccountId, org.hiero.sdk.proto.Transaction>> txs)
            throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody                    protobuf TransactionBody
     */
    AccountDeleteTransaction(org.hiero.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Extract the account id.
     *
     * @return                          the account id
     */
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

    /**
     * Extract the receiving account id.
     *
     * @return                          the account id that receives the hbar
     */
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
    MethodDescriptor<org.hiero.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getCryptoDeleteMethod();
    }

    /**
     * Build the transaction body.
     *
     * @return {@link CryptoDeleteTransactionBody}
     */
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

    /**
     * Initialize from the transaction body.
     */
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
