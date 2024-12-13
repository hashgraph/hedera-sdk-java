/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2024 Hedera Hashgraph, LLC
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
package com.hiero.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hiero.sdk.proto.AddressBookServiceGrpc;
import com.hiero.sdk.proto.NodeDeleteTransactionBody;
import com.hiero.sdk.proto.SchedulableTransactionBody;
import com.hiero.sdk.proto.TransactionBody;
import com.hiero.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java.util.LinkedHashMap;

/**
 * A transaction to delete a node from the network address book.
 *
 * This transaction body SHALL be considered a "privileged transaction".
 *
 * - A transaction MUST be signed by the governing council.
 * - Upon success, the address book entry SHALL enter a "pending delete"
 *   state.
 * - All address book entries pending deletion SHALL be removed from the
 *   active network configuration during the next `freeze` transaction with
 *   the field `freeze_type` set to `PREPARE_UPGRADE`.<br/>
 * - A deleted address book node SHALL be removed entirely from network state.
 * - A deleted address book node identifier SHALL NOT be reused.
 *
 * ### Record Stream Effects
 * Upon completion the "deleted" `node_id` SHALL be in the transaction
 * receipt.
 */
public class NodeDeleteTransaction extends Transaction<NodeDeleteTransaction> {

    /**
     * A consensus node identifier in the network state.
     * <p>
     * The node identified MUST exist in the network address book.<br/>
     * The node identified MUST NOT be deleted.<br/>
     * This value is REQUIRED.
     */
    private long nodeId = 0;

    /**
     * Constructor.
     */
    public NodeDeleteTransaction() {}

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction) records
     * @throws InvalidProtocolBufferException when there is an issue with the protobuf
     */
    NodeDeleteTransaction(
        LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hiero.sdk.proto.Transaction>> txs)
        throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    NodeDeleteTransaction(TransactionBody txBody) {
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
    public NodeDeleteTransaction setNodeId(long nodeId) {
        requireNotFrozen();
        this.nodeId = nodeId;
        return this;
    }

    /**
     * Build the transaction body.
     *
     * @return {@link com.hiero.sdk.proto.NodeDeleteTransactionBody}
     */
    NodeDeleteTransactionBody.Builder build() {
        var builder = NodeDeleteTransactionBody.newBuilder();
        builder.setNodeId(nodeId);
        return builder;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getNodeDelete();
        nodeId = body.getNodeId();
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        // no-op
    }

    @Override
    MethodDescriptor<com.hiero.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return AddressBookServiceGrpc.getDeleteNodeMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setNodeDelete(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setNodeDelete(build());
    }
}
