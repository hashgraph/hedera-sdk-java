// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import org.hiero.sdk.proto.AddressBookServiceGrpc;
import org.hiero.sdk.proto.NodeUpdateTransactionBody;
import org.hiero.sdk.proto.SchedulableTransactionBody;
import org.hiero.sdk.proto.TransactionBody;
import org.hiero.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * A transaction to modify address book node attributes.
 *
 * - This transaction SHALL enable the node operator, as identified by the
 *   `admin_key`, to modify operational attributes of the node.
 * - This transaction MUST be signed by the active `admin_key` for the node.
 * - If this transaction sets a new value for the `admin_key`, then both the
 *   current `admin_key`, and the new `admin_key` MUST sign this transaction.
 * - This transaction SHALL NOT change any field that is not set (is null) in
 *   this transaction body.
 * - This SHALL create a pending update to the node, but the change SHALL NOT
 *   be immediately applied to the active configuration.
 * - All pending node updates SHALL be applied to the active network
 *   configuration during the next `freeze` transaction with the field
 *   `freeze_type` set to `PREPARE_UPGRADE`.
 *
 * ### Record Stream Effects
 * Upon completion the `node_id` for the updated entry SHALL be in the
 * transaction receipt.
 */
public class NodeUpdateTransaction extends Transaction<NodeUpdateTransaction> {

    /**
     * A consensus node identifier in the network state.
     * <p>
     * The node identified MUST exist in the network address book.<br/>
     * The node identified MUST NOT be deleted.<br/>
     * This value is REQUIRED.
     */
    private long nodeId = 0;

    /**
     * An account identifier.
     * <p>
     * If set, this SHALL replace the node account identifier.<br/>
     * If set, this transaction MUST be signed by the active `key` for _both_
     * the current node account _and_ the identified new node account.
     */
    @Nullable
    private AccountId accountId = null;

    /**
     * A short description of the node.
     * <p>
     * This value, if set, MUST NOT exceed 100 bytes when encoded as UTF-8.<br/>
     * If set, this value SHALL replace the previous value.
     */
    @Nullable
    private String description = null;

    /**
     * A list of service endpoints for gossip.
     * <p>
     * If set, this list MUST meet the following requirements.
     * <hr/>
     * These endpoints SHALL represent the published endpoints to which other
     * consensus nodes may _gossip_ transactions.<br/>
     * These endpoints SHOULD NOT specify both address and DNS name.<br/>
     * This list MUST NOT be empty.<br/>
     * This list MUST NOT contain more than `10` entries.<br/>
     * The first two entries in this list SHALL be the endpoints published to
     * all consensus nodes.<br/>
     * All other entries SHALL be reserved for future use.
     * <p>
     * Each network may have additional requirements for these endpoints.
     * A client MUST check network-specific documentation for those
     * details.<br/>
     * <blockquote>Example<blockquote>
     * Hedera Mainnet _requires_ that address be specified, and does not
     * permit DNS name (FQDN) to be specified.<br/>
     * Mainnet also requires that the first entry be an "internal" IP
     * address and the second entry be an "external" IP address.
     * </blockquote>
     * <blockquote>
     * Solo, however, _requires_ DNS name (FQDN) but also permits
     * address.
     * </blockquote></blockquote>
     * <p>
     * If set, the new list SHALL replace the existing list.
     */
    private List<Endpoint> gossipEndpoints = new ArrayList<>();

    /**
     * A list of service endpoints for gRPC calls.
     * <p>
     * If set, this list MUST meet the following requirements.
     * <hr/>
     * These endpoints SHALL represent the published endpoints to which clients
     * may submit transactions.<br/>
     * These endpoints SHOULD specify address and port.<br/>
     * These endpoints MAY specify a DNS name.<br/>
     * These endpoints SHOULD NOT specify both address and DNS name.<br/>
     * This list MUST NOT be empty.<br/>
     * This list MUST NOT contain more than `8` entries.
     * <p>
     * Each network may have additional requirements for these endpoints.
     * A client MUST check network-specific documentation for those
     * details.
     * <p>
     * If set, the new list SHALL replace the existing list.
     */
    private List<Endpoint> serviceEndpoints = new ArrayList<>();

    /**
     * A certificate used to sign gossip events.
     * <p>
     * This value MUST be a certificate of a type permitted for gossip
     * signatures.<br/>
     * This value MUST be the DER encoding of the certificate presented.
     * <p>
     * If set, the new value SHALL replace the existing bytes value.
     */
    @Nullable
    private byte[] gossipCaCertificate = null;

