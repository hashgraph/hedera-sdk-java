package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

class Node {

    final AccountId accountId;
    final String address;

    // volatile is required for correct double-checked locking
    @Nullable
    private volatile ManagedChannel channel = null;

    Node(AccountId accountId, String address) {
        this.accountId = accountId;
        this.address = address;
    }

    ManagedChannel getChannel() {
        if (channel == null) {
            synchronized (this) {
                if (channel == null) {
                    channel = ManagedChannelBuilder.forTarget(address)
                        .usePlaintext()
                        .build();
                }
            }
        }

        return channel;
    }

    void closeChannel() {
        // because `channel` is volatile, we have to explicitly load it so we can null-check it
        // otherwise it could be set to `null` between when we checked it and when we used it
        final ManagedChannel channel = this.channel;

        if (channel != null) {
            channel.shutdown();
        }
    }

    void awaitChannelTermination(long timeout, TimeUnit timeUnit) throws InterruptedException, TimeoutException {
        final ManagedChannel channel = this.channel;

        if (channel != null && channel.shutdown().awaitTermination(timeout, timeUnit)) {
            throw new TimeoutException("Timed out waiting for node channel to shutdown: "
                + accountId + " :: " + address);
        }
    }
}
