/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

public class NetworkTest {
    /*
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
    }*/
}
