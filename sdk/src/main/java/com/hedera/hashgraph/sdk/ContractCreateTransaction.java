package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ContractCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Start a new smart contract instance.
 * After the instance is created,
 * the ContractID for it is in the receipt.
 * <p>
 * The instance will exist for autoRenewPeriod seconds. When that is reached, it will renew itself for another
 * autoRenewPeriod seconds by charging its associated cryptocurrency account (which it creates here).
 * If it has insufficient cryptocurrency to extend that long, it will extend as long as it can.
 * If its balance is zero, the instance will be deleted.
 * <p>
 * A smart contract instance normally enforces rules, so "the code is law". For example, an
 * ERC-20 contract prevents a transfer from being undone without a signature by the recipient of the transfer.
 * This is always enforced if the contract instance was created with the adminKeys being null.
 * But for some uses, it might be desirable to create something like an ERC-20 contract that has a
 * specific group of trusted individuals who can act as a "supreme court" with the ability to override the normal
 * operation, when a sufficient number of them agree to do so. If adminKeys is not null, then they can
 * sign a transaction that can change the state of the smart contract in arbitrary ways, such as to reverse
 * a transaction that violates some standard of behavior that is not covered by the code itself.
 * The admin keys can also be used to change the autoRenewPeriod, and change the adminKeys field itself.
 * The API currently does not implement this ability. But it does allow the adminKeys field to be set and
 * queried, and will in the future implement such admin abilities for any instance that has a non-null adminKeys.
 * <p>
 * If this constructor stores information, it is charged gas to store it. There is a fee in hbars to
 * maintain that storage until the expiration time, and that fee is added as part of the transaction fee.
 * <p>
 * An entity (account, file, or smart contract instance) must be created in a particular realm.
 * If the realmID is left null, then a new realm will be created with the given admin key. If a new realm has
 * a null adminKey, then anyone can create/modify/delete entities in that realm. But if an admin key is given,
 * then any transaction to create/modify/delete an entity in that realm must be signed by that key,
 * though anyone can still call functions on smart contract instances that exist in that realm.
 * A realm ceases to exist when everything within it has expired and no longer exists.
 * <p>
 * The current API ignores shardID, realmID, and newRealmAdminKey, and creates everything in shard 0 and realm 0,
 * with a null key. Future versions of the API will support multiple realms and multiple shards.
 * <p>
 * The optional memo field can contain a string whose length is up to 100 bytes. That is the size after Unicode
 * NFD then UTF-8 conversion. This field can be used to describe the smart contract. It could also be used for
 * other purposes. One recommended purpose is to hold a hexadecimal string that is the SHA-384 hash of a
 * PDF file containing a human-readable legal contract. Then, if the admin keys are the
 * public keys of human arbitrators, they can use that legal document to guide their decisions during a binding
 * arbitration tribunal, convened to consider any changes to the smart contract in the future. The memo field can only
 * be changed using the admin keys. If there are no admin keys, then it cannot be
 * changed after the smart contract is created.
 */
public final class ContractCreateTransaction extends Transaction<ContractCreateTransaction> {

    @Nullable
    private FileId bytecodeFileId = null;
    @Nullable
    private AccountId proxyAccountId = null;
    @Nullable
    private Key adminKey = null;
    private long gas = 0;
    private Hbar initialBalance = new Hbar(0);
    @Nullable
    private Duration autoRenewPeriod = null;
    private byte[] constructorParameters = {};
    private String contractMemo = "";

    public ContractCreateTransaction() {
        setAutoRenewPeriod(DEFAULT_AUTO_RENEW_PERIOD);
        defaultMaxTransactionFee = new Hbar(20);
    }

    ContractCreateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    ContractCreateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    @Nullable
    public FileId getBytecodeFileId() {
        return bytecodeFileId;
    }

    /**
     * Sets the file containing the smart contract byte code.
     * <p>
     * A copy will be made and held by the contract instance, and have the same expiration time as
     * the instance.
     * <p>
     * The file must be the ASCII hexadecimal representation of the smart contract bytecode.
     *
     * @param bytecodeFileId The FileId to be set
     * @return {@code this}
     */
    public ContractCreateTransaction setBytecodeFileId(FileId bytecodeFileId) {
        Objects.requireNonNull(bytecodeFileId);
        requireNotFrozen();
        this.bytecodeFileId = bytecodeFileId;
        return this;
    }

    @Nullable
    public Key getAdminKey() {
        return adminKey;
    }

    /**
     * Sets the state of the instance and its fields can be modified arbitrarily if this key signs a transaction
     * to modify it. If this is null, then such modifications are not possible, and there is no administrator
     * that can override the normal operation of this smart contract instance. Note that if it is created with no
     * admin keys, then there is no administrator to authorize changing the admin keys, so
     * there can never be any admin keys for that instance.
     *
     * @param adminKey The Key to be set
     * @return {@code this}
     */
    public ContractCreateTransaction setAdminKey(Key adminKey) {
        Objects.requireNonNull(adminKey);
        requireNotFrozen();
        this.adminKey = adminKey;
        return this;
    }

    public long getGas() {
        return gas;
    }

