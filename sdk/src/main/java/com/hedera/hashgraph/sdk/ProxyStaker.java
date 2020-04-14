package com.hedera.hashgraph.sdk;

public final class ProxyStaker {
    public final AccountId accountId;

    public final Hbar amount;

    private ProxyStaker(AccountId accountId, long amount) {
        this.accountId = accountId;
        this.amount = Hbar.fromTinybar(amount);
    }

    static ProxyStaker fromProtobuf(com.hedera.hashgraph.sdk.proto.ProxyStaker proxyStaker) {
        return new ProxyStaker(AccountId.fromProtobuf(proxyStaker.getAccountID()), proxyStaker.getAmount());
    }
}
