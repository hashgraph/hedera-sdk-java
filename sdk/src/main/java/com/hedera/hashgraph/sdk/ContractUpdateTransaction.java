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

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.proto.ContractUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.grpc.MethodDescriptor;
import org.checkerframework.checker.units.qual.A;
import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Modify a smart contract instance to have the given parameter values.
 * <p>
 * Any null field is ignored (left unchanged).
 * <p>
 * If only the contractInstanceExpirationTime is being modified, then no signature is
 * needed on this transaction other than for the account paying for the transaction itself.
 * <p>
 * But if any of the other fields are being modified, then it must be signed by the adminKey.
 * <p>
 * The use of adminKey is not currently supported in this API, but in the future will
 * be implemented to allow these fields to be modified, and also to make modifications
 * to the state of the instance.
 * <p>
 * If the contract is created with no admin key, then none of the fields can be
 * changed that need an admin signature, and therefore no admin key can ever be added.
 * So if there is no admin key, then things like the bytecode are immutable.
 * But if there is an admin key, then they can be changed. For example, the
 * admin key might be a threshold key, which requires 3 of 5 binding arbitration judges to
 * agree before the bytecode can be changed. This can be used to add flexibility to the management
 * of smart contract behavior. But this is optional. If the smart contract is created
 * without an admin key, then such a key can never be added, and its bytecode will be immutable.
 */
public final class ContractUpdateTransaction extends Transaction<ContractUpdateTransaction> {
    @Nullable
    private ContractId contractId = null;
    @Nullable
    private AccountId proxyAccountId = null;
    @Nullable
    private FileId bytecodeFileId = null;
    @Nullable
    private Instant expirationTime = null;
    @Nullable
    private Key adminKey = null;
    @Nullable
    private Integer maxAutomaticTokenAssociations = null;
    @Nullable
    private Duration autoRenewPeriod = null;
    @Nullable
    private String contractMemo = null;

    @Nullable
    private AccountId stakedAccountId = null;

    @Nullable
    private Long stakedNodeId = null;

    @Nullable
    private Boolean declineStakingReward = null;

    @Nullable
    private AccountId autoRenewAccountId = null;

    /**
     * Contract.
     */
    public ContractUpdateTransaction() {
    }

    /**
     * Contract.
     *
     * @param txs                       Compound list of transaction id's list of (AccountId, Transaction) record
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    ContractUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }
    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    ContractUpdateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
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
     * Sets the Contract ID instance to update.
     *
     * @param contractId The ContractId to be set
     * @return {@code this}
     */
    public ContractUpdateTransaction setContractId(ContractId contractId) {
        Objects.requireNonNull(contractId);
        requireNotFrozen();
        this.contractId = contractId;
        return this;
    }

