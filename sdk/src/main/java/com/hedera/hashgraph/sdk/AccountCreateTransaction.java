package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.util.HashMap;

/**
 * Create a new Hedera™ account.
 */
public final class AccountCreateTransaction extends Transaction<AccountCreateTransaction> {
    private static final Hbar DEFAULT_RECORD_THRESHOLD = Hbar.fromTinybars(Long.MAX_VALUE);

    private final CryptoCreateTransactionBody.Builder builder;

    public AccountCreateTransaction() {
        builder = CryptoCreateTransactionBody.newBuilder();

        setAutoRenewPeriod(DEFAULT_AUTO_RENEW_PERIOD);
        setSendRecordThreshold(DEFAULT_RECORD_THRESHOLD);
        setReceiveRecordThreshold(DEFAULT_RECORD_THRESHOLD);
    }

    AccountCreateTransaction(HashMap<TransactionId, HashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) {
        super(txs.values().iterator().next());

        builder = bodyBuilder.getCryptoCreateAccount().toBuilder();
    }

    @Nullable
    public Key getKey() {
        return builder.hasKey() ? Key.fromProtobuf(builder.getKey()) : null;
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
        requireNotFrozen();
        builder.setKey(key.toKeyProtobuf());
        return this;
    }

    public Hbar getInitialBalance() {
        return Hbar.fromTinybars(builder.getInitialBalance());
    }

    /**
     * Set the initial amount to transfer into this account.
     *
     * @param initialBalance the initial balance.
     * @return {@code this}
     */
    public AccountCreateTransaction setInitialBalance(Hbar initialBalance) {
        requireNotFrozen();
        builder.setInitialBalance(initialBalance.toTinybars());
        return this;
    }

    Hbar getSendRecordThreshold() {
        return Hbar.fromTinybars(builder.getSendRecordThreshold());
    }

    /**
     * Set the threshold amount for which a transaction record is created for any transfer of hbars
     * from this account.
     *
     * <p>This is defaulted to {@code Hbar.MAX} by the SDK as the fee for threshold records
     * can be surprising. Do not adjust the threshold unless you understand that you be charged an
     * additional (small) fee any time your account sends money.
     *
     * @param sendRecordThreshold the threshold amount.
     * @return {@code this}
     */
    AccountCreateTransaction setSendRecordThreshold(Hbar sendRecordThreshold) {
        requireNotFrozen();
        builder.setSendRecordThreshold(sendRecordThreshold.toTinybars());
        return this;
    }

    Hbar getReceiveRecordThreshold() {
        return Hbar.fromTinybars(builder.getReceiveRecordThreshold());
    }

    /**
     * Set the threshold amount for which a transaction record is created for any transfer of hbars
     * <b>to this account</b>.
     *
     * <p>This is defaulted to {@code Hbar.MAX} by the SDK as the fee for threshold records
     * can be surprising. Do not adjust the threshold unless you understand that you be charged an
     * additional (small) fee any time your account <b>receives</b> money.
     *
     * @param receiveRecordThreshold the threshold amount.
     * @return {@code this}
     */
    AccountCreateTransaction setReceiveRecordThreshold(Hbar receiveRecordThreshold) {
        requireNotFrozen();
        builder.setReceiveRecordThreshold(receiveRecordThreshold.toTinybars());
        return this;
    }

    public boolean getReceiverSignatureRequired() {
        return builder.getReceiverSigRequired();
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
        builder.setReceiverSigRequired(receiveSignatureRequired);
        return this;
    }

    @Nullable
    public AccountId getProxyAccountId() {
        return builder.hasProxyAccountID() ? AccountId.fromProtobuf(builder.getProxyAccountID()) : null;
    }

    /**
     * Set the ID of the account to which this account is proxy staked.
     *
     * @param proxyAccountId the proxy account ID.
     * @return {@code this}
     */
    public AccountCreateTransaction setProxyAccountId(AccountId proxyAccountId) {
        requireNotFrozen();
        builder.setProxyAccountID(proxyAccountId.toProtobuf());
        return this;
    }

    @Nullable
    public Duration getAutoRenewPeriod() {
        return builder.hasAutoRenewPeriod() ? DurationConverter.fromProtobuf(builder.getAutoRenewPeriod()) : null;
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
        builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getCreateAccountMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoCreateAccount(builder);
        return true;
    }
}
