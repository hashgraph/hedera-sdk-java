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

    @Nullable
    public byte[] nonce;

    boolean scheduled = false;


    private TransactionId(AccountId accountId, Instant transactionValidStart) {
        inner = TransactionID.newBuilder()
            .setAccountID(accountId.toProto())
            .setTransactionValidStart(
                Timestamp.newBuilder()
                    .setSeconds(transactionValidStart.getEpochSecond())
                    .setNanos(transactionValidStart.getNano()));

        this.accountId = accountId;
        this.validStart = transactionValidStart;
        this.scheduled = false;
        this.nonce = null;
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
     * will throw {@link HederaStatusException} with code {@code DUPLICATE_TRANSACTION}.
     * <p>
     * <p>
     * Use the primary constructor to get an ID with a known-valid {@code transactionValidStart}.
     *
     * @param accountId
     * @param transactionValidStart the time by which the transaction takes effect; must be in the
     *                              past by the time it is submitted to the network.
     */
    public static TransactionId withValidStart(AccountId accountId, Instant transactionValidStart) {
        return new TransactionId(accountId, transactionValidStart);
    }

    public static TransactionId withNonce(byte[] nonce) {
        TransactionId txId = new TransactionId(new AccountId(0), Instant.EPOCH);
        txId.nonce = nonce;

        return txId;
    }

    @Internal
    public TransactionId(TransactionIDOrBuilder transactionId) {
        inner = TransactionID.newBuilder()
            .setAccountID(transactionId.getAccountID())
            .setTransactionValidStart(transactionId.getTransactionValidStart())
            .setScheduled(transactionId.getScheduled())
            .setNonce(transactionId.getNonce());

        accountId = new AccountId(transactionId.getAccountIDOrBuilder());
        validStart = TimestampHelper.timestampTo(transactionId.getTransactionValidStart());
        nonce = transactionId.getNonce().toByteArray();
        scheduled = transactionId.getScheduled();
    }

    @Internal
    public TransactionID toProto() {
        return inner.build();
    }

    public TransactionId setScheduled(boolean scheduled) {
        this.scheduled = scheduled;

        return this;
    }

    @Override
    public String toString() {
        Timestamp timestampProto = TimestampHelper.timestampFrom(validStart);

        return accountId.toString() + "@" + timestampProto.getSeconds() + "."
            + timestampProto.getNanos();
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

    // RFC: do we want to expose this method and its async version?
    void waitForConsensus(Client client, @Nullable Duration timeout) throws HederaStatusException {
        try {
            // use the receipt query to wait for consensus
            if (timeout != null) {
                getReceipt(client, timeout);
            } else {
                getReceipt(client);
            }
        } catch (HederaReceiptStatusException e) {
            // these errors mean the transaction was dropped or timed out, we need to bubble them up
            if (e.status.equalsAny(Status.Busy, Status.Unknown)) {
                throw e;
            }
            // otherwise ignore the status in the receipt; for failed transactions the record might
            // contain useful context that we don't want to lose/discard
        }
    }

    void waitForConsensusAsync(Client client, @Nullable Duration timeout, Runnable onSuccess, Consumer<HederaThrowable> onError) {
        // same motivation as synchronous version above
        Consumer<HederaThrowable> onError2 = e -> {
            if (e instanceof HederaReceiptStatusException) {
                // these errors mean the transaction was dropped
                if (((HederaReceiptStatusException) e).status.equalsAny(Status.Busy, Status.Unknown)) {
                    onError.accept(e);
                } else {
                    onSuccess.run();
                }
            }
        };

        if (timeout != null) {
            getReceiptAsync(client, timeout, r -> onSuccess.run(), onError2);
        } else {
            getReceiptAsync(client, r -> onSuccess.run(), onError2);
        }
    }

    public TransactionReceipt getReceipt(Client client) throws HederaStatusException {
        return new TransactionReceiptQuery()
            .setTransactionId(this)
            .execute(client);
    }

    public TransactionReceipt getReceipt(Client client, Duration timeout) throws HederaStatusException {
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

    public TransactionRecord getRecord(Client client) throws HederaStatusException, HederaNetworkException {
        waitForConsensus(client, null);

        return new TransactionRecordQuery()
            .setTransactionId(this)
            .execute(client);
    }

    public TransactionRecord getRecord(Client client, Duration timeout) throws HederaStatusException {
        waitForConsensus(client, timeout);

        return new TransactionRecordQuery()
            .setTransactionId(this)
            .execute(client, timeout);
    }

    public void getRecordAsync(Client client, Consumer<TransactionRecord> onRecord, Consumer<HederaThrowable> onError) {
        waitForConsensusAsync(client, null, () -> {
            new TransactionRecordQuery()
                .setTransactionId(this)
                .executeAsync(client, onRecord, onError);
        }, onError);
    }

    public void getRecordAsync(Client client, Duration timeout, Consumer<TransactionRecord> onRecord, Consumer<HederaThrowable> onError) {
        waitForConsensusAsync(client, timeout, () -> {
            new TransactionRecordQuery()
                .setTransactionId(this)
                .executeAsync(client, timeout, onRecord, onError);
        }, onError);
    }

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
}
