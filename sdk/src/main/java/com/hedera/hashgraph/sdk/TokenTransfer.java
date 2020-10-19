package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.sdk.proto.AccountAmount;

/**
 * A transfer of tokens that occurred within a transaction.
 *
 * Returned with a {@link TransactionRecord}.
 */
public final class TokenTransfer {
    /**
     * The Account ID that sends or receives crypto-currency.
     */
    public final AccountId accountId;

    /**
     * The amount that the account sends (negative) or receives (positive).
     */
    public final long amount;

    private TokenTransfer(AccountId accountId, long amount) {
        this.accountId = accountId;
        this.amount = amount;
    }

    static TokenTransfer fromProtobuf(AccountAmount accountAmount) {
        return new TokenTransfer(AccountId.fromProtobuf(accountAmount.getAccountID()), accountAmount.getAmount());
    }

    AccountAmount toProtobuf() {
        return AccountAmount.newBuilder()
            .setAccountID(accountId.toProtobuf())
            .setAmount(amount)
            .build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("accountId", accountId)
            .add("amount", amount)
            .toString();
    }
}
