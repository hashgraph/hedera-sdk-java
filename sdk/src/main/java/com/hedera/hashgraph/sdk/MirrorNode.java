package com.hedera.hashgraph.sdk;

import java.util.concurrent.ExecutorService;

/**
 * An individual mirror node.
 */
class MirrorNode extends ManagedNode<MirrorNode, ManagedNodeAddress> {
    /**
     * Constructor.
     *
     * @param address                   the node address as a managed node address
     * @param executor                  the executor service
     */
    MirrorNode(ManagedNodeAddress address, ExecutorService executor) {
        super(address, executor);
    }

    /**
     * Constructor.
     *
     * @param address                   the node address as a string
     * @param executor                  the executor service
     */
    MirrorNode(String address, ExecutorService executor) {
        this(ManagedNodeAddress.fromString(address), executor);
    }

    /**
     * Constructor.
     *
     * @param node                      the mirror node
     * @param address                   the address as a managed node address
     */
    private MirrorNode(MirrorNode node, ManagedNodeAddress address) {
        super(node, address);
    }

    @Override
    protected String getAuthority() {
        return null;
    }

    @Override
    MirrorNode toInsecure() {
        return new MirrorNode(this, address.toInsecure());
    }

    @Override
    MirrorNode toSecure() {
        return new MirrorNode(this, address.toSecure());
    }

    @Override
    ManagedNodeAddress getKey() {
        return address;
    }
}
