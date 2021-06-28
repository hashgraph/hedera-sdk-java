package com.hedera.hashgraph.sdk;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import javax.annotation.Nullable;

public class CustomFeeList {
    private boolean canUpdate = true;
    private List<CustomFee> customFees = new ArrayList<>();

    public CustomFeeList() {
    }

    static CustomFeeList fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFees customFees, @Nullable NetworkName networkName) throws IllegalStateException {
        var customFeeList = new CustomFeeList().setCanUpdate(customFees.getCanUpdateWithAdminKey());
        for(var customFee : customFees.getCustomFeesList()) {
            customFeeList.addCustomFee(CustomFee.fromProtobuf(customFee, networkName));
        }
        return customFeeList;
    }

    static CustomFeeList fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFees customFees) throws IllegalStateException {
        return fromProtobuf(customFees, null);
    }

    public static CustomFeeList fromBytes(byte[] bytes) throws Exception {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFees.parseFrom(bytes).toBuilder().build());
    }

    public boolean getCanUpdate() {
        return canUpdate;
    }

    public CustomFeeList setCanUpdate(boolean value) {
        canUpdate = value;
        return this;
    }

    public List<CustomFee> getCustomFees() {
        return customFees;
    }

    public CustomFeeList addCustomFee(CustomFee customFee) {
        customFees.add(Objects.requireNonNull(customFee));
        return this;
    }

    CustomFeeList deepClone() {
        var feeList = new CustomFeeList().setCanUpdate(getCanUpdate());
        for(var fee : customFees) {
            feeList.addCustomFee(fee.deepClone());
        }
        return feeList;
    }

    void validateNetworkOnIds(Client client) {
        for(var fee : customFees) {
            if(fee.getFeeCollectorAccountId() != null) {
                fee.getFeeCollectorAccountId().validate(client);
            }
            if(fee instanceof CustomFixedFee) {
                var fixedFee = (CustomFixedFee)fee;
                if(fixedFee.getDenominatingTokenId() != null) {
                    fixedFee.getDenominatingTokenId().validate(client);
                }
            }
        }
    }

    com.hedera.hashgraph.sdk.proto.CustomFees toProtobuf() {
        var feesBuilder = com.hedera.hashgraph.sdk.proto.CustomFees.newBuilder()
            .setCanUpdateWithAdminKey(getCanUpdate());
        for(var fee : customFees) {
            feesBuilder.addCustomFees(fee.toProtobuf());
        }
        return feesBuilder.build();
    }

    byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}