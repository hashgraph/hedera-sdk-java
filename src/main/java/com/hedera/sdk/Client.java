package com.hedera.sdk;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

class ChannelPair {
    final String address;
    @Nullable
    ManagedChannel channel;

    ChannelPair(String address) {
        this.address = address;
    }
}

public final class Client {

    private final Random random = new Random();
    private Map<AccountId, ChannelPair> channels;

    public Client(Map<AccountId, String> targets) {

        if (targets.isEmpty()) {
            throw new IllegalArgumentException("List of targets must not be empty");
        }

        channels = targets.entrySet()
            .stream()
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, t -> new ChannelPair(t.getValue())));
    }

    ManagedChannel getChannel() {

        if (channels.isEmpty()) {
            throw new IllegalStateException("List of channels has become empty");
        }

        var r = random.nextInt(channels.size());
        var channelIter = channels.values()
            .iterator();

        for (int i = 1; i < r; i++) {
            channelIter.next();
        }

        var selectedChannel = channelIter.next();

        if (selectedChannel.channel == null) {
            selectedChannel.channel = ManagedChannelBuilder.forTarget(selectedChannel.address)
                .usePlaintext()
                .build();
        }

        return Objects.requireNonNull(selectedChannel.channel);
    }

    ManagedChannel getChannelForNode(AccountId node) {
        var selectedChannel = channels.get(node);

        if (selectedChannel == null) {
            throw new IllegalArgumentException("Node Id does not exist");
        }

        if (selectedChannel.channel == null) {
            selectedChannel.channel = ManagedChannelBuilder.forTarget(selectedChannel.address)
                .usePlaintext()
                .build();
        }

        return Objects.requireNonNull(selectedChannel.channel);
    }
}
