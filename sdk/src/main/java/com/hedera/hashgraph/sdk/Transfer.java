package com.hedera.hashgraph.sdk;

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
}
