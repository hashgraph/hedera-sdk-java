// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.java;

import java.util.concurrent.ExecutorService;

/**
 * An individual mirror node.
 */
class MirrorNode extends BaseNode<MirrorNode, BaseNodeAddress> {
    /**
     * Constructor.
     *
     * @param address                   the node address as a managed node address
     * @param executor                  the executor service
     */
    MirrorNode(BaseNodeAddress address, ExecutorService executor) {
        super(address, executor);
    }

    /**
     * Constructor.
     *
     * @param address                   the node address as a string
     * @param executor                  the executor service
     */
    MirrorNode(String address, ExecutorService executor) {
        this(BaseNodeAddress.fromString(address), executor);
    }

    @Override
    protected String getAuthority() {
        return null;
    }

    @Override
    BaseNodeAddress getKey() {
        return address;
    }
}
