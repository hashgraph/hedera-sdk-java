package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.AccountAmountOrBuilder;
import com.hedera.hashgraph.sdk.account.AccountId;

/**
 * A transfer in a {@link TransactionRecord}.
 */
public final class Transfer {
    public final AccountId accountId;
    public final Hbar amount;

    Transfer(AccountAmountOrBuilder accountAmount) {
        this(
            new AccountId(accountAmount.getAccountIDOrBuilder()),
            Hbar.fromTinybar(accountAmount.getAmount()));
    }

    Transfer(AccountId accountId, Hbar amount) {
        this.accountId = accountId;
        this.amount = amount;
    }

    /**
     * Get a debug printout of this transfer.
     *
     * The format is not considered part of the stable API.
     */
    @Override
    public String toString() {
        return "Transfer{"
            + "accountId=" + accountId
            + ", amount=" + amount
            + '}';
    }
}
