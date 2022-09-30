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

import com.google.common.base.MoreObjects;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.BiConsumer;
import java8.util.function.Consumer;
import org.bouncycastle.util.encoders.Hex;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * When the client sends the node a transaction of any kind, the node
 * replies with this, which simply says that the transaction passed
 * the precheck (so the node will submit it to the network) or it failed
 * (so it won't). To learn the consensus result, the client should later
 * obtain a receipt (free), or can buy a more detailed record (not free).
 *
 * See <a href="https://docs.hedera.com/guides/docs/hedera-api/miscellaneous/transactionresponse">Hedera Documentation</a>
 */
public final class TransactionResponse {
    public final AccountId nodeId;

    public final byte[] transactionHash;

    public final TransactionId transactionId;

    @Nullable
    @Deprecated
    public final TransactionId scheduledTransactionId;

    private boolean validateStatus = true;

    /**
     * Constructor.
     *
     * @param nodeId                    the node id
     * @param transactionId             the transaction id
     * @param transactionHash           the transaction hash
     * @param scheduledTransactionId    the scheduled transaction id
     */
    TransactionResponse(
        AccountId nodeId,
        TransactionId transactionId,
        byte[] transactionHash,
        @Nullable TransactionId scheduledTransactionId
    ) {
        this.nodeId = nodeId;
        this.transactionId = transactionId;
        this.transactionHash = transactionHash;
        this.scheduledTransactionId = scheduledTransactionId;
    }

    /**
     *
     * @return whether getReceipt() or getRecord() will throw an exception if the receipt status is not SUCCESS
     */
    public boolean getValidateStatus() {
        return validateStatus;
    }

    /**
     *
     * @param validateStatus whether getReceipt() or getRecord() will throw an exception if the receipt status is not SUCCESS
     * @return {@code this}
     */
    public TransactionResponse setValidateStatus(boolean validateStatus) {
        this.validateStatus = validateStatus;
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
        var receipt = getReceiptQuery()
            .execute(client, timeout)
            .validateStatus(validateStatus);

        return receipt;
    }

    public TransactionReceiptQuery getReceiptQuery() {
        return new TransactionReceiptQuery()
            .setTransactionId(transactionId)
            .setNodeAccountIds(Collections.singletonList(nodeId));
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
        return getReceiptQuery()
            .executeAsync(client, timeout)
            .thenCompose(receipt -> {
                try {
                    return CompletableFuture.completedFuture(receipt.validateStatus(validateStatus));
                } catch (ReceiptStatusException e) {
                    return CompletableFuture.failedFuture(e);
                }
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
        return getRecordQuery().execute(client, timeout);
    }

    public TransactionRecordQuery getRecordQuery() {
        return new TransactionRecordQuery()
            .setTransactionId(transactionId)
            .setNodeAccountIds(Collections.singletonList(nodeId));
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
        return getReceiptAsync(client, timeout).thenCompose((receipt) -> getRecordQuery().executeAsync(client, timeout));
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("nodeId", nodeId)
            .add("transactionHash", Hex.toHexString(transactionHash))
            .add("transactionId", transactionId)
            .toString();
    }
}
