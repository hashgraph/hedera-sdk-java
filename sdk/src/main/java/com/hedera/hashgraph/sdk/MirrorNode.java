package com.hedera.hashgraph.sdk;

import java.util.concurrent.ExecutorService;

class MirrorNode extends ManagedNode<MirrorNode> {
    public MirrorNode(ManagedNodeAddress address, ExecutorService executor) {
        super(address, executor);
    }

    public MirrorNode(String address, ExecutorService executor) {
        this(ManagedNodeAddress.fromString(address), executor);
    }

    private MirrorNode(MirrorNode node, ManagedNodeAddress address) {
        super(node, address);
    }

    public MirrorNode toInsecure() {
        return new MirrorNode(this, address.toInsecure());
    }

    public MirrorNode toSecure() {
        return new MirrorNode(this, address.toSecure());
    }
}
