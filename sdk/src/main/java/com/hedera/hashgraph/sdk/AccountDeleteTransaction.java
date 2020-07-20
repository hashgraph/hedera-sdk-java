package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

/**
 * Marks an account as deleted, moving all its current hbars to another account.
 *
 * It will remain in the ledger, marked as deleted, until it expires.
 * Transfers into it a deleted account fail. But a deleted account can still have its
 * expiration extended in the normal way.
 */
public final class AccountDeleteTransaction extends SingleTransactionBuilder<AccountDeleteTransaction> {
    private final CryptoDeleteTransactionBody.Builder builder;

    public AccountDeleteTransaction() {
        builder = CryptoDeleteTransactionBody.newBuilder();
    }

    /**
     * Sets the account ID which should be deleted.
     *
     * @return {@code this}
     * @param deleteAccountId The AccountId to be set
     */
    public AccountDeleteTransaction setAccountId(AccountId deleteAccountId) {
        builder.setDeleteAccountID(deleteAccountId.toProtobuf());
        return this;
    }

    /**
     * Sets the account ID which will receive all remaining hbars.
     *
     * @return {@code this}
     * @param transferAccountId The AccountId to be set
     */
    public AccountDeleteTransaction setTransferAccountId(AccountId transferAccountId) {
        builder.setTransferAccountID(transferAccountId.toProtobuf());
        return this;
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoDelete(builder);
    }
}
