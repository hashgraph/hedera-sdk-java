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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static java.nio.charset.StandardCharsets.US_ASCII;

// an implementation of function selector and parameter encoding as specified here:
// https://solidity.readthedocs.io/en/v0.5.7/abi-spec.html#
public final class CallParams<Kind> {

    private final FunctionSelector funcSelector;
    private final ArrayList<Argument> args = new ArrayList<>();

    private CallParams(FunctionSelector funcSelector) {
        this.funcSelector = funcSelector;
    }

    public static CallParams<Constructor> constructor() {
        return new CallParams<>(FunctionSelector.constructor());
    }

    public static CallParams<Function> function(String funcName) {
        return new CallParams<>(FunctionSelector.function(funcName));
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
    public CallParams<Kind> addString(String param) {
        var strBytes = ByteString.copyFromUtf8(param);

        funcSelector.addParamType("string");
        args.add(new Argument(strBytes.size(), strBytes));

        return this;
    }

    public CallParams<Kind> addBytes(byte[] param) {
        var bytes = ByteString.copyFrom(param);

        funcSelector.addParamType("bytes");
        args.add(new Argument(param.length, bytes));

        return this;
    }

    public CallParams<Kind> addBool(boolean bool) {
        funcSelector.addParamType("bool");
        // boolean encodes to `uint8` of values [0, 1]
        args.add(new Argument(int256(bool ? 1 : 0, 8)));
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
            throw new IllegalArgumentException("addUint() does not accept negative values");
        }
    }

    private static void checkBigInt(BigInteger val, int width, boolean signed) {
        checkIntWidth(width);

        // bitLength() does not include the sign bit
        final var actualBitLen = val.bitLength() + (signed ? 1 : 0);

        if (actualBitLen > 256) {
            throw new IllegalArgumentException("BigInteger out of range for Solidity integers");
        }

        if (width < actualBitLen) {
            throw new IllegalArgumentException(
                "BigInteger.bitLength() is greater than the nominal parameter width");
        }
    }

    /**
     * Add an integer as an signed {@code intN} param, explicitly setting the parameter width.
     * <p>
     * The value will be truncated to the last {@code width} bits, the same as Java's
     * behavior when casting from a larger integer type to a smaller one. When passing a smaller
     * integer type, Java will widen it by sign-extending so if it is truncated again it should
     * still result in the same two's complement value.
     *
     * @param width the nominal bit width for encoding the integer type in the function selector,
     *              e.g. {@code width = 128} produces a param type of {@code int128};
     *              must be a multiple of 8 and between 8 and 256.
     * @throws IllegalArgumentException if {@code width} is not in a valid range (see above).
     */
    public CallParams<Kind> addInt(long i64, int width) {
        checkIntWidth(width);

        funcSelector.addParamType("int" + width);
        args.add(new Argument(int256(i64, width)));

        return this;
    }

    /**
     * Add an arbitrary precision integer as a signed {@code intN} param, explicitly
     * setting the parameter width.
     *
     * @param width the nominal bit width for encoding the integer type in the function selector,
     *              e.g. {@code width = 128} produces a param type of {@code int128};
     *              must be a multiple of 8 and between 8 and 256.
     * @throws IllegalArgumentException if {@code bigInt.bitLength() > 255}
     *                                  (max range including the sign bit),
     *                                  {@code width < uint.bitLength()} or {@code width} is not in
     *                                  a valid range (see above).
     */
    public CallParams<Kind> addInt(BigInteger bigInt, int width) {
        checkBigInt(bigInt, width, true);

        final var bytes = bigInt.toByteArray();

        funcSelector.addParamType("int" + width);
        args.add(new Argument(leftPad32(bytes, bigInt.signum() < 0)));

        return this;
    }

    /**
     * Add a non-negative integer as an unsigned {@code uintN} param,
     * explicitly setting the parameter width.
     * <p>
     * The value will be truncated to the last {@code width} bits, the same as Java's
     * behavior when casting from a larger integer type to a smaller one. Passing a smaller
     * integer type is allowed.
     *
     * @param width the nominal bit width for encoding the integer type in the function selector,
     *              e.g. {@code width = 128} produces a param type of {@code uint128};
     *              must be a multiple of 8 and between 8 and 256.
     * @throws IllegalArgumentException if {@code uint < 0},
     *                                  or {@code width} is not in a valid range (see above).
     */
    public CallParams<Kind> addUint(@Nonnegative long uint, int width) {
        checkIntWidth(width);
        checkUnsignedVal(uint);

        funcSelector.addParamType("uint" + width);
        args.add(new Argument(int256(uint, width)));

        return this;
    }

