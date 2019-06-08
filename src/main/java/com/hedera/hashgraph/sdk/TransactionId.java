package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.proto.Timestamp;
import com.hedera.hashgraph.sdk.proto.TransactionID;
import com.hedera.hashgraph.sdk.proto.TransactionIDOrBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

// TODO: TransactionId.toString
// TODO: TransactionId.fromString

public final class TransactionId {
    private final TransactionID.Builder inner;

    /**
     * Generates a new transaction ID for the given `accountId`.
     *
     * <p>Note that transaction IDs are made up of the current time and the account that is
     * primarily signing the transaction. This account will also be the account that is charged for
     * any transaction fees.
     */
    public TransactionId(AccountId accountId) {
        // Allows the transaction to be accepted as long as the
        // server is not more than 10 seconds behind us
        this(
                accountId, Clock.systemUTC()
                    .instant()
                    .minusSeconds(10));
    }

    public TransactionId(AccountId accountId, Instant transactionValidStart) {
        inner = TransactionID.newBuilder()
            .setAccountID(accountId.toProto())
            .setTransactionValidStart(
                Timestamp.newBuilder()
                    .setSeconds(transactionValidStart.getEpochSecond())
                    .setNanos(transactionValidStart.getNano()));
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
        if (this == other) return true;

        if (!(other instanceof TransactionId)) return false;

        var otherId = (TransactionId) other;
        return getAccountId().equals(otherId.getAccountId()) && getValidStart().equals(otherId.getValidStart());
    }
}
