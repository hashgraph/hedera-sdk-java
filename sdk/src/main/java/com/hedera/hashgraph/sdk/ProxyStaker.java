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
 * Information about a single account that is proxy staking.
 */
public final class ProxyStaker {
    /**
     * The Account ID that is proxy staking.
     */
    public final AccountId accountId;

    /**
     * The number of hbars that are currently proxy staked.
     */
    public final Hbar amount;

    /**
     * Constructor.
     *
     * @param accountId                 the account id
     * @param amount                    the amount
     */
    private ProxyStaker(AccountId accountId, long amount) {
        this.accountId = accountId;
        this.amount = Hbar.fromTinybars(amount);
    }

    /**
     * Create a proxy staker object from a protobuf.
     *
     * @param proxyStaker               the protobuf
     * @return                          the new proxy staker object
     */
    static ProxyStaker fromProtobuf(com.hedera.hashgraph.sdk.proto.ProxyStaker proxyStaker) {
        return new ProxyStaker(AccountId.fromProtobuf(proxyStaker.getAccountID()), proxyStaker.getAmount());
    }

    @Override
    public String toString() {
        return "ProxyStaker{" +
            "accountId=" + accountId +
            ", amount=" + amount +
            '}';
    }
}
