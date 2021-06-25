package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ContractLoginfo;
import org.bouncycastle.util.encoders.Hex;

import javax.annotation.Nullable;
import java.util.ArrayList;
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

    static ContractLogInfo fromProtobuf(com.hedera.hashgraph.sdk.proto.ContractLoginfo logInfo) {
        return ContractLogInfo.fromProtobuf(logInfo, null);
    }

    static ContractLogInfo fromProtobuf(com.hedera.hashgraph.sdk.proto.ContractLoginfo logInfo, @Nullable NetworkName networkName) {
        return new ContractLogInfo(
            ContractId.fromProtobuf(logInfo.getContractID(), networkName),
            logInfo.getBloom(),
            logInfo.getTopicList(),
            logInfo.getData()
        );
    }

    com.hedera.hashgraph.sdk.proto.ContractLoginfo toProtobuf() {
        var contractLogInfo = com.hedera.hashgraph.sdk.proto.ContractLoginfo.newBuilder()
            .setContractID(contractId.toProtobuf())
            .setBloom(bloom);

        for (ByteString topic : topics) {
            contractLogInfo.addTopic(topic);
        }

        return contractLogInfo.build();
    }

    public static ContractLogInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(ContractLoginfo.parseFrom(bytes));
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        var stringHelper = MoreObjects.toStringHelper(this)
            .add("contractId", contractId)
            .add("bloom", Hex.toHexString(bloom.toByteArray()));

        var topicList = new ArrayList<>();

        for (var topic : topics) {
            topicList.add(Hex.toHexString(topic.toByteArray()));
        }

        return stringHelper
            .add("topics", topicList)
            .toString();
    }
}