    /**
     * A hash of the node gRPC TLS certificate.
     * <p>
     * This value MAY be used to verify the certificate presented by the node
     * during TLS negotiation for gRPC.<br/>
     * This value MUST be a SHA-384 hash.<br/>
     * The TLS certificate to be hashed MUST first be in PEM format and MUST be
     * encoded with UTF-8 NFKD encoding to a stream of bytes provided to
     * the hash algorithm.<br/>
     * <p>
     * If set, the new value SHALL replace the existing hash value.
     */
    @Nullable
    private byte[] grpcCertificateHash = null;

    /**
     * An administrative key controlled by the node operator.
     * <p>
     * This field is OPTIONAL.<br/>
     * If set, this key MUST sign this transaction.<br/>
     * If set, this key MUST sign each subsequent transaction to
     * update this node.<br/>
     * If set, this field MUST contain a valid `Key` value.<br/>
     * If set, this field MUST NOT be set to an empty `KeyList`.
     */
    @Nullable
    private Key adminKey = null;

    /**
     * Constructor.
     */
    public NodeUpdateTransaction() {}

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction) records
     * @throws InvalidProtocolBufferException when there is an issue with the protobuf
     */
    NodeUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, org.hiero.sdk.proto.Transaction>> txs)
            throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    NodeUpdateTransaction(org.hiero.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Extract the consensus node identifier in the network state.
     * @return the consensus node identifier in the network state.
     */
    public long getNodeId() {
        return nodeId;
    }

    /**
     * Assign the consensus node identifier in the network state.
     * @param nodeId the consensus node identifier in the network state.
     * @return {@code this}
     */
    public NodeUpdateTransaction setNodeId(long nodeId) {
        requireNotFrozen();
        this.nodeId = nodeId;
        return this;
    }

    /**
     * Extract the Account ID of the Node.
     * @return the Account ID of the Node.
     */
    public AccountId getAccountId() {
        return accountId;
    }

    /**
     * Assign the Account ID of the Node.
     * @param accountId the Account ID of the Node.
     * @return {@code this}
     */
    public NodeUpdateTransaction setAccountId(AccountId accountId) {
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    /**
     * Extract the description of the node.
     * @return the node's description.
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the node.
     * @param description The String to be set as the description of the node.
     * @return {@code this}
     */
    public NodeUpdateTransaction setDescription(String description) {
        requireNotFrozen();
        this.description = description;
        return this;
    }

    /**
     * Remove the description contents.
     * @return {@code this}
     */
    public NodeUpdateTransaction clearDescription() {
        requireNotFrozen();
        description = "";
        return this;
    }

    /**
     * Extract the list of service endpoints for gossip.
     * @return the list of service endpoints for gossip.
     */
    public List<Endpoint> getGossipEndpoints() {
        return gossipEndpoints;
    }

    /**
     * Assign the list of service endpoints for gossip.
     * @param gossipEndpoints the list of service endpoints for gossip.
     * @return {@code this}
     */
    public NodeUpdateTransaction setGossipEndpoints(List<Endpoint> gossipEndpoints) {
        requireNotFrozen();
        Objects.requireNonNull(gossipEndpoints);
        this.gossipEndpoints = new ArrayList<>(gossipEndpoints);
        return this;
    }

    /**
     * Add an endpoint for gossip to the list of service endpoints for gossip.
     * @param gossipEndpoint endpoints for gossip to add.
     * @return {@code this}
     */
    public NodeUpdateTransaction addGossipEndpoint(Endpoint gossipEndpoint) {
        requireNotFrozen();
        gossipEndpoints.add(gossipEndpoint);
        return this;
    }

    /**
     * Extract the list of service endpoints for gRPC calls.
     * @return the list of service endpoints for gRPC calls.
     */
    public List<Endpoint> getServiceEndpoints() {
        return serviceEndpoints;
    }

    /**
     * Assign the list of service endpoints for gRPC calls.
     * @param serviceEndpoints list of service endpoints for gRPC calls.
     * @return {@code this}
     */
    public NodeUpdateTransaction setServiceEndpoints(List<Endpoint> serviceEndpoints) {
        requireNotFrozen();
        Objects.requireNonNull(serviceEndpoints);
        this.serviceEndpoints = new ArrayList<>(serviceEndpoints);
        return this;
    }

    /**
     * Add an endpoint for gRPC calls to the list of service endpoints for gRPC calls.
     * @param serviceEndpoint endpoints for gRPC calls to add.
     * @return {@code this}
     */
    public NodeUpdateTransaction addServiceEndpoint(Endpoint serviceEndpoint) {
        requireNotFrozen();
        serviceEndpoints.add(serviceEndpoint);
        return this;
    }

    /**
     * Extract the certificate used to sign gossip events.
     * @return the DER encoding of the certificate presented.
     */
    @Nullable
    public byte[] getGossipCaCertificate() {
        return gossipCaCertificate;
    }

    /**
     * Sets the certificate used to sign gossip events.
     * <br>
     * This value MUST be the DER encoding of the certificate presented.
     * @param gossipCaCertificate the DER encoding of the certificate presented.
     * @return {@code this}
     */
    public NodeUpdateTransaction setGossipCaCertificate(byte[] gossipCaCertificate) {
        requireNotFrozen();
        this.gossipCaCertificate = gossipCaCertificate;
        return this;
    }

    /**
     * Extract the hash of the node gRPC TLS certificate.
     * @return SHA-384 hash of the node gRPC TLS certificate.
     */
    @Nullable
    public byte[] getGrpcCertificateHash() {
        return grpcCertificateHash;
    }

    /**
     * Sets the hash of the node gRPC TLS certificate.
     * <br>
     * This value MUST be a SHA-384 hash.
     * @param grpcCertificateHash SHA-384 hash of the node gRPC TLS certificate.
     * @return {@code this}
     */
    public NodeUpdateTransaction setGrpcCertificateHash(byte[] grpcCertificateHash) {
        requireNotFrozen();
        this.grpcCertificateHash = grpcCertificateHash;
        return this;
    }

    /**
     * Get an administrative key controlled by the node operator.
     * @return an administrative key controlled by the node operator.
     */
    @Nullable
    public Key getAdminKey() {
        return adminKey;
    }

    /**
     * Sets an administrative key controlled by the node operator.
     * @param adminKey an administrative key to be set.
     * @return {@code this}
     */
    public NodeUpdateTransaction setAdminKey(Key adminKey) {
        requireNotFrozen();
        this.adminKey = adminKey;
        return this;
    }

    /**
     * Build the transaction body.
     *
     * @return {@link org.hiero.sdk.proto.NodeUpdateTransactionBody}
     */
    NodeUpdateTransactionBody.Builder build() {
        var builder = NodeUpdateTransactionBody.newBuilder();

        builder.setNodeId(nodeId);

        if (accountId != null) {
            builder.setAccountId(accountId.toProtobuf());
        }

        if (description != null) {
            builder.setDescription(StringValue.of(description));
        }

        for (Endpoint gossipEndpoint : gossipEndpoints) {
            builder.addGossipEndpoint(gossipEndpoint.toProtobuf());
        }

        for (Endpoint serviceEndpoint : serviceEndpoints) {
            builder.addServiceEndpoint(serviceEndpoint.toProtobuf());
        }

        if (gossipCaCertificate != null) {
            builder.setGossipCaCertificate(BytesValue.of(ByteString.copyFrom(gossipCaCertificate)));
        }

        if (grpcCertificateHash != null) {
            builder.setGrpcCertificateHash(BytesValue.of(ByteString.copyFrom(grpcCertificateHash)));
        }

        if (adminKey != null) {
            builder.setAdminKey(adminKey.toProtobufKey());
        }

        return builder;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getNodeUpdate();

        nodeId = body.getNodeId();

        if (body.hasAccountId()) {
            accountId = AccountId.fromProtobuf(body.getAccountId());
        }

        if (body.hasDescription()) {
            description = body.getDescription().getValue();
        }

        for (var gossipEndpoint : body.getGossipEndpointList()) {
            gossipEndpoints.add(Endpoint.fromProtobuf(gossipEndpoint));
        }

        for (var serviceEndpoint : body.getServiceEndpointList()) {
            serviceEndpoints.add(Endpoint.fromProtobuf(serviceEndpoint));
        }

        if (body.hasGossipCaCertificate()) {
            gossipCaCertificate = body.getGossipCaCertificate().getValue().toByteArray();
        }

        if (body.hasGrpcCertificateHash()) {
            grpcCertificateHash = body.getGrpcCertificateHash().getValue().toByteArray();
        }

        if (body.hasAdminKey()) {
            adminKey = Key.fromProtobufKey(body.getAdminKey());
        }
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (accountId != null) {
            accountId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<org.hiero.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return AddressBookServiceGrpc.getUpdateNodeMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setNodeUpdate(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setNodeUpdate(build());
    }
}
