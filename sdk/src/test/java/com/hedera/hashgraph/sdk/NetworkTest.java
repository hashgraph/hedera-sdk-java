package com.hedera.hashgraph.sdk;

import org.junit.jupiter.api.Test;
import org.assertj.core.api.Assertions;

import java.util.ArrayList;
import java.util.Collections;

public class NetworkTest {
    @Test
    void sortsNetworkCorrectly() throws InterruptedException {
        var executor = Client.createExecutor();

        var node3 = new Node(AccountId.fromString("0.0.3"), "localhost:50213", executor);
        var node4 = new Node(AccountId.fromString("0.0.4"), "localhost:50214", executor);
        var node5 = new Node(AccountId.fromString("0.0.5"), "localhost:50215", executor);
        var node6 = new Node(AccountId.fromString("0.0.6"), "localhost:50216", executor);

        var network = new ArrayList<Node>();
        network.add(node3);
        network.add(node4);
        network.add(node5);
        network.add(node6);

        Collections.sort(network);
        Assertions.assertThat(network.toArray()).isEqualTo(new Node[]{node3, node4, node5, node6});

        node3.inUse();
        node3.increaseBackoff();
        Collections.sort(network);
        Assertions.assertThat(network.toArray()).isEqualTo(new Node[]{node4, node5, node6, node3});

        Thread.sleep(10);
        node5.inUse();
        node5.increaseBackoff();
        Collections.sort(network);
        Assertions.assertThat(network.toArray()).isEqualTo(new Node[]{node4, node6, node3, node5});

        Thread.sleep(10);
        node5.inUse();
        node5.decreaseBackoff();
        Collections.sort(network);
        Assertions.assertThat(network.toArray()).isEqualTo(new Node[]{node4, node6, node3, node5});

        Thread.sleep(10);
        node4.inUse();
        node4.decreaseBackoff();
        Collections.sort(network);
        Assertions.assertThat(network.toArray()).isEqualTo(new Node[]{node6, node4, node3, node5});
    }
}
