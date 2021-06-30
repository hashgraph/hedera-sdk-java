package com.hedera.hashgraph.sdk.token;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.proto.CustomFees;

public class CustomFeeList {
    private boolean canUpdate = true;
    private List<CustomFee> customFees = new ArrayList<>();

    public CustomFeeList() {
    }

    CustomFeeList(com.hedera.hashgraph.proto.CustomFees customFees) throws IllegalStateException {
        canUpdate = customFees.getCanUpdateWithAdminKey();
        for(com.hedera.hashgraph.proto.CustomFee customFee : customFees.getCustomFeesList()) {
            this.customFees.add(CustomFee.fromProto(customFee));
        }
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
        CustomFeeList feeList = new CustomFeeList().setCanUpdate(getCanUpdate());
        for(CustomFee fee : customFees) {
            feeList.addCustomFee(fee.deepClone());
        }
        return feeList;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("canUpdate", getCanUpdate())
            .add("customFees", getCustomFees())
            .toString();
    }

    com.hedera.hashgraph.proto.CustomFees toProto() {
        CustomFees.Builder feesBuilder = CustomFees.newBuilder()
            .setCanUpdateWithAdminKey(this.canUpdate);
        for(CustomFee fee : customFees) {
            feesBuilder.addCustomFees(fee.toProto());
        }
        return feesBuilder.build();
    }

    byte[] toBytes() {
        return toProto().toByteArray();
    }
}
