package com.hedera.hashgraph.sdk;

import java.time.Instant;
import java.util.concurrent.ExecutorService;

class Node extends ManagedNode implements Comparable<Node>{
    AccountId accountId;
    long delay;
    long delayUntil;

    Node(AccountId accountId, String address, ExecutorService executor) {
        super(address, executor);
        this.accountId = accountId;
        this.delay = 250;
        this.delayUntil = 0;
        useCount = 0;
    }

    boolean isHealthy() {
        return delayUntil < Instant.now().toEpochMilli();
    }

    void increaseDelay() {
        this.delay = Math.min(this.delay * 2, 8000);
    }

    void decreaseDelay() {
        this.delay = Math.max(this.delay / 2, 250);
    }

    long delay() {
        return delayUntil - Instant.now().toEpochMilli();
    }

    @Override
    public int compareTo(Node node) {
        if (this.isHealthy() && node.isHealthy()) {
            return compareToSameHealth(node);
        } else if (this.isHealthy() && !node.isHealthy()) {
            return -1;
        } else if (!this.isHealthy() && node.isHealthy()) {
            return 1;
        } else {
            return compareToSameHealth(node);
        }
    }

    private int compareToSameHealth(Node node) {
        if (this.useCount < node.useCount) {
            return -1;
        } else if (this.useCount > node.useCount) {
            return 1;
        } else {
            if (this.lastUsed < node.lastUsed) {
                return -1;
            } else if (this.lastUsed > node.lastUsed) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public String toString() {
        return accountId.toString();
    }
}
