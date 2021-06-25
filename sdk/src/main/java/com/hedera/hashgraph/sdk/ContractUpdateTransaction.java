package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

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
    private final ContractUpdateTransactionBody.Builder builder;

    ContractId contractId;
    AccountId proxyAccountId;
    FileId bytecodeFileId;

    public ContractUpdateTransaction() {
        builder = ContractUpdateTransactionBody.newBuilder();
    }

    ContractUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getContractUpdateInstance().toBuilder();

        if (builder.hasContractID()) {
            contractId = ContractId.fromProtobuf(builder.getContractID());
        }

        if (builder.hasProxyAccountID()) {
            proxyAccountId = AccountId.fromProtobuf(builder.getProxyAccountID());
        }

        if (builder.hasFileID()) {
            bytecodeFileId = FileId.fromProtobuf(builder.getFileID());
        }
    }

    ContractUpdateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getContractUpdateInstance().toBuilder();

        if (builder.hasContractID()) {
            contractId = ContractId.fromProtobuf(builder.getContractID());
        }

        if (builder.hasProxyAccountID()) {
            proxyAccountId = AccountId.fromProtobuf(builder.getProxyAccountID());
        }

        if (builder.hasFileID()) {
            bytecodeFileId = FileId.fromProtobuf(builder.getFileID());
        }
    }

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

    @Nullable
    public Instant getExpirationTime() {
        return builder.hasExpirationTime() ? InstantConverter.fromProtobuf(builder.getExpirationTime()) : null;
    }

    /**
     * Sets the expiration of the instance and its account to this time (
     * no effect if it already is this time or later).
     *
     * @param expirationTime The Instant to be set for expiration time
     * @return {@code this}
     */
    public ContractUpdateTransaction setExpirationTime(Instant expirationTime) {
        Objects.requireNonNull(expirationTime);
        requireNotFrozen();
        builder.setExpirationTime(InstantConverter.toProtobuf(expirationTime));
        return this;
    }

    @Nullable
    public Key getAdminKey() {
        return builder.hasAdminKey() ? Key.fromProtobufKey(builder.getAdminKey()) : null;
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
        builder.setAdminKey(adminKey.toProtobufKey());
        return this;
    }

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

    @Nullable
    public Duration getAutoRenewPeriod() {
        return builder.hasAutoRenewPeriod() ? DurationConverter.fromProtobuf(builder.getAutoRenewPeriod()) : null;
    }

    /**
     * Sets the auto renew period for this contract.
     *
     * @param autoRenewPeriod The Duration to be set for auto renewal
     * @return {@code this}
     */
    public ContractUpdateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        Objects.requireNonNull(autoRenewPeriod);
        requireNotFrozen();
        builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        return this;
    }

    @Nullable
    public FileId getBytecodeFileId() {
        return bytecodeFileId;
    }

    /**
     * Sets the file ID of file containing the smart contract byte code.
     * <p>
     * A copy will be made and held by the contract instance, and have the same expiration
     * time as the instance.
     *
     * @param byteCodeFileId The FileId to be set
     * @return {@code this}
     */
    public ContractUpdateTransaction setBytecodeFileId(FileId byteCodeFileId) {
        Objects.requireNonNull(byteCodeFileId);
        requireNotFrozen();
        this.bytecodeFileId = byteCodeFileId;
        return this;
    }

    public String getContractMemo() {
        return builder.getMemoWrapper().getValue();
    }

    /**
     * Sets the memo associated with the contract (max: 100 bytes).
     *
     * @param memo The memo to be set
     * @return {@code this}
     */
    public ContractUpdateTransaction setContractMemo(String memo) {
        requireNotFrozen();
        builder.setMemoWrapper(StringValue.of(memo));
        return this;
    }

    public ContractUpdateTransaction clearMemo() {
        requireNotFrozen();
        builder.clearMemoWrapper();
        return this;
    }

    ContractUpdateTransactionBody.Builder build() {
        if (contractId != null) {
            builder.setContractID(contractId.toProtobuf());
        }

        if (proxyAccountId != null) {
            builder.setProxyAccountID(proxyAccountId.toProtobuf());
        }

        if (bytecodeFileId != null) {
            builder.setFileID(bytecodeFileId.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateNetworkOnIds(Client client) {
        if (contractId != null) {
            contractId.validate(client);
        }

        if (bytecodeFileId != null) {
            bytecodeFileId.validate(client);
        }

        if (proxyAccountId != null) {
            proxyAccountId.validate(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return SmartContractServiceGrpc.getUpdateContractMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractUpdateInstance(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setContractUpdateInstance(build());
    }
}
