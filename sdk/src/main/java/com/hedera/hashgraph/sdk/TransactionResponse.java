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
import org.bouncycastle.util.encoders.Hex;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

public final class TransactionResponse implements WithGetReceipt, WithGetRecord {
    public final AccountId nodeId;

    public final byte[] transactionHash;

    public final TransactionId transactionId;

    @Nullable
    @Deprecated
    public final TransactionId scheduledTransactionId;

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

    public TransactionReceipt getReceipt(Client client) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        var receipt = new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .setNodeAccountIds(Collections.singletonList(nodeId))
                .execute(client);

        if (receipt.status != Status.SUCCESS) {
            throw new ReceiptStatusException(transactionId, receipt);
        }

        return receipt;
    }

    @Override
    public CompletableFuture<TransactionReceipt> getReceiptAsync(Client client) {
        return new TransactionReceiptQuery()
            .setTransactionId(transactionId)
            .setNodeAccountIds(Collections.singletonList(nodeId))
            .executeAsync(client);
    }

    public TransactionRecord getRecord(Client client) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        getReceipt(client);

        return new TransactionRecordQuery()
            .setTransactionId(transactionId)
            .setNodeAccountIds(Collections.singletonList(nodeId))
            .execute(client);
    }

    @Override
    public CompletableFuture<TransactionRecord> getRecordAsync(Client client) {
        return getReceiptAsync(client).thenCompose((receipt) -> new TransactionRecordQuery()
            .setTransactionId(transactionId)
            .setNodeAccountIds(Collections.singletonList(nodeId))
            .executeAsync(client)
        );
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
