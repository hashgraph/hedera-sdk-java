package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.proto.ProxyStaker;
import com.hedera.hashgraph.sdk.Hbar;

/**
 * An account that has some amount proxy staked to another account.
 *
 * Returned in a list from {@link AccountStakersQuery}.
 */
public final class AccountProxyStaker {
    public final AccountId accountId;
    public final Hbar amount;

    AccountProxyStaker(ProxyStaker proxyStaker) {
        this.accountId = new AccountId(proxyStaker.getAccountID());
        this.amount = Hbar.fromTinybar(proxyStaker.getAmount());
    }
}
