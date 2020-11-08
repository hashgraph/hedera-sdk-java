package com.hedera.hashgraph.sdk;

import java.util.concurrent.ExecutorService;

class MirrorNode extends ManagedNode {
    MirrorNode(String address, ExecutorService executor) {
        super(address, executor);
    }
}
