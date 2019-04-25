package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import javax.annotation.Nullable;
import java.util.Objects;

class Node {
    final AccountId accountId;
    final String address;

    @Nullable
    private volatile ManagedChannel channel;

    Node(AccountId accountId, String address) {
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
