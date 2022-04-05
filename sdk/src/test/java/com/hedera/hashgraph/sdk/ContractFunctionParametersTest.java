package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import java8.util.Lists;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContractFunctionParametersTest {
    @SuppressWarnings("unused")
    private static List<Arguments> int256Arguments() {
        return Lists.of(
            Arguments.of(0, "0000000000000000000000000000000000000000000000000000000000000000"),
            Arguments.of(2, "0000000000000000000000000000000000000000000000000000000000000002"),
            Arguments.of(255, "00000000000000000000000000000000000000000000000000000000000000ff"),
            Arguments.of(4095, "0000000000000000000000000000000000000000000000000000000000000fff"),
            Arguments.of(127 << 24, "000000000000000000000000000000000000000000000000000000007f000000"),
            Arguments.of(2047 << 20, "000000000000000000000000000000000000000000000000000000007ff00000"),
            // deadbeef as an integer literal is negative
            Arguments.of(0xdeadbeefL, "00000000000000000000000000000000000000000000000000000000deadbeef"),
            Arguments.of(-1, "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
            Arguments.of(-2, "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe"),
            Arguments.of(-256, "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff00"),
            Arguments.of(-4096, "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff000"),
            Arguments.of(255 << 24, "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffff000000"),
            Arguments.of(4095 << 20, "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffff00000"),
            Arguments.of(0xdeadbeef, "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffdeadbeef")
        );
    }

    @SuppressWarnings("unused")
    private static List<Arguments> uInt256Arguments() {
        return Lists.of(
            Arguments.of(0, "0000000000000000000000000000000000000000000000000000000000000000", 8),
            Arguments.of(2, "0000000000000000000000000000000000000000000000000000000000000002", 8),
            Arguments.of(255, "00000000000000000000000000000000000000000000000000000000000000ff", 8),
            Arguments.of(4095, "0000000000000000000000000000000000000000000000000000000000000fff", 32),
            Arguments.of(127 << 24, "000000000000000000000000000000000000000000000000000000007f000000", 32),
            Arguments.of(2047 << 20, "000000000000000000000000000000000000000000000000000000007ff00000", 32),
            // deadbeef as an integer literal is negative
            Arguments.of(0xdeadbeef, "00000000000000000000000000000000000000000000000000000000deadbeef", 32),
            Arguments.of(-1, "000000000000000000000000000000000000000000000000ffffffffffffffff", 64),
            Arguments.of(-2, "000000000000000000000000000000000000000000000000fffffffffffffffe", 64),
            Arguments.of(-256, "000000000000000000000000000000000000000000000000ffffffffffffff00", 64),
            Arguments.of(-4096, "000000000000000000000000000000000000000000000000fffffffffffff000", 64),
            Arguments.of(255 << 24, "000000000000000000000000000000000000000000000000ffffffffff000000", 64),
            Arguments.of(4095 << 20, "000000000000000000000000000000000000000000000000fffffffffff00000", 64),
            Arguments.of(0xdeadbeefL, "00000000000000000000000000000000000000000000000000000000deadbeef", 64)
        );
    }

    @Test
    @DisplayName("encodes int types correctly")
    void intTypes() {
        ContractFunctionParameters params = new ContractFunctionParameters()
            .addUint8((byte) 0x1)
            .addInt8((byte) -0x2)
            .addUint32(0x3)
            .addInt32(-0x4)
            .addUint64(0x4)
            .addInt64(-0x5)
            .addUint256(BigInteger.valueOf(0x6))
            .addInt256(BigInteger.valueOf(-0x7))
            .addUint8Array(new byte[]{(byte) 0x1, (byte) 0x2, (byte) 0x3, (byte) 0x4})
            .addInt8Array(new byte[]{(byte) -0x5, (byte) 0x6, (byte) 0x7, (byte) -0x8})
            .addUint32Array(new int[]{0x9, 0xA, 0xB, 0xC})
            .addInt32Array(new int[]{-0xD, 0xE, 0xF, -0x10})
            .addUint64Array(new long[]{0x11, 0x12, 0x13, 0x14})
            .addInt64Array(new long[]{-0x15, 0x16, 0x17, -0x18})
            .addUint256Array(new BigInteger[]{BigInteger.valueOf(0x19)})
            .addInt256Array(new BigInteger[]{BigInteger.valueOf(-0x1A)});

        assertEquals(
            "11bcd903" +
                "0000000000000000000000000000000000000000000000000000000000000001" +
                "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe" +
                "0000000000000000000000000000000000000000000000000000000000000003" +
                "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc" +
                "0000000000000000000000000000000000000000000000000000000000000004" +
                "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffb" +
                "0000000000000000000000000000000000000000000000000000000000000006" +
                "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff9" +
                "0000000000000000000000000000000000000000000000000000000000000200" +
                "00000000000000000000000000000000000000000000000000000000000002a0" +
                "0000000000000000000000000000000000000000000000000000000000000340" +
                "00000000000000000000000000000000000000000000000000000000000003e0" +
                "0000000000000000000000000000000000000000000000000000000000000480" +
                "0000000000000000000000000000000000000000000000000000000000000520" +
                "00000000000000000000000000000000000000000000000000000000000005c0" +
                "0000000000000000000000000000000000000000000000000000000000000600" +
                "0000000000000000000000000000000000000000000000000000000000000004" +
                "0000000000000000000000000000000000000000000000000000000000000001" +
                "0000000000000000000000000000000000000000000000000000000000000002" +
                "0000000000000000000000000000000000000000000000000000000000000003" +
                "0000000000000000000000000000000000000000000000000000000000000004" +
                "0000000000000000000000000000000000000000000000000000000000000004" +
                "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffb" +
                "0000000000000000000000000000000000000000000000000000000000000006" +
                "0000000000000000000000000000000000000000000000000000000000000007" +
                "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8" +
                "0000000000000000000000000000000000000000000000000000000000000004" +
                "0000000000000000000000000000000000000000000000000000000000000009" +
                "000000000000000000000000000000000000000000000000000000000000000a" +
                "000000000000000000000000000000000000000000000000000000000000000b" +
                "000000000000000000000000000000000000000000000000000000000000000c" +
                "0000000000000000000000000000000000000000000000000000000000000004" +
                "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff3" +
                "000000000000000000000000000000000000000000000000000000000000000e" +
                "000000000000000000000000000000000000000000000000000000000000000f" +
                "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0" +
                "0000000000000000000000000000000000000000000000000000000000000004" +
                "0000000000000000000000000000000000000000000000000000000000000011" +
                "0000000000000000000000000000000000000000000000000000000000000012" +
                "0000000000000000000000000000000000000000000000000000000000000013" +
                "0000000000000000000000000000000000000000000000000000000000000014" +
                "0000000000000000000000000000000000000000000000000000000000000004" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffeb" +
                "0000000000000000000000000000000000000000000000000000000000000016" +
                "0000000000000000000000000000000000000000000000000000000000000017" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe8" +
                "0000000000000000000000000000000000000000000000000000000000000001" +
                "0000000000000000000000000000000000000000000000000000000000000019" +
                "0000000000000000000000000000000000000000000000000000000000000001" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe6",

            Hex.toHexString(params.toBytes("foo").toByteArray())
        );
    }

    @Test
    @DisplayName("uint256 chops off sign bit if length is 256 bits")
    void uint256BitLength() {
        var params = new ContractFunctionParameters()
            .addUint256(BigInteger.valueOf(2).pow(255));

        assertEquals(
            "2fbebd38" +
                "8000000000000000000000000000000000000000000000000000000000000000",
            Hex.toHexString(params.toBytes("foo").toByteArray())
        );
    }

    @Test
    @DisplayName("uint256 errors if less than 0")
    void uint256Errors() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ContractFunctionParameters()
                .addUint256(BigInteger.valueOf(-0x1));
        });

        /*
        assertThrows(IllegalArgumentException.class, () -> {
            new ContractFunctionParameters()
                .addUint256(BigInteger.valueOf(2).pow(256));
        });
         */
    }

    @Test
    @DisplayName("encodes addresses correctly")
    void addresses() {
        var params = new ContractFunctionParameters()
            .addAddress("1122334455667788990011223344556677889900")
            .addAddress("0x1122334455667788990011223344556677889900")
            .addAddressArray(new String[]{"1122334455667788990011223344556677889900", "1122334455667788990011223344556677889900"});

        assertEquals(
            "7d48c86d" +
                "0000000000000000000000001122334455667788990011223344556677889900" +
                "0000000000000000000000001122334455667788990011223344556677889900" +
                "0000000000000000000000000000000000000000000000000000000000000060" +
                "0000000000000000000000000000000000000000000000000000000000000002" +
                "0000000000000000000000001122334455667788990011223344556677889900" +
                "0000000000000000000000001122334455667788990011223344556677889900",
            Hex.toHexString(params.toBytes("foo").toByteArray())
        );
    }

    @Test
    @DisplayName("encodes functions correctly")
    void addressesError() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ContractFunctionParameters()
                .addAddress("112233445566778899001122334455667788990011");
        });
    }

    @Test
    @DisplayName("encodes functions correctly")
    void functions() {
        var params = new ContractFunctionParameters()
            .addFunction("1122334455667788990011223344556677889900", new byte[]{1, 2, 3, 4})
            .addFunction("0x1122334455667788990011223344556677889900", new ContractFunctionSelector("randomFunction").addBool());

        assertEquals(
            "c99c40cd" +
                "1122334455667788990011223344556677889900010203040000000000000000" +
                "112233445566778899001122334455667788990063441d820000000000000000",
            Hex.toHexString(params.toBytes("foo").toByteArray())
        );
    }

    @Test
    @DisplayName("encodes functions correctly")
    void functionsError() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ContractFunctionParameters()
                .addFunction("112233445566778899001122334455667788990011", new byte[]{1, 2, 3, 4});
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new ContractFunctionParameters()
                .addFunction("1122334455667788990011223344556677889900", new byte[]{1, 2, 3, 4, 5});
        });
    }

    @Test
    @DisplayName("encodes bytes32 correctly")
    void bytes() {
        var params = new ContractFunctionParameters()
            .addBytes32(new byte[]{
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
                17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32
            });

        assertEquals(
            "11e814c1" +
                "0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20",
            Hex.toHexString(params.toBytes("foo").toByteArray())
        );
    }

    @Test
    @DisplayName("fails to encode bytes32 if length too long")
    void bytesError() {
        assertThrows(IllegalArgumentException.class,
            () -> {
                new ContractFunctionParameters()
                    .addBytes32(new byte[]{
                        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
                        17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32,
                        33
                    });
            }
        );
    }

    @Test
    @DisplayName("encodes boolean correctly")
    void bool() {
        var params = new ContractFunctionParameters()
            .addBool(true)
            .addBool(false);

        assertEquals(
            "b3cedfcf" +
                "0000000000000000000000000000000000000000000000000000000000000001" +
                "0000000000000000000000000000000000000000000000000000000000000000",
            Hex.toHexString(params.toBytes("foo").toByteArray())
        );
    }

    @Test
    @DisplayName("encodes dynamic params correctly")
    void dynamicParamsEncoding() {
        ByteString paramsStringArg = new ContractFunctionParameters()
            .addString("Hello, world!")
            .toBytes("set_message");

        ByteString paramsBytesArg = new ContractFunctionParameters()
            .addBytes("Hello, world!".getBytes(StandardCharsets.UTF_8))
            .toBytes("set_message");

        String paramsStringArgHex = Hex.toHexString(paramsStringArg.toByteArray());
        String paramsBytesArgHex = Hex.toHexString(paramsBytesArg.toByteArray());

        assertEquals(
            "2e982602"
                + "0000000000000000000000000000000000000000000000000000000000000020"
                + "000000000000000000000000000000000000000000000000000000000000000d"
                + "48656c6c6f2c20776f726c642100000000000000000000000000000000000000",
            paramsStringArgHex);

        // signature should encode differently but the contents are identical
        assertEquals(
            "010473a7"
                + "0000000000000000000000000000000000000000000000000000000000000020"
                + "000000000000000000000000000000000000000000000000000000000000000d"
                + "48656c6c6f2c20776f726c642100000000000000000000000000000000000000",
            paramsBytesArgHex);
    }

    @Test
    @DisplayName("encodes static params correctly")
    void staticParamsEncoding() {
        ContractFunctionParameters params = new ContractFunctionParameters()
            .addInt32(0x11223344)
            .addInt32(-65536)
            .addUint64(-65536)
            .addAddress("00112233445566778899aabbccddeeff00112233");

        String paramsHex = Hex.toHexString(params.toBytes(null).toByteArray());

        assertEquals(
            "0000000000000000000000000000000000000000000000000000000011223344"
                // sign-extended
                + "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0000"
                // zero-padded
                + "000000000000000000000000000000000000000000000000ffffffffffff0000"
                + "00000000000000000000000000112233445566778899aabbccddeeff00112233",
            paramsHex
        );
    }

    @Test
    @DisplayName("encodes mixed static and dynamic params correctly")
    void mixedParamsEncoding() {
        ContractFunctionParameters params = new ContractFunctionParameters()
            .addInt256(BigInteger.valueOf(0xdeadbeef).shiftLeft(8))
            .addString("Hello, world!")
            .addBytes(new byte[]{-1, -18, 63, 127})
            .addBool(true)
            .addUint8Array(new byte[]{-1, 127});

        String paramsHex = Hex.toHexString(params.toBytes("foo").toByteArray());

        assertEquals(
            "6a5bb8f2"
                + "ffffffffffffffffffffffffffffffffffffffffffffffffffffffdeadbeef00"
                + "00000000000000000000000000000000000000000000000000000000000000a0"
                + "00000000000000000000000000000000000000000000000000000000000000e0"
                + "0000000000000000000000000000000000000000000000000000000000000001"
                + "0000000000000000000000000000000000000000000000000000000000000120"
                + "000000000000000000000000000000000000000000000000000000000000000d"
                + "48656c6c6f2c20776f726c642100000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000000004"
                + "ffee3f7f00000000000000000000000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000000002"
                + "00000000000000000000000000000000000000000000000000000000000000ff"
                + "000000000000000000000000000000000000000000000000000000000000007f",
            paramsHex
        );
    }

    @Test
    @DisplayName("encodes array types correctly")
    void arrayTypesEncoding() {
        ContractFunctionParameters params = new ContractFunctionParameters()
            .addStringArray(new String[]{"hello", ",", "world!"})
            .addInt32Array(new int[]{0x88, 0x99, 0xAA, 0xBB})
            .addInt256Array(new BigInteger[]{BigInteger.valueOf(0x1111)});

        assertEquals(
            "025838fc" +
                "0000000000000000000000000000000000000000000000000000000000000060" +
                "00000000000000000000000000000000000000000000000000000000000001a0" +
                "0000000000000000000000000000000000000000000000000000000000000240" +
                "0000000000000000000000000000000000000000000000000000000000000003" +
                "0000000000000000000000000000000000000000000000000000000000000060" +
                "00000000000000000000000000000000000000000000000000000000000000a0" +
                "00000000000000000000000000000000000000000000000000000000000000e0" +
                "0000000000000000000000000000000000000000000000000000000000000005" +
                "68656c6c6f000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000001" +
                "2c00000000000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000006" +
                "776f726c64210000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000004" +
                "0000000000000000000000000000000000000000000000000000000000000088" +
                "0000000000000000000000000000000000000000000000000000000000000099" +
                "00000000000000000000000000000000000000000000000000000000000000aa" +
                "00000000000000000000000000000000000000000000000000000000000000bb" +
                "0000000000000000000000000000000000000000000000000000000000000001" +
                "0000000000000000000000000000000000000000000000000000000000001111",
            Hex.toHexString(params.toBytes("foo").toByteArray())
        );
    }

    @Test
    @DisplayName("bytes32[] encodes correctly")
    void fixedBytesArrayEncoding() {
        // each string should be padded to 32 bytes and have no length prefix

        ContractFunctionParameters params = new ContractFunctionParameters()
            .addBytes32Array(new byte[][]{
                "Hello".getBytes(StandardCharsets.UTF_8),
                ",".getBytes(StandardCharsets.UTF_8),
                "world!".getBytes(StandardCharsets.UTF_8)
            });

        assertEquals(
            "0000000000000000000000000000000000000000000000000000000000000020" +
                "0000000000000000000000000000000000000000000000000000000000000003" + // length of array
                "48656c6c6f000000000000000000000000000000000000000000000000000000" + // "Hello" UTF-8 encoded
                "2c00000000000000000000000000000000000000000000000000000000000000" + // "," UTF-8 encoded
                "776f726c64210000000000000000000000000000000000000000000000000000", // "world!" UTF-8 encoded
            Hex.toHexString(params.toBytes(null).toByteArray())
        );
    }

    @Test
    @DisplayName("bytes[] encodes correctly")
    void dynBytesArrayEncoding() {
        // result should be the exact same as the strings test below
        ContractFunctionParameters params = new ContractFunctionParameters()
            .addBytesArray(new byte[][]{
                "Hello".getBytes(StandardCharsets.UTF_8),
                ",".getBytes(StandardCharsets.UTF_8),
                "world!".getBytes(StandardCharsets.UTF_8)
            });

        assertEquals(
            "0000000000000000000000000000000000000000000000000000000000000020" + // offset to array
                "0000000000000000000000000000000000000000000000000000000000000003" + // length of array
                "0000000000000000000000000000000000000000000000000000000000000060" + // first element offset, relative to beginning of this list (after length)
                "00000000000000000000000000000000000000000000000000000000000000a0" + // second element offset
                "00000000000000000000000000000000000000000000000000000000000000e0" + // third element offset
                "0000000000000000000000000000000000000000000000000000000000000005" + // "Hello".length
                "48656c6c6f000000000000000000000000000000000000000000000000000000" + // "Hello" UTF-8 encoded
                "0000000000000000000000000000000000000000000000000000000000000001" + // ",".length
                "2c00000000000000000000000000000000000000000000000000000000000000" + // "," UTF-8 encoded
                "0000000000000000000000000000000000000000000000000000000000000006" + // "world!".length
                "776f726c64210000000000000000000000000000000000000000000000000000", // "world!" UTF-8 encoded
            Hex.toHexString(params.toBytes(null).toByteArray())
        );
    }

    @Test
    @DisplayName("issue 376: string[] encodes correctly")
    void stringArrayEncoding() {
        ContractFunctionParameters params = new ContractFunctionParameters()
            .addStringArray(new String[]{"Hello", ",", "world!"});

        assertEquals(
            "0000000000000000000000000000000000000000000000000000000000000020" + // offset to array
                "0000000000000000000000000000000000000000000000000000000000000003" + // length of array
                "0000000000000000000000000000000000000000000000000000000000000060" + // first element offset, relative to beginning of this list (after length)
                "00000000000000000000000000000000000000000000000000000000000000a0" + // second element offset
                "00000000000000000000000000000000000000000000000000000000000000e0" + // third element offset
                "0000000000000000000000000000000000000000000000000000000000000005" + // "Hello".length
                "48656c6c6f000000000000000000000000000000000000000000000000000000" + // "Hello" UTF-8 encoded
                "0000000000000000000000000000000000000000000000000000000000000001" + // ",".length
                "2c00000000000000000000000000000000000000000000000000000000000000" + // "," UTF-8 encoded
                "0000000000000000000000000000000000000000000000000000000000000006" + // "world!".length
                "776f726c64210000000000000000000000000000000000000000000000000000", // "world!" UTF-8 encoded
            Hex.toHexString(params.toBytes(null).toByteArray())
        );
    }

    @Test
    @Disabled
    @DisplayName("BigInteger checks")
    void bigIntChecks() {
        ContractFunctionParameters params = new ContractFunctionParameters();

        // allowed values for BigInteger
        params.addInt256(BigInteger.ONE.shiftLeft(254));
        params.addInt256(BigInteger.ONE.negate().shiftLeft(255));

        String rangeErr = "BigInteger out of range for Solidity integers";

        assertEquals(
            rangeErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addInt256(BigInteger.ONE.shiftLeft(255))).getMessage());

        assertEquals(
            rangeErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addInt256(BigInteger.ONE.negate().shiftLeft(256)))
                .getMessage());
    }

    @Test
    @DisplayName("address param checks")
    void addressParamChecks() {
        ContractFunctionParameters params = new ContractFunctionParameters();

        String lenErr = "Solidity addresses must be 40 hex chars";
        assertEquals(
            lenErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addAddress("")).getMessage());

        assertEquals(
            lenErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addAddress("aabbccdd")).getMessage());

        assertEquals(
            lenErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addAddress("00112233445566778899aabbccddeeff0011223344"))
                .getMessage());

        assertEquals(
            "failed to decode Solidity address as hex",
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addAddress("gghhii--__zz66778899aabbccddeeff00112233"))
                .getMessage());
    }

    @ParameterizedTest
    @DisplayName("int256() encodes correctly")
    @MethodSource("int256Arguments")
    void int256EncodesCorrectly(long val, String hexString) {
        assertEquals(
            hexString,
            Hex.toHexString(ContractFunctionParameters.int256(val, 64).toByteArray())
        );
    }

    @ParameterizedTest
    @DisplayName("uint256() encodes correctly")
    @MethodSource("uInt256Arguments")
    void uInt256EncodesCorrectly(long val, String hexString, int bitWidth) {
        assertEquals(
            hexString,
            Hex.toHexString(ContractFunctionParameters.uint256(val, bitWidth).toByteArray())
        );
    }
}
