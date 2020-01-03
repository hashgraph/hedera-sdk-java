package com.hedera.hashgraph.sdk.contract;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.ContractFunctionResultOrBuilder;
import com.hedera.hashgraph.sdk.Internal;
import com.hedera.hashgraph.sdk.TransactionRecord;

import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Result of invoking a contract via {@link ContractCallQuery},
 * or {@link ContractExecuteTransaction}, or the result of a contract constructor being called
 * by {@link ContractCreateTransaction}.
 *
 * Return type of {@link TransactionRecord#getContractCreateResult()}
 * and {@link TransactionRecord#getContractExecuteResult()}.
 *
 * If you require a type which is not supported here, please let us know on
 * <a href="https://github.com/hashgraph/hedera-sdk-java/issues/298>this Github issue</a>.
 */
public final class ContractFunctionResult {
    private final ByteString rawResult;

    /**
     * The ID of the contract that was invoked.
     */
    public final ContractId contractId;

    @Nullable
    public final String errorMessage;

    public final byte[] bloom;

    public final long gasUsed;

    public final List<ContractLogInfo> logs;

    @Internal
    // exposed for use in `TransactionRecord`
    public ContractFunctionResult(ContractFunctionResultOrBuilder inner) {
        rawResult = inner.getContractCallResult();

        contractId = new ContractId(inner.getContractIDOrBuilder());

        String errMsg = inner.getErrorMessage();
        errorMessage = !errMsg.isEmpty() ? errMsg : null;

        bloom = inner.getBloom().toByteArray();

        gasUsed = inner.getGasUsed();

        logs = inner.getLogInfoList().stream().map(ContractLogInfo::new).collect(Collectors.toList());
    }

    /**
     * Get the whole raw function result.
     */
    public byte[] asBytes() { return rawResult.toByteArray(); }

    /** Get the nth returned value as a string */
    public String getString(int valIndex) {
        return getDynamicBytes(valIndex).toStringUtf8();
    }

    /**
     * Get the nth value in the result as a dynamic byte array.
     */
    public byte[] getBytes(int valIndex) {
        return getDynamicBytes(valIndex).toByteArray();
    }

    private ByteString getDynamicBytes(int valIndex) {
        int offset = getInt32(valIndex);
        int len = getIntValueAt(offset);
        return getByteString(offset + 32, offset + 32 + len);
    }

    /**
     * Get the nth value as a boolean.
     */
    public boolean getBool(int valIndex) {
        return getByteBuffer(valIndex * 32 + 31).get() != 0;
    }

    /**
     * Get the nth returned value as a 32-bit integer.
     *
     * If the actual value is wider it will be truncated to the last 4 bytes (similar to Java's
     * integer narrowing semantics).
     */
    public int getInt32(int valIndex) {
        // int will be the last 4 bytes in the "value"
        return getIntValueAt(valIndex * 32);
    }

    /**
     * Get the nth returned value as a 64-bit integer.
     * <p>
     * If the actual value is wider it will be truncated to the last 8 bytes (similar to Java's
     * integer narrowing semantics).
     */
    public long getInt64(int valIndex) {
        return getByteBuffer(valIndex * 32 + 24).getLong();
    }

    /**
     * Get the nth returned value as a 256-bit integer.
     * <p>
     * This type can represent the full width of Solidity integers.
     */
    public BigInteger getInt256(int valIndex) {
        return new BigInteger(getByteString(valIndex * 32, (valIndex + 1) * 32).toByteArray());
    }

    /**
     * Get the nth returned value as a Solidity address.
     */
    public String getAddress(int valIndex) {
        final int offset = valIndex * 32;
        // address is a uint160
        return Hex.toHexString(getByteString(offset + 12, offset + 32).toByteArray());
    }

    private int getIntValueAt(int valueOffset) {
        return getByteBuffer(valueOffset + 28).getInt();
    }

    private ByteBuffer getByteBuffer(int offset) {
        // **NB** `.asReadOnlyByteBuffer()` on a substring reads from the start of the parent,
        // not the substring (bug)
        return (ByteBuffer) rawResult.asReadOnlyByteBuffer().position(offset);
    }

    private ByteString getByteString(int startIndex, int endIndex) {
        return rawResult.substring(startIndex, endIndex);
    }

}
