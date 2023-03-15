/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TransactionID;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.BiConsumer;
import java8.util.function.Consumer;
import org.threeten.bp.Clock;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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
public final class TransactionId implements Comparable<TransactionId> {
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

    private boolean scheduled = false;

    @Nullable
    private Integer nonce = null;

    /**
     * No longer part of the public API. Use `Transaction.withValidStart()` instead.
     *
     * @param accountId     the account id
     * @param validStart    the valid start time
     */
    public TransactionId(@Nullable AccountId accountId, @Nullable Instant validStart) {
        this.accountId = accountId;
        this.validStart = validStart;
        this.scheduled = false;
    }

    /**
     * Create a transaction id.
     *
     * @param accountId                 the account id
     * @param validStart                the valid start time
     * @return                          the new transaction id
     */
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

    /**
     * Create a transaction id from a protobuf.
     *
     * @param transactionID             the protobuf
     * @return                          the new transaction id
     */
    static TransactionId fromProtobuf(TransactionID transactionID) {
        var accountId = transactionID.hasAccountID() ? AccountId.fromProtobuf(transactionID.getAccountID()) : null;
        var validStart = transactionID.hasTransactionValidStart() ? InstantConverter.fromProtobuf(transactionID.getTransactionValidStart()) : null;

        return new TransactionId(accountId, validStart)
            .setScheduled(transactionID.getScheduled())
            .setNonce((transactionID.getNonce() != 0) ? transactionID.getNonce() : null);
    }

    /**
     * Create a new transaction id from a string.
     *
     * @param s                         the string representing the transaction id
     * @return                          the new transaction id
     */
    public static TransactionId fromString(String s) {
        @Var
        var parts = s.split("/", 2);

        var nonce = (parts.length == 2) ? Integer.parseInt(parts[1]) : null;

        parts = parts[0].split("\\?", 2);

        var scheduled = parts.length == 2 && parts[1].equals("scheduled");

        parts = parts[0].split("@", 2);

        if (parts.length != 2) {
            throw new IllegalArgumentException("expecting {account}@{seconds}.{nanos}[?scheduled][/nonce]");
        }

        @Nullable AccountId accountId = AccountId.fromString(parts[0]);

        var validStartParts = parts[1].split("\\.", 2);

        if (validStartParts.length != 2) {
            throw new IllegalArgumentException("expecting {account}@{seconds}.{nanos}");
        }

        @Nullable Instant validStart = Instant.ofEpochSecond(
            Long.parseLong(validStartParts[0]),
            Long.parseLong(validStartParts[1]));

        return new TransactionId(accountId, validStart).setScheduled(scheduled).setNonce(nonce);
    }

    /**
     * Create a new transaction id from a byte array.
     *
     * @param bytes                     the byte array
     * @return                          the new transaction id
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    public static TransactionId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(TransactionID.parseFrom(bytes).toBuilder().build());
    }

    /**
     * Extract the scheduled status.
     *
     * @return                          the scheduled status
     */
    public boolean getScheduled() {
        return scheduled;
    }

