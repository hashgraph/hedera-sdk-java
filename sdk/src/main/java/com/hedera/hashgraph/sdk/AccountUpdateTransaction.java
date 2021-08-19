package com.hedera.hashgraph.sdk;

import com.google.protobuf.BoolValue;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.CryptoUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Change properties for the given account.
 * <p>
 * Any null field is ignored (left unchanged).
 * <p>
 * This transaction must be signed by the existing key for this account.
 * <p>
 * If the transaction is changing the key field, then the transaction must be signed by
 * both the old key (from before the change) and the new key. The old key must sign for security.
 * The new key must sign as a safeguard to avoid accidentally
 * changing to an invalid key, and then having no way to recover.
 * <p>
 * When extending the expiration date, the cost is affected by the size
 * of the list of attached claims, and of the keys
 * associated with the claims and the account.
 */
public final class AccountUpdateTransaction extends Transaction<AccountUpdateTransaction> {
    @Nullable
    private AccountId accountId = null;
    @Nullable
    private AccountId proxyAccountId = null;
    @Nullable
    private Key key = null;
    @Nullable
    private Instant expirationTime = null;
    @Nullable
    private Duration autoRenewPeriod = null;
    @Nullable
    private Boolean receiverSigRequired = null;
    @Nullable
    private String accountMemo = null;

    public AccountUpdateTransaction() {
    }

    AccountUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    AccountUpdateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    @Nullable
    public AccountId getAccountId() {
        return accountId;
    }

    /**
     * Sets the account ID which is being updated in this transaction.
     *
     * @param accountId The AccountId to be set
     * @return {@code this}
     */
    public AccountUpdateTransaction setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    @Nullable
    public Key getKey() {
        return key;
    }

    /**
     * Sets the new key.
     *
     * @param key The Key to be set
     * @return {@code this}
     */
    public AccountUpdateTransaction setKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        this.key = key;
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
     * that isn't a node, then this account is automatically proxy staked to
     * a node chosen by the network, but without earning payments.
     * <p>
     * If the proxyAccountID account refuses to accept proxy staking, or
     * if it is not currently running a node, then it
     * will behave as if proxyAccountID was null.
     *
     * @param proxyAccountId The AccountId to be set
     * @return {@code this}
     */
    public AccountUpdateTransaction setProxyAccountId(AccountId proxyAccountId) {
        Objects.requireNonNull(proxyAccountId);
        requireNotFrozen();
        this.proxyAccountId = proxyAccountId;
        return this;
    }

    @Nullable
    public Instant getExpirationTime() {
        return expirationTime;
    }

    /**
     * Sets the new expiration time to extend to (ignored if equal to or
     * before the current one).
     *
     * @param expirationTime The Instant to be set as the expiration time
     * @return {@code this}
     */
    public AccountUpdateTransaction setExpirationTime(Instant expirationTime) {
        Objects.requireNonNull(expirationTime);
        requireNotFrozen();
        this.expirationTime = expirationTime;
        return this;
    }

    @Nullable
    public Duration getAutoRenewPeriod() {
        return autoRenewPeriod;
    }

    /**
     * Sets the duration in which it will automatically extend the expiration period.
     * <p>
     * If it doesn't have enough balance, it extends as long as possible.
     * If it is empty when it expires, then it is deleted.
     *
     * @param autoRenewPeriod The Duration to be set for auto renewal
     * @return {@code this}
     */
    @Deprecated
    public AccountUpdateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        Objects.requireNonNull(autoRenewPeriod);
        requireNotFrozen();
        this.autoRenewPeriod = autoRenewPeriod;
        return this;
    }

    @Nullable
    public Boolean getReceiverSignatureRequired() {
        return receiverSigRequired;
    }

    /**
     * Sets whether this account's key must sign any transaction
     * depositing into this account (in addition to all withdrawals).
     *
     * @param receiverSignatureRequired The bool to be set
     * @return {@code this}
     */
    public AccountUpdateTransaction setReceiverSignatureRequired(boolean receiverSignatureRequired) {
        requireNotFrozen();
        receiverSigRequired = receiverSignatureRequired;
        return this;
    }

    @Nullable
    public String getAccountMemo() {
        return accountMemo;
    }

    public AccountUpdateTransaction setAccountMemo(String memo) {
        requireNotFrozen();
        Objects.requireNonNull(memo);
        accountMemo = memo;
        return this;
    }

    public AccountUpdateTransaction clearMemo() {
        requireNotFrozen();
        accountMemo = "";
        return this;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (accountId != null) {
            accountId.validateChecksum(client);
        }
        if (proxyAccountId != null) {
            proxyAccountId.validateChecksum(client);
        }
    }

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getCryptoUpdateAccount();

        if (body.hasAccountIDToUpdate()) {
            accountId = AccountId.fromProtobuf(body.getAccountIDToUpdate());
        }
        if (body.hasProxyAccountID()) {
            proxyAccountId = AccountId.fromProtobuf(body.getProxyAccountID());
        }
        if (body.hasKey()) {
            key = Key.fromProtobufKey(body.getKey());
        }
        if (body.hasExpirationTime()) {
            expirationTime = InstantConverter.fromProtobuf(body.getExpirationTime());
        }
        if (body.hasAutoRenewPeriod()) {
            autoRenewPeriod = DurationConverter.fromProtobuf(body.getAutoRenewPeriod());
        }
        if (body.hasReceiverSigRequiredWrapper()) {
            receiverSigRequired = body.getReceiverSigRequiredWrapper().getValue();
        }
        if (body.hasMemo()) {
            accountMemo = body.getMemo().getValue();
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getUpdateAccountMethod();
    }

    CryptoUpdateTransactionBody.Builder build() {
        var builder = CryptoUpdateTransactionBody.newBuilder();
        if (accountId != null) {
            builder.setAccountIDToUpdate(accountId.toProtobuf());
        }
        if (proxyAccountId != null) {
            builder.setProxyAccountID(proxyAccountId.toProtobuf());
        }
        if (key != null) {
            builder.setKey(key.toProtobufKey());
        }
        if (expirationTime != null) {
            builder.setExpirationTime(InstantConverter.toProtobuf(expirationTime));
        }
        if (autoRenewPeriod != null) {
            builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        }
        if (receiverSigRequired != null) {
            builder.setReceiverSigRequiredWrapper(BoolValue.of(receiverSigRequired));
        }
        if (accountMemo != null) {
            builder.setMemo(StringValue.of(accountMemo));
        }

        return builder;
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoUpdateAccount(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setCryptoUpdateAccount(build());
    }
}
