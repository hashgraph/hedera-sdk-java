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

import com.google.protobuf.InvalidProtocolBufferException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

abstract public class CustomFee {
    @Nullable
    protected AccountId feeCollectorAccountId = null;

    CustomFee() {
    }

    static CustomFee fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee customFee) {
        switch (customFee.getFeeCase()) {
            case FIXED_FEE:
                return CustomFixedFee.fromProtobuf(customFee);

            case FRACTIONAL_FEE:
                return CustomFractionalFee.fromProtobuf(customFee);

            case ROYALTY_FEE:
                return CustomRoyaltyFee.fromProtobuf(customFee);

            default:
                throw new IllegalStateException("CustomFee#fromProtobuf: unhandled fee case: " + customFee.getFeeCase());
        }
    }

    public static CustomFee fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee.parseFrom(bytes).toBuilder().build());
    }

    public static List<CustomFee> deepCloneList(List<CustomFee> customFees) {
        var returnCustomFees = new ArrayList<CustomFee>(customFees.size());
        for (var fee : customFees) {
            returnCustomFees.add(fee.deepClone());
        }
        return returnCustomFees;
    }

    @Nullable
    public AccountId getFeeCollectorAccountId() {
        return feeCollectorAccountId;
    }

    protected void doSetFeeCollectorAccountId(AccountId feeCollectorAccountId) {
        this.feeCollectorAccountId = Objects.requireNonNull(feeCollectorAccountId);
    }

    CustomFee deepClone() {
        if (this instanceof CustomFixedFee) {
            return CustomFixedFee.clonedFrom((CustomFixedFee) this);
        } else if (this instanceof CustomFractionalFee) {
            return CustomFractionalFee.clonedFrom((CustomFractionalFee) this);
        } else {
            return CustomRoyaltyFee.clonedFrom((CustomRoyaltyFee) this);
        }
    }

    void validateChecksums(Client client) throws BadEntityIdException {
        if (feeCollectorAccountId != null) {
            feeCollectorAccountId.validateChecksum(client);
        }
    }

    protected com.hedera.hashgraph.sdk.proto.CustomFee finishToProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee.Builder customFeeBuilder) {
        if (getFeeCollectorAccountId() != null) {
            customFeeBuilder.setFeeCollectorAccountId(getFeeCollectorAccountId().toProtobuf());
        }
        return customFeeBuilder.build();
    }

    abstract com.hedera.hashgraph.sdk.proto.CustomFee toProtobuf();

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
