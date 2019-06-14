package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.DecoderException;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

import static java.nio.charset.StandardCharsets.US_ASCII;

// an implementation of function selector and parameter encoding as specified here:
// https://solidity.readthedocs.io/en/v0.5.7/abi-spec.html#
public final class CallParams<Kind> {

    private final FunctionSelector funcSelector;
    private final ArrayList<Argument> args = new ArrayList<>();

    private CallParams(@Nullable String funcName) {
        funcSelector = new FunctionSelector(funcName);
    }

    public static CallParams<Constructor> constructor() {
        return new CallParams<>(null);
    }

    public static CallParams<Function> function(String funcName) {
        return new CallParams<>(funcName);
    }

    /**
     * Add a parameter of type {@code string}.
     * <p>
     * For Solidity addresses, use {@link #addAddress(byte[])}.
     *
     * @param param
     * @return
     * @see #addAddress(byte[])
     */
    public CallParams<Kind> add(String param) {
        var strBytes = ByteString.copyFromUtf8(param);

        funcSelector.addParamType("string");
        args.add(new Argument(strBytes.size(), strBytes));

        return this;
    }

    public CallParams<Kind> add(byte[] param) {
        var bytes = ByteString.copyFrom(param);

        funcSelector.addParamType("bytes");
        args.add(new Argument(param.length, bytes));

        return this;
    }

    public CallParams<Kind> add(boolean bool) {
        funcSelector.addParamType("bool");
        // boolean encodes to `uint8` of values [0, 1]
        args.add(new Argument(int256(bool ? 1 : 0, 8)));
        return this;
    }

    /**
     * Add an 8-bit integer as an unsigned param, inferring a type of {@code int8}.
     */
    public CallParams<Kind> add(byte int8) {
        return add(int8, 8);
    }

    /**
     * Add an 16-bit integer as an unsigned param, inferring a type of {@code int16}.
     */
    public CallParams<Kind> add(short int16) {
        return add(int16, 16);
    }

    /**
     * Add an 32-bit integer as an unsigned param, inferring a type of {@code int32}.
     */
    public CallParams<Kind> add(int int32) {
        return add(int32, 32);
    }

    /**
     * Add an 64-bit integer as an unsigned param, inferring a type of {@code int64}.
     */
    public CallParams<Kind> add(long int64) {
        return add(int64, 64);
    }

    /**
     * Add an integer as an signed {@code intN} param, explicitly setting the parameter width.
     * <p>
     * The value will be truncated to the last {@code width} bits, the same as Java's
     * behavior when casting from a larger integer type to a smaller one.
     *
     * @param width the nominal bit width for encoding the integer type in the function funcSelector,
     *              e.g. {@code width = 128} produces a param type of {@code int128};
     *              must be a multiple of 8 and between 8 and 256.
     * @throws IllegalArgumentException if {@code width} is not in a valid range (see above).
     */
    public CallParams<Kind> add(long i64, int width) {
        checkIntWidth(width);
        if (width > 64) {
            throw new IllegalArgumentException("integer width > 64");
        }

        funcSelector.addParamType("int" + width);
        args.add(new Argument(int256(i64, width)));

        return this;
    }

    /**
     * Add an arbitrary precision integer as a signed {@code intN} param, explicitly
     * setting the parameter width.
     *
     * @param width the nominal bit width for encoding the integer type in the function funcSelector,
     *              e.g. {@code width = 128} produces a param type of {@code int128};
     *              must be a multiple of 8 and between 8 and 256.
     * @throws IllegalArgumentException if {@code uint.bitLength() > 256}
     *                                  (cannot be represented as a Solidity integer type),
     *                                  {@code width < uint.bitLength()} or {@code width} is not in a valid range (see above).
     */
    public CallParams<Kind> add(BigInteger bigInt, int width) {
        checkBigInt(bigInt, width);

        final var bytes = bigInt.toByteArray();

        funcSelector.addParamType("int" + width);
        args.add(new Argument(leftPad32(bytes, bigInt.signum() < 0)));

        return this;
    }

    private static void checkIntWidth(int width) {
        if (width % 8 != 0 || width < 8 || width > 256) {
            throw new IllegalArgumentException(
                "Solidity integer width must be a multiple of 8, in the closed range [8, 256]");
        }
    }