    /**
     * Add an arbitrary precision non-negative integer as an unsigned {@code uintN} param,
     * explicitly setting the parameter width.
     * <p>
     * As this uses the unsigned type, it gets an extra bit of range over
     * {@link #addInt(BigInteger, int)} which has to count the sign bit.
     *
     * @param width the nominal bit width for encoding the integer type in the function selector,
     *              e.g. {@code width = 128} produces a param type of {@code uint128};
     *              must be a multiple of 8 and between 8 and 256.
     * @throws IllegalArgumentException if {@code uint.signum() < 0},
     *                                  if {@code uint.bitLength() > 256}
     *                                  (cannot be represented as a Solidity integer type),
     *                                  {@code width < uint.bitLength()} or
     *                                  {@code width} is not in a valid range (see above).
     */
    public CallParams<Kind> addUint(@Nonnegative BigInteger uint, int width) {
        checkBigInt(uint, width, false);
        checkUnsignedVal(uint.signum());

        final var bytes = uint.toByteArray();

        final ByteString byteStr;

        if (uint.bitLength() == 256) {
            // cut out the extra byte added by the sign bit so we get full range
            byteStr = ByteString.copyFrom(bytes, 1, bytes.length - 1);
        } else {
            byteStr = ByteString.copyFrom(bytes);
        }

        funcSelector.addParamType("uint" + (bytes.length * 8));
        args.add(new Argument(leftPad32(byteStr, false)));

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
                "Solidity addresses must be 20 bytes or 40 hex chars");
        }
    }

    private static byte[] decodeAddress(String address) {
        if (address.length() != ADDRESS_LEN_HEX) {
            throw new IllegalArgumentException(
                "Solidity addresses must be 20 bytes or 40 hex chars");
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
     * @throws IllegalArgumentException if the address is not exactly {@value ADDRESS_LEN_HEX}
     *                                  characters long or fails to decode as hexadecimal.
     */
    public CallParams<Kind> addAddress(String address) {
        return addAddress(decodeAddress(address));
    }

    /**
     * Function selector length in bytes
     */
    public static final int SELECTOR_LEN = 4;

    /**
     * Function selector length in hex characters
     */
    public static final int SELECTOR_LEN_HEX = 8;

    /**
     * Add a Solidity function reference as a {@value ADDRESS_LEN}-byte contract address and a
     * {@value SELECTOR_LEN}-byte function selector.
     *
     * @throws IllegalArgumentException if {@code address} is not {@value ADDRESS_LEN} bytes or
     *                                  {@code selector} is not {@value SELECTOR_LEN} bytes.
     */
    public CallParams<Kind> addFunction(byte[] address, byte[] selector) {
        checkAddressLen(address);

        if (selector.length != SELECTOR_LEN) {
            throw new IllegalArgumentException("function selectors must be 4 bytes or 8 hex chars");
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
     * {@value SELECTOR_LEN}-byte function selector.
     *
     * @param address  a hex-encoded {@value ADDRESS_LEN_HEX}-character Solidity address.
     * @param selector a
     * @throws IllegalArgumentException if {@code address} is not {@value ADDRESS_LEN_HEX}
     *                                  characters or {@code selector} is not
     *                                  {@value SELECTOR_LEN} bytes.
     */
    public CallParams<Kind> addFunction(String address, byte[] selector) {
        return addFunction(decodeAddress(address), selector);
    }

    /**
     * Add a Solidity function reference as a {@value ADDRESS_LEN}-byte contract address and a
     * {@value SELECTOR_LEN_HEX}-character hexadecimal function selector.
     *
     * @param address  a hex-encoded {@value ADDRESS_LEN_HEX}-character Solidity address.
     * @param selector a
     * @throws IllegalArgumentException if {@code address} is not {@value ADDRESS_LEN_HEX}
     *                                  characters or {@code selector} is not
     *                                  {@value SELECTOR_LEN_HEX} characters or fails to decode
     *                                  as hex.
     */
    public CallParams<Kind> addFunction(String address, String selector) {
        if (selector.length() != SELECTOR_LEN_HEX) {
            throw new IllegalArgumentException("function selectors must be 4 bytes or 8 hex chars");
        }

        final byte[] selectorBytes;

        try {
            selectorBytes = Hex.decode(selector);
        } catch (DecoderException e) {
            throw new IllegalArgumentException(
                "failed to decode Solidity function selector as hex", e);
        }

        return addFunction(decodeAddress(address), selectorBytes);
    }

    /**
     * Add a Solidity function reference as a {@value ADDRESS_LEN}-byte contract address and a
     * constructed {@link FunctionSelector}.
     *
     * @return
     * @throws IllegalArgumentException if {@code address} is not {@value ADDRESS_LEN_HEX}
     *                                  characters.
     */
    public CallParams<Kind> addFunction(String address, FunctionSelector selector) {
        // allow the `FunctionSelector` to be reused multiple times
        return addFunction(decodeAddress(address), selector.finishIntermediate());
    }

    // TODO: arrays and tuples

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

    static ByteString int256(long val, int bitWidth) {
        // don't try to get wider than a `long` as it should just be filled with padding
        bitWidth = Math.min(bitWidth, 64);
        final var output = ByteString.newOutput(bitWidth / 8);

        // write bytes in big-endian order
        for (int i = bitWidth - 8; i >= 0; i -= 8) {
            // widening conversion sign-extends so we don't have to do anything special when
            // truncating a previously widened value
            final var u8 = (byte) (val >> i);
            output.write(u8);
        }

        // byte padding will sign-extend appropriately
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

    /**
     * Builder class for Solidity function selectors.
     */
    @SuppressFBWarnings(value = {"EI_EXPOSE_REP"},
        justification = "we don't care about the contents of `finished`")
    public static final class FunctionSelector {

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
            var lenBytes = int256(len, 32);
            this.len = len;
            this.value = lenBytes.concat(rightPad32(dynamic));
            isDynamic = true;
        }
    }

}
