package com.hedera.hashgraph.sdk.contract;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.SolidityUtil;

import org.bouncycastle.util.encoders.DecoderException;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

// an implementation of function selector and parameter encoding as specified here:
// https://solidity.readthedocs.io/en/v0.5.7/abi-spec.html#

/**
 * Builder for encoding parameters for a Solidity contract constructor/function call.
 *
 * If you require a type which is not supported here, please let us know on
 * <a href="https://github.com/hashgraph/hedera-sdk-java/issues/298>this Github issue</a>.
 */
public final class ContractFunctionParams {
    /**
     * The length of a Solidity address in bytes.
     */
    public static final int ADDRESS_LEN = SolidityUtil.ADDRESS_LEN;
    /**
     * The length of a hexadecimal-encoded Solidity address, in ASCII characters (bytes).
     */
    public static final int ADDRESS_LEN_HEX = SolidityUtil.ADDRESS_LEN_HEX;
    /**
     * Function selector length in bytes
     */
    public static final int SELECTOR_LEN = 4;
    /**
     * Function selector length in hex characters
     */
    public static final int SELECTOR_LEN_HEX = 8;
    // padding that we can substring without new allocations
    private static final ByteString padding = ByteString.copyFrom(new byte[31]);
    private static final ByteString negativePadding;

    static {
        final byte[] fill = new byte[31];
        Arrays.fill(fill, (byte) 0xFF);
        negativePadding = ByteString.copyFrom(fill);
    }

    private final ArrayList<Argument> args = new ArrayList<>();

    /**
     * Add a parameter of type {@code string}.
     * <p>
     * For Solidity addresses, use {@link #addAddress(String)}.
     *
     * @return {@code this} for fluent usage
     */
    public ContractFunctionParams addString(String param) {
        args.add(new Argument("string", encodeString(param), true));

        return this;
    }

    /**
     * Add a parameter of type {@code string[]}.
     *
     * @throws NullPointerException if any value in `strings` is null
     */
    public ContractFunctionParams addStringArray(String[] strings) {
        final List<ByteString> byteStrings = Arrays.stream(strings)
            .map(ContractFunctionParams::encodeString)
            .collect(Collectors.toList());

        final ByteString argBytes = encodeDynArr(byteStrings);

        args.add(new Argument("string[]", argBytes, true));

        return this;
    }

    /**
     * Add a parameter of type {@code bytes}, a byte-string.
     */
    public ContractFunctionParams addBytes(byte[] param) {
        args.add(new Argument("bytes", encodeBytes(param), true));

        return this;
    }

    /**
     * Add a parameter of type {@code bytes[]}, an array of byte-strings.
     */
    public ContractFunctionParams addBytesArray(byte[][] param) {
        final List<ByteString> byteArrays = Arrays.stream(param)
            .map(ContractFunctionParams::encodeBytes)
            .collect(Collectors.toList());

        args.add(new Argument("bytes[]", encodeDynArr(byteArrays), true));

        return this;
    }

    /**
     * Add a parameter of type {@code bytes32}, a 32-byte byte-string.
     */
    public ContractFunctionParams addBytes32(byte[] param) {
        args.add(new Argument("bytes32", encodeBytes32(param), false));

        return this;
    }

    /**
     * Add a parameter of type {@code bytes32[]}, an array of 32-byte byte-strings.
     */
    public ContractFunctionParams addBytes32Array(byte[][] param) {
        final List<ByteString> byteArrays = Arrays.stream(param)
            .map(ContractFunctionParams::encodeBytes32)
            .collect(Collectors.toList());

        args.add(new Argument("bytes32[]", encodeDynArr(byteArrays), true));

        return this;
    }

    public ContractFunctionParams addBool(boolean bool) {
        // boolean encodes to `uint8` of values [0, 1]
        args.add(new Argument("bool", int256(bool ? 1 : 0, 8), false));
        return this;
    }

    /**
     * Add an 8-bit integer.
     *
     * @implNote The implementation is wasteful as we must pad to 32-bytes to store 1 byte.
     */
    public ContractFunctionParams addInt8(byte value) {
        args.add(new Argument("int8", int256(value, 32), false));

        return this;
    }

