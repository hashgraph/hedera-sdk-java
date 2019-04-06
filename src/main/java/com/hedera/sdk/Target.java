package com.hedera.sdk;

public enum Target {
    //todo: add the other 40ish targets
    TESTNET_139("testnet.hedera.com:50139", 0, 0, 3);

    private final String address;
    private final long shard, realm, account;

    Target(final String address, final long realm, final long shard, final long account) {
        this.address = address;
        this.realm = realm;
        this.shard = shard;
        this.account = account;
    }

    public String getAddress() {
        return address;
    }

    public AccountId getNode() {
        return new AccountId(shard, realm, account);
    }

    @Override
    public String toString() {
        return address;
    }
}