    private static void checkUnsignedVal(long unsignedVal) {
        if (unsignedVal < 0) {
            throw new IllegalArgumentException("addUnsigned() does not accept negative values");
        }
    }

    private static void checkBigInt(BigInteger val, int width) {
        checkIntWidth(width);

        if (val.bitLength() > 256) {
            throw new IllegalArgumentException("BigInteger out of range for Solidity uint256");
        }

        if (width < val.bitLength()) {
            throw new IllegalArgumentException(
                "BigInteger.bitLength() is greater than the nominal parameter width");
        }
    }

    /**
     * Add an 8-bit non-negative integer as an unsigned param, inferring a type of {@code uint8}.
     *
     * @throws IllegalArgumentException if {@code uint8 < 0}
     */
    public CallParams<Kind> addUnsigned(@Nonnegative byte uint8) {
        return addUnsigned(uint8, 8);
    }

    /**
     * Add a 16-bit non-negative integer as an unsigned param, inferring a type of {@code uint16}.
     *
     * @throws IllegalArgumentException if {@code uint16 < 0}
     */
    public CallParams<Kind> addUnsigned(@Nonnegative short uint16) {
        return addUnsigned(uint16, 16);
    }

    /**
     * Add a 32-bit non-negative integer as an unsigned param, inferring a type of {@code uint32}.
     *
     * @throws IllegalArgumentException if {@code uint32 < 0}
     */
    public CallParams<Kind> addUnsigned(@Nonnegative int uint32) {
        return addUnsigned(uint32, 32);
    }

    /**
     * Add a 64-bit non-negative integer as an unsigned param, inferring a type of {@code uint64}.
     *
     * @throws IllegalArgumentException if {@code uint64 < 0}
     */
    public CallParams<Kind> addUnsigned(@Nonnegative long uint64) {
        return addUnsigned(uint64, 64);
    }

    /**
     * Add a non-negative integer as an unsigned {@code uintN} param,
     * explicitly setting the parameter width.
     * <p>
     * The value will be truncated to the last {@code width} bits, the same as Java's
     * behavior when casting from a larger integer type to a smaller one.
     *
     * @param width the nominal bit width for encoding the integer type in the function funcSelector,
     *              e.g. {@code width = 128} produces a param type of {@code uint128};
     *              must be a multiple of 8 and between 8 and 256.
     * @throws IllegalArgumentException if {@code uint < 0},
     *                                  or {@code width} is not in a valid range (see above).
     */
    public CallParams<Kind> addUnsigned(@Nonnegative long uint, int width) {
        checkIntWidth(width);
        checkUnsignedVal(uint);

        funcSelector.addParamType("uint" + width);
        args.add(new Argument(int256(uint, width)));

        return this;
    }

    /**
     * Add an arbitrary precision non-negative integer as an unsigned {@code uintN} param,
     * explicitly setting the parameter width.
     *
     * @param width the nominal bit width for encoding the integer type in the function funcSelector,
     *              e.g. {@code width = 128} produces a param type of {@code uint128};
     *              must be a multiple of 8 and between 8 and 256.
     * @throws IllegalArgumentException if {@code uint.signum() < 0},
     *                                  if {@code uint.bitLength() > 256} (cannot be represented as a Solidity integer type),
     *                                  {@code width < uint.bitLength()} or {@code width} is not in a valid range (see above).
     */
    public CallParams<Kind> addUnsigned(@Nonnegative BigInteger uint, int width) {
        checkBigInt(uint, width);
        checkUnsignedVal(uint.signum());

        final var bytes = uint.toByteArray();

        funcSelector.addParamType("uint" + (bytes.length * 8));
        args.add(new Argument(leftPad32(bytes, false)));

        return this;
    }

    /**
     * The length of a Solidity address in bytes.
     */
    public static final int ADDRESS_LEN = 20;
    /**
     * The length of a hexadecimal-encoded Solidity address, in ASCII characters (bytes).
     */
    public static final int ADDRESS_LEN_HEX = ADDRESS_LEN * 2;

    private static void checkAddressLen(byte[] address) {
        if (address.length != ADDRESS_LEN) {
            throw new IllegalArgumentException(
                "solidity addresses must be 20 bytes or 40 hex chars");
        }
    }

