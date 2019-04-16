package com.hedera.sdk;

import com.hedera.sdk.account.AccountId;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public final class Client {
    private final Random random = new Random();
    private Map<AccountId, Node> channels;

    static final long DEFAULT_MAX_TXN_FEE = 100_000;

    // todo: transaction fees should be defaulted to whatever the transaction fee schedule is
    private long maxTransactionFee = DEFAULT_MAX_TXN_FEE;

    @Nullable
    private AccountId operatorId;
    @Nullable
    private Ed25519PrivateKey operatorKey;

    public Client(Map<AccountId, String> nodes) {

        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("List of nodes must not be empty");
        }

        channels = nodes.entrySet()
            .stream()
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, t -> new Node(t.getKey(), t.getValue())));
    }

    public Client setMaxTransactionFee(@Nonnegative long maxTransactionFee) {
        if (maxTransactionFee <= 0) {
            throw new IllegalArgumentException("maxTransactionFee must be > 0");
        }

        this.maxTransactionFee = maxTransactionFee;
        return this;
    }

    public long getMaxTransactionFee() {
        return maxTransactionFee;
    }

    public Client setOperator(AccountId operatorId, Ed25519PrivateKey operatorKey) {
        this.operatorId = operatorId;
        this.operatorKey = operatorKey;
        return this;
    }

    @Nullable
    public AccountId getOperatorId() {
        return operatorId;
    }

    @Nullable
    public Ed25519PrivateKey getOperatorKey() {
        return operatorKey;
    }

    Node pickNode() {
        if (channels.isEmpty()) {
            throw new IllegalStateException("List of channels has become empty");
        }

        var r = random.nextInt(channels.size());
        var channelIter = channels.values()
            .iterator();

        for (int i = 1; i < r; i++) {
            channelIter.next();
        }

        return channelIter.next();
    }

    Node getNodeForId(AccountId node) {
        var selectedChannel = channels.get(node);

        if (selectedChannel == null) {
            throw new IllegalArgumentException("Node Id does not exist");
        }

        return selectedChannel;
    }
}
