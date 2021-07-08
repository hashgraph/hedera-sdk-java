package com.hedera.hashgraph.sdk.token;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.sdk.Internal;
import com.hedera.hashgraph.sdk.account.AccountId;

import javax.annotation.Nullable;

@Beta
public class AssessedCustomFee {
    public final long amount;
    @Nullable
    public final TokenId tokenId;
    @Nullable
    public final AccountId feeCollectorAccountId;

    AssessedCustomFee() {
        this.amount = 0;
        this.tokenId = null;
        this.feeCollectorAccountId = null;
    }

    @Internal
    public AssessedCustomFee(com.hedera.hashgraph.proto.AssessedCustomFee assessedCustomFee) {
        this.amount = assessedCustomFee.getAmount();
        this.tokenId = assessedCustomFee.hasTokenId() ? new TokenId(assessedCustomFee.getTokenId()) : null;
        this.feeCollectorAccountId = assessedCustomFee.hasFeeCollectorAccountId() ? new AccountId(assessedCustomFee.getFeeCollectorAccountId()) : null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("amount", amount)
            .add("tokenId", tokenId)
            .add("feeCollectorAccountId", feeCollectorAccountId)
            .toString();
    }

    com.hedera.hashgraph.proto.AssessedCustomFee toProto() {
        com.hedera.hashgraph.proto.AssessedCustomFee.Builder builder = com.hedera.hashgraph.proto.AssessedCustomFee.newBuilder().setAmount(amount);
        if (tokenId != null) {
            builder.setTokenId(tokenId.toProto());
        }
        if (feeCollectorAccountId != null) {
            builder.setFeeCollectorAccountId(feeCollectorAccountId.toProto());
        }
        return builder.build();
    }

    public byte[] toBytes() {
        return toProto().toByteArray();
    }
}
