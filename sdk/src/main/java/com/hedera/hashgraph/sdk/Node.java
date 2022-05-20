package com.hedera.hashgraph.sdk;

import io.grpc.ChannelCredentials;
import io.grpc.TlsChannelCredentials;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;

/**
 * Internal utility class.
 */
class Node extends ManagedNode<Node, AccountId> {
    private final AccountId accountId;

    @Nullable
    private NodeAddress addressBook;

    private boolean verifyCertificates;

    /**
     * Constructor.
     *
     * @param accountId                 the account id
     * @param address                   the address as a managed node address
     * @param executor                  the executor service
     */
    Node(AccountId accountId, ManagedNodeAddress address, ExecutorService executor) {
        super(address, executor);

        this.accountId = accountId;
    }

    /**
     * Constructor.
     * @param accountId                 the account id
     * @param address                   the address as a string
     * @param executor                  the executor service
     */
    Node(AccountId accountId, String address, ExecutorService executor) {
        this(accountId, ManagedNodeAddress.fromString(address), executor);
    }

    /**
     * Constructor for a node that verifies certificates.
     *
     * @param node                      the node
     * @param address                   the address as a managed node address
     */
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

    /**
     * @return                          the account id
     */
    AccountId getAccountId() {
        return accountId;
    }

    /**
     * @return                          the address book
     */
    NodeAddress getAddressBook() {
        return addressBook;
    }

    /**
     * Assign the address book.
     *
     * @param addressBook               the address book
     * @return {@code this}
     */
    Node setAddressBook(@Nullable NodeAddress addressBook) {
        this.addressBook = addressBook;
        return this;
    }

    /**
     * @return                          are the certificates being verified
     */
    boolean isVerifyCertificates() {
        return verifyCertificates;
    }

    /**
     * Assign the certificate status.
     *
     * @param verifyCertificates        should certificates be verified
     * @return {@code this}
     */
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
