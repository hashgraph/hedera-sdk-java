package com.hedera.sdk;

import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

class ChannelHolder {
    final AccountId accountId;
    final String address;
    @Nullable
    private volatile ManagedChannel channel;

    ChannelHolder(AccountId accountId, String address) {
        this.accountId = accountId;
        this.address = address;
    }

    ManagedChannel getChannel() {
        // only build the channel once; check before locking the mutex
        if (channel == null) {
            synchronized (this) {
                // might have been initialized while we were waiting
                if (channel == null) {
                    channel = ManagedChannelBuilder.forTarget(address)
                        .usePlaintext()
                        .build();
                }
            }
        }

        return Objects.requireNonNull(channel);
    }
}

public final class Client {

    private final Random random = new Random();
    private Map<AccountId, ChannelHolder> channels;

    static final long DEFAULT_MAX_TXN_FEE = 100_000;

    // todo: transaction fees should be defaulted to whatever the transaction fee schedule is
    private long maxTransactionFee = DEFAULT_MAX_TXN_FEE;

    @Nullable
    private AccountId operatorId;
    @Nullable
    private Ed25519PrivateKey operatorKey;

    public Client(Map<AccountId, String> targets) {

        if (targets.isEmpty()) {
            throw new IllegalArgumentException("List of targets must not be empty");
        }

        channels = targets.entrySet()
            .stream()
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, t -> new ChannelHolder(t.getKey(), t.getValue())));
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

    ChannelHolder getChannel() {
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

    ChannelHolder getChannelForNode(AccountId node) {
        var selectedChannel = channels.get(node);

        if (selectedChannel == null) {
            throw new IllegalArgumentException("Node Id does not exist");
        }

        return selectedChannel;
    }
}
