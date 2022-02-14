package com.hedera.hashgraph.sdk;

import io.grpc.ChannelCredentials;
import io.grpc.TlsChannelCredentials;
import org.threeten.bp.Duration;

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

    Node setAddressBook(@Nullable NodeAddress addressBook) {
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
    ChannelCredentials getChannelCredentials() {
        return TlsChannelCredentials.newBuilder()
            .trustManager(new HederaTrustManager(addressBook == null ? null : addressBook.certHash, verifyCertificates))
            .build();
    }

    @Override
    public String toString() {
        return accountId.toString();
    }
}
