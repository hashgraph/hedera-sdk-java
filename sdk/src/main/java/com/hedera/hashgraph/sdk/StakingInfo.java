package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;

public class StakingInfo {
    public final boolean declineStakingReward;
    public final Instant stakePeriodStart;
    public final Hbar pendingReward;
    public final Hbar stakedToMe;

    @Nullable
    public final AccountId stakedAccountId;

    @Nullable
    public final Long stakedNodeId;

    public StakingInfo(boolean declineStakingReward, Instant stakePeriodStart, Hbar pendingReward, Hbar stakedToMe, @Nullable AccountId stakedAccountId, @Nullable Long stakedNodeId) {
        this.declineStakingReward = declineStakingReward;
        this.stakePeriodStart = stakePeriodStart;
        this.pendingReward = pendingReward;
        this.stakedToMe = stakedToMe;
        this.stakedAccountId = stakedAccountId;
        this.stakedNodeId = stakedNodeId;
    }

    static StakingInfo fromProtobuf(com.hedera.hashgraph.sdk.proto.StakingInfo info) {
        return new StakingInfo(
            info.getDeclineReward(),
            InstantConverter.fromProtobuf(info.getStakePeriodStart()),
            Hbar.fromTinybars(info.getPendingReward()),
            Hbar.fromTinybars(info.getStakedToMe()),
            info.hasStakedAccountId() ? AccountId.fromProtobuf(info.getStakedAccountId()) : null,
            info.hasStakedNodeId() ? info.getStakedNodeId() : null
        );
    }

    public static StakingInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.StakingInfo.parseFrom(bytes));
    }

    com.hedera.hashgraph.sdk.proto.StakingInfo toProtobuf() {
        var builder = com.hedera.hashgraph.sdk.proto.StakingInfo.newBuilder()
            .setDeclineReward(declineStakingReward)
            .setStakePeriodStart(InstantConverter.toProtobuf(stakePeriodStart))
            .setPendingReward(pendingReward.toTinybars())
            .setStakedToMe(stakedToMe.toTinybars());

        if (stakedAccountId != null) {
            builder.setStakedAccountId(stakedAccountId.toProtobuf());
        }

        if (stakedNodeId != null) {
            builder.setStakedNodeId(stakedNodeId);
        }

        return builder.build();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("declineStakingReward", declineStakingReward)
            .add("stakePeriodStart", stakePeriodStart)
            .add("pendingReward", pendingReward)
            .add("stakedToMe", stakedToMe)
            .add("stakedAccountId", stakedAccountId)
            .add("stakedNodeId", stakedNodeId)
            .toString();
    }
}
