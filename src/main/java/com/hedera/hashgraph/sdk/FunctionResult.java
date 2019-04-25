package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.proto.ContractFunctionResultOrBuilder;
import com.hedera.hashgraph.sdk.proto.ContractLoginfoOrBuilder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public final class FunctionResult {
    private final ContractFunctionResultOrBuilder inner;

    public FunctionResult(ContractFunctionResultOrBuilder inner) {
        this.inner = inner;
    }

    public ContractId getContractId() {
        return new ContractId(inner.getContractIDOrBuilder());
    }

    // The call result as the Solidity-encoded bytes, does NOT get the function result as bytes
    // RFC: do we want to remove this in favor of the strong getters below?
    public byte[] getCallResult() {
        return getByteString().toByteArray();
    }

    @Nullable
    public String getErrorMessage() {
        var errMsg = inner.getErrorMessage();
        return !errMsg.isEmpty() ? errMsg : null;
    }

    public byte[] getBloomFilter() {
        return inner.getBloom()
            .toByteArray();
    }

    public long getGasUsed() {
        return inner.getGasUsed();
    }

    public List<LogInfo> getLogs() {
        return inner.getLogInfoList()
            .stream()
            .map(LogInfo::new)
            .collect(Collectors.toList());
    }

    // get the first (or only) returned value as a string
    public String getString() {
        return getString(0);
    }

    // get the nth returned value as a string
    public String getString(int valIndex) {
        return getBytes(valIndex).toStringUtf8();
    }

    // get the first (or only) returned value as an int
    public int getInt() {
        return getInt(0);
    }

    // get the nth returned value as an int
    public int getInt(int valIndex) {
        // int will be the last 4 bytes in the "value"
        return getIntValueAt(valIndex * 32);
    }

    private int getIntValueAt(int valueOffset) {
        // **NB** `.asReadOnlyByteBuffer()` on a substring reads from the start of the parent, not the substring (bug)
        return getByteString().asReadOnlyByteBuffer()
            .getInt(valueOffset + 28);
    }

    // get a dynamic byte array with the offset at the given valIndex
    private ByteString getBytes(int valIndex) {
        var offset = getInt(valIndex);
        var len = getIntValueAt(offset);
        return getByteString().substring(offset + 32, offset + 32 + len);
    }

    private ByteString getByteString() {
        return inner.getContractCallResult();
    }

    // this only appears in this API so
    public static class LogInfo {
        public final ContractId contractId;
        public final byte[] bloomFilter;
        public final List<byte[]> topics;
        public final byte[] data;

        private LogInfo(ContractLoginfoOrBuilder logInfo) {
            contractId = new ContractId(logInfo.getContractIDOrBuilder());
            bloomFilter = logInfo.getBloom()
                .toByteArray();
            topics = logInfo.getTopicList()
                .stream()
                .map(ByteString::toByteArray)
                .collect(Collectors.toList());
            data = logInfo.getData()
                .toByteArray();
        }
    }
}
