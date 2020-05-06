package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.sdk.proto.AccountAmount;

/**
 * A transfer of Hbar that occurred within a transaction.
 *
 * Returned with a {@link TransactionRecord}.
 */
public final class Transfer {
    /**
     * The Account ID that sends or receives crypto-currency.
     */
    public final AccountId accountId;

    /**
     * The amount that the account sends (negative) or receives (positive).
     */
    public final Hbar amount;

    private Transfer(AccountId accountId, Hbar amount) {
        this.accountId = accountId;
        this.amount = amount;
    }

    static Transfer fromProtobuf(AccountAmount accountAmount) {
        return new Transfer(AccountId.fromProtobuf(accountAmount.getAccountID()), Hbar.fromTinybars(accountAmount.getAmount()));
    }

    AccountAmount toProtobuf() {
        return AccountAmount.newBuilder()
            .setAccountID(accountId.toProtobuf())
            .setAmount(amount.toTinybars())
            .build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("accoundId", accountId)
            .add("amount", amount)
            .toString();
    }
}
