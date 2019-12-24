package com.hedera.hashgraph.sdk.contract;

import org.bouncycastle.jcajce.provider.digest.Keccak;

import java.util.Arrays;
import java.util.Objects;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * Builder class for Solidity function selectors.
 */
@SuppressFBWarnings(value = {"EI_EXPOSE_REP"},
    justification = "we don't care about the contents of `finished`")
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

    /**
     * Add a Solidity type name to this selector;
     * {@see https://solidity.readthedocs.io/en/v0.5.9/types.html}
     *
     * @param typeName the name of the Solidity type for a parameter.
     * @return {@code this} for fluent usage.
     * @throws IllegalStateException if {@link #finish()} has already been called.
     */
    public ContractFunctionSelector addParamType(String typeName) {
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
     * Complete the function selector and return its bytes, but leave the selector in a
     * state which allows adding more parameters.
     * <p>
     * This requires copying the digest state and so is less efficient than {@link #finish()}
     * but is more efficient than throwing the selector state out and starting over
     * with the same subset of parameters.
     *
     * @return the computed selector bytes.
     */
    public byte[] finishIntermediate() {
        if (finished == null) {
            try {
                final Keccak.Digest256 resetDigest =
                    (Keccak.Digest256) Objects.requireNonNull(digest).clone();
                final byte[] ret = finish();
                digest = resetDigest;
                return ret;
            } catch (CloneNotSupportedException e) {
                throw new Error("Keccak.Digest256 should implement Cloneable", e);
            }
        }

        return finished;
    }

    /**
     * Complete the function selector after all parameters have been added and get the selector
     * bytes.
     * <p>
     * No more parameters may be added after this method call.
     * If you want to reuse the state of this selector, call {@link #finishIntermediate()}.
     * <p>
     * However, this can be called multiple times; it will always return the same result.
     *
     * @return the computed selector bytes.
     */
    public byte[] finish() {
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
