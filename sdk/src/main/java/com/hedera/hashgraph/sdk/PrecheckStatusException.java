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

import javax.annotation.Nullable;

/**
 * Signals that a transaction has failed the pre-check.
 * <p>
 * Before a node submits a transaction to the rest of the network,
 * it attempts some cheap assertions. This process is called the "pre-check".
 */
public class PrecheckStatusException extends Exception {
    /**
     * The status of the failing transaction
     */
    public final Status status;

    /**
     * The ID of the transaction that failed.
     * <p>
     * This can be `null` if a query fails pre-check without an
     * associated payment transaction.
     */
    @Nullable
    public final TransactionId transactionId;

    PrecheckStatusException(Status status, @Nullable TransactionId transactionId) {
        this.status = status;
        this.transactionId = transactionId;
    }

    @Override
    public String getMessage() {
        var stringBuilder = new StringBuilder();

        if (transactionId != null) {
            stringBuilder.append("Hedera transaction `").append(transactionId).append("` ");
        }

        stringBuilder.append("failed pre-check with the status `").append(status).append("`");

        return stringBuilder.toString();
    }
}
