package com.hedera.sdk;

public enum Targets {
    //todo: add the other 40ish targets
    MAIN_NET("some target"),
    TEST_NET_0("some test net");

    private final String target;

    Targets(final String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return target;
    }
}
