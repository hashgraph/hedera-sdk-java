package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoCreateTransactionBody;

// TODO: #setProxyAccountID
// TODO: #setSendRecordThreshold
// TODO: #setReceiveRecordThreshold
// TODO: #setReceiverSignatureRequired
// TODO: #setAutoRenewPeriod

/**
 * Create a new Hedera™ account.
 */
public final class AccountCreateTransaction extends TransactionBuilder {
    private final CryptoCreateTransactionBody.Builder builder;

    public AccountCreateTransaction() {
        super();

        // TODO: Default auto-renew duration
        // TODO: Default thresholds
        builder = CryptoCreateTransactionBody.newBuilder();
        bodyBuilder.setCryptoCreateAccount(builder);
    }

    /**
     * Set the key for this account.
     * <p>
     * The key that must sign each transfer out of the account.
     * If receiverSignatureRequired is true, then it must also sign any
     * transfer into the account.
     */
    public AccountCreateTransaction setKey(Key key) {
        builder.setKey(key.toProtobuf());
        return this;
    }

    /**
     * Set the initial amount to transfer into this account.
     *
     * @param initialBalance the initial balance in tinybars.
     * @return {@code this}.
     */
    public AccountCreateTransaction setInitialBalance(long initialBalance) {
        builder.setInitialBalance(initialBalance);
        return this;
    }
}
