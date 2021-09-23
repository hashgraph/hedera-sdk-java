package com.hedera.hashgraph.sdk;

import io.grpc.ChannelCredentials;
import io.grpc.TlsChannelCredentials;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;

class Node extends ManagedNode implements Comparable<Node> {
    private final AccountId accountId;

    @Nullable
    private NodeAddress addressBook;

    private long backoffUntil;
    private Duration currentBackoff;
    private Duration minBackoff;
    private long attempts;
    private boolean verifyCertificates;

    public Node(AccountId accountId, ManagedNodeAddress address, ExecutorService executor) {
        super(address, executor);

        this.accountId = accountId;
        this.currentBackoff = Client.DEFAULT_MIN_BACKOFF;
        this.minBackoff = Client.DEFAULT_MIN_BACKOFF;
    }

    public Node(AccountId accountId, String address, ExecutorService executor) {
        this(accountId, ManagedNodeAddress.fromString(address), executor);
    }

    private Node(Node node, ManagedNodeAddress address) {
        super(node, address);

        this.accountId = node.accountId;
        this.minBackoff = node.minBackoff;
        this.verifyCertificates = node.verifyCertificates;
        this.addressBook = node.addressBook;
        this.backoffUntil = node.backoffUntil;
        this.currentBackoff = node.currentBackoff;
        this.attempts = node.attempts;
    }

    public Node toInsecure() {
        return new Node(this, address.toInsecure());
    }

    public Node toSecure() {
        return new Node(this, address.toSecure());
    }

    public AccountId getAccountId() {
        return accountId;
    }

    public NodeAddress getAddressBook() {
        return addressBook;
    }

    public Node setAddressBook(@Nullable NodeAddress addressBook) {
        this.addressBook = addressBook;
        return this;
    }

    public long getBackoffUntil() {
        return backoffUntil;
    }

    public Duration getMinBackoff() {
        return minBackoff;
    }

    public long getAttempts() {
        return attempts;
    }

    public boolean isVerifyCertificates() {
        return verifyCertificates;
    }

    public Node setVerifyCertificates(boolean verifyCertificates) {
        this.verifyCertificates = verifyCertificates;
        return this;
    }

    public Node setMinBackoff(Duration minBackoff) {
        // If delay is equal to the old minBackoff we should change it to the new minBackoff
        if (currentBackoff == this.minBackoff) {
            currentBackoff = minBackoff;
        }

        this.minBackoff = minBackoff;

        return this;
    }

    boolean isHealthy() {
        return backoffUntil < System.currentTimeMillis();
    }

    void increaseDelay() {
        this.attempts++;
        this.backoffUntil = System.currentTimeMillis() + this.currentBackoff.toMillis();
        this.currentBackoff = Duration.ofMillis(Math.min(this.currentBackoff.toMillis() * 2, 8000));
    }

    void decreaseDelay() {
        this.currentBackoff = Duration.ofMillis(Math.max(this.currentBackoff.toMillis() / 2, minBackoff.toMillis()));
    }

    long getRemainingTimeForBackoff() {
        return backoffUntil - System.currentTimeMillis();
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
    public ChannelCredentials getChannelCredentials() {
        return TlsChannelCredentials.newBuilder()
            .trustManager(new HederaTrustManager(addressBook == null ? null : addressBook.certHash, verifyCertificates))
            .build();
    }

    @Override
    public String toString() {
        return accountId.toString();
    }
}
