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

    /**
     * Constructor.
     *
     * @param node                      the mirror node
     * @param address                   the address as a managed node address
     */
    private MirrorNode(MirrorNode node, BaseNodeAddress address) {
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
    BaseNodeAddress getKey() {
        return address;
    }
}
