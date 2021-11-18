package com.hedera.hashgraph.sdk;

import java.util.concurrent.ExecutorService;

class MirrorNode extends ManagedNode<MirrorNode, ManagedNodeAddress> {
    MirrorNode(ManagedNodeAddress address, ExecutorService executor) {
        super(address, executor);
    }

    MirrorNode(String address, ExecutorService executor) {
        this(ManagedNodeAddress.fromString(address), executor);
    }

    private MirrorNode(MirrorNode node, ManagedNodeAddress address) {
        super(node, address);
    }

    MirrorNode toInsecure() {
        return new MirrorNode(this, address.toInsecure());
    }

    MirrorNode toSecure() {
        return new MirrorNode(this, address.toSecure());
    }

    @Override
    ManagedNodeAddress getKey() {
        return address;
    }
}
