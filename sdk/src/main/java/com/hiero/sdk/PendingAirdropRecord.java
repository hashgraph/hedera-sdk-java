/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2024 Hedera Hashgraph, LLC
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
package com.hiero.sdk;

import com.google.common.base.MoreObjects;
import com.hiero.sdk.proto.PendingAirdropValue;

public class PendingAirdropRecord {
    private final PendingAirdropId pendingAirdropId;
    private final long pendingAirdropAmount;

    PendingAirdropRecord(PendingAirdropId pendingAirdropId, long pendingAirdropAmount) {
        this.pendingAirdropId = pendingAirdropId;
        this.pendingAirdropAmount = pendingAirdropAmount;
    }

    public PendingAirdropId getPendingAirdropId() {
        return pendingAirdropId;
    }

    public long getPendingAirdropAmount() {
        return pendingAirdropAmount;
    }

    com.hiero.sdk.proto.PendingAirdropRecord toProtobuf() {
        return com.hiero.sdk.proto.PendingAirdropRecord.newBuilder()
            .setPendingAirdropId(this.pendingAirdropId.toProtobuf())
            .setPendingAirdropValue(PendingAirdropValue.newBuilder().setAmount(pendingAirdropAmount))
            .build();
    }

    static PendingAirdropRecord fromProtobuf(com.hiero.sdk.proto.PendingAirdropRecord pendingAirdropRecord) {
        return new PendingAirdropRecord(
            PendingAirdropId.fromProtobuf(pendingAirdropRecord.getPendingAirdropId()),
            pendingAirdropRecord.getPendingAirdropValue().getAmount());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("pendingAirdropId", pendingAirdropId)
            .add("pendingAirdropAmount", pendingAirdropAmount)
            .toString();
    }
}
