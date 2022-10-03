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
import com.google.protobuf.InvalidProtocolBufferException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class for custom fees.
 */
abstract public class CustomFee {
    @Nullable
    protected AccountId feeCollectorAccountId = null;

    protected boolean allCollectorsAreExempt = false;

    /**
     * Constructor.
     */
    CustomFee() {
    }

    /**
     * Convert the protobuf object to a custom fee object.
     *
     * @param customFee                 protobuf response object
     * @return                          the converted custom fee object
     */
    static CustomFee fromProtobufInner(com.hedera.hashgraph.sdk.proto.CustomFee customFee) {
        switch (customFee.getFeeCase()) {
            case FIXED_FEE:
                return CustomFixedFee.fromProtobuf(customFee.getFixedFee());

            case FRACTIONAL_FEE:
                return CustomFractionalFee.fromProtobuf(customFee.getFractionalFee());

            case ROYALTY_FEE:
                return CustomRoyaltyFee.fromProtobuf(customFee.getRoyaltyFee());

            default:
                throw new IllegalStateException("CustomFee#fromProtobuf: unhandled fee case: " + customFee.getFeeCase());
        }
    }

    static CustomFee fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee customFee) {
        var outFee = fromProtobufInner(customFee);
        if (customFee.hasFeeCollectorAccountId()) {
            outFee.feeCollectorAccountId = AccountId.fromProtobuf(customFee.getFeeCollectorAccountId());
        }
        // TODO: allCollectorsAreExempt is temporarily commented out for 2.18.0, it is implemented in 2.18.0-beta.1
        //outFee.allCollectorsAreExempt = customFee.getAllCollectorsAreExempt();

        return outFee;
    }

    /**
     * Convert byte array to a custom fee object.
     *
     * @param bytes                     the byte array
     * @return                          the converted custom fee object
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    public static CustomFee fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee.parseFrom(bytes).toBuilder().build());
    }

    /**
     * Create a new copy of a custom fee list.
     *
     * @param customFees                existing custom fee list
     * @return                          new custom fee list
     */
    public static List<CustomFee> deepCloneList(List<CustomFee> customFees) {
        var returnCustomFees = new ArrayList<CustomFee>(customFees.size());
        for (var fee : customFees) {
            returnCustomFees.add(fee.deepClone());
        }
        return returnCustomFees;
    }

    /**
     * Extract the fee collector account id.
     *
     * @return                          the fee collector account id
     */
    @Nullable
    public AccountId getFeeCollectorAccountId() {
        return feeCollectorAccountId;
    }

    /**
     *
     * @return whether all fee collectors are exempt from fees
     */
    // TODO: this is temporarily made private for 2.18.0, it is public in 2.18.0-beta.1
    private boolean getAllCollectorsAreExempt() {
        return allCollectorsAreExempt;
    }

    /**
     * Create a deep clone.
     *
     * @return                          the correct cloned fee type
     */
    abstract CustomFee deepClone();

    /**
     * Verify the validity of the client object.
     *
     * @param client                    the configured client
     * @throws BadEntityIdException     if entity ID is formatted poorly
     */
    void validateChecksums(Client client) throws BadEntityIdException {
        if (feeCollectorAccountId != null) {
            feeCollectorAccountId.validateChecksum(client);
        }
    }

    /**
     * Finalize the builder into the protobuf.
     *
     * @param customFeeBuilder              the builder object
     * @return                              the protobuf
     */
    protected com.hedera.hashgraph.sdk.proto.CustomFee finishToProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee.Builder customFeeBuilder) {
        if (feeCollectorAccountId != null) {
            customFeeBuilder.setFeeCollectorAccountId(feeCollectorAccountId.toProtobuf());
        }
        // TODO: allCollectorsAreExempt is temporarily commented out for 2.18.0, it is implemented in 2.18.0-beta.1
        //customFeeBuilder.setAllCollectorsAreExempt(allCollectorsAreExempt);
        return customFeeBuilder.build();
    }

    /**
     * Create the protobuf.
     *
     * @return                              the protobuf for the custom fee object
     */
    abstract com.hedera.hashgraph.sdk.proto.CustomFee toProtobuf();

    /**
     * Create the byte array.
     *
     * @return                              the byte array representing the protobuf
     */
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    protected MoreObjects.ToStringHelper toStringHelper() {
        // TODO: allCollectorsAreExempt is temporarily commented out for 2.18.0, it is implemented in 2.18.0-beta.1
        return MoreObjects.toStringHelper(this)
            .add("feeCollectorAccountId", feeCollectorAccountId);
            //.add("allCollectorsAreExempt", allCollectorsAreExempt);
    }
}
