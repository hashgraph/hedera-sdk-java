package com.hedera.hashgraph.sdk;

import java.time.Instant;

class Node {
    AccountId accountId;
    String address;
    long delay;
    Long lastUsed;

    Node(AccountId accountId, String address) {
        this.accountId = accountId;
        this.address = address;

        this.delay = 250;

        this.lastUsed = null;
    }

    boolean isHealthy() {
        if (this.lastUsed != null) {
            return this.lastUsed + this.delay < Instant.now().toEpochMilli();
        }

        return true;
    }

    void increaseDelay() {
        this.lastUsed = Instant.now().toEpochMilli();
        this.delay = Math.min(this.delay * 2, 8000);
    }

    void decreaseDelay() {
        this.delay = Math.max(this.delay / 2, 250);
    }

    long delay() {
        return (this.lastUsed != null ? this.lastUsed : 0) + this.delay - Instant.now().toEpochMilli();
    }
}
