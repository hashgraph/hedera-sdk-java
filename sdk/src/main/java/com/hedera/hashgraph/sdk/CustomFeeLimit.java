// SPDX-License-Identifier: Apache-2.0
package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CustomFee;
import com.hedera.hashgraph.sdk.proto.FixedFee;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A maximum custom fee that the user is willing to pay.
 * <p>
 * This message is used to specify the maximum custom fee that given user is
 * willing to pay.
 */
public class CustomFeeLimit {

    /**
     * A payer account identifier.
     */
    public final AccountId payerId;

    /**
     * The maximum fees that the user is willing to pay for the message.
     */
    public final List<CustomFixedFee> customFees;

    /**
     * Constructor
     *
     * @param payerId       the payer's account id
     * @param customFees    maximum fees user is willing to pay
     */
    private CustomFeeLimit(AccountId payerId, List<CustomFixedFee> customFees) {
        this.payerId = payerId;
        this.customFees = customFees;
    }

    public static CustomFeeLimit fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFeeLimit customFeeLimit) {
        return new CustomFeeLimit(
                AccountId.fromProtobuf(customFeeLimit.getAccountId()),
                customFeeLimit.getFeesList().stream()
                        .map(CustomFixedFee::fromProtobuf)
                        .collect(Collectors.toList()));
    }

    public com.hedera.hashgraph.sdk.proto.CustomFeeLimit toProtobuf() {
        com.hedera.hashgraph.sdk.proto.CustomFeeLimit.Builder builder =
                com.hedera.hashgraph.sdk.proto.CustomFeeLimit.newBuilder();

        builder.setAccountId(payerId.toProtobuf());

        List<FixedFee> protoFixedFees = customFees.stream()
                .map(CustomFixedFee::toProtobuf)
                .map(CustomFee::getFixedFee)
                .collect(Collectors.toList());

        builder.addAllFees(protoFixedFees);

        return builder.build();
    }
}
