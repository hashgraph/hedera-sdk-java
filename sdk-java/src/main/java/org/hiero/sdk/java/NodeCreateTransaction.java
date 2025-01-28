// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.java;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.MethodDescriptor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import org.hiero.sdk.java.proto.AddressBookServiceGrpc;
import org.hiero.sdk.java.proto.NodeCreateTransactionBody;
import org.hiero.sdk.java.proto.SchedulableTransactionBody;
import org.hiero.sdk.java.proto.TransactionBody;
import org.hiero.sdk.java.proto.TransactionResponse;

/**
 * A transaction to create a new node in the network address book.
 * The transaction, once complete, enables a new consensus node
 * to join the network, and requires governing council authorization.
 * <p>
 * This transaction body SHALL be considered a "privileged transaction".
 * <p>
 *
 * - MUST be signed by the governing council.
 * - MUST be signed by the `Key` assigned to the
 *   `admin_key` field.
 * - The newly created node information SHALL be added to the network address
 *   book information in the network state.
 * - The new entry SHALL be created in "state" but SHALL NOT participate in
 *   network consensus and SHALL NOT be present in network "configuration"
 *   until the next "upgrade" transaction (as noted below).
 * - All new address book entries SHALL be added to the active network
 *   configuration during the next `freeze` transaction with the field
 *   `freeze_type` set to `PREPARE_UPGRADE`.
 *
 * ### Record Stream Effects
 * Upon completion the newly assigned `node_id` SHALL be in the transaction
 * receipt.
 */
public class NodeCreateTransaction extends Transaction<NodeCreateTransaction> {

    /**
     * A Node account identifier.
     * <p>
     * This account identifier MUST be in the "account number" form.<br/>
     * This account identifier MUST NOT use the alias field.<br/>
     * If the identified account does not exist, this transaction SHALL fail.<br/>
     * Multiple nodes MAY share the same node account.<br/>
     * This field is REQUIRED.
     */
    @Nullable
    private AccountId accountId = null;

    /**
     * A short description of the node.
     * <p>
     * This value, if set, MUST NOT exceed 100 bytes when encoded as UTF-8.<br/>
     * This field is OPTIONAL.
     */
    private String description = "";

    /**
     * A list of service endpoints for gossip.
     * <p>
     * These endpoints SHALL represent the published endpoints to which other
     * consensus nodes may _gossip_ transactions.<br/>
     * These endpoints MUST specify a port.<br/>
     * This list MUST NOT be empty.<br/>
     * This list MUST NOT contain more than `10` entries.<br/>
     * The first two entries in this list SHALL be the endpoints published to
     * all consensus nodes.<br/>
     * All other entries SHALL be reserved for future use.
     * <p>
     * Each network may have additional requirements for these endpoints.
     * A client MUST check network-specific documentation for those
     * details.<br/>
     * If the network configuration value `gossipFqdnRestricted` is set, then
     * all endpoints in this list MUST supply only IP address.<br/>
     * If the network configuration value `gossipFqdnRestricted` is _not_ set,
     * then endpoints in this list MAY supply either IP address or FQDN, but
     * MUST NOT supply both values for the same endpoint.
     */
    private List<Endpoint> gossipEndpoints = new ArrayList<>();

    /**
     * A list of service endpoints for gRPC calls.
     * <p>
     * These endpoints SHALL represent the published gRPC endpoints to which
     * clients may submit transactions.<br/>
     * These endpoints MUST specify a port.<br/>
     * Endpoints in this list MAY supply either IP address or FQDN, but MUST
     * NOT supply both values for the same endpoint.<br/>
     * This list MUST NOT be empty.<br/>
     * This list MUST NOT contain more than `8` entries.
     */
    private List<Endpoint> serviceEndpoints = new ArrayList<>();

    /**
     * A certificate used to sign gossip events.
     * <p>
     * This value MUST be a certificate of a type permitted for gossip
     * signatures.<br/>
     * This value MUST be the DER encoding of the certificate presented.<br/>
     * This field is REQUIRED and MUST NOT be empty.
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
     * This field is OPTIONAL.
     */
    @Nullable
    private byte[] grpcCertificateHash = null;

