package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.InvalidProtocolBufferException;

import javax.annotation.Nullable;
import java.math.BigInteger;

public class StorageChange {
    public final BigInteger slot;
    public final BigInteger valueRead;
    @Nullable
    public final BigInteger valueWritten;

    StorageChange(BigInteger slot, BigInteger valueRead, @Nullable BigInteger valueWritten) {
        this.slot = slot;
        this.valueRead = valueRead;
        this.valueWritten = valueWritten;
    }

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

    public static StorageChange fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.StorageChange.parseFrom(bytes));
    }

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

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
