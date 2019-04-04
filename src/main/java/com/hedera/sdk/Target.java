package com.hedera.sdk;

public enum Target {
    //todo: add the other 40ish targets
    MAIN_NET("testnet.hedera.com:50139", new AccountId(3));

    private final String address;
    private final AccountId node;

    Target(final String address, final AccountId node) {
        this.address = address;
        this.node = node;
    }

    public String getAddress() {
        return address;
    }

    public AccountId getNode() {
        return node;
    }

    @Override
    public String toString() {
        return address;
    }
}