    /**
     * Add a 32-bit integer.
     */
    public ContractFunctionParams addInt32(int value) {
        args.add(new Argument("int32", int256(value, 32), false));

        return this;
    }

    /**
     * Add a 64-bit integer.
     */
    public ContractFunctionParams addInt64(long value) {
        args.add(new Argument("int64", int256(value, 64), false));

        return this;
    }

    /**
     * Add a 256-bit integer.
     *
     * @throws IllegalArgumentException if {@code bigInt.bitLength() > 255}
     *                                  (max range including the sign bit).
     */
    public ContractFunctionParams addInt256(BigInteger bigInt) {
        checkBigInt(bigInt);
        args.add(new Argument("int256", int256(bigInt), false));

        return this;
    }

    /**
     * Add a dynamic array of 8-bit integers.
     *
     * @implNote The implementation is wasteful as we must pad to 32-bytes to store 1 byte.
     */
    public ContractFunctionParams addInt8Array(byte[] intArray) {
        IntStream intStream = IntStream.range(0, intArray.length).map(idx -> intArray[idx]);

        ByteString arrayBytes = ByteString.copyFrom(
            intStream.mapToObj(i -> int256(i, 8))
                .collect(Collectors.toList()));

        arrayBytes = uint256(intArray.length, 32).concat(arrayBytes);

        args.add(new Argument("int8[]", arrayBytes, true));

        return this;
    }

    /**
     * Add a dynamic array of 32-bit integers.
     */
    public ContractFunctionParams addInt32Array(int[] intArray) {
        ByteString arrayBytes = ByteString.copyFrom(
            Arrays.stream(intArray).mapToObj(i -> int256(i, 32))
                .collect(Collectors.toList()));

        arrayBytes = uint256(intArray.length, 32).concat(arrayBytes);

        args.add(new Argument("int32[]", arrayBytes, true));

        return this;
    }

    /**
     * Add a dynamic array of 64-bit integers.
     */
    public ContractFunctionParams addInt64Array(long[] intArray) {
        ByteString arrayBytes = ByteString.copyFrom(
            Arrays.stream(intArray).mapToObj(i -> int256(i, 64))
                .collect(Collectors.toList()));

        arrayBytes = uint256(intArray.length, 64).concat(arrayBytes);

        args.add(new Argument("int64[]", arrayBytes, true));

        return this;
    }

    /**
     * Add a dynamic array of 256-bit integers.
     *
     * @throws IllegalArgumentException if any value's {@code .bitLength() > 255}
     *                                  (max range including the sign bit).
     */
    public ContractFunctionParams addInt256Array(BigInteger[] intArray) {
        ByteString arrayBytes = ByteString.copyFrom(
            Arrays.stream(intArray).map(ContractFunctionParams::int256)
                .collect(Collectors.toList()));

        arrayBytes = uint256(intArray.length, 256).concat(arrayBytes);

        args.add(new Argument("int256[]", arrayBytes, true));

        return this;
    }

    /**
     * Add an unsigned 8-bit integer.
     *
     * @implNote The implementation is wasteful as we must pad to 32-bytes to store 1 byte.
     */
    public ContractFunctionParams addUint8(byte value) {
        args.add(new Argument("uint8", uint256(value, 8), false));

        return this;
    }

    /**
     * Add a 32-bit unsigned integer.
     *
     * The value will be treated as unsigned during encoding (it will be zero-padded instead of
     * sign-extended to 32 bytes).
     */
    public ContractFunctionParams addUint32(int value) {
        args.add(new Argument("uint32", uint256(value, 32), false));

        return this;
    }

    /**
     * Add a 64-bit unsigned integer.
     *
     * The value will be treated as unsigned during encoding (it will be zero-padded instead of
     * sign-extended to 32 bytes).
     */
    public ContractFunctionParams addUint64(long value) {
        args.add(new Argument("uint64", uint256(value, 64), false));

        return this;
    }