    /**
     * An administrative key controlled by the node operator.
     * <p>
     * This key MUST sign this transaction.<br/>
     * This key MUST sign each transaction to update this node.<br/>
     * This field MUST contain a valid `Key` value.<br/>
     * This field is REQUIRED and MUST NOT be set to an empty `KeyList`.
     */
    @Nullable
    private Key adminKey = null;

    /**
     * Constructor.
     */
    public NodeCreateTransaction() {}

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction) records
     * @throws InvalidProtocolBufferException when there is an issue with the protobuf
     */
    NodeCreateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, org.hiero.sdk.java.proto.Transaction>> txs)
            throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    NodeCreateTransaction(org.hiero.sdk.java.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
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
    public NodeCreateTransaction setAccountId(AccountId accountId) {
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    /**
     * Extract the description of the node.
     * @return the node's description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the node.
     * @param description The String to be set as the description of the node.
     * @return {@code this}
     */
    public NodeCreateTransaction setDescription(String description) {
        requireNotFrozen();
        this.description = description;
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
    public NodeCreateTransaction setGossipEndpoints(List<Endpoint> gossipEndpoints) {
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
    public NodeCreateTransaction addGossipEndpoint(Endpoint gossipEndpoint) {
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
    public NodeCreateTransaction setServiceEndpoints(List<Endpoint> serviceEndpoints) {
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
    public NodeCreateTransaction addServiceEndpoint(Endpoint serviceEndpoint) {
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
    public NodeCreateTransaction setGossipCaCertificate(byte[] gossipCaCertificate) {
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
    public NodeCreateTransaction setGrpcCertificateHash(byte[] grpcCertificateHash) {
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
    public NodeCreateTransaction setAdminKey(Key adminKey) {
        requireNotFrozen();
        this.adminKey = adminKey;
        return this;
    }

    /**
     * Build the transaction body.
     *
     * @return {@link org.hiero.sdk.java.proto.NodeCreateTransactionBody}
     */
    NodeCreateTransactionBody.Builder build() {
        var builder = NodeCreateTransactionBody.newBuilder();

        if (accountId != null) {
            builder.setAccountId(accountId.toProtobuf());
        }

        builder.setDescription(description);

        for (Endpoint gossipEndpoint : gossipEndpoints) {
            builder.addGossipEndpoint(gossipEndpoint.toProtobuf());
        }

        for (Endpoint serviceEndpoint : serviceEndpoints) {
            builder.addServiceEndpoint(serviceEndpoint.toProtobuf());
        }

        if (gossipCaCertificate != null) {
            builder.setGossipCaCertificate(ByteString.copyFrom(gossipCaCertificate));
        }

        if (grpcCertificateHash != null) {
            builder.setGrpcCertificateHash(ByteString.copyFrom(grpcCertificateHash));
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
        var body = sourceTransactionBody.getNodeCreate();

        if (body.hasAccountId()) {
            accountId = AccountId.fromProtobuf(body.getAccountId());
        }

        description = body.getDescription();

        for (var gossipEndpoint : body.getGossipEndpointList()) {
            gossipEndpoints.add(Endpoint.fromProtobuf(gossipEndpoint));
        }

        for (var serviceEndpoint : body.getServiceEndpointList()) {
            serviceEndpoints.add(Endpoint.fromProtobuf(serviceEndpoint));
        }

        var protobufGossipCert = body.getGossipCaCertificate();
        gossipCaCertificate = protobufGossipCert.equals(ByteString.empty()) ? null : protobufGossipCert.toByteArray();

        var protobufGrpcCert = body.getGrpcCertificateHash();
        grpcCertificateHash = protobufGrpcCert.equals(ByteString.empty()) ? null : protobufGrpcCert.toByteArray();

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
    MethodDescriptor<org.hiero.sdk.java.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return AddressBookServiceGrpc.getCreateNodeMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setNodeCreate(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setNodeCreate(build());
    }
}
