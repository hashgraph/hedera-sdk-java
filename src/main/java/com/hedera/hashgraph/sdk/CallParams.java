package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;

import org.bouncycastle.jcajce.provider.digest.Keccak;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

import static java.nio.charset.StandardCharsets.US_ASCII;

// an implementation of function selector and parameter encoding as specified here:
// https://solidity.readthedocs.io/en/v0.5.7/abi-spec.html#
public final class CallParams<Kind> {
    @Nullable
    private final String funcName;
    private final ArrayList<String> paramTypes = new ArrayList<>();
    private final ArrayList<Argument> args = new ArrayList<>();

    CallParams(@Nullable String funcName) {
        this.funcName = funcName;
    }

    public static CallParams<Constructor> constructor() {
        return new CallParams<>(null);
    }

    public static CallParams<Function> function(String funcName) {
        return new CallParams<>(funcName);
    }

    public CallParams<Kind> add(String param) {
        var strBytes = ByteString.copyFromUtf8(param);

        paramTypes.add("string");
        args.add(new Argument(strBytes.size(), strBytes));

        return this;
    }

    public CallParams<Kind> add(byte[] param) {
        var bytes = ByteString.copyFrom(param);

        paramTypes.add("bytes");
        args.add(new Argument(param.length, bytes));

        return this;
    }

    public CallParams<Kind> add(byte i8) {
        return add((long) i8, 8);
    }

    public CallParams<Kind> add(short i16) {
        return add((long) i16, 16);
    }

    public CallParams<Kind> add(int i32) {
        return add((long) i32, 32);
    }

    public CallParams<Kind> add(long i64) {
        return add(i64, 8);
    }

    public CallParams<Kind> add(long i64, int width) {
        checkIntWidth(width);
        if (width > 64) {
            throw new IllegalArgumentException("integer width > 64");
        }

        paramTypes.add("int<" + width + ">");
        args.add(new Argument(int256(i64, width)));

        return this;
    }

    public CallParams<Kind> add(BigInteger bigInt, byte width) {
        checkIntWidth(width);

        paramTypes.add("int<" + width + ">");
        args.add(new Argument(int256(bigInt)));

        return this;
    }

    private static void checkIntWidth(int width) {
        if (width % 8 != 0) {
            throw new IllegalArgumentException("Solidity integer width must be a multiple of 8");
        }
    }

    public CallParams<Kind> addUnsigned(@Nonnegative long uint, int width) {
        checkIntWidth(width);

        paramTypes.add("uint<" + width + ">");
        args.add(new Argument(int256(uint, width)));

        return this;
    }

    public CallParams<Kind> addUnsigned(BigInteger bigInt, int width) {
        checkIntWidth(width);

        paramTypes.add("int<" + width + ">");
        args.add(new Argument(int256(bigInt)));

        return this;
    }

    private static final byte OPEN_PAREN = '(';
    private static final byte COMMA = ',';
    private static final byte CLOSE_PAREN = ')';

    // get the function selector, given function name and argument types
    // static for easier testing
    static ByteString funcSelector(String funcName, List<String> paramTypes) {
        var digest = new Keccak.Digest256();
        digest.update(funcName.getBytes(US_ASCII));
        digest.update(OPEN_PAREN);

        var needsComma = false;

        for (String paramType : paramTypes) {
            if (needsComma) {
                digest.update(COMMA);
            }

            digest.update(paramType.getBytes(US_ASCII));
            needsComma = true;
        }

        digest.update(CLOSE_PAREN);

        // spec only cares about the first four bytes
        return ByteString.copyFrom(digest.digest(), 0, 4);
    }

    public ByteString toProto() {
        // offset for dynamic-length data, immediately after value arguments
        var dynamicOffset = args.size() * 32;

        var argBytes = new ArrayList<ByteString>(args.size());
        var dynamicArgs = new ArrayList<ByteString>();

        // iterate the arguments and determine whether they are dynamic or not
        for (var arg : args) {
            if (arg.isDynamic) {
                // dynamic arguments supply their offset in value position and append their data at that offset
                argBytes.add(int256(dynamicOffset, 256));
                dynamicArgs.add(arg.value);
                dynamicOffset += arg.len;
            } else {
                // value arguments are dropped in the current arg position
                argBytes.add(arg.value);
            }
        }

        argBytes.addAll(dynamicArgs);

        var argByteStr = ByteString.copyFrom(argBytes);

        return funcName != null ? funcSelector(funcName, paramTypes).concat(argByteStr) : argByteStr;
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

        for (int i = 0; i < width; i += 8) {
            output.write((byte) val >> i);
        }

        return leftPad32(output.toByteString(), val < 0);
    }

    static ByteString int256(BigInteger val) {
        if (val.bitLength() > 256) {
            throw new IllegalArgumentException("BigInteger out of range for Solidity int<256>");
        }

        return leftPad32(ByteString.copyFrom(val.toByteArray()), val.signum() < 0);
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

    static ByteString rightPad32(ByteString input) {
        var rem = 32 - input.size() % 32;
        return rem == 32 ? input : input.concat(padding.substring(0, rem));
    }

    // some Rust-inspired type magic
    public final static class Constructor {
        private Constructor() { }
    }

    public final static class Function {
        private Function() { }
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
