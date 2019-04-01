package com.hedera.sdk;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.*;

public final class Client {

    private final List<ManagedChannel> channels;
    private final Random random = new Random();

    public Client(String... targets) {
        this(Arrays.asList(targets));
    }

    public Client(Iterable<String> targets) {
        var channelList = new ArrayList<ManagedChannel>();
        for (String target : targets) {
            channelList.add(
                ManagedChannelBuilder.forTarget(target)
                    .usePlaintext()
                    .build()
            );
        }

        assert !channelList.isEmpty();
        channels = channelList;
    }

    ManagedChannel getChannel() {
        return channels.get(random.nextInt(channels.size()));
    }
}
