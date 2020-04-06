package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.AccountAmount;

public final class Transfer {
    public final AccountId accountId;

    public final Hbar amount;

    private Transfer(AccountId accountId, Hbar amount) {
        this.accountId = accountId;
        this.amount = amount;
    }

    static Transfer fromProtobuf(AccountAmount accountAmount) {
        return new Transfer(AccountId.fromProtobuf(accountAmount.getAccountID()), Hbar.fromTinybar(accountAmount.getAmount()));
    }
}
