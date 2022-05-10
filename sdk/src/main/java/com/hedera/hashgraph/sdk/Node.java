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

import io.grpc.ChannelCredentials;
import io.grpc.TlsChannelCredentials;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;

class Node extends ManagedNode<Node, AccountId> {
    private final AccountId accountId;

    @Nullable
    private NodeAddress addressBook;

    private boolean verifyCertificates;

    Node(AccountId accountId, ManagedNodeAddress address, ExecutorService executor) {
        super(address, executor);

        this.accountId = accountId;
    }

    Node(AccountId accountId, String address, ExecutorService executor) {
        this(accountId, ManagedNodeAddress.fromString(address), executor);
    }

    Node(Node node, ManagedNodeAddress address) {
        super(node, address);

        this.accountId = node.accountId;
        this.verifyCertificates = node.verifyCertificates;
        this.addressBook = node.addressBook;
    }

    @Override
    Node toInsecure() {
        return new Node(this, address.toInsecure());
    }

    @Override
    Node toSecure() {
        return new Node(this, address.toSecure());
    }

    @Override
    AccountId getKey() {
        return accountId;
    }

    AccountId getAccountId() {
        return accountId;
    }

    NodeAddress getAddressBook() {
        return addressBook;
    }

    synchronized Node setAddressBook(@Nullable NodeAddress addressBook) {
        this.addressBook = addressBook;
        return this;
    }

    boolean isVerifyCertificates() {
        return verifyCertificates;
    }

    Node setVerifyCertificates(boolean verifyCertificates) {
        this.verifyCertificates = verifyCertificates;
        return this;
    }

    @Override
    synchronized ChannelCredentials getChannelCredentials() {
        return TlsChannelCredentials.newBuilder()
            .trustManager(new HederaTrustManager(addressBook == null ? null : addressBook.certHash, verifyCertificates))
            .build();
    }

    @Override
    public String toString() {
        return accountId.toString();
    }
}
