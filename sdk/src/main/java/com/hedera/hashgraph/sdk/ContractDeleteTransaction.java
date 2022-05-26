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

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ContractDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Marks a contract as deleted, moving all its current hbars to another account.
 */
public final class ContractDeleteTransaction extends Transaction<ContractDeleteTransaction> {

    @Nullable
    private ContractId contractId = null;
    @Nullable
    private ContractId transferContractId = null;
    @Nullable
    private AccountId transferAccountId = null;

    /**
     * Constructor.
     */
    public ContractDeleteTransaction() {
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    ContractDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    ContractDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Extract the contract id.
     *
     * @return                          the contract id
     */
    @Nullable
    public ContractId getContractId() {
        return contractId;
    }

    /**
     * Sets the contract ID which should be deleted.
     *
     * @param contractId The ContractId to be set
     * @return {@code this}
     */
    public ContractDeleteTransaction setContractId(ContractId contractId) {
        Objects.requireNonNull(contractId);
        requireNotFrozen();
        this.contractId = contractId;
        return this;
    }

    /**
     * Extract the transfer account id.
     *
     * @return                          the account id that will receive the remaining hbars
     */
    @Nullable
    public AccountId getTransferAccountId() {
        return transferAccountId;
    }

    /**
     * Sets the account ID which will receive all remaining hbars.
     * <p>
     * This is mutually exclusive with {@link #setTransferContractId(ContractId)}.
     *
     * @param transferAccountId The AccountId to be set
     * @return {@code this}
     */
    public ContractDeleteTransaction setTransferAccountId(AccountId transferAccountId) {
        Objects.requireNonNull(transferAccountId);
        requireNotFrozen();
        this.transferAccountId = transferAccountId;
        return this;
    }

    /**
     * Extract the transfer contract id.
     *
     * @return                          the contract id that will receive the remaining hbars
     */
    @Nullable
    public ContractId getTransferContractId() {
        return transferContractId;
    }

    /**
     * Sets the contract ID which will receive all remaining hbars.
     * <p>
     * This is mutually exclusive with {@link #setTransferAccountId(AccountId)}.
     *
     * @param transferContractId The ContractId to be set
     * @return {@code this}
     */
    public ContractDeleteTransaction setTransferContractId(ContractId transferContractId) {
        Objects.requireNonNull(transferContractId);
        requireNotFrozen();
        this.transferContractId = transferContractId;
        return this;
    }

    /**
     * Validates tha the contract id, transfer contract id and the transfer account id are valid.
     *
     * @param client                    the configured client
     * @throws BadEntityIdException     if entity ID is formatted poorly
     */
    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (contractId != null) {
            contractId.validateChecksum(client);
        }

        if (transferContractId != null) {
            transferContractId.validateChecksum(client);
        }

        if (transferAccountId != null) {
            transferAccountId.validateChecksum(client);
        }
    }


    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return SmartContractServiceGrpc.getDeleteContractMethod();
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getContractDeleteInstance();
        if (body.hasContractID()) {
            contractId = ContractId.fromProtobuf(body.getContractID());
        }
        if (body.hasTransferContractID()) {
            transferContractId = ContractId.fromProtobuf(body.getTransferContractID());
        }
        if (body.hasTransferAccountID()) {
            transferAccountId = AccountId.fromProtobuf(body.getTransferAccountID());
        }
    }

    /**
     * Build the transaction body.
     *
     * @return {@link ContractDeleteTransactionBody}
     */
    ContractDeleteTransactionBody.Builder build() {
        var builder = ContractDeleteTransactionBody.newBuilder();
        if (contractId != null) {
            builder.setContractID(contractId.toProtobuf());
        }

        if (transferAccountId != null) {
            builder.setTransferAccountID(transferAccountId.toProtobuf());
        }

        if (transferContractId != null) {
            builder.setTransferContractID(transferContractId.toProtobuf());
        }

        return builder;
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractDeleteInstance(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setContractDeleteInstance(build());
    }
}