    /**
     * Add a 256-bit unsigned integer.
     *
     * The value will be treated as unsigned during encoding (it will be zero-padded instead of
     * sign-extended to 32 bytes).
     *
     * @throws IllegalArgumentException if {@code bigUint.bitLength() > 256}
     *                                  (max range including the sign bit) or
     *                                  {@code bigUint.signum() < 0}.
     */
    public ContractFunctionParams addUint256(@Nonnegative BigInteger bigUint) {
        checkBigUint(bigUint);
        args.add(new Argument("uint256", uint256(bigUint), false));

        return this;
    }

    /**
     * Add a dynamic array of unsigned 8-bit integers.
     *
     * @implNote The implementation is wasteful as we must pad to 32-bytes to store 1 byte.
     */
    public ContractFunctionParams addUint8Array(byte[] intArray) {
        IntStream intStream = IntStream.range(0, intArray.length).map(idx -> intArray[idx]);

        ByteString arrayBytes = ByteString.copyFrom(
            intStream.mapToObj(i -> uint256(i, 8))
                .collect(Collectors.toList()));

        arrayBytes = uint256(intArray.length, 32).concat(arrayBytes);

        args.add(new Argument("uint8[]", arrayBytes, true));

        return this;
    }

    /**
     * Add a dynamic array of 32-bit unsigned integers.
     *
     * Each value will be treated as unsigned during encoding (it will be zero-padded instead of
     * sign-extended to 32 bytes).
     */
    public ContractFunctionParams addUint32Array(int[] intArray) {
        ByteString arrayBytes = ByteString.copyFrom(
            Arrays.stream(intArray).mapToObj(i -> uint256(i, 32))
                .collect(Collectors.toList()));

        arrayBytes = uint256(intArray.length, 32).concat(arrayBytes);

        args.add(new Argument("uint32[]", arrayBytes, true));

        return this;
    }

    /**
     * Add a dynamic array of 64-bit unsigned integers.
     *
     * Each value will be treated as unsigned during encoding (it will be zero-padded instead of
     * sign-extended to 32 bytes).
     */
    public ContractFunctionParams addUint64Array(long[] intArray) {
        ByteString arrayBytes = ByteString.copyFrom(
            Arrays.stream(intArray).mapToObj(i -> uint256(i, 64))
                .collect(Collectors.toList()));

        arrayBytes = uint256(intArray.length, 64).concat(arrayBytes);

        args.add(new Argument("uint64[]", arrayBytes, true));

        return this;
    }

    /**
     * Add a dynamic array of 256-bit unsigned integers.
     *
     * Each value will be treated as unsigned during encoding (it will be zero-padded instead of
     * sign-extended to 32 bytes).
     *
     * @throws IllegalArgumentException if any value has a {@code .bitLength() > 256}
     *                                  (max range including the sign bit) or is negative.
     */
    public ContractFunctionParams addUint256Array(BigInteger[] intArray) {
        ByteString arrayBytes = ByteString.copyFrom(
            Arrays.stream(intArray).map(ContractFunctionParams::uint256)
                .collect(Collectors.toList()));

        arrayBytes = uint256(intArray.length, 256).concat(arrayBytes);

        args.add(new Argument("uint256[]", arrayBytes, true));

        return this;
    }

    /**
     * Add a {@value ADDRESS_LEN_HEX}-character hex-encoded Solidity address parameter with the type
     * {@code address}.
     * <p>
     * Note: adding a {@code address payable} or {@code contract} parameter must also use
     * this function as the ABI does not support those types directly.
     *
     * @throws IllegalArgumentException if the address is not exactly {@value ADDRESS_LEN_HEX}
     *                                  characters long or fails to decode as hexadecimal.
     */
    public ContractFunctionParams addAddress(String address) {
        final byte[] addressBytes = decodeAddress(address);

        args.add(new Argument("address", ByteString.copyFrom(addressBytes).concat(padding.substring(19)), false));

        return this;
    }

