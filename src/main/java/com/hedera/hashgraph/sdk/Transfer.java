package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.AccountAmount;

public final class Transfer {
    public final AccountId accountId;

    public final long amount;

    private Transfer(AccountId accountId, long amount) {
        this.accountId = accountId;
        this.amount = amount;
    }

    static Transfer fromProtobuf(AccountAmount accountAmount) {
        return new Transfer(AccountId.fromProtobuf(accountAmount.getAccountID()), accountAmount.getAmount());
    }
}
