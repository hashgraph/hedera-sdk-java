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

import java.util.Objects;

abstract class CustomFeeBase <F extends CustomFeeBase<F>> extends CustomFee {

    /**
     * Assign the fee collector account id.
     *
     * @param feeCollectorAccountId     the account id of the fee collector
     * @return {@code this}
     */
    public F setFeeCollectorAccountId(AccountId feeCollectorAccountId) {
        this.feeCollectorAccountId = Objects.requireNonNull(feeCollectorAccountId);
        // noinspection unchecked
        return (F) this;
    }

    /**
     * If true, exempts all the token's fee collection accounts from this fee.
     * (The token's treasury and the above fee_collector_account_id will always
     * be exempt. Please see <a href="https://hips.hedera.com/hip/hip-573">HIP-573</a>
     * for details.)
     *
     * @param allCollectorsAreExempt whether all fee collectors are exempt from fees
     * @return {@code this}
     */
    public F setAllCollectorsAreExempt(boolean allCollectorsAreExempt) {
        this.allCollectorsAreExempt = allCollectorsAreExempt;
        // noinspection unchecked
        return (F) this;
    }

    abstract F deepCloneSubclass();

    /**
     * Finishes the deep clone by setting the fields of the {@link CustomFeeBase} class
     *
     * @param source    the source object
     * @return the cloned object
     */
    protected F finishDeepClone(CustomFeeBase<F> source) {
        feeCollectorAccountId = source.feeCollectorAccountId;
        allCollectorsAreExempt = source.getAllCollectorsAreExempt();

        // noinspection unchecked
        return (F) this;
    }

    @Override
    CustomFee deepClone() {
        return deepCloneSubclass();
    }
}
