package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.Timestamp;
import com.hedera.hashgraph.proto.TransactionID;
import com.hedera.hashgraph.proto.TransactionIDOrBuilder;
import com.hedera.hashgraph.sdk.account.AccountId;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nullable;

// TODO: TransactionId.toString
// TODO: TransactionId.fromString

public final class TransactionId {
    public final AccountId accountId;

    public final Instant validStart;

    private final TransactionID.Builder inner;

    @Nullable
    private static Instant lastInstant;

    // `synchronized` is necessary for correctness with multiple threads
    private static synchronized Instant getIncreasingInstant() {
        // Allows the transaction to be accepted as long as the
        // server is not more than 10 seconds behind us
        final Instant instant = Clock.systemUTC()
            .instant()
            .minusSeconds(10);

        // ensures every instant is at least always greater than the last
        lastInstant = lastInstant != null && instant.compareTo(lastInstant) <= 0
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

        this.accountId = accountId;
        this.validStart = transactionValidStart;
    }

    TransactionId(TransactionIDOrBuilder transactionId) {
        inner = TransactionID.newBuilder()
            .setAccountID(transactionId.getAccountID())
            .setTransactionValidStart(transactionId.getTransactionValidStart());

        accountId = new AccountId(transactionId.getAccountIDOrBuilder());
        validStart = TimestampHelper.timestampTo(transactionId.getTransactionValidStart());
    }

    /**
     * @deprecated use {@link #accountId} instead.
     */
    @Deprecated
    public AccountId getAccountId() {
        return accountId;
    }

    /**
     * @deprecated use {@link #validStart} instead.
     */
    @Deprecated
    public Instant getValidStart() {
        return validStart;
    }

    @Internal
    public TransactionID toProto() {
        return inner.build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, validStart);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (!(other instanceof TransactionId)) return false;

        TransactionId otherId = (TransactionId) other;
        return accountId.equals(otherId.accountId) && validStart.equals(otherId.validStart);
    }

    public TransactionReceipt getReceipt(Client client) throws HederaException {
        return new TransactionReceiptQuery()
            .setTransactionId(this)
            .execute(client);
    }

    public TransactionReceipt getReceipt(Client client, Duration timeout) throws HederaException {
        return new TransactionReceiptQuery()
            .setTransactionId(this)
            .execute(client, timeout);
    }

    public void getReceiptAsync(Client client, Consumer<TransactionReceipt> onReceipt, Consumer<HederaThrowable> onError) {
        new TransactionReceiptQuery()
            .setTransactionId(this)
            .executeAsync(client, onReceipt, onError);
    }

    public void getReceiptAsync(Client client, Duration timeout, Consumer<TransactionReceipt> onReceipt, Consumer<HederaThrowable> onError) {
        new TransactionReceiptQuery()
            .setTransactionId(this)
            .executeAsync(client, timeout, onReceipt, onError);
    }

    public TransactionRecord getRecord(Client client) throws HederaException, HederaNetworkException {
        getReceipt(client);

        return new TransactionRecordQuery()
            .setTransactionId(this)
            .execute(client);
    }

    public TransactionRecord getRecord(Client client, Duration timeout) throws HederaException {
        getReceipt(client, timeout);

        return new TransactionRecordQuery()
            .setTransactionId(this)
            .execute(client, timeout);
    }

    public void getRecordAsync(Client client, Consumer<TransactionRecord> onRecord, Consumer<HederaThrowable> onError) {
        getReceiptAsync(client, (receipt) -> {
            new TransactionRecordQuery()
                .setTransactionId(this)
                .executeAsync(client, onRecord, onError);
        }, onError);
    }

    public void getRecordAsync(Client client, Duration timeout, Consumer<TransactionRecord> onRecord, Consumer<HederaThrowable> onError) {
        getReceiptAsync(client, timeout, (receipt) -> {
            new TransactionRecordQuery()
                .setTransactionId(this)
                .executeAsync(client, timeout, onRecord, onError);
        }, onError);
    }
}
