package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hedera.hashgraph.proto.ConsensusTopicDefinitionOrBuilder;

import javax.annotation.Nullable;
import java.time.Instant;

public class ConsensusTopicDefinition {
    @Nullable
    public final String memo;

    @Nullable
    public final Instant validStartTime;

    @Nullable
    public final Instant expirationTime;

    @Nullable
    public final PublicKey adminKey;

    @Nullable
    public final PublicKey submitKey;

    public final Instant lastUpdateTime;

    public final Instant creationTime;

    public ConsensusTopicDefinition(ConsensusTopicDefinitionOrBuilder definition) {
        validStartTime = definition.hasValidStartTime() ? TimestampHelper.timestampTo(definition.getValidStartTime()) : null;

        expirationTime = definition.hasExpirationTime() ? TimestampHelper.timestampTo(definition.getExpirationTime()) : null;

        adminKey = definition.hasAdminKey() ? PublicKey.fromProtoKey(definition.getAdminKey()) : null;

        submitKey = definition.hasSubmitKey() ? PublicKey.fromProtoKey(definition.getSubmitKey()) : null;

        lastUpdateTime = TimestampHelper.timestampTo(definition.getLastUpdateTime());

        creationTime = TimestampHelper.timestampTo(definition.getCreationTime());

        String memo = definition.getMemo();
        this.memo = memo.isEmpty() ? null : memo;
    }
}
