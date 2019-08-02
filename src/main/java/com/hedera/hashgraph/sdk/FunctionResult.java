package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.proto.ContractFunctionResultOrBuilder;
import com.hedera.hashgraph.sdk.proto.ContractLoginfoOrBuilder;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

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
        return getRawResult().toByteArray();
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

    /**
     * Get the first (or only) returned value as a string
     */
    @Deprecated(forRemoval = true)
    public String getString() {
        return getString(0);
    }

    /** Get the nth returned value as a string */
    public String getString(int valIndex) {
        return getBytes(valIndex).toStringUtf8();
    }

    /**
     * Get the nth value in the result as a dynamic byte array.
     */
    public ByteString getBytes(int valIndex) {
        var offset = getInt(valIndex);
        var len = getIntValueAt(offset);
        return getByteString(offset + 32, offset + 32 + len);
    }

    /** Get the nth returned value as a string  from byte 32*/
    public String getBytes32(int valIndex) {
        var offset = getInt(valIndex);
        return getByteString(offset + 0, offset + 32).toStringUtf8();
    }

    /**
     * Get the nth 32-byte value as an untyped byte string.
     */
    public ByteString getRawValue(int valIndex) {
        return getByteString(valIndex * 32, valIndex * 32 + 32);
    }

    /**
     * Get the nth value as a boolean.
     */
    public boolean getBool(int valIndex) {
        return getByteBuffer(valIndex * 32 + 31).get() != 0;
    }

    /**
     * Get the first (or only) returned value as an int.
     *
     * If the actual value is wider it will be truncated to the last 4 bytes (similar to Java's
     * integer narrowing semantics).
     */
    @Deprecated(forRemoval = true)
    public int getInt() {
        return getInt(0);
    }

    /**
     * Get the nth returned value as an int.
     *
     * If the actual value is wider it will be truncated to the last 4 bytes (similar to Java's
     * integer narrowing semantics).
     */
    public int getInt(int valIndex) {
        // int will be the last 4 bytes in the "value"
        return getIntValueAt(valIndex * 32);
    }

    /**
     * Get the nth returned value as a long.
     * <p>
     * If the actual value is wider it will be truncated to the last 8 bytes (similar to Java's
     * integer narrowing semantics).
     */
    public long getLong(int valIndex) {
        return getByteBuffer(valIndex * 32 + 24).getLong();
    }

    /**
     * Get the nth returned value as {@link BigInteger}.
     * <p>
     * This type can represent the full width of Solidity integers.
     */
    public BigInteger getBigInt(int valIndex) {
        return new BigInteger(getInt256(valIndex).toByteArray());
    }

    /**
     * Get the nth returned value as a Solidity address.
     */
    public ByteString getAddress(int valIndex) {
        final var offset = valIndex * 32;
        // address is a uint160
        return getByteString(offset + 12, offset + 32);
    }

    private int getIntValueAt(int valueOffset) {
        return getByteBuffer(valueOffset + 28).getInt();
    }

    private ByteString getInt256(int valIndex) {
        return getByteString(valIndex * 32, (valIndex + 1) * 32);
    }

    private ByteBuffer getByteBuffer(int offset) {
        // **NB** `.asReadOnlyByteBuffer()` on a substring reads from the start of the parent,
        // not the substring (bug)
        return getRawResult().asReadOnlyByteBuffer().position(offset);
    }

    private ByteString getRawResult() {
        return inner.getContractCallResult();
    }

    private ByteString getByteString(int startIndex, int endIndex) {
        return getRawResult().substring(startIndex, endIndex);
    }

    // this only appears in this API so
    public final static class LogInfo {
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