    /**
     * Extract the contract expiration time.
     *
     * @return                          the contract expiration time
     */
    @Nullable
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "An instant can't actually be mutated"
    )
    public Instant getExpirationTime() {
        return expirationTime;
    }

    /**
     * Sets the expiration of the instance and its account to this time (
     * no effect if it already is this time or later).
     *
     * @param expirationTime The Instant to be set for expiration time
     * @return {@code this}
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "An Instant can't actually be mutated"
    )
    public ContractUpdateTransaction setExpirationTime(Instant expirationTime) {
        Objects.requireNonNull(expirationTime);
        requireNotFrozen();
        this.expirationTime = expirationTime;
        return this;
    }

    /**
     * Extract the administrator key.
     *
     * @return                          the administrator key
     */
    @Nullable
    public Key getAdminKey() {
        return adminKey;
    }

    /**
     * Sets a new admin key for this contract.
     *
     * @param adminKey The Key to be set
     * @return {@code this}
     */
    public ContractUpdateTransaction setAdminKey(Key adminKey) {
        Objects.requireNonNull(adminKey);
        requireNotFrozen();
        this.adminKey = adminKey;
        return this;
    }

    /**
     * Extract the proxy account id.
     *
     * @return                          the proxy account id
     */
    @Nullable
    public AccountId getProxyAccountId() {
        return proxyAccountId;
    }

    /**
     * Sets the ID of the account to which this account is proxy staked.
     * <p>
     * If proxyAccountID is null, or is an invalid account, or is an account
     * that isn't a node, then this account is automatically proxy staked to a
     * node chosen by the network, but without earning payments.
     * <p>
     * If the proxyAccountID account refuses to accept proxy staking, or if it is
     * not currently running a node, then it will behave as if proxyAccountID was null.
     *
     * @param proxyAccountId The AccountId to be set
     * @return {@code this}
     */
    public ContractUpdateTransaction setProxyAccountId(AccountId proxyAccountId) {
        Objects.requireNonNull(proxyAccountId);
        requireNotFrozen();
        this.proxyAccountId = proxyAccountId;
        return this;
    }

    /**
     * Extract the auto renew period.
     *
     * @return                          the auto renew period
     */
    @Nullable
    public Integer getMaxAutomaticTokenAssociations() {
        return maxAutomaticTokenAssociations;
    }

    /**
     * Sets the new maximum number of tokens that this contract can be
     * automatically associated with (i.e., receive air-drops from).
     *
     * @param maxAutomaticTokenAssociations The maximum automatic token associations
     * @return  {@code this}
     */

    public ContractUpdateTransaction setMaxAutomaticTokenAssociations(int maxAutomaticTokenAssociations) {
        requireNotFrozen();
        this.maxAutomaticTokenAssociations = maxAutomaticTokenAssociations;
        return this;
    }

    /**
     * Extract the duration for the auto renew period.
     *
     * @return                          the duration for auto-renew
     */
    @Nullable
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "A Duration can't actually be mutated"
    )
    public Duration getAutoRenewPeriod() {
        return autoRenewPeriod;
    }

    /**
     * Sets the auto renew period for this contract.
     *
     * @param autoRenewPeriod The Duration to be set for auto renewal
     * @return {@code this}
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "A Duration can't actually be mutated"
    )
    public ContractUpdateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        Objects.requireNonNull(autoRenewPeriod);
        requireNotFrozen();
        this.autoRenewPeriod = autoRenewPeriod;
        return this;
    }

    /**
     * @deprecated with no replacement
     * @return the bytecodeFileId
     */
    @Nullable
    @Deprecated
    public FileId getBytecodeFileId() {
        return bytecodeFileId;
    }

    /**
     * @deprecated with no replacement
     *
     * Sets the file ID of file containing the smart contract byte code.
     * <p>
     * A copy will be made and held by the contract instance, and have the same expiration
     * time as the instance.
     *
     * @param bytecodeFileId The FileId to be set
     * @return {@code this}
     */
    @Deprecated
    public ContractUpdateTransaction setBytecodeFileId(FileId bytecodeFileId) {
        Objects.requireNonNull(bytecodeFileId);
        requireNotFrozen();
        this.bytecodeFileId = bytecodeFileId;
        return this;
    }

    /**
     * Extract the contents of the memo.
     *
     * @return                          the contents of the memo
     */
    @Nullable
    public String getContractMemo() {
        return contractMemo;
    }

    /**
     * Sets the memo associated with the contract (max: 100 bytes).
     *
     * @param memo The memo to be set
     * @return {@code this}
     */
    public ContractUpdateTransaction setContractMemo(String memo) {
        Objects.requireNonNull(memo);
        requireNotFrozen();
        contractMemo = memo;
        return this;
    }

    /**
     * Remove the memo contents.
     *
     * @return {@code this}
     */
    public ContractUpdateTransaction clearMemo() {
        requireNotFrozen();
        contractMemo = "";
        return this;
    }

    /**
     * ID of the account to which this contract will stake
     *
     * @return ID of the account to which this contract will stake.
     */
    @Nullable
    public AccountId getStakedAccountId() {
        return stakedAccountId;
    }

    /**
     * Set the account to which this contract will stake
     *
     * @param stakedAccountId ID of the account to which this contract will stake.
     * @return {@code this}
     */
    public ContractUpdateTransaction setStakedAccountId(@Nullable AccountId stakedAccountId) {
        requireNotFrozen();
        this.stakedAccountId = stakedAccountId;
        this.stakedNodeId = null;
        return this;
    }

    /**
     * Clear the staked account ID
     *
     * @return {@code this}
     */
    public ContractUpdateTransaction clearStakedAccountId() {
        requireNotFrozen();
        this.stakedAccountId = new AccountId(0);
        this.stakedNodeId = null;
        return this;
    }

    /**
     * The node to which this contract will stake
     *
     * @return ID of the node this contract will be staked to.
     */
    @Nullable
    public Long getStakedNodeId() {
        return stakedNodeId;
    }

    /**
     * Set the node to which this contract will stake
     *
     * @param stakedNodeId ID of the node this contract will be staked to.
     * @return {@code this}
     */
    public ContractUpdateTransaction setStakedNodeId(@Nullable Long stakedNodeId) {
        requireNotFrozen();
        this.stakedNodeId = stakedNodeId;
        this.stakedAccountId = null;
        return this;
    }

    /**
     * clear the staked node account ID
     *
     * @return {@code this}
     */
    public ContractUpdateTransaction clearStakedNodeId() {
        requireNotFrozen();
        this.stakedNodeId = -1L;
        this.stakedAccountId = null;
        return this;
    }

    /**
     * If true, the contract declines receiving a staking reward. The default value is false.
     *
     * @return If true, the contract declines receiving a staking reward. The default value is false.
     */
    @Nullable
    public Boolean getDeclineStakingReward() {
        return declineStakingReward;
    }

    /**
     * If true, the contract declines receiving a staking reward. The default value is false.
     *
     * @param declineStakingReward - If true, the contract declines receiving a staking reward. The default value is false.
     * @return {@code this}
     */
    public ContractUpdateTransaction setDeclineStakingReward(boolean declineStakingReward) {
        requireNotFrozen();
        this.declineStakingReward = declineStakingReward;
        return this;
    }

    /**
     * Clear decline staking reward
     *
     * @return {@code this}
     */
    public ContractUpdateTransaction clearDeclineStakingReward() {
        requireNotFrozen();
        this.declineStakingReward = null;
        return this;
    }

    /**
     * Get the auto renew accountId.
     *
     * @return                          the auto renew accountId
     */
    @Nullable
    public AccountId getAutoRenewAccountId() {
        return autoRenewAccountId;
    }

    /**
     * An account to charge for auto-renewal of this contract. If not set, or set to an
     * account with zero hbar balance, the contract's own hbar balance will be used to
     * cover auto-renewal fees.
     *
     * @param autoRenewAccountId The AccountId to be set for auto renewal
     * @return {@code this}
     */
    public ContractUpdateTransaction setAutoRenewAccountId(AccountId autoRenewAccountId) {
        Objects.requireNonNull(autoRenewAccountId);
        requireNotFrozen();
        this.autoRenewAccountId = autoRenewAccountId;
        return this;
    }

    /**
     * Clears the auto-renew account ID
     *
     * @return {@code this}
     */
    public ContractUpdateTransaction clearAutoRenewAccountId() {
        this.autoRenewAccountId = new AccountId(0);
        return this;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getContractUpdateInstance();
        if (body.hasContractID()) {
            contractId = ContractId.fromProtobuf(body.getContractID());
        }
        if (body.hasProxyAccountID()) {
            proxyAccountId = AccountId.fromProtobuf(body.getProxyAccountID());
        }
        if (body.hasExpirationTime()) {
            expirationTime = InstantConverter.fromProtobuf(body.getExpirationTime());
        }
        if (body.hasAdminKey()) {
            adminKey = Key.fromProtobufKey(body.getAdminKey());
        }
        if (body.hasMaxAutomaticTokenAssociations()) {
            maxAutomaticTokenAssociations = body.getMaxAutomaticTokenAssociations().getValue();
        }
        if (body.hasAutoRenewPeriod()) {
            autoRenewPeriod = DurationConverter.fromProtobuf(body.getAutoRenewPeriod());
        }
        if (body.hasMemoWrapper()) {
            contractMemo = body.getMemoWrapper().getValue();
        }

        if (body.hasDeclineReward()) {
            declineStakingReward = body.getDeclineReward().getValue();
        }

        if (body.hasStakedAccountId()) {
            stakedAccountId = AccountId.fromProtobuf(body.getStakedAccountId());
        }

        if (body.hasStakedNodeId()) {
            stakedNodeId = body.getStakedNodeId();
        }

        if (body.hasAutoRenewAccountId()) {
            autoRenewAccountId = AccountId.fromProtobuf(body.getAutoRenewAccountId());
        }
    }

    /**
     * Build the correct transaction body.
     *
     * @return {@link com.hedera.hashgraph.sdk.proto.ContractUpdateTransactionBody builder }
     */
    ContractUpdateTransactionBody.Builder build() {
        var builder = ContractUpdateTransactionBody.newBuilder();
        if (contractId != null) {
            builder.setContractID(contractId.toProtobuf());
        }
        if (proxyAccountId != null) {
            builder.setProxyAccountID(proxyAccountId.toProtobuf());
        }
        if (expirationTime != null) {
            builder.setExpirationTime(InstantConverter.toProtobuf(expirationTime));
        }
        if (adminKey != null) {
            builder.setAdminKey(adminKey.toProtobufKey());
        }
        if (maxAutomaticTokenAssociations != null) {
            builder.setMaxAutomaticTokenAssociations(Int32Value.of(maxAutomaticTokenAssociations));
        }
        if (autoRenewPeriod != null) {
            builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        }
        if (contractMemo != null) {
            builder.setMemoWrapper(StringValue.of(contractMemo));
        }

        if (stakedAccountId != null) {
            builder.setStakedAccountId(stakedAccountId.toProtobuf());
        }

        if (stakedNodeId != null) {
            builder.setStakedNodeId(stakedNodeId);
        }

        if (declineStakingReward != null) {
            builder.setDeclineReward(BoolValue.newBuilder().setValue(declineStakingReward).build());
        }

        if (autoRenewAccountId != null) {
            builder.setAutoRenewAccountId(autoRenewAccountId.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (contractId != null) {
            contractId.validateChecksum(client);
        }

        if (proxyAccountId != null) {
            proxyAccountId.validateChecksum(client);
        }

        if (stakedAccountId != null) {
            stakedAccountId.validateChecksum(client);
        }

        if (autoRenewAccountId != null) {
            autoRenewAccountId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return SmartContractServiceGrpc.getUpdateContractMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractUpdateInstance(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setContractUpdateInstance(build());
    }
}
