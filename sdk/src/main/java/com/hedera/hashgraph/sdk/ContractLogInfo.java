package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ContractLoginfo;

import java.util.List;

/**
 * The log information for an event returned by a smart contract function call.
 * One function call may return several such events.
 */
public final class ContractLogInfo {
    /**
     * Address of a contract that emitted the event.
     */
    public final ContractId contractId;

    /**
     * Bloom filter for a particular log.
     */
    public final ByteString bloom;

    /**
     * Topics of a particular event.
     */
    public final List<ByteString> topics;

    /**
     * The event data.
     */
    public final ByteString data;

    private ContractLogInfo(ContractId contractId, ByteString bloom, List<ByteString> topics, ByteString data) {
        this.contractId = contractId;
        this.bloom = bloom;
        this.topics = topics;
        this.data = data;
    }

    static ContractLogInfo fromProtobuf(ContractLoginfo logInfo) {
        return new ContractLogInfo(
            ContractId.fromProtobuf(logInfo.getContractID()),
            logInfo.getBloom(),
            logInfo.getTopicList(),
            logInfo.getData()
        );
    }
}
