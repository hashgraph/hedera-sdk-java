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
import com.hedera.hashgraph.sdk.proto.FixedFee;

import javax.annotation.Nullable;

public class CustomFixedFee extends CustomFee {
    private long amount = 0;
    @Nullable
    private TokenId denominatingTokenId = null;

    public CustomFixedFee() {
    }

    static CustomFixedFee clonedFrom(CustomFixedFee source) {
        var returnFee = new CustomFixedFee();
        returnFee.amount = source.amount;
        returnFee.denominatingTokenId = source.denominatingTokenId;
        returnFee.feeCollectorAccountId = source.feeCollectorAccountId;
        return returnFee;
    }

    static CustomFixedFee fromProtobuf(FixedFee fixedFee) {
        var returnFee = new CustomFixedFee()
            .setAmount(fixedFee.getAmount());
        if (fixedFee.hasDenominatingTokenId()) {
            returnFee.setDenominatingTokenId(TokenId.fromProtobuf(fixedFee.getDenominatingTokenId()));
        }
        return returnFee;
    }

    static CustomFixedFee fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee customFee) {
        var returnFee = fromProtobuf(customFee.getFixedFee());
        if (customFee.hasFeeCollectorAccountId()) {
            returnFee.setFeeCollectorAccountId(AccountId.fromProtobuf(customFee.getFeeCollectorAccountId()));
        }
        return returnFee;
    }

    public CustomFixedFee setFeeCollectorAccountId(AccountId feeCollectorAccountId) {
        doSetFeeCollectorAccountId(feeCollectorAccountId);
        return this;
    }

    public long getAmount() {
        return amount;
    }

    public CustomFixedFee setAmount(long amount) {
        this.amount = amount;
        return this;
    }

    public Hbar getHbarAmount() {
        return Hbar.fromTinybars(amount);
    }

    public CustomFixedFee setHbarAmount(Hbar amount) {
        denominatingTokenId = null;
        this.amount = amount.toTinybars();
        return this;
    }

    @Nullable
    public TokenId getDenominatingTokenId() {
        return denominatingTokenId;
    }

    public CustomFixedFee setDenominatingTokenId(@Nullable TokenId tokenId) {
        denominatingTokenId = tokenId;
        return this;
    }

    public CustomFixedFee setDenominatingTokenToSameToken() {
        denominatingTokenId = new TokenId(0, 0, 0);
        return this;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        super.validateChecksums(client);
        if (denominatingTokenId != null) {
            denominatingTokenId.validateChecksum(client);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("feeCollectorAccountId", getFeeCollectorAccountId())
            .add("amount", getAmount())
            .add("demoninatingTokenId", getDenominatingTokenId())
            .toString();
    }

    FixedFee toFixedFeeProtobuf() {
        var fixedFeeBuilder = FixedFee.newBuilder()
            .setAmount(getAmount());
        if (getDenominatingTokenId() != null) {
            fixedFeeBuilder.setDenominatingTokenId(getDenominatingTokenId().toProtobuf());
        }
        return fixedFeeBuilder.build();
    }

    @Override
    com.hedera.hashgraph.sdk.proto.CustomFee toProtobuf() {
        var customFeeBuilder = com.hedera.hashgraph.sdk.proto.CustomFee.newBuilder()
            .setFixedFee(toFixedFeeProtobuf());
        return finishToProtobuf(customFeeBuilder);
    }
}