    private static byte[] decodeAddress(String address) {
        if (address.length() != ADDRESS_LEN_HEX) {
            throw new IllegalArgumentException(
                "solidity addresses must be 20 bytes or 40 hex chars");
        }

        try {
            return Hex.decode(address);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("failed to decode Solidity address as hex", e);
        }
    }

    /**
     * Add a {@value ADDRESS_LEN}-byte Solidity address parameter with the type {@code address}.
     * <p>
     * Note: adding a {@code address payable} or {@code contract} parameter must also use
     * this function as the ABI does not support those types directly.
     *
     * @return
     * @throws IllegalArgumentException if the address is not exactly {@value ADDRESS_LEN} bytes
     *                                  long.
     */
    public CallParams<Kind> addAddress(byte[] address) {
        checkAddressLen(address);

        funcSelector.addParamType("address");
        // address encodes as `uint160`
        args.add(new Argument(leftPad32(ByteString.copyFrom(address))));

        return this;
    }

    /**
     * Add a {@value ADDRESS_LEN * 2}-character hex-encoded Solidity address parameter with the type
     * {@code address}.
     * <p>
     * Note: adding a {@code address payable} or {@code contract} parameter must also use
     * this function as the ABI does not support those types directly.
     *
     * @return
     * @throws IllegalArgumentException if the address is not exactly {@value ADDRESS_LEN_HEX}
     *                                  characters long or fails to decode as hexadecimal.
     */
    public CallParams<Kind> addAddress(String address) {
        return addAddress(decodeAddress(address));
    }

    private static final int SELECTOR_LEN = 4;

    /**
     * Add a Solidity function reference as a {@value ADDRESS_LEN}-byte contract address and a
     * {@value SELECTOR_LEN}-byte function funcSelector.
     *
     * @return
     * @throws IllegalArgumentException if {@code address} is not {@value ADDRESS_LEN} bytes or
     *                                  {@code funcSelector} is not {@value SELECTOR_LEN} bytes.
     */
    public CallParams<Kind> addFunction(byte[] address, byte[] selector) {
        checkAddressLen(address);

        if (selector.length != SELECTOR_LEN) {
            throw new IllegalArgumentException("function selectors must be 4 bytes");
        }

        final var output = ByteString.newOutput(ADDRESS_LEN + SELECTOR_LEN);
        output.write(address, 0, address.length);
        output.write(selector, 0, selector.length);

        funcSelector.addParamType("function");
        // function reference encodes as `bytes24`
        args.add(new Argument(rightPad32(output.toByteString())));

        return this;
    }

    /**
     * Add a Solidity function reference as a {@value ADDRESS_LEN}-byte contract address and a
     * {@value SELECTOR_LEN}-byte function funcSelector.
     *
     * @return
     * @throws IllegalArgumentException if {@code address} is not {@value ADDRESS_LEN_HEX}
     *                                  characters or {@code funcSelector} is not
     *                                  {@value SELECTOR_LEN} bytes.
     */
    public CallParams<Kind> addFunction(String address, byte[] selector) {
        return addFunction(decodeAddress(address), selector);
    }

    /**
     * Add a Solidity function reference as a {@value ADDRESS_LEN}-byte contract address and a
     * constructed {@link FunctionSelector}.
     *
     * @return
     * @throws IllegalArgumentException if {@code address} is not {@value ADDRESS_LEN_HEX}
     *                                  characters or {@code funcSelector} is not
     *                                  {@value SELECTOR_LEN} bytes.
     */
    public CallParams<Kind> addFunction(String address, FunctionSelector selector) {
        return addFunction(decodeAddress(address), selector.finish());
    }

    /**
     * Builder class for Solidity function selectors.
     */
    public static class FunctionSelector {

        @Nullable
        private Keccak.Digest256 digest;

        private boolean needsComma = false;

        @Nullable
        private byte[] finished = null;

        private FunctionSelector(@Nullable String funcName) {
            digest = new Keccak.Digest256();

            if (funcName != null) {
                digest.update(funcName.getBytes(US_ASCII));
            }

            digest.update((byte) '(');
        }

