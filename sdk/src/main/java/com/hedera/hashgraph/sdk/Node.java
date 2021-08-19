package com.hedera.hashgraph.sdk;

import io.grpc.ChannelCredentials;
import io.grpc.TlsChannelCredentials;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;

class Node extends ManagedNode implements Comparable<Node> {
    AccountId accountId;

    @Nullable
    NetworkName networkName;

    long delay;
    long delayUntil;
    long waitTime;
    long attempts;

    Node(AccountId accountId, String address, long waitTime, ExecutorService executor) {
        super(address, executor);

        this.accountId = accountId;
        this.delay = waitTime;
        this.waitTime = waitTime;
        this.delayUntil = 0;
        this.attempts = 0;
    }

    @Nullable
    public NetworkName getNetworkName() {
        return networkName;
    }

    public Node setNetworkName(NetworkName networkName) {
        this.networkName = networkName;
        return this;
    }

    @Override
    ChannelCredentials getChannelCredentials() {
        return TlsChannelCredentials.newBuilder()
            .trustManager(new HederaTrustManager(networkName, accountId))
            .build();
    }

    void setWaitTime(long waitTime) {
        // If delay is equal to the old waitTime we should change it to the new waitTime
        if (delay == this.waitTime) {
            delay = waitTime;
        }

        this.waitTime = waitTime;
    }

    boolean isHealthy() {
        return delayUntil < System.currentTimeMillis();
    }

    void increaseDelay() {
        this.attempts++;
        this.delayUntil = System.currentTimeMillis() + this.delay;
        this.delay = Math.min(this.delay * 2, 8000);
    }

    void decreaseDelay() {
        this.delay = Math.max(this.delay / 2, waitTime);
    }

    long delay() {
        return delayUntil - System.currentTimeMillis();
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
