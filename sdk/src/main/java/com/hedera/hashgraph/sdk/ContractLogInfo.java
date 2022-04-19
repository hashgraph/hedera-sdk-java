/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ContractLoginfo;
import org.bouncycastle.util.encoders.Hex;

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
        return new ContractLogInfo(
            ContractId.fromProtobuf(logInfo.getContractID()),
            logInfo.getBloom(),
            logInfo.getTopicList(),
            logInfo.getData()
        );
    }

    public static ContractLogInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(ContractLoginfo.parseFrom(bytes));
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
