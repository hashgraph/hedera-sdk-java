package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.proto.CryptoCreateTransactionBody;
import com.hedera.hashgraph.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaConstants;
import com.hedera.hashgraph.sdk.SingleTransactionBuilder;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import io.grpc.MethodDescriptor;

import java.time.Duration;

// Corresponds to `CryptoCreateTransaction`
public final class AccountCreateTransaction extends SingleTransactionBuilder<AccountCreateTransaction> {
    private final CryptoCreateTransactionBody.Builder builder = bodyBuilder.getCryptoCreateAccountBuilder()
        // Required fixed autorenew duration.
        .setAutoRenewPeriod(DurationHelper.durationFrom(HederaConstants.DEFAULT_AUTORENEW_DURATION))
        // Default to maximum values for record thresholds. Without this records would be
        // auto-created whenever a send or receive transaction takes place for this new account. This should
        // be an explicit ask.
        .setSendRecordThreshold(Long.MAX_VALUE)
        .setReceiveRecordThreshold(Long.MAX_VALUE);

    public AccountCreateTransaction() {
        super();
    }

    public AccountCreateTransaction setKey(PublicKey publicKey) {
        builder.setKey(publicKey.toKeyProto());
        return this;
    }

    /**
     * Set the initial balance of the account, transferred from the operator account, in tinybar.
     *
     * @param initialBalance the initial balance of the account in tinybar.
     * @return {@code this} for fluent API usage.
     */
    public AccountCreateTransaction setInitialBalance(long initialBalance) {
        builder.setInitialBalance(initialBalance);
        return this;
    }

    /**
     * Set the initial balance of the account, transferred from the operator account.
     *
     * @param initialBalance the initial balance of the account.
     * @return {@code this} for fluent API usage.
     */
    public AccountCreateTransaction setInitialBalance(Hbar initialBalance) {
        builder.setInitialBalance(initialBalance.asTinybar());
        return this;
    }

    public AccountCreateTransaction setProxyAccountId(AccountId accountId) {
        builder.setProxyAccountID(accountId.toProto());
        return this;
    }

    /**
     * Set the threshold for generating records when sending currency, in tinybar.
     */
    @Deprecated
    public AccountCreateTransaction setSendRecordThreshold(long sendRecordThreshold) {
        builder.setSendRecordThreshold(sendRecordThreshold);
        return this;
    }

    @Deprecated
    public AccountCreateTransaction setSendRecordThreshold(Hbar sendRecordThreshold) {
        builder.setSendRecordThreshold(sendRecordThreshold.asTinybar());
        return this;
    }

    /**
     * Set the threshold for generating records when receiving currency, in tinybar.
     */
    @Deprecated
    public AccountCreateTransaction setReceiveRecordThreshold(long receiveRecordThreshold) {
        builder.setReceiveRecordThreshold(receiveRecordThreshold);
        return this;
    }

    @Deprecated
    public AccountCreateTransaction setReceiveRecordThreshold(Hbar receiveRecordThreshold) {
        builder.setReceiveRecordThreshold(receiveRecordThreshold.asTinybar());
        return this;
    }

    public AccountCreateTransaction setReceiverSignatureRequired(boolean receiverSignatureRequired) {
        builder.setReceiverSigRequired(receiverSignatureRequired);
        return this;
    }

    public AccountCreateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        builder.setAutoRenewPeriod(DurationHelper.durationFrom(autoRenewPeriod));
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasKey(), ".setKey() required");
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return CryptoServiceGrpc.getCreateAccountMethod();
    }
}
