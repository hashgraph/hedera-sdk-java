package com.hedera.sdk;

import com.google.protobuf.ByteString;
import com.hedera.sdk.proto.ContractFunctionResultOrBuilder;
import com.hedera.sdk.proto.ContractLoginfoOrBuilder;

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

    public byte[] getCallResult() {
        return inner.getContractCallResult()
            .toByteArray();
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
