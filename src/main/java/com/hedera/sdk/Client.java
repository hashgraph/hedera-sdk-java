package com.hedera.sdk;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.*;
import java.util.stream.Collectors;

public final class Client {

    private final Random random = new Random();
    private Map<AccountId, String> channels;

    public Client(Map<String, AccountId> targets) {

        if (targets.isEmpty()) {
            throw new IllegalArgumentException("List of targets must not be empty");
        }

        channels = targets.entrySet()
            .stream()
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    ManagedChannel openChannel() throws IllegalArgumentException {

        var r = random.nextInt(channels.size());
        var channelIter = channels.entrySet()
            .iterator();

        for (int i = 0; i < r - 1; i++) {
            channelIter.next();
        }

        var target = channelIter.next()
            .getValue();

        return ManagedChannelBuilder.forTarget(target)
            .usePlaintext()
            .build();
    }

    ManagedChannel openChannelForNode(AccountId node) throws IllegalArgumentException {
        var address = channels.get(node);

        if (address == null) {
            throw new IllegalArgumentException("Node Id does not exist");
        }

        return ManagedChannelBuilder.forTarget(address)
            .usePlaintext()
            .build();
    }
}
