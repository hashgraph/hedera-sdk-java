package com.hedera.sdk;

import com.hedera.sdk.proto.Timestamp;
import com.hedera.sdk.proto.TransactionID;
import com.hedera.sdk.proto.TransactionIDOrBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

// TODO: TransactionId.toString
// TODO: TransactionId.fromString

public final class TransactionId {
    final TransactionID.Builder inner;

    /**
     * Generates a new transaction ID for the given `accountId`.
     *
     * <p>Note that transaction IDs are made up of the current time and the account that is
     * primarily signing the transaction. This account will also be the account that is charged for
     * any transaction fees.
     */
    public TransactionId(AccountId accountId) {
        this(
                accountId, Clock.systemUTC()
                    .instant()
        );
    }

    TransactionId(AccountId accountId, Instant transactionValidStart) {
        inner = TransactionID.newBuilder()
            .setAccountID(accountId.inner)
            .setTransactionValidStart(
                Timestamp.newBuilder()
                    .setSeconds(transactionValidStart.getEpochSecond())
                    .setNanos(transactionValidStart.getNano())
            );
    }

    TransactionId(TransactionIDOrBuilder transactionId) {
        inner = TransactionID.newBuilder()
            .setAccountID(transactionId.getAccountID())
            .setTransactionValidStart(transactionId.getTransactionValidStart());
    }

    public AccountId getAccountId() {
        return new AccountId(inner.getAccountIDOrBuilder());
    }

    public Instant getValidStart() {
        return TimestampHelper.timestampTo(inner.getTransactionValidStart());
    }

    public TransactionID toProto() {
        return inner.build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccountId(), getValidStart());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TransactionId))
            return false;
        var txnId = (TransactionId) other;
        return getAccountId().equals(txnId.getAccountId()) && getValidStart().equals(txnId.getValidStart());
    }

    @Override
    public String toString() {
        return inner.toString();
    }
}
