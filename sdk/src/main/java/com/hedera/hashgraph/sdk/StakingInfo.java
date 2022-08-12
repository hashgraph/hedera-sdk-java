package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;

public class StakingInfo {
    /**
     * If true, the contract declines receiving a staking reward. The default value is false.
     */
    public final boolean declineStakingReward;
    /**
     * The staking period during which either the staking settings for this account or contract changed (such as starting
     * staking or changing staked_node_id) or the most recent reward was earned, whichever is later. If this account or contract
     * is not currently staked to a node, then this field is not set.
     */
    public final Instant stakePeriodStart;
    /**
     * The amount in Hbar that will be received in the next reward situation.
     */
    public final Hbar pendingReward;
    /**
     * The total of balance of all accounts staked to this account or contract.
     */
    public final Hbar stakedToMe;

    /**
     * The account to which this account or contract is staking.
     */
    @Nullable
    public final AccountId stakedAccountId;

    /**
     * The ID of the node this account or contract is staked to.
     */
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

    static StakingInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
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

    byte[] toBytes() {
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
