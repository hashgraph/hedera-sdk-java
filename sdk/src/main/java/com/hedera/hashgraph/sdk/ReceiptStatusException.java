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

/**
 * An Exception thrown on error status by {@link TransactionId#getReceipt(Client)}.
 * <p>
 * The receipt is included, though only the {@link TransactionReceipt#status} field will be
 * initialized; all the getters should throw.
 */
public class ReceiptStatusException extends Exception {
    /**
     * The ID of the transaction that failed, in case that context is no longer available
     * (e.g. the exception was bubbled up).
     */
    public final TransactionId transactionId;

    /**
     * The receipt of the transaction that failed; the only initialized field is
     * {@link TransactionReceipt#status}.
     */
    public final TransactionReceipt receipt;

    /**
     * Constructor.
     *
     * @param transactionId             the transaction id
     * @param receipt                   the receipt
     */
    ReceiptStatusException(TransactionId transactionId, TransactionReceipt receipt) {
        this.transactionId = transactionId;
        this.receipt = receipt;
    }

    @Override
    public String getMessage() {
        return "receipt for transaction " + transactionId + " raised status " + receipt.status;
    }
}
