package com.hedera.hashgraph.sdk;

import java.time.Instant;
import java.util.concurrent.ExecutorService;

class Node extends ManagedNode {
    AccountId accountId;
    long delay;

    Node(AccountId accountId, String address, ExecutorService executor) {
        super(address, executor);
        this.accountId = accountId;
        this.delay = 250;
    }

    boolean isHealthy() {
        if (this.lastUsed != null) {
            return this.lastUsed + this.delay < Instant.now().toEpochMilli();
        }

        return true;
    }

    void increaseDelay() {
        this.delay = Math.min(this.delay * 2, 8000);
    }

    void decreaseDelay() {
        this.delay = Math.max(this.delay / 2, 250);
    }

    long delay() {
        return (this.lastUsed != null ? this.lastUsed : 0) + this.delay - Instant.now().toEpochMilli();
    }
}
