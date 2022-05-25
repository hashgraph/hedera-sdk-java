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
import com.hedera.hashgraph.sdk.proto.AccountAmount;

/**
 * A transfer of Hbar that occurred within a transaction.
 * <p>
 * Returned with a {@link TransactionRecord}.
 */
public final class Transfer {
    /**
     * The Account ID that sends or receives crypto-currency.
     */
    public final AccountId accountId;

    /**
     * The amount that the account sends (negative) or receives (positive).
     */
    public final Hbar amount;

    private Transfer(AccountId accountId, Hbar amount) {
        this.accountId = accountId;
        this.amount = amount;
    }

    /**
     * Create a transfer from a protobuf.
     *
     * @param accountAmount             the protobuf
     * @return                          the new transfer
     */
    static Transfer fromProtobuf(AccountAmount accountAmount) {
        return new Transfer(AccountId.fromProtobuf(accountAmount.getAccountID()), Hbar.fromTinybars(accountAmount.getAmount()));
    }

    /**
     * Create the protobuf.
     *
     * @return                          the protobuf representation
     */
    AccountAmount toProtobuf() {
        return AccountAmount.newBuilder()
            .setAccountID(accountId.toProtobuf())
            .setAmount(amount.toTinybars())
            .build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("accountId", accountId)
            .add("amount", amount)
            .toString();
    }
}