        /**
         * Start building a selector for a function with a given name.
         */
        public static FunctionSelector function(String funcName) {
            return new FunctionSelector(funcName);
        }

        /**
         * Start building a selector for an unnamed constructor.
         */
        public static FunctionSelector constructor() {
            return new FunctionSelector(null);
        }

        /**
         * Add a Solidity type name to this selector;
         * {@see https://solidity.readthedocs.io/en/v0.5.9/types.html}
         *
         * @param typeName the name of the Solidity type for a parameter.
         * @return {@code this} for fluent usage.
         * @throws IllegalStateException if {@link #finish()} has already been called.
         */
        public FunctionSelector addParamType(String typeName) {
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
                    final var resetDigest =
                        (Keccak.Digest256) Objects.requireNonNull(digest).clone();
                    final var ret = finish();
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

    /**
     * Get the encoding of the currently added parameters as a {@link ByteString}.
     * <p>
     * You may continue to add parameters and call this again.
     *
     * @return the Solidity encoding of the call parameters in the order they were added.
     */
    public ByteString toProto() {
        // offset for dynamic-length data, immediately after value arguments
        var dynamicOffset = args.size() * 32;

        var paramsBytes = new ArrayList<ByteString>(args.size() + 1);

        // use `finishIntermediate()` so this object can continue being used
        paramsBytes.add(ByteString.copyFrom(funcSelector.finishIntermediate()));

        var dynamicArgs = new ArrayList<ByteString>();

        // iterate the arguments and determine whether they are dynamic or not
        for (var arg : args) {
            if (arg.isDynamic) {
                // dynamic arguments supply their offset in value position and append their data at that offset
                paramsBytes.add(int256(dynamicOffset, 256));
                dynamicArgs.add(arg.value);
                dynamicOffset += arg.len;
            } else {
                // value arguments are dropped in the current arg position
                paramsBytes.add(arg.value);
            }
        }

        paramsBytes.addAll(dynamicArgs);

        return ByteString.copyFrom(paramsBytes);
    }

    // padding that we can substring without new allocations
    private static final ByteString padding = ByteString.copyFrom(new byte[31]);
    private static final ByteString negativePadding;

    static {
        final var fill = new byte[31];
        Arrays.fill(fill, (byte) 0xFF);
        negativePadding = ByteString.copyFrom(fill);
    }

    static ByteString int256(long val, int width) {
        final var output = ByteString.newOutput(width);

        for (int i = width - 8; i >= 0; i -= 8) {
            // write bytes in big-endian order
            // widening conversion sign-extends so we don't have to do anything special when
            // truncating a previously widened value
            output.write((byte) val >> i);
        }

        return leftPad32(output.toByteString(), val < 0);
    }

    // Solidity contracts require all parameters to be padded to 32 byte multiples but specifies
    // different requirements for padding for strings/byte arrays vs integers

    static ByteString leftPad32(ByteString input) {
        return leftPad32(input, false);
    }

    static ByteString leftPad32(ByteString input, boolean negative) {
        var rem = 32 - input.size() % 32;
        return rem == 32
            ? input
            : (negative ? negativePadding : padding).substring(0, rem)
            .concat(input);
    }

    static ByteString leftPad32(byte[] input, boolean negative) {
        return leftPad32(ByteString.copyFrom(input), negative);
    }

    static ByteString rightPad32(ByteString input) {
        var rem = 32 - input.size() % 32;
        return rem == 32 ? input : input.concat(padding.substring(0, rem));
    }

    // some Rust-inspired type magic
    public final static class Constructor {
        private Constructor() {
        }
    }

    public final static class Function {
        private Function() {
        }
    }

    private final static class Argument {
        private final ByteString value;
        private final int len;
        private final boolean isDynamic;

        // value constructor
        private Argument(ByteString value) {
            if (value.size() != 32) {
                throw new IllegalArgumentException("value argument that was not 32 bytes");
            }

            this.value = value;
            this.len = 0;
            this.isDynamic = false;
        }

        // dynamic constructor
        private Argument(int len, ByteString dynamic) {
            var lenBytes = int256(len, 256);
            this.len = len;
            this.value = lenBytes.concat(rightPad32(dynamic));
            isDynamic = true;
        }
    }

}
