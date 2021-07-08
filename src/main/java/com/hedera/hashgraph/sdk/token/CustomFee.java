package com.hedera.hashgraph.sdk.token;

import com.google.common.annotations.Beta;
import com.hedera.hashgraph.sdk.account.AccountId;

import javax.annotation.Nullable;
import java.util.Objects;

@Beta
abstract public class CustomFee {
    @Nullable
    AccountId feeCollectorAccountId = null;

    public CustomFee() {
    }

    static CustomFee fromProto(com.hedera.hashgraph.proto.CustomFee customFee) throws IllegalStateException {
        switch(customFee.getFeeCase()) {
            case FIXED_FEE:
                return new CustomFixedFee(customFee);

            case FRACTIONAL_FEE:
                return new CustomFractionalFee(customFee);

            default:
                throw new IllegalStateException("CustomFee#fromProtobuf: unhandled fee case: " + customFee.getFeeCase());
        }
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
            CustomFixedFee fixedFee = (CustomFixedFee)this;
            return new CustomFixedFee()
                .setFeeCollectorAccountId(fixedFee.getFeeCollectorAccountId())
                .setAmount(fixedFee.getAmount())
                .setDenominatingTokenId(fixedFee.getDenominatingTokenId());
        } else {
            CustomFractionalFee fractionalFee = (CustomFractionalFee)this;
            return new CustomFractionalFee()
                .setFeeCollectorAccountId(fractionalFee.getFeeCollectorAccountId())
                .setNumerator(fractionalFee.getNumerator())
                .setDenominator(fractionalFee.getDenominator())
                .setMin(fractionalFee.getMin())
                .setMax(fractionalFee.getMax());
        }
    }

    abstract com.hedera.hashgraph.proto.CustomFee toProto();

    public byte[] toBytes() {
        return toProto().toByteArray();
    }
}
