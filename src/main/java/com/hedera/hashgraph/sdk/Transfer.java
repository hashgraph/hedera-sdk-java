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
        accountId = new AccountId(accountAmount.getAccountIDOrBuilder());
        amount = Hbar.fromTinybar(accountAmount.getAmount());
    }
}
