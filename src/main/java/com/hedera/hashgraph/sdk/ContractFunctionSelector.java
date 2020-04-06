package com.hedera.hashgraph.sdk;

import org.bouncycastle.jcajce.provider.digest.Keccak;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * Builder class for Solidity function selectors.
 */
public final class ContractFunctionSelector {
    @Nullable
    private Keccak.Digest256 digest;

    private boolean needsComma = false;

    @Nullable
    private byte[] finished = null;

    /**
     * Start building a selector for a function with a given name.
     */
    public ContractFunctionSelector(String funcName) {
        digest = new Keccak.Digest256();
        digest.update(funcName.getBytes(US_ASCII));
        digest.update((byte) '(');
    }

    public ContractFunctionSelector addString() {
        return addParamType("string");
    }

    public ContractFunctionSelector addStringArray() {
        return addParamType("string[]");
    }

    public ContractFunctionSelector addBytes() {
        return addParamType("bytes");
    }

    public ContractFunctionSelector addBytesArray() {
        return addParamType("bytes[]");
    }

    public ContractFunctionSelector addBytes32() {
        return addParamType("bytes32");
    }

    public ContractFunctionSelector addBytes32Array() {
        return addParamType("bytes32[]");
    }

    public ContractFunctionSelector addBool() {
        return addParamType("bool");
    }

    public ContractFunctionSelector addInt8() {
        return addParamType("int8");
    }

    public ContractFunctionSelector addInt32() {
        return addParamType("int32");
    }

    public ContractFunctionSelector addInt64() {
        return addParamType("int64");
    }

    public ContractFunctionSelector addInt256() {
        return addParamType("int256");
    }

    public ContractFunctionSelector addInt8Array() {
        return addParamType("int8[]");
    }

    public ContractFunctionSelector addInt32Array() {
        return addParamType("int32[]");
    }

    public ContractFunctionSelector addInt64Array() {
        return addParamType("int64[]");
    }

    public ContractFunctionSelector addInt256Array() {
        return addParamType("int256[]");
    }

    public ContractFunctionSelector addAddress() {
        return addParamType("address");
    }

    public ContractFunctionSelector addAddressArray() {
        return addParamType("address[]");
    }

    public ContractFunctionSelector addFunction() {
        return addParamType("function");
    }

    /**
     * Add a Solidity type name to this selector;
     * {@see https://solidity.readthedocs.io/en/v0.5.9/types.html}
     *
     * @param typeName the name of the Solidity type for a parameter.
     * @return {@code this} for fluent usage.
     * @throws IllegalStateException if {@link #finish()} has already been called.
     */
    ContractFunctionSelector addParamType(String typeName) {
        if (finished != null) {
            throw new IllegalStateException("FunctionSelector already finished");
        }

        Objects.requireNonNull(digest);

        if (needsComma) {
            digest.update((byte) ',');
        }

        digest.update(typeName.getBytes(US_ASCII));
        needsComma = true;

        return this;
    }

    /**
     * Complete the function selector after all parameters have been added and get the selector
     * bytes.
     * <p>
     * No more parameters may be added after this method call.
     * <p>
     * However, this can be called multiple times; it will always return the same result.
     *
     * @return the computed selector bytes.
     */
    byte[] finish() {
        if (finished == null) {
            Objects.requireNonNull(digest);
            digest.update((byte) ')');
            finished = Arrays.copyOf(digest.digest(), 4);
            // release digest state
            digest = null;
        }

        return finished;
    }
}
