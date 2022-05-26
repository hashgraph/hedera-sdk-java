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

import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.InvalidProtocolBufferException;

import javax.annotation.Nullable;
import java.math.BigInteger;

/**
 * A storage slot change description.
 *
 * See <a href="https://docs.hedera.com/guides/docs/hedera-api/smart-contracts/contractcalllocal#storagechange">Hedera Documentation</a>
 */
public class StorageChange {
    /**
     * The storage slot changed. Up to 32 bytes, big-endian, zero bytes left trimmed
     */
    public final BigInteger slot;
    /**
     * The value read from the storage slot. Up to 32 bytes, big-endian, zero
     * bytes left trimmed. Because of the way SSTORE operations are charged
     * the slot is always read before being written to
     */
    public final BigInteger valueRead;
    /**
     * The new value written to the slot. Up to 32 bytes, big-endian, zero
     * bytes left trimmed. If a value of zero is written the valueWritten
     * will be present but the inner value will be absent. If a value was
     * read and not written this value will not be present.
     */
    @Nullable
    public final BigInteger valueWritten;

    /**
     * Constructor.
     *
     * @param slot                      the storage slot charged
     * @param valueRead                 the value read
     * @param valueWritten              the value written
     */
    StorageChange(BigInteger slot, BigInteger valueRead, @Nullable BigInteger valueWritten) {
        this.slot = slot;
        this.valueRead = valueRead;
        this.valueWritten = valueWritten;
    }

    /**
     * Create a storage charge from a protobuf.
     *
     * @param storageChangeProto        the protobuf
     * @return                          the new storage charge object
     */
    static StorageChange fromProtobuf(com.hedera.hashgraph.sdk.proto.StorageChange storageChangeProto) {
        return new StorageChange(
            new BigInteger(storageChangeProto.getSlot().toByteArray()),
            new BigInteger(storageChangeProto.getValueRead().toByteArray()),
            storageChangeProto.hasValueWritten() ? (
                storageChangeProto.getValueWritten().getValue().size() == 0 ?
                    BigInteger.ZERO :
                    new BigInteger(storageChangeProto.getValueWritten().getValue().toByteArray())
            ) : null
        );
    }

    /**
     * Create a storage charge from a byte array.
     *
     * @param bytes                     the byte array
     * @return                          the new storage charge object
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    public static StorageChange fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.StorageChange.parseFrom(bytes));
    }

    /**
     * Create the byte array.
     *
     * @return                          the byte array representation
     */
    com.hedera.hashgraph.sdk.proto.StorageChange toProtobuf() {
        var builder = com.hedera.hashgraph.sdk.proto.StorageChange.newBuilder()
            .setSlot(ByteString.copyFrom(slot.toByteArray()))
            .setValueRead(ByteString.copyFrom(valueRead.toByteArray()));
        if (valueWritten != null) {
            if (valueWritten.equals(BigInteger.ZERO)) {
                builder.setValueWritten(BytesValue.newBuilder().setValue(ByteString.EMPTY).build());
            } else {
                builder.setValueWritten(BytesValue.newBuilder().setValue(ByteString.copyFrom(valueWritten.toByteArray())).build());
            }
        }
        return builder.build();
    }

    /**
     * Create the byte array.
     *
     * @return                          the byte array representation
     */
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
