package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.proto.*;

public class ConsensusTopicInfo {
    /** Topic identifier */
    public final ConsensusTopicId id;

    /**
     * Topic definition
     *
     * Modified by {@link ConsensusTopicCreateTransaction}, {@link ConsensusTopicUpdateTransaction},
     * and {@link ConsensusTopicDeleteTransaction}.
     */
    public final ConsensusTopicDefinition definition;

    /** Topic's state as last modified by a message */
    public final ConsensusTopicState state;

    public ConsensusTopicInfo(ConsensusGetTopicInfoResponseOrBuilder response) {
        id = new ConsensusTopicId(response.getTopicIDOrBuilder());
        definition = new ConsensusTopicDefinition(response.getTopicDefinitionOrBuilder());
        state = new ConsensusTopicState(response.getTopicStateOrBuilder());
    }

    static ConsensusTopicInfo fromResponse(Response response) {
        if (!response.hasConsensusGetTopicInfo()) {
            throw new IllegalArgumentException("response was not `consensusGetTopicInfo`");
        }

        return new ConsensusTopicInfo(response.getConsensusGetTopicInfo());
    }
}
