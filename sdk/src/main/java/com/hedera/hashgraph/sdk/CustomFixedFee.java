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

/**
 * Custom fixed fee utility class.
 * See <a href="https://docs.hedera.com/guides/docs/sdks/tokens/custom-token-fees#fixed-fee">Hedera Documentation</a>
 */
public class CustomFixedFee extends CustomFeeBase<CustomFixedFee> {
    private long amount;
    /**
     * The shard, realm, number of the tokens.
     */
    @Nullable
    private TokenId denominatingTokenId;

    /**
     * Constructor.
     */
    public CustomFixedFee() {
    }

    /**
     * Create a custom fixed fee from a fixed fee protobuf.
     *
     * @param fixedFee                  the fixed fee protobuf
     * @return                          the new custom fixed fee object
     */
    static CustomFixedFee fromProtobuf(FixedFee fixedFee) {
        var returnFee = new CustomFixedFee()
            .setAmount(fixedFee.getAmount());
        if (fixedFee.hasDenominatingTokenId()) {
            returnFee.setDenominatingTokenId(TokenId.fromProtobuf(fixedFee.getDenominatingTokenId()));
        }
        return returnFee;
    }

    @Override
    CustomFixedFee deepCloneSubclass() {
        return new CustomFixedFee()
            .setAmount(amount)
            .setDenominatingTokenId(denominatingTokenId)
            .finishDeepClone(this);
    }

    /**
     * Extract the amount.
     *
     * @return                          the amount of the fee in tiny bar
     */
    public long getAmount() {
        return amount;
    }

    /**
     * Assign the fee amount in tiny bar.
     *
     * @param amount                    the amount of the fee in tiny bar
     * @return {@code this}
     */
    public CustomFixedFee setAmount(long amount) {
        this.amount = amount;
        return this;
    }

    /**
     * Extract the fee amount.
     *
     * @return                          the fee amount in hbar
     */
    public Hbar getHbarAmount() {
        return Hbar.fromTinybars(amount);
    }

    /**
     * Assign the fee amount in hbar.
     *
     * @param amount                    the fee amount in hbar
     * @return {@code this}
     */
    public CustomFixedFee setHbarAmount(Hbar amount) {
        denominatingTokenId = null;
        this.amount = amount.toTinybars();
        return this;
    }

    /**
     * Extract the token id.
     *
     * @return                          the token id object
     */
    @Nullable
    public TokenId getDenominatingTokenId() {
        return denominatingTokenId;
    }

    /**
     * Assign the desired token id.
     *
     * @param tokenId                   the token id
     * @return {@code this}
     */
    public CustomFixedFee setDenominatingTokenId(@Nullable TokenId tokenId) {
        denominatingTokenId = tokenId;
        return this;
    }

    /**
     * Assign the default token 0.0.0.
     *
     * @return {@code this}
     */
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
        return toStringHelper()
            .add("amount", getAmount())
            .add("demoninatingTokenId", getDenominatingTokenId())
            .toString();
    }

    /**
     * Convert to a protobuf.
     *
     * @return                          the protobuf converted object
     */
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