    /**
     * Sets the gas to run the constructor.
     *
     * @param gas The long to be set as gas
     * @return {@code this}
     */
    public ContractCreateTransaction setGas(long gas) {
        requireNotFrozen();
        this.gas = gas;
        return this;
    }

    public Hbar getInitialBalance() {
        return initialBalance;
    }

    /**
     * Sets the initial number of hbars to put into the cryptocurrency account
     * associated with and owned by the smart contract.
     *
     * @param initialBalance The Hbar to be set as the initial balance
     * @return {@code this}
     */
    public ContractCreateTransaction setInitialBalance(Hbar initialBalance) {
        Objects.requireNonNull(initialBalance);
        requireNotFrozen();
        this.initialBalance = initialBalance;
        return this;
    }

    @Nullable
    public AccountId getProxyAccountId() {
        return proxyAccountId;
    }

    /**
     * Sets the ID of the account to which this account is proxy staked.
     * <p>
     * If proxyAccountID is null, or is an invalid account, or is an account that isn't a node,
     * then this account is automatically proxy staked to a node chosen by the network, but without earning payments.
     * <p>
     * If the proxyAccountID account refuses to accept proxy staking , or if it is not currently running a node,
     * then it will behave as if  proxyAccountID was null.
     *
     * @param proxyAccountId The AccountId to be set
     * @return {@code this}
     */
    public ContractCreateTransaction setProxyAccountId(AccountId proxyAccountId) {
        Objects.requireNonNull(proxyAccountId);
        requireNotFrozen();
        this.proxyAccountId = proxyAccountId;
        return this;
    }

    @Nullable
    public Duration getAutoRenewPeriod() {
        return autoRenewPeriod;
    }

    /**
     * Sets the period that the instance will charge its account every this many seconds to renew.
     *
     * @param autoRenewPeriod The Duration to be set for auto renewal
     * @return {@code this}
     */
    public ContractCreateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        Objects.requireNonNull(autoRenewPeriod);
        requireNotFrozen();
        this.autoRenewPeriod = autoRenewPeriod;
        return this;
    }

    public ByteString getConstructorParameters() {
        return ByteString.copyFrom(constructorParameters);
    }

    /**
     * Sets the constructor parameters as their raw bytes.
     * <p>
     * Use this instead of {@link #setConstructorParameters(ContractFunctionParameters)} if you have already
     * pre-encoded a solidity function call.
     *
     * @param constructorParameters The constructor parameters
     * @return {@code this}
     */
    public ContractCreateTransaction setConstructorParameters(byte[] constructorParameters) {
        requireNotFrozen();
        this.constructorParameters = constructorParameters;
        return this;
    }

    /**
     * Sets the parameters to pass to the constructor.
     *
     * @param constructorParameters The contructor parameters
     * @return {@code this}
     */
    public ContractCreateTransaction setConstructorParameters(ContractFunctionParameters constructorParameters) {
        Objects.requireNonNull(constructorParameters);
        return setConstructorParameters(constructorParameters.toBytes(null).toByteArray());
    }

    public String getContractMemo() {
        return contractMemo;
    }

    /**
     * Sets the memo to be associated with this contract.
     *
     * @param memo The String to be set as the memo
     * @return {@code this}
     */
    public ContractCreateTransaction setContractMemo(String memo) {
        requireNotFrozen();
        Objects.requireNonNull(memo);
        contractMemo = memo;
        return this;
    }

    ContractCreateTransactionBody.Builder build() {
        var builder = ContractCreateTransactionBody.newBuilder();
        if (bytecodeFileId != null) {
            builder.setFileID(bytecodeFileId.toProtobuf());
        }
        if (proxyAccountId != null) {
            builder.setProxyAccountID(proxyAccountId.toProtobuf());
        }
        if(adminKey != null) {
            builder.setAdminKey(adminKey.toProtobufKey());
        }
        if(autoRenewPeriod != null) {
            builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        }
        builder.setGas(gas);
        builder.setInitialBalance(initialBalance.toTinybars());
        builder.setConstructorParameters(ByteString.copyFrom(constructorParameters));
        builder.setMemo(contractMemo);

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (bytecodeFileId != null) {
            bytecodeFileId.validateChecksum(client);
        }

        if (proxyAccountId != null) {
            proxyAccountId.validateChecksum(client);
        }
    }

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getContractCreateInstance();

        if(body.hasFileID()) {
            bytecodeFileId = FileId.fromProtobuf(body.getFileID());
        }
        if(body.hasProxyAccountID()) {
            proxyAccountId = AccountId.fromProtobuf(body.getProxyAccountID());
        }
        if(body.hasAdminKey()) {
            adminKey = Key.fromProtobufKey(body.getAdminKey());
        }
        if(body.hasAutoRenewPeriod()) {
            autoRenewPeriod = DurationConverter.fromProtobuf(body.getAutoRenewPeriod());
        }
        gas = body.getGas();
        initialBalance = Hbar.fromTinybars(body.getInitialBalance());
        constructorParameters = body.getConstructorParameters().toByteArray();
        contractMemo = body.getMemo();
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return SmartContractServiceGrpc.getCreateContractMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractCreateInstance(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setContractCreateInstance(build());
    }
}
