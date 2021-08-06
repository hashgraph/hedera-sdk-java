package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Create a new Hedera™ account.
 */
public final class AccountCreateTransaction extends Transaction<AccountCreateTransaction> {
    private static final Hbar DEFAULT_RECORD_THRESHOLD = Hbar.fromTinybars(Long.MAX_VALUE);

    @Nullable
    private AccountId proxyAccountId = null;
    @Nullable
    private Key key = null;
    private String accountMemo = "";
    private Hbar initialBalance = new Hbar(0);
    private boolean receiverSigRequired = false;
    private Duration autoRenewPeriod = DEFAULT_AUTO_RENEW_PERIOD;

    public AccountCreateTransaction() {
    }

    AccountCreateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    AccountCreateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    @Nullable
    public Key getKey() {
        return key;
    }

    /**
     * Set the key for this account.
     *
     * <p>The key that must sign each transfer out of the account. If receiverSignatureRequired is
     * true, then it must also sign any transfer into the account.
     *
     * @param key the key for this account.
     * @return {@code this}
     */
    public AccountCreateTransaction setKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        this.key = key;
        return this;
    }

    public Hbar getInitialBalance() {
        return initialBalance;
    }

    /**
     * Set the initial amount to transfer into this account.
     *
     * @param initialBalance the initial balance.
     * @return {@code this}
     */
    public AccountCreateTransaction setInitialBalance(Hbar initialBalance) {
        Objects.requireNonNull(initialBalance);
        requireNotFrozen();
        this.initialBalance = initialBalance;
        return this;
    }

    public boolean getReceiverSignatureRequired() {
        return receiverSigRequired;
    }

    /**
     * Set to true to require this account to sign any transfer of hbars to this account.
     *
     * <p>All transfers of hbars from this account must always be signed. This property only affects
     * transfers to this account.
     *
     * @param receiveSignatureRequired true to require a signature when receiving hbars.
     * @return {@code this}
     */
    public AccountCreateTransaction setReceiverSignatureRequired(boolean receiveSignatureRequired) {
        requireNotFrozen();
        receiverSigRequired = receiveSignatureRequired;
        return this;
    }

    @Nullable
    public AccountId getProxyAccountId() {
        return proxyAccountId;
    }

    /**
     * Set the ID of the account to which this account is proxy staked.
     *
     * @param proxyAccountId the proxy account ID.
     * @return {@code this}
     */
    public AccountCreateTransaction setProxyAccountId(AccountId proxyAccountId) {
        requireNotFrozen();
        Objects.requireNonNull(proxyAccountId);
        this.proxyAccountId = proxyAccountId;
        return this;
    }

    @Nullable
    public Duration getAutoRenewPeriod() {
        return autoRenewPeriod;
    }

    /**
     * Set the auto renew period for this account.
     *
     * <p>A Hedera™ account is charged to extend its expiration date every renew period. If it
     * doesn't have enough balance, it extends as long as possible. If the balance is zero when it
     * expires, then the account is deleted.
     *
     * <p>This is defaulted to 3 months by the SDK.
     *
     * @param autoRenewPeriod the auto renew period for this account.
     * @return {@code this}
     */
    public AccountCreateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        requireNotFrozen();
        Objects.requireNonNull(autoRenewPeriod);
        this.autoRenewPeriod = autoRenewPeriod;
        return this;
    }

    public String getAccountMemo() {
        return accountMemo;
    }

    public AccountCreateTransaction setAccountMemo(String memo) {
        Objects.requireNonNull(memo);
        requireNotFrozen();
        accountMemo = memo;
        return this;
    }

    CryptoCreateTransactionBody.Builder build() {
        var builder = CryptoCreateTransactionBody.newBuilder();

        if (proxyAccountId != null) {
            builder.setProxyAccountID(proxyAccountId.toProtobuf());
        }
        if(key != null) {
            builder.setKey(key.toProtobufKey());
        }
        builder.setInitialBalance(initialBalance.toTinybars());
        builder.setReceiverSigRequired(receiverSigRequired);
        builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        builder.setMemo(accountMemo);

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (proxyAccountId != null) {
            proxyAccountId.validateChecksum(client);
        }
    }

    void initFromTransactionBody() {
        var body = txBody.getCryptoCreateAccount();

        if (body.hasProxyAccountID()) {
            proxyAccountId = AccountId.fromProtobuf(body.getProxyAccountID());
        }
        if(body.hasKey()) {
            key = Key.fromProtobufKey(body.getKey());
        }
        if(body.hasAutoRenewPeriod()) {
            autoRenewPeriod = DurationConverter.fromProtobuf(body.getAutoRenewPeriod());
        }
        initialBalance = Hbar.fromTinybars(body.getInitialBalance());
        accountMemo = body.getMemo();
        receiverSigRequired = body.getReceiverSigRequired();
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getCreateAccountMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoCreateAccount(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setCryptoCreateAccount(build());
    }
}
