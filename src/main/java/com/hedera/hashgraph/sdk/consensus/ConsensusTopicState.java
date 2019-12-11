package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.proto.ConsensusTopicStateOrBuilder;

import javax.annotation.Nullable;
import java.time.Instant;

public class ConsensusTopicState {
    public final long sequenceNumber;

    public final byte[] runningHash;

    @Nullable
    public final Instant lastSubmitTime;

    public ConsensusTopicState(ConsensusTopicStateOrBuilder state) {
        sequenceNumber = state.getSequenceNumber();

        runningHash = state.getRunningHash().toByteArray();

        lastSubmitTime = state.hasLastSubmitTime() ? TimestampHelper.timestampTo(state.getLastSubmitTime()) : null;
    }
}
