package com.hedera.hashgraph.sdk;

import com.google.protobuf.BoolValue;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.google.protobuf.UInt64Value;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.CryptoUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;

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
    private final CryptoUpdateTransactionBody.Builder builder;

    public AccountUpdateTransaction() {
        builder = CryptoUpdateTransactionBody.newBuilder();
    }

    AccountUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getCryptoUpdateAccount().toBuilder();
    }

    @Nullable
    public AccountId getAccountId() {
        return builder.hasAccountIDToUpdate() ? AccountId.fromProtobuf(builder.getAccountIDToUpdate()) : null;
    }

    /**
     * Sets the account ID which is being updated in this transaction.
     *
     * @param accountId The AccountId to be set
     * @return {@code this}
     */
    public AccountUpdateTransaction setAccountId(AccountId accountId) {
        requireNotFrozen();
        builder.setAccountIDToUpdate(accountId.toProtobuf());
        return this;
    }

    @Nullable
    public Key getKey() {
        return builder.hasKey() ? Key.fromProtobufKey(builder.getKey()) : null;
    }

    /**
     * Sets the new key.
     *
     * @param key The Key to be set
     * @return {@code this}
     */
    public AccountUpdateTransaction setKey(Key key) {
        requireNotFrozen();
        builder.setKey(key.toProtobufKey());
        return this;
    }

    @Nullable
    public AccountId getProxyAccountId() {
        return builder.hasProxyAccountID() ? AccountId.fromProtobuf(builder.getProxyAccountID()) : null;
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
        requireNotFrozen();
        builder.setProxyAccountID(proxyAccountId.toProtobuf());
        return this;
    }

    @Nullable
    Hbar getSendRecordThreshold() {
        return builder.hasSendRecordThresholdWrapper() ? Hbar.fromTinybars(builder.getSendRecordThresholdWrapper().getValue()) : null;
    }

    /**
     * Sets the new threshold amount for which an account record is created
     * for any send/withdraw transaction.
     *
     * @param sendRecordThreshold The Hbar to be set as the threshold
     * @return {@code this}
     */
    AccountUpdateTransaction setSendRecordThreshold(Hbar sendRecordThreshold) {
        requireNotFrozen();
        builder.setSendRecordThresholdWrapper(UInt64Value.of(sendRecordThreshold.toTinybars()));
        return this;
    }

    @Nullable
    Hbar getReceiveRecordThreshold() {
        return builder.hasReceiveRecordThresholdWrapper() ? Hbar.fromTinybars(builder.getReceiveRecordThresholdWrapper().getValue()) : null;
    }

    /**
     * Sets the new threshold amount for which an account record is created
     * for any receive/deposit transaction.
     *
     * @param receiveRecordThreshold The Hbar to be set as the threshold
     * @return {@code this}
     */
    AccountUpdateTransaction setReceiveRecordThreshold(Hbar receiveRecordThreshold) {
        requireNotFrozen();
        builder.setReceiveRecordThresholdWrapper(UInt64Value.of(receiveRecordThreshold.toTinybars()));
        return this;
    }

    @Nullable
    public Instant getExpirationTime() {
        return builder.hasExpirationTime() ? InstantConverter.fromProtobuf(builder.getExpirationTime()) : null;
    }

    /**
     * Sets the new expiration time to extend to (ignored if equal to or
     * before the current one).
     *
     * @param expirationTime The Instant to be set as the expiration time
     * @return {@code this}
     */
    public AccountUpdateTransaction setExpirationTime(Instant expirationTime) {
        requireNotFrozen();
        builder.setExpirationTime(InstantConverter.toProtobuf(expirationTime));
        return this;
    }

    @Nullable
    public Duration getAutoRenewPeriod() {
        return builder.hasAutoRenewPeriod() ? DurationConverter.fromProtobuf(builder.getAutoRenewPeriod()) : null;
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
        requireNotFrozen();
        builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        return this;
    }

    @Nullable
    public Boolean getReceiverSignatureRequired() {
        return builder.hasReceiverSigRequiredWrapper() ? builder.getReceiverSigRequiredWrapper().getValue() : null;
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
        builder.setReceiverSigRequiredWrapper(BoolValue.of(receiverSignatureRequired));
        return this;
    }

    public String getAccountMemo() {
        return builder.getMemo().getValue();
    }

    public AccountUpdateTransaction setAccountMemo(String memo) {
        requireNotFrozen();
        this.builder.setMemo(StringValue.of(memo));
        return this;
    }

    public AccountUpdateTransaction clearMemo() {
        requireNotFrozen();
        this.builder.clearMemo();
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getUpdateAccountMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoUpdateAccount(builder);
        return true;
    }
}
