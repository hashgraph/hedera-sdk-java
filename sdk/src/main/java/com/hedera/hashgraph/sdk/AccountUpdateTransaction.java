package com.hedera.hashgraph.sdk;

import com.google.protobuf.BoolValue;
import com.google.protobuf.UInt64Value;
import com.hedera.hashgraph.sdk.proto.CryptoUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

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
public final class AccountUpdateTransaction extends SingleTransactionBuilder<AccountUpdateTransaction> {
    private final CryptoUpdateTransactionBody.Builder builder;

    public AccountUpdateTransaction() {
        builder = CryptoUpdateTransactionBody.newBuilder();
    }

    /**
     * Sets the account ID which is being updated in this transaction.
     *
     * @return {@code this}
     * @param accountId The AccountId to be set
     */
    public AccountUpdateTransaction setAccountId(AccountId accountId) {
        builder.setAccountIDToUpdate(accountId.toProtobuf());
        return this;
    }

    /**
     * Sets the new key.
     *
     * @return {@code this}
     * @param key The Key to be set
     */
    public AccountUpdateTransaction setKey(Key key) {
        builder.setKey(key.toKeyProtobuf());
        return this;
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
     * @return {@code this}
     * @param proxyAccountId The AccountId to be set
     */
    public AccountUpdateTransaction setProxyAccountId(AccountId proxyAccountId) {
        builder.setProxyAccountID(proxyAccountId.toProtobuf());
        return this;
    }

    /**
     * Sets the new threshold amount for which an account record is created
     * for any send/withdraw transaction.
     *
     * @return {@code this}
     * @param sendRecordThreshold The Hbar to be set as the threshold
     */
    public AccountUpdateTransaction setSendRecordThreshold(Hbar sendRecordThreshold) {
        builder.setSendRecordThresholdWrapper(UInt64Value.of(sendRecordThreshold.toTinybars()));
        return this;
    }

    /**
     * Sets the new threshold amount for which an account record is created
     * for any receive/deposit transaction.
     *
     * @return {@code this}
     * @param receiveRecordThreshold The Hbar to be set as the threshold
     */
    public AccountUpdateTransaction setReceiveRecordThreshold(Hbar receiveRecordThreshold) {
        builder.setReceiveRecordThresholdWrapper(UInt64Value.of(receiveRecordThreshold.toTinybars()));
        return this;
    }

    /**
     * Sets the new expiration time to extend to (ignored if equal to or
     * before the current one).
     *
     * @return {@code this}
     * @param expirationTime The Instant to be set as the expiration time
     */
    public AccountUpdateTransaction setExpirationTime(Instant expirationTime) {
        builder.setExpirationTime(InstantConverter.toProtobuf(expirationTime));
        return this;
    }

    /**
     * Sets the duration in which it will automatically extend the expiration period.
     * <p>
     * If it doesn't have enough balance, it extends as long as possible.
     * If it is empty when it expires, then it is deleted.
     *
     * @return {@code this}
     * @param autoRenewPeriod The Duration to be set for auto renewal
     */
    public AccountUpdateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        return this;
    }

    /**
     * Sets whether this account's key must sign any transaction
     * depositing into this account (in addition to all withdrawals).
     *
     * @return {@code this}
     * @param receiverSignatureRequired The bool to be set
     */
    public AccountUpdateTransaction setReceiverSignatureRequired(boolean receiverSignatureRequired) {
        builder.setReceiverSigRequiredWrapper(BoolValue.of(receiverSignatureRequired));
        return this;
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoUpdateAccount(builder);
    }
}
