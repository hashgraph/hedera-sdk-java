package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ContractUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

/**
 * Modify a smart contract instance to have the given parameter values.
 *
 * Any null field is ignored (left unchanged).
 *
 * If only the contractInstanceExpirationTime is being modified, then no signature is
 * needed on this transaction other than for the account paying for the transaction itself.
 *
 * But if any of the other fields are being modified, then it must be signed by the adminKey.
 *
 * The use of adminKey is not currently supported in this API, but in the future will
 * be implemented to allow these fields to be modified, and also to make modifications
 * to the state of the instance.
 *
 * If the contract is created with no admin key, then none of the fields can be
 * changed that need an admin signature, and therefore no admin key can ever be added.
 * So if there is no admin key, then things like the bytecode are immutable.
 * But if there is an admin key, then they can be changed. For example, the
 * admin key might be a threshold key, which requires 3 of 5 binding arbitration judges to
 * agree before the bytecode can be changed. This can be used to add flexibility to the management
 * of smart contract behavior. But this is optional. If the smart contract is created
 * without an admin key, then such a key can never be added, and its bytecode will be immutable.
 */
public final class ContractUpdateTransaction extends SingleTransactionBuilder<ContractUpdateTransaction> {
    private final ContractUpdateTransactionBody.Builder builder;

    public ContractUpdateTransaction() {
        builder = ContractUpdateTransactionBody.newBuilder();
    }

    /**
     * Sets the Contract ID instance to update.
     *
     * @return {@code this}
     * @param contractId The ContractId to be set
     */
    public ContractUpdateTransaction setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    /**
     * Sets the expiration of the instance and its account to this time (
     * no effect if it already is this time or later).
     *
     * @return {@code this}
     * @param expirationTime The Instant to be set for expiration time
     */
    public ContractUpdateTransaction setExpirationTime(Instant expirationTime) {
        builder.setExpirationTime(InstantConverter.toProtobuf(expirationTime));
        return this;
    }

    /**
     * Sets a new admin key for this contract.
     *
     * @return {@code this}
     * @param adminKey The Key to be set
     */
    public ContractUpdateTransaction setAdminKey(Key adminKey) {
        builder.setAdminKey(adminKey.toKeyProtobuf());
        return this;
    }

    /**
     * Sets the ID of the account to which this account is proxy staked.
     *
     * If proxyAccountID is null, or is an invalid account, or is an account
     * that isn't a node, then this account is automatically proxy staked to a
     * node chosen by the network, but without earning payments.
     *
     * If the proxyAccountID account refuses to accept proxy staking, or if it is
     * not currently running a node, then it will behave as if proxyAccountID was null.
     *
     * @return {@code this}
     * @param proxyAccountId The AccountId to be set
     */
    public ContractUpdateTransaction setProxyAccountId(AccountId proxyAccountId) {
        builder.setProxyAccountID(proxyAccountId.toProtobuf());
        return this;
    }

    /**
     * Sets the auto renew period for this contract.
     *
     * @return {@code this}
     * @param autoRenewPeriod The Duration to be set for auto renewal
     */
    public ContractUpdateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        return this;
    }

    /**
     * Sets the file ID of file containing the smart contract byte code.
     *
     * A copy will be made and held by the contract instance, and have the same expiration
     * time as the instance.
     *
     * @return {@code this}
     * @param byteCodeFileId The FileId to be set
     */
    public ContractUpdateTransaction setBytecodeFileId(FileId byteCodeFileId) {
        builder.setFileID(byteCodeFileId.toProtobuf());
        return this;
    }

    /**
     * Sets the memo associated with the contract (max: 100 bytes).
     *
     * @return {@code this}
     * @param memo The memo to be set
     */
    public ContractUpdateTransaction setContractMemo(String memo) {
        builder.setMemo(memo);
        return this;
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractUpdateInstance(builder);
    }
}
