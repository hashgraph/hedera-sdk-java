package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

abstract public class CustomFee {
    @Nullable
    private AccountId feeCollectorAccountId = null;

    CustomFee() {
    }

    static CustomFee fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee customFee, @Nullable NetworkName networkName) {
        switch(customFee.getFeeCase()) {
            case FIXED_FEE:
                return CustomFixedFee.fromProtobuf(customFee, networkName);

            case FRACTIONAL_FEE:
                return CustomFractionalFee.fromProtobuf(customFee);

            default:
                throw new IllegalStateException("CustomFee#fromProtobuf: unhandled fee case: " + customFee.getFeeCase());
        }
    }

    static CustomFee fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee customFee) {
        return fromProtobuf(customFee, null);
    }

    public static CustomFee fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee.parseFrom(bytes).toBuilder().build());
    }

    public static List<CustomFee> deepCloneList(List<CustomFee> customFees) {
        var returnCustomFees = new ArrayList<CustomFee>(customFees.size());
        for(var fee : customFees) {
            returnCustomFees.add(fee.deepClone());
        }
        return returnCustomFees;
    }

    @Nullable
    public AccountId getFeeCollectorAccountId() {
        return feeCollectorAccountId;
    }

    void doSetFeeCollectorAccountId(AccountId feeCollectorAccountId) {
        this.feeCollectorAccountId = Objects.requireNonNull(feeCollectorAccountId);
    }

    CustomFee deepClone() {
        if(this instanceof CustomFixedFee) {
            var fixedFee = (CustomFixedFee)this;
            var returnFee = new CustomFixedFee().setAmount(fixedFee.getAmount());
            if(fixedFee.getFeeCollectorAccountId() != null) {
                returnFee.setFeeCollectorAccountId(fixedFee.getFeeCollectorAccountId());
            }
            if(fixedFee.getDenominatingTokenId() != null) {
                returnFee.setDenominatingTokenId(fixedFee.getDenominatingTokenId());
            }
            return returnFee;
        } else {
            var fractionalFee = (CustomFractionalFee)this;
            var returnFee = new CustomFractionalFee()
                .setNumerator(fractionalFee.getNumerator())
                .setDenominator(fractionalFee.getDenominator())
                .setMin(fractionalFee.getMin())
                .setMax(fractionalFee.getMax());
            if(fractionalFee.getFeeCollectorAccountId() != null) {
                returnFee.setFeeCollectorAccountId(fractionalFee.getFeeCollectorAccountId());
            }
            return returnFee;
        }
    }

    void validate(Client client) {
        if(feeCollectorAccountId != null) {
            feeCollectorAccountId.validate(client);
        }
    }

    abstract com.hedera.hashgraph.sdk.proto.CustomFee toProtobuf();

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
