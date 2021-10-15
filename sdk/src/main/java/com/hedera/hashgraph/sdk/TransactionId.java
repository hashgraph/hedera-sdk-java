package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TransactionID;
import java8.util.concurrent.CompletableFuture;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;

import java.util.Collections;
import java.util.concurrent.TimeoutException;

import static java8.util.concurrent.CompletableFuture.completedFuture;
import static java8.util.concurrent.CompletableFuture.failedFuture;

/**
 * The client-generated ID for a transaction.
 *
 * <p>This is used for retrieving receipts and records for a transaction, for appending to a file
 * right after creating it, for instantiating a smart contract with bytecode in a file just created,
 * and internally by the network for detecting when duplicate transactions are submitted.
 */
public final class TransactionId implements WithGetReceipt, WithGetRecord {
    /**
     * The Account ID that paid for this transaction.
     */
    @Nullable
    public final AccountId accountId;

    /**
     * The time from when this transaction is valid.
     *
     * <p>When a transaction is submitted there is additionally a validDuration (defaults to 120s)
     * and together they define a time window that a transaction may be processed in.
     */
    @Nullable
    public final Instant validStart;

    boolean scheduled = false;

    /**
     * No longer part of the public API. Use `Transaction.withValidStart()` instead.
     */
    public TransactionId(@Nullable AccountId accountId, @Nullable Instant validStart) {
        this.accountId = accountId;
        this.validStart = validStart;
        this.scheduled = false;
    }

    public static TransactionId withValidStart(AccountId accountId, Instant validStart) {
        return new TransactionId(accountId, validStart);
    }

    /**
     * Generates a new transaction ID for the given account ID.
     *
     * <p>Note that transaction IDs are made of the valid start of the transaction and the account
     * that will be charged the transaction fees for the transaction.
     *
     * @param accountId the ID of the Hedera account that will be charge the transaction fees.
     * @return {@link com.hedera.hashgraph.sdk.TransactionId}
     */
    public static TransactionId generate(AccountId accountId) {
        Instant instant = Clock.systemUTC().instant().minusNanos((long) (Math.random() * 5000000000L + 8000000000L));
        return new TransactionId(accountId, instant);
    }

    static TransactionId fromProtobuf(TransactionID transactionID) {
        var accountId = transactionID.hasAccountID() ? AccountId.fromProtobuf(transactionID.getAccountID()) : null;
        var validStart = transactionID.hasTransactionValidStart() ? InstantConverter.fromProtobuf(transactionID.getTransactionValidStart()) : null;

        return new TransactionId(accountId, validStart).setScheduled(transactionID.getScheduled());
    }

    public static TransactionId fromString(String s) {
        @Var
        var parts = s.split("\\?", 2);

        var scheduled = parts.length == 2 && parts[1].equals("scheduled");

        parts = parts[0].split("@", 2);

        if (parts.length != 2) {
            throw new IllegalArgumentException("expecting {account}@{seconds}.{nanos}[?scheduled]");
        }

        @Nullable AccountId accountId = AccountId.fromString(parts[0]);

        var validStartParts = parts[1].split("\\.", 2);

        if (validStartParts.length != 2) {
            throw new IllegalArgumentException("expecting {account}@{seconds}.{nanos}");
        }

        @Nullable Instant validStart = Instant.ofEpochSecond(
            Long.parseLong(validStartParts[0]),
            Long.parseLong(validStartParts[1]));

        return new TransactionId(accountId, validStart).setScheduled(scheduled);
    }

    public static TransactionId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(TransactionID.parseFrom(bytes).toBuilder().build());
    }

    public boolean getScheduled() {
        return scheduled;
    }

    public TransactionId setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
        return this;
    }

    public TransactionReceipt getReceipt(Client client) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        var receipt = new TransactionReceiptQuery()
            .setTransactionId(this)
            .execute(client);

        if (receipt.status != Status.SUCCESS) {
            throw new ReceiptStatusException(this, receipt);
        }

        return receipt;
    }

    @Override
    @FunctionalExecutable(type = "TransactionReceipt", exceptionTypes = {"ReceiptStatusException"})
    public CompletableFuture<TransactionReceipt> getReceiptAsync(Client client) {
        return new TransactionReceiptQuery()
            .setTransactionId(this)
            .executeAsync(client)
            .thenCompose(receipt -> {
                if (receipt.status != Status.SUCCESS) {
                    return failedFuture(new ReceiptStatusException(this, receipt));
                }

                return completedFuture(receipt);
            });
    }

    public TransactionRecord getRecord(Client client) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        getReceipt(client);

        return new TransactionRecordQuery()
            .setTransactionId(this)
            .execute(client);
    }

    @Override
    @FunctionalExecutable(type = "TransactionRecord", exceptionTypes = {"ReceiptStatusException"})
    public CompletableFuture<TransactionRecord> getRecordAsync(Client client) {
        // note: we get the receipt first to ensure consensus has been reached
        return getReceiptAsync(client).thenCompose(receipt -> new TransactionRecordQuery()
            .setTransactionId(this)
            .executeAsync(client));
    }

    TransactionID toProtobuf() {
        var id = TransactionID.newBuilder();

        if (accountId != null) {
            id.setAccountID(accountId.toProtobuf());
        }

        if (validStart != null) {
            id.setTransactionValidStart(InstantConverter.toProtobuf(validStart));
        }

        return id.build();
    }

    @Override
    public String toString() {
        if (accountId != null && validStart != null) {
            return "" + accountId + "@" + validStart.getEpochSecond() + "." + validStart.getNano() + (scheduled ? "?scheduled" : "");
        } else {
            throw new IllegalStateException("`TransactionId.toString()` is non-exhaustive");
        }
    }

    public String toStringWithChecksum(Client client) {
        if (accountId != null && validStart != null) {
            return "" + accountId.toStringWithChecksum(client) + "@" + validStart.getEpochSecond() + "." + validStart.getNano() + (scheduled ? "?scheduled" : "");
        } else {
            throw new IllegalStateException("`TransactionId.toStringWithChecksum()` is non-exhaustive");
        }
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (!(object instanceof TransactionId)) {
            return false;
        }

        var id = (TransactionId) object;

        if (accountId != null && validStart != null && id.accountId != null && id.validStart != null) {
            return id.accountId.equals(accountId) && id.validStart.equals(validStart) && scheduled == id.scheduled;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
