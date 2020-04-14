package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ContractLoginfo;

import java.util.List;

public final class ContractLogInfo {
    public final ContractId contractId;

    public final ByteString bloom;

    public final List<ByteString> topics;

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
