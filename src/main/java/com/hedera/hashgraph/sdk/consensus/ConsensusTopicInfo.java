package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import org.bouncycastle.util.encoders.Hex;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nullable;

public class ConsensusTopicInfo {
    public final ConsensusTopicId id;

    @Nullable
    public final String memo;

    public final long sequenceNumber;

    public final byte[] runningHash;

    @Nullable
    public final Instant expirationTime;

    @Nullable
    public final PublicKey adminKey;

    @Nullable
    public final PublicKey submitKey;

    public final Duration autoRenewPeriod;

    @Nullable
    public final AccountId autoRenewAccount;

    public ConsensusTopicInfo(com.hedera.hashgraph.proto.ConsensusGetTopicInfoResponse response) {
        id = new ConsensusTopicId(response.getTopicIDOrBuilder());

        com.hedera.hashgraph.proto.ConsensusTopicInfo info = response.getTopicInfo();

        sequenceNumber = info.getSequenceNumber();

        runningHash = info.getRunningHash().toByteArray();

        expirationTime = info.hasExpirationTime() ? TimestampHelper.timestampTo(info.getExpirationTime()) : null;

        adminKey = info.hasAdminKey() ? PublicKey.fromProtoKey(info.getAdminKey()) : null;

        submitKey = info.hasSubmitKey() ? PublicKey.fromProtoKey(info.getSubmitKey()) : null;

        String memo = info.getMemo();
        this.memo = memo.isEmpty() ? null : memo;

        autoRenewPeriod = DurationHelper.durationTo(info.getAutoRenewPeriod());

        autoRenewAccount = info.hasAutoRenewAccount() ? new AccountId(info.getAutoRenewAccount()) : null;
    }

    static ConsensusTopicInfo fromResponse(Response response) {
        if (!response.hasConsensusGetTopicInfo()) {
            throw new IllegalArgumentException("response was not `consensusGetTopicInfo`");
        }

        return new ConsensusTopicInfo(response.getConsensusGetTopicInfo());
    }

    @Override
    public String toString() {
        return "topic ID: " + id +
            " sequence number: " + sequenceNumber +
            " running hash: " + Hex.toHexString(runningHash) +
            " expiration time: " + expirationTime +
            " admin key: " + adminKey +
            " submit key: " + submitKey +
            " auto-renew period: " + autoRenewPeriod +
            " auto-renew account: " + autoRenewAccount +
            " memo: " + memo;
    }
}