    /**
     * Assign the scheduled status.
     *
     * @param scheduled                 the scheduled status
     * @return {@code this}
     */
    public TransactionId setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
        return this;
    }

    /**
     * Extract the nonce.
     *
     * @return                          the nonce value
     */
    @Nullable
    public Integer getNonce() {
        return nonce;
    }

    /**
     * Assign the nonce value.
     *
     * @param nonce                     the nonce value
     * @return {@code this}
     */
    public TransactionId setNonce(@Nullable Integer nonce) {
        this.nonce = nonce;
        return this;
    }

    /**
     * Fetch the receipt of the transaction.
     *
     * @param client                    The client with which this will be executed.
     * @return                          the transaction receipt
     * @throws TimeoutException             when the transaction times out
     * @throws PrecheckStatusException      when the precheck fails
     * @throws ReceiptStatusException       when there is an issue with the receipt
     */
    public TransactionReceipt getReceipt(Client client) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        return getReceipt(client, client.getRequestTimeout());
    }

    /**
     * Fetch the receipt of the transaction.
     *
     * @param client                    The client with which this will be executed.
     * @param timeout The timeout after which the execution attempt will be cancelled.
     * @return                          the transaction receipt
     * @throws TimeoutException             when the transaction times out
     * @throws PrecheckStatusException      when the precheck fails
     * @throws ReceiptStatusException       when there is an issue with the receipt
     */
    public TransactionReceipt getReceipt(Client client, Duration timeout) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        var receipt = new TransactionReceiptQuery()
            .setTransactionId(this)
            .execute(client, timeout);

        if (receipt.status != Status.SUCCESS) {
            throw new ReceiptStatusException(this, receipt);
        }

        return receipt;
    }

    /**
     * Fetch the receipt of the transaction asynchronously.
     *
     * @param client                    The client with which this will be executed.
     * @return                          future result of the transaction receipt
     */
    public CompletableFuture<TransactionReceipt> getReceiptAsync(Client client) {
        return getReceiptAsync(client, client.getRequestTimeout());
    }

    /**
     * Fetch the receipt of the transaction asynchronously.
     *
     * @param client                    The client with which this will be executed.
     * @param timeout The timeout after which the execution attempt will be cancelled.
     * @return                          the transaction receipt
     */
    public CompletableFuture<TransactionReceipt> getReceiptAsync(Client client, Duration timeout) {
        return new TransactionReceiptQuery()
            .setTransactionId(this)
            .executeAsync(client, timeout)
            .thenCompose(receipt -> {
                if (receipt.status != Status.SUCCESS) {
                    return failedFuture(new ReceiptStatusException(this, receipt));
                }

                return completedFuture(receipt);
            });
    }

    /**
     * Fetch the receipt of the transaction asynchronously.
     *
     * @param client                    The client with which this will be executed.
     * @param callback a BiConsumer which handles the result or error.
     */
    public void getReceiptAsync(Client client, BiConsumer<TransactionReceipt, Throwable> callback) {
        ConsumerHelper.biConsumer(getReceiptAsync(client), callback);
    }

    /**
     * Fetch the receipt of the transaction asynchronously.
     *
     * @param client                    The client with which this will be executed.
     * @param timeout The timeout after which the execution attempt will be cancelled.
     * @param callback a BiConsumer which handles the result or error.
     */
    public void getReceiptAsync(Client client, Duration timeout, BiConsumer<TransactionReceipt, Throwable> callback) {
        ConsumerHelper.biConsumer(getReceiptAsync(client, timeout), callback);
    }

    /**
     * Fetch the receipt of the transaction asynchronously.
     *
     * @param client                    The client with which this will be executed.
     * @param onSuccess a Consumer which consumes the result on success.
     * @param onFailure a Consumer which consumes the error on failure.
     */
    public void getReceiptAsync(Client client, Consumer<TransactionReceipt> onSuccess, Consumer<Throwable> onFailure) {
        ConsumerHelper.twoConsumers(getReceiptAsync(client), onSuccess, onFailure);
    }

    /**
     * Fetch the receipt of the transaction asynchronously.
     *
     * @param client                    The client with which this will be executed.
     * @param timeout The timeout after which the execution attempt will be cancelled.
     * @param onSuccess a Consumer which consumes the result on success.
     * @param onFailure a Consumer which consumes the error on failure.
     */
    public void getReceiptAsync(Client client, Duration timeout, Consumer<TransactionReceipt> onSuccess, Consumer<Throwable> onFailure) {
        ConsumerHelper.twoConsumers(getReceiptAsync(client, timeout), onSuccess, onFailure);
    }

    /**
     * Fetch the record of the transaction.
     *
     * @param client                    The client with which this will be executed.
     * @return                          the transaction record
     * @throws TimeoutException             when the transaction times out
     * @throws PrecheckStatusException      when the precheck fails
     * @throws ReceiptStatusException       when there is an issue with the receipt
     */
    public TransactionRecord getRecord(Client client) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        return getRecord(client, client.getRequestTimeout());
    }

    /**
     * Fetch the record of the transaction.
     *
     * @param client                    The client with which this will be executed.
     * @param timeout The timeout after which the execution attempt will be cancelled.
     * @return                          the transaction record
     * @throws TimeoutException             when the transaction times out
     * @throws PrecheckStatusException      when the precheck fails
     * @throws ReceiptStatusException       when there is an issue with the receipt
     */
    public TransactionRecord getRecord(Client client, Duration timeout) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        getReceipt(client, timeout);

        return new TransactionRecordQuery()
            .setTransactionId(this)
            .execute(client, timeout);
    }

    /**
     * Fetch the record of the transaction asynchronously.
     *
     * @param client                    The client with which this will be executed.
     * @return                          future result of the transaction record
     */
    public CompletableFuture<TransactionRecord> getRecordAsync(Client client) {
        return getRecordAsync(client, client.getRequestTimeout());
    }

    /**
     * Fetch the record of the transaction asynchronously.
     *
     * @param client                    The client with which this will be executed.
     * @param timeout The timeout after which the execution attempt will be cancelled.
     * @return                          future result of the transaction record
     */
    public CompletableFuture<TransactionRecord> getRecordAsync(Client client, Duration timeout) {
        // note: we get the receipt first to ensure consensus has been reached
        return getReceiptAsync(client, timeout).thenCompose(receipt -> new TransactionRecordQuery()
            .setTransactionId(this)
            .executeAsync(client, timeout));
    }

    /**
     * Fetch the record of the transaction asynchronously.
     *
     * @param client                    The client with which this will be executed.
     * @param callback a BiConsumer which handles the result or error.
     */
    public void getRecordAsync(Client client, BiConsumer<TransactionRecord, Throwable> callback) {
        ConsumerHelper.biConsumer(getRecordAsync(client), callback);
    }

    /**
     * Fetch the record of the transaction asynchronously.
     *
     * @param client                    The client with which this will be executed.
     * @param timeout The timeout after which the execution attempt will be cancelled.
     * @param callback a BiConsumer which handles the result or error.
     */
    public void getRecordAsync(Client client, Duration timeout, BiConsumer<TransactionRecord, Throwable> callback) {
        ConsumerHelper.biConsumer(getRecordAsync(client, timeout), callback);
    }

    /**
     * Fetch the record of the transaction asynchronously.
     *
     * @param client                    The client with which this will be executed.
     * @param onSuccess a Consumer which consumes the result on success.
     * @param onFailure a Consumer which consumes the error on failure.
     */
    public void getRecordAsync(Client client, Consumer<TransactionRecord> onSuccess, Consumer<Throwable> onFailure) {
        ConsumerHelper.twoConsumers(getRecordAsync(client), onSuccess, onFailure);
    }

    /**
     * Fetch the record of the transaction asynchronously.
     *
     * @param client                    The client with which this will be executed.
     * @param timeout The timeout after which the execution attempt will be cancelled.
     * @param onSuccess a Consumer which consumes the result on success.
     * @param onFailure a Consumer which consumes the error on failure.
     */
    public void getRecordAsync(Client client, Duration timeout, Consumer<TransactionRecord> onSuccess, Consumer<Throwable> onFailure) {
        ConsumerHelper.twoConsumers(getRecordAsync(client, timeout), onSuccess, onFailure);
    }

    /**
     * Extract the transaction id protobuf.
     *
     * @return                          the protobuf representation
     */
    TransactionID toProtobuf() {
        var id = TransactionID.newBuilder()
            .setScheduled(scheduled)
            .setNonce((nonce != null) ? nonce : 0);

        if (accountId != null) {
            id.setAccountID(accountId.toProtobuf());
        }

        if (validStart != null) {
            id.setTransactionValidStart(InstantConverter.toProtobuf(validStart));
        }

        return id.build();
    }

    private String toStringPostfix() {
        Objects.requireNonNull(validStart);
        return "@" + validStart.getEpochSecond() + "." + validStart.getNano() +
            (scheduled ? "?scheduled" : "") + ((nonce != null) ? "/" + nonce : "");
    }

    @Override
    public String toString() {
        if (accountId != null && validStart != null) {
            return "" + accountId + toStringPostfix();
        } else {
            throw new IllegalStateException("`TransactionId.toString()` is non-exhaustive");
        }
    }

    /**
     * Convert to a string representation with checksum.
     *
     * @param client                    the configured client
     * @return                          the string representation with checksum
     */
    public String toStringWithChecksum(Client client) {
        if (accountId != null && validStart != null) {
            return "" + accountId.toStringWithChecksum(client) + toStringPostfix();
        } else {
            throw new IllegalStateException("`TransactionId.toStringWithChecksum()` is non-exhaustive");
        }
    }

    /**
     * Extract the byte array representation.
     *
     * @return                          the byte array representation
     */
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public boolean equals( Object object) {
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

    @Override
    public int compareTo(TransactionId o) {
        Objects.requireNonNull(o);
        if (scheduled != o.scheduled) {
            return scheduled ? 1 : -1;
        }
        var thisAccountIdIsNull = (accountId == null);
        var otherAccountIdIsNull = (o.accountId == null);
        if (thisAccountIdIsNull != otherAccountIdIsNull) {
            return thisAccountIdIsNull ? -1 : 1;
        }
        if (!thisAccountIdIsNull) {
            int accountIdComparison = accountId.compareTo(o.accountId);
            if (accountIdComparison != 0) {
                return accountIdComparison;
            }
        }
        var thisStartIsNull = (validStart == null);
        var otherStartIsNull = (o.validStart == null);
        if (thisStartIsNull != otherStartIsNull) {
            return thisAccountIdIsNull ? -1 : 1;
        }
        if (!thisStartIsNull) {
            return validStart.compareTo(o.validStart);
        }
        return 0;
    }
}
