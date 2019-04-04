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

    //private final List<ManagedChannel> channels;
    private final Random random = new Random();

    private Map<String, ChannelPair> channels;

    public Client(Map<String, AccountId> targets) {
        assert !targets.isEmpty();

        channels = targets.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, t -> new ChannelPair(ManagedChannelBuilder.forTarget(t.getKey())
                .build(), t.getValue()
            )));
    }

    public Client(Target... targets) {
        this(
                Arrays.stream(targets)
                    .collect(Collectors.toMap(Target::getAddress, Target::getNode))
        );
    }

    ManagedChannel getChannel() {
        var r = random.nextInt(channels.size() - 1);
        var channelIter = channels.entrySet()
            .iterator();

        for (int i = 0; i < r; i++) {
            channelIter.next();
        }

        return channelIter.next()
            .getValue().channel;
    }

    AccountId getNodeForTarget(String target) throws IllegalArgumentException {
        AccountId node;

        try {
            node = channels.get(target).node;
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }

        return node;
    }
}
