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
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CurrentAndNextFeeSchedule;

import javax.annotation.Nullable;

public class FeeSchedules {
    @Nullable
    private FeeSchedule current;
    @Nullable
    private FeeSchedule next;

    public FeeSchedules() {
        current = next = null;
    }

    static FeeSchedules fromProtobuf(CurrentAndNextFeeSchedule feeSchedules) {
        return new FeeSchedules()
            .setCurrent(feeSchedules.hasCurrentFeeSchedule() ? FeeSchedule.fromProtobuf(feeSchedules.getCurrentFeeSchedule()) : null)
            .setNext(feeSchedules.hasNextFeeSchedule() ? FeeSchedule.fromProtobuf(feeSchedules.getNextFeeSchedule()) : null);
    }

    public static FeeSchedules fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(CurrentAndNextFeeSchedule.parseFrom(bytes).toBuilder().build());
    }

    @Nullable
    public FeeSchedule getCurrent() {
        return current;
    }

    public FeeSchedules setCurrent(@Nullable FeeSchedule current) {
        this.current = current;
        return this;
    }

    @Nullable
    public FeeSchedule getNext() {
        return next;
    }

    public FeeSchedules setNext(@Nullable FeeSchedule next) {
        this.next = next;
        return this;
    }

    CurrentAndNextFeeSchedule toProtobuf() {
        var returnBuilder = CurrentAndNextFeeSchedule.newBuilder();
        if (current != null) {
            returnBuilder.setCurrentFeeSchedule(current.toProtobuf());
        }
        if (next != null) {
            returnBuilder.setNextFeeSchedule(next.toProtobuf());
        }
        return returnBuilder.build();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("current", getCurrent())
            .add("next", getNext())
            .toString();
    }
}
