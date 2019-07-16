package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.proto.Timestamp;
import com.hedera.hashgraph.sdk.proto.TransactionID;
import com.hedera.hashgraph.sdk.proto.TransactionIDOrBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

import javax.annotation.Nullable;

// TODO: TransactionId.fromString

public final class TransactionId {
    private final TransactionID.Builder inner;

    @Nullable
    private static Instant lastInstant;

    // `synchronized` is necessary for correctness with multiple threads
    private static synchronized Instant getIncreasingInstant() {
        // Allows the transaction to be accepted as long as the
        // server is not more than 10 seconds behind us
        final var instant = Clock.systemUTC()
            .instant()
            .minusSeconds(10);

        // ensures every instant is at least always greater than the last
        lastInstant = lastInstant != null && instant.compareTo(lastInstant) < 1
            ? lastInstant.plusNanos(1)
            : instant;

        return lastInstant;
    }

    /**
     * Generates a new transaction ID for the given `accountId`.
     *
     * <p>Note that transaction IDs are made up of the current time and the account that is
     * primarily signing the transaction. This account will also be the account that is charged for
     * any transaction fees.
     */
    public TransactionId(AccountId accountId) {
        this(accountId, getIncreasingInstant());
    }

    /**
     * Generate a transaction ID with a given account ID and valid start time.
     *
     * <i>Nota bene</i>: executing transactions with the same ID (account ID & account start time)
     * will throw {@link HederaException} with code {@code DUPLICATE_TRANSACTION}.
     * <p>
     * Use the other constructor to get an ID with a known-valid {@code transactionValidStart}.
     *
     * @param accountId
     * @param transactionValidStart the time by which the transaction takes effect; must be in the
     *                              past by the time it is submitted to the network.
     */
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

    @Override
    public String toString() {
        return String.format("accountId: %s, validStart: %s", getAccountId(), getValidStart());
    }
}