    /**
     * Add an array of {@value ADDRESS_LEN_HEX}-character hex-encoded Solidity addresses as a
     * {@code address[]} param.
     *
     * @throws IllegalArgumentException if any value is not exactly {@value ADDRESS_LEN_HEX}
     *                                  characters long or fails to decode as hexadecimal.
     * @throws NullPointerException     if any value in the array is null.
     */
    public ContractFunctionParams addAddressArray(String[] addresses) {
        final ByteString addressArray = encodeArray(
            Arrays.stream(addresses).map(a -> {
                final byte[] address = decodeAddress(a);
                checkAddressLen(address);
                return leftPad32(ByteString.copyFrom(address));
            }));

        args.add(new Argument("address[]", addressArray, true));

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
    public ContractFunctionParams addFunction(String address, byte[] selector) {
        return addFunction(decodeAddress(address), selector);
    }

    /**
     * Add a Solidity function reference as a {@value ADDRESS_LEN}-byte contract address and a
     * constructed {@link ContractFunctionSelector}. The {@link ContractFunctionSelector}
     * may not be modified after this call.
     *
     * @return {@code this} for fluent usage.
     * @throws IllegalArgumentException if {@code address} is not {@value ADDRESS_LEN_HEX}
     *                                  characters.
     */
    public ContractFunctionParams addFunction(String address, ContractFunctionSelector selector) {
        // allow the `FunctionSelector` to be reused multiple times
        return addFunction(decodeAddress(address), selector.finish());
    }

    /**
     * Get the encoding of the currently added parameters as a {@link ByteString}.
     * <p>
     * You may continue to add parameters and call this again.
     *
     * @return the Solidity encoding of the call parameters in the order they were added.
     */
    ByteString toBytes(@Nullable String funcName) {
        // offset for dynamic-length data, immediately after value arguments
        int dynamicOffset = args.size() * 32;

        ArrayList<ByteString> paramsBytes = new ArrayList<ByteString>(args.size() + 1);

        ArrayList<ByteString> dynamicArgs = new ArrayList<ByteString>();

        final ContractFunctionSelector functionSelector = funcName != null
            ? new ContractFunctionSelector(funcName) : null;

        // iterate the arguments and determine whether they are dynamic or not
        for (Argument arg : args) {
            if (functionSelector != null) {
                 functionSelector.addParamType(arg.type);
            }

            if (arg.isDynamic) {
                // dynamic arguments supply their offset in value position and append their data at
                // that offset
                paramsBytes.add(int256(dynamicOffset, 256));
                dynamicArgs.add(arg.value);
                dynamicOffset += arg.value.size();
            } else {
                // value arguments are dropped in the current arg position
                paramsBytes.add(arg.value);
            }
        }

        if (functionSelector != null) {
            paramsBytes.add(0, ByteString.copyFrom(functionSelector.finish()));
        }

        paramsBytes.addAll(dynamicArgs);

        return ByteString.copyFrom(paramsBytes);
    }

    private static ByteString encodeString(String string) {
        final ByteString strBytes = ByteString.copyFromUtf8(string);
        // prepend the size of the string in UTF-8 bytes
        return int256(strBytes.size(), 32)
            .concat(rightPad32(strBytes));
    }

    private static ByteString encodeBytes(byte[] bytes) {
        return int256(bytes.length, 32)
            .concat(rightPad32(ByteString.copyFrom(bytes)));
    }

    private static ByteString encodeBytes32(byte[] bytes) {
        return rightPad32(ByteString.copyFrom(bytes, 0, 32));
    }

    private static ByteString encodeArray(Stream<ByteString> elements) {
        final List<ByteString> list = elements.collect(Collectors.toList());

        return int256(list.size(), 32)
            .concat(ByteString.copyFrom(list));
    }

    private static ByteString encodeDynArr(List<ByteString> elements) {
        final int offsetsLen = elements.size() + 1;

        final ArrayList<ByteString> offsets = new ArrayList<ByteString>(offsetsLen);

        offsets.add(uint256(elements.size(), 32));

        // points to start of dynamic segment
        long currOffset = offsetsLen * 32L;

        for (final ByteString elem : elements) {
            offsets.add(uint256(currOffset, 64));
            currOffset += elem.size();
        }

        return ByteString.copyFrom(offsets).concat(ByteString.copyFrom(elements));
    }

    private static void checkBigInt(BigInteger val) {
        // bitLength() does not include the sign bit
        if (val.bitLength() > 255) {
            throw new IllegalArgumentException("BigInteger out of range for Solidity integers");
        }
    }

    private static void checkBigUint(BigInteger val) {
        if (val.signum() < 0) {
            throw new IllegalArgumentException("negative BigInteger passed to unsigned function");
        }

        // bitLength() does not include the sign bit
        if (val.bitLength() > 256) {
            throw new IllegalArgumentException("BigInteger out of range for Solidity integers");
        }
    }

    static ByteString uint256(long val, int bitWidth) {
        return int256(val, bitWidth, false);
    }

    static ByteString int256(long val, int bitWidth) {
        return int256(val, bitWidth, true);
    }

    static ByteString int256(long val, int bitWidth, boolean signed) {
        // don't try to get wider than a `long` as it should just be filled with padding
        bitWidth = Math.min(bitWidth, 64);
        final ByteString.Output output = ByteString.newOutput(bitWidth / 8);

        // write bytes in big-endian order
        for (int i = bitWidth - 8; i >= 0; i -= 8) {
            // widening conversion sign-extends so we don't have to do anything special when
            // truncating a previously widened value
            final byte u8 = (byte) (val >> i);
            output.write(u8);
        }

        // byte padding will sign-extend appropriately
        return leftPad32(output.toByteString(), signed && val < 0);
    }

    static ByteString uint256(BigInteger bigInt) {
        if (bigInt.bitLength() == 256) {
            // we have to chop off the sign bit or else we'll have a 33 byte value
            // we have no choice but to copy twice here but hopefully the JIT elides one
            return ByteString.copyFrom(bigInt.toByteArray(), 1, 32);
        }

        return leftPad32(bigInt.toByteArray(), false);
    }

    static ByteString int256(BigInteger bigInt) {
        return leftPad32(bigInt.toByteArray(), bigInt.signum() < 0);
    }

    static ByteString leftPad32(ByteString input) {
        return leftPad32(input, false);
    }

    // Solidity contracts require all parameters to be padded to 32 byte multiples but specifies
    // different requirements for padding for strings/byte arrays vs integers
    static ByteString leftPad32(ByteString input, boolean negative) {
        int rem = 32 - input.size() % 32;
        return rem == 32
            ? input
            : (negative ? negativePadding : padding).substring(0, rem)
            .concat(input);
    }

    static ByteString leftPad32(byte[] input, boolean negative) {
        return leftPad32(ByteString.copyFrom(input), negative);
    }

    static ByteString rightPad32(ByteString input) {
        int rem = 32 - input.size() % 32;
        return rem == 32 ? input : input.concat(padding.substring(0, rem));
    }

    private static void checkAddressLen(byte[] address) {
        if (address.length != ADDRESS_LEN) {
            throw new IllegalArgumentException(
                "Solidity addresses must be 20 bytes or 40 hex chars");
        }
    }

    private static byte[] decodeAddress(String address) {
        if (address.length() != ADDRESS_LEN_HEX) {
            throw new IllegalArgumentException(
                "Solidity addresses must be 40 hex chars");
        }

        try {
            return Hex.decode(address);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("failed to decode Solidity address as hex", e);
        }
    }

    private ContractFunctionParams addFunction(byte[] address, byte[] selector) {
        checkAddressLen(address);

        if (selector.length != SELECTOR_LEN) {
            throw new IllegalArgumentException("function selectors must be 4 bytes or 8 hex chars");
        }

        final ByteString.Output output = ByteString.newOutput(ADDRESS_LEN + SELECTOR_LEN);
        output.write(address, 0, address.length);
        output.write(selector, 0, selector.length);

        // function reference encodes as `bytes24`
        args.add(new Argument("function", rightPad32(output.toByteString()), false));

        return this;
    }

    private final static class Argument {
        private final String type;
        private final ByteString value;
        private final boolean isDynamic;

        private Argument(String type, ByteString value, boolean isDynamic) {
            this.type = type;
            if (!isDynamic && value.size() != 32) {
                throw new IllegalArgumentException("value argument that was not 32 bytes");
            }

            this.value = value;
            this.isDynamic = isDynamic;
        }
    }

}
