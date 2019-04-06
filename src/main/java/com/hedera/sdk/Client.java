package com.hedera.sdk;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.*;
import java.util.stream.Collectors;

class ChannelPair {
    final ManagedChannel channel;
    final AccountId node;

    ChannelPair(ManagedChannel channel, AccountId node) {
        this.channel = channel;
        this.node = node;
    }
}

public final class Client {

    private final Random random = new Random();
    private Map<String, ChannelPair> channels;

    public Client(Map<String, AccountId> targets) {

        if (targets.isEmpty()) {
            throw new IllegalArgumentException("List of targets must not be empty");
        }

        channels = targets.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, t -> new ChannelPair(ManagedChannelBuilder.forTarget(t.getKey())
                .usePlaintext()
                .build(), t.getValue()
            )));
    }

    ManagedChannel getChannel() {

        var r = random.nextInt(channels.size() > 1 ? channels.size() - 1 : 1);
        var channelIter = channels.entrySet()
            .iterator();

        for (int i = 0; i < r; i++) {
            channelIter.next();
        }

        return channelIter.next()
            .getValue().channel;
    }

    AccountId getNodeForTarget(String target) throws IllegalArgumentException {
        var channel = channels.get(target);

        if (channel == null) {
            throw new IllegalArgumentException("Invalid target string provided");
        }

        return channel.node;
    }
}
