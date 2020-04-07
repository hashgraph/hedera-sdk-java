package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.google.protobuf.ByteString;
import java8.util.J8Arrays;
import java8.util.stream.Collectors;
import java8.util.stream.IntStream;
import java8.util.stream.IntStreams;
import java8.util.stream.Stream;
import org.bouncycastle.util.encoders.DecoderException;
import org.bouncycastle.util.encoders.Hex;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// an implementation of function selector and parameter encoding as specified here:
// https://solidity.readthedocs.io/en/v0.5.7/abi-spec.html#

/**
 * Builder for encoding parameters for a Solidity contract constructor/function call.
 * <p>
 * If you require a type which is not supported here, please let us know on
 * <a href="https://github.com/hashgraph/hedera-sdk-java/issues/298>this Github issue</a>.
 */
public final class ContractFunctionParameters {
    /**
     * The length of a Solidity address in bytes.
     */
    public static final int ADDRESS_LEN = EntityId.SOLIDITY_ADDRESS_LEN;
    /**
     * The length of a hexadecimal-encoded Solidity address, in ASCII characters (bytes).
     */
    public static final int ADDRESS_LEN_HEX = EntityId.SOLIDITY_ADDRESS_LEN_HEX;
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
        byte[] fill = new byte[31];
        Arrays.fill(fill, (byte) 0xFF);
        negativePadding = ByteString.copyFrom(fill);
    }

    private final ArrayList<Argument> args = new ArrayList<>();

    private static ByteString encodeString(String string) {
        ByteString strBytes = ByteString.copyFromUtf8(string);
        // prepend the size of the string in UTF-8 bytes
        return int256(strBytes.size(), 32)
            .concat(rightPad32(strBytes));
    }

    private static ByteString encodeBytes(byte[] bytes) {
        return int256(bytes.length, 32)
            .concat(rightPad32(ByteString.copyFrom(bytes)));
    }

    private static ByteString encodeBytes32(byte[] bytes) {
        if (bytes.length > 32) {
            throw new IllegalArgumentException("byte32 encoding forbids byte array length greater than 32");
        }

        return rightPad32(ByteString.copyFrom(bytes));
    }

    private static ByteString encodeArray(Stream<ByteString> elements) {
        List<ByteString> list = elements.collect(Collectors.toList());

        return int256(list.size(), 32)
            .concat(ByteString.copyFrom(list));
    }

    private static ByteString encodeDynArr(List<ByteString> elements) {
        int offsetsLen = elements.size();

        // [len, offset[0], offset[1], ... offset[len - 1]]
        ArrayList<ByteString> head = new ArrayList<>(offsetsLen + 1);

        head.add(uint256(elements.size(), 32));

        // points to start of dynamic segment, *not* including the length of the array
        @Var long currOffset = offsetsLen * 32L;

        for (ByteString elem : elements) {
            head.add(uint256(currOffset, 64));
            currOffset += elem.size();
        }

        return ByteString.copyFrom(head).concat(ByteString.copyFrom(elements));
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

    static ByteString int256(long val, @Var int bitWidth, boolean signed) {
        // don't try to get wider than a `long` as it should just be filled with padding
        bitWidth = Math.min(bitWidth, 64);
        ByteString.Output output = ByteString.newOutput(bitWidth / 8);

        // write bytes in big-endian order
        for (int i = bitWidth - 8; i >= 0; i -= 8) {
            // widening conversion sign-extends so we don't have to do anything special when
            // truncating a previously widened value
            byte u8 = (byte) (val >> i);
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

    /**
     * Add a parameter of type {@code string}.
     * <p>
     * For Solidity addresses, use {@link #addAddress(String)}.
     *
     * @return {@code this} for fluent usage
     */
    public ContractFunctionParameters addString(String param) {
        args.add(new Argument("string", encodeString(param), true));

        return this;
    }

    /**
     * Add a parameter of type {@code string[]}.
     *
     * @throws NullPointerException if any value in `strings` is null
     */
    public ContractFunctionParameters addStringArray(String[] strings) {
        List<ByteString> byteStrings = J8Arrays.stream(strings)
            .map(ContractFunctionParameters::encodeString)
            .collect(Collectors.toList());

        ByteString argBytes = encodeDynArr(byteStrings);

        args.add(new Argument("string[]", argBytes, true));

        return this;
    }

    /**
     * Add a parameter of type {@code bytes}, a byte-string.
     */
    public ContractFunctionParameters addBytes(byte[] param) {
        args.add(new Argument("bytes", encodeBytes(param), true));

        return this;
    }

    /**
     * Add a parameter of type {@code bytes[]}, an array of byte-strings.
     */
    public ContractFunctionParameters addBytesArray(byte[][] param) {
        List<ByteString> byteArrays = J8Arrays.stream(param)
            .map(ContractFunctionParameters::encodeBytes)
            .collect(Collectors.toList());

        args.add(new Argument("bytes[]", encodeDynArr(byteArrays), true));

        return this;
    }

    /**
     * Add a parameter of type {@code bytes32}, a 32-byte byte-string.
     * <p>
     * If applicable, the array will be right-padded with zero bytes to a length of 32 bytes.
     *
     * @throws IllegalArgumentException if the length of the byte array is greater than 32.
     */
    public ContractFunctionParameters addBytes32(byte[] param) {
        args.add(new Argument("bytes32", encodeBytes32(param), false));

        return this;
    }

    /**
     * Add a parameter of type {@code bytes32[]}, an array of 32-byte byte-strings.
     * <p>
     * Each byte array will be right-padded with zero bytes to a length of 32 bytes.
     *
     * @throws IllegalArgumentException if the length of any byte array is greater than 32.
     */
    public ContractFunctionParameters addBytes32Array(byte[][] param) {
        // array of fixed-size elements
        Stream<ByteString> byteArrays = J8Arrays.stream(param)
            .map(ContractFunctionParameters::encodeBytes32);

        args.add(new Argument("bytes32[]", encodeArray(byteArrays), true));

        return this;
    }

    public ContractFunctionParameters addBool(boolean bool) {
        // boolean encodes to `uint8` of values [0, 1]
        args.add(new Argument("bool", int256(bool ? 1 : 0, 8), false));
        return this;
    }

    /**
     * Add an 8-bit integer.
     *
     * @implNote The implementation is wasteful as we must pad to 32-bytes to store 1 byte.
     */
    public ContractFunctionParameters addInt8(byte value) {
        args.add(new Argument("int8", int256(value, 32), false));

        return this;
    }

    /**
     * Add a 32-bit integer.
     */
    public ContractFunctionParameters addInt32(int value) {
        args.add(new Argument("int32", int256(value, 32), false));

        return this;
    }

    /**
     * Add a 64-bit integer.
     */
    public ContractFunctionParameters addInt64(long value) {
        args.add(new Argument("int64", int256(value, 64), false));

        return this;
    }

    /**
     * Add a 256-bit integer.
     *
     * @throws IllegalArgumentException if {@code bigInt.bitLength() > 255}
     *                                  (max range including the sign bit).
     */
    public ContractFunctionParameters addInt256(BigInteger bigInt) {
        checkBigInt(bigInt);
        args.add(new Argument("int256", int256(bigInt), false));

        return this;
    }

    /**
     * Add a dynamic array of 8-bit integers.
     *
     * @implNote The implementation is wasteful as we must pad to 32-bytes to store 1 byte.
     */
    public ContractFunctionParameters addInt8Array(byte[] intArray) {
        IntStream intStream = IntStreams.range(0, intArray.length).map(idx -> intArray[idx]);

        @Var ByteString arrayBytes = ByteString.copyFrom(
            intStream.mapToObj(i -> int256(i, 8))
                .collect(Collectors.toList()));

        arrayBytes = uint256(intArray.length, 32).concat(arrayBytes);

        args.add(new Argument("int8[]", arrayBytes, true));

        return this;
    }

    /**
     * Add a dynamic array of 32-bit integers.
     */
    public ContractFunctionParameters addInt32Array(int[] intArray) {
        @Var ByteString arrayBytes = ByteString.copyFrom(
            J8Arrays.stream(intArray).mapToObj(i -> int256(i, 32))
                .collect(Collectors.toList()));

        arrayBytes = uint256(intArray.length, 32).concat(arrayBytes);

        args.add(new Argument("int32[]", arrayBytes, true));

        return this;
    }

    /**
     * Add a dynamic array of 64-bit integers.
     */
    public ContractFunctionParameters addInt64Array(long[] intArray) {
        @Var ByteString arrayBytes = ByteString.copyFrom(
            J8Arrays.stream(intArray).mapToObj(i -> int256(i, 64))
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
    public ContractFunctionParameters addInt256Array(BigInteger[] intArray) {
        @Var ByteString arrayBytes = ByteString.copyFrom(
            J8Arrays.stream(intArray).map(ContractFunctionParameters::int256)
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
    public ContractFunctionParameters addUint8(byte value) {
        args.add(new Argument("uint8", uint256(value, 8), false));

        return this;
    }

    /**
     * Add a 32-bit unsigned integer.
     * <p>
     * The value will be treated as unsigned during encoding (it will be zero-padded instead of
     * sign-extended to 32 bytes).
     */
    public ContractFunctionParameters addUint32(int value) {
        args.add(new Argument("uint32", uint256(value, 32), false));

        return this;
    }

    /**
     * Add a 64-bit unsigned integer.
     * <p>
     * The value will be treated as unsigned during encoding (it will be zero-padded instead of
     * sign-extended to 32 bytes).
     */
    public ContractFunctionParameters addUint64(long value) {
        args.add(new Argument("uint64", uint256(value, 64), false));

        return this;
    }

    /**
     * Add a 256-bit unsigned integer.
     * <p>
     * The value will be treated as unsigned during encoding (it will be zero-padded instead of
     * sign-extended to 32 bytes).
     *
     * @throws IllegalArgumentException if {@code bigUint.bitLength() > 256}
     *                                  (max range including the sign bit) or
     *                                  {@code bigUint.signum() < 0}.
     */
    public ContractFunctionParameters addUint256(@Nonnegative BigInteger bigUint) {
        checkBigUint(bigUint);
        args.add(new Argument("uint256", uint256(bigUint), false));

        return this;
    }

    /**
     * Add a dynamic array of unsigned 8-bit integers.
     *
     * @implNote The implementation is wasteful as we must pad to 32-bytes to store 1 byte.
     */
    public ContractFunctionParameters addUint8Array(byte[] intArray) {
        IntStream intStream = IntStreams.range(0, intArray.length).map(idx -> intArray[idx]);

        @Var ByteString arrayBytes = ByteString.copyFrom(
            intStream.mapToObj(i -> uint256(i, 8))
                .collect(Collectors.toList()));

        arrayBytes = uint256(intArray.length, 32).concat(arrayBytes);

        args.add(new Argument("uint8[]", arrayBytes, true));

        return this;
    }

    /**
     * Add a dynamic array of 32-bit unsigned integers.
     * <p>
     * Each value will be treated as unsigned during encoding (it will be zero-padded instead of
     * sign-extended to 32 bytes).
     */
    public ContractFunctionParameters addUint32Array(int[] intArray) {
        @Var ByteString arrayBytes = ByteString.copyFrom(
            J8Arrays.stream(intArray).mapToObj(i -> uint256(i, 32))
                .collect(Collectors.toList()));

        arrayBytes = uint256(intArray.length, 32).concat(arrayBytes);

        args.add(new Argument("uint32[]", arrayBytes, true));

        return this;
    }

    /**
     * Add a dynamic array of 64-bit unsigned integers.
     * <p>
     * Each value will be treated as unsigned during encoding (it will be zero-padded instead of
     * sign-extended to 32 bytes).
     */
    public ContractFunctionParameters addUint64Array(long[] intArray) {
        @Var ByteString arrayBytes = ByteString.copyFrom(
            J8Arrays.stream(intArray).mapToObj(i -> uint256(i, 64))
                .collect(Collectors.toList()));

        arrayBytes = uint256(intArray.length, 64).concat(arrayBytes);

        args.add(new Argument("uint64[]", arrayBytes, true));

        return this;
    }

    /**
     * Add a dynamic array of 256-bit unsigned integers.
     * <p>
     * Each value will be treated as unsigned during encoding (it will be zero-padded instead of
     * sign-extended to 32 bytes).
     *
     * @throws IllegalArgumentException if any value has a {@code .bitLength() > 256}
     *                                  (max range including the sign bit) or is negative.
     */
    public ContractFunctionParameters addUint256Array(BigInteger[] intArray) {
        @Var ByteString arrayBytes = ByteString.copyFrom(
            J8Arrays.stream(intArray).map(ContractFunctionParameters::uint256)
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
    public ContractFunctionParameters addAddress(String address) {
        byte[] addressBytes = decodeAddress(address);

        args.add(new Argument("address", leftPad32(ByteString.copyFrom(addressBytes)), false));

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
    public ContractFunctionParameters addAddressArray(String[] addresses) {
        ByteString addressArray = encodeArray(
            J8Arrays.stream(addresses).map(a -> {
                byte[] address = decodeAddress(a);
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
    public ContractFunctionParameters addFunction(String address, byte[] selector) {
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
    public ContractFunctionParameters addFunction(String address, ContractFunctionSelector selector) {
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
        @Var var dynamicOffset = args.size() * 32;

        var paramsBytes = new ArrayList<ByteString>(args.size() + 1);

        var dynamicArgs = new ArrayList<ByteString>();

        ContractFunctionSelector functionSelector = funcName != null
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

    private ContractFunctionParameters addFunction(byte[] address, byte[] selector) {
        checkAddressLen(address);

        if (selector.length != SELECTOR_LEN) {
            throw new IllegalArgumentException("function selectors must be 4 bytes or 8 hex chars");
        }

        var output = ByteString.newOutput(ADDRESS_LEN + SELECTOR_LEN);
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
