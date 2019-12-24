package com.hedera.hashgraph.sdk.contract;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.ContractLoginfoOrBuilder;

import java.util.List;
import java.util.stream.Collectors;

public final class ContractLogInfo {
    public final ContractId contractId;
    public final byte[] bloom;
    public final List<byte[]> topics;
    public final byte[] data;

    ContractLogInfo(ContractLoginfoOrBuilder logInfo) {
        contractId = new ContractId(logInfo.getContractIDOrBuilder());
        bloom = logInfo.getBloom()
            .toByteArray();
        topics = logInfo.getTopicList()
            .stream()
            .map(ByteString::toByteArray)
            .collect(Collectors.toList());
        data = logInfo.getData()
            .toByteArray();
    }
}
