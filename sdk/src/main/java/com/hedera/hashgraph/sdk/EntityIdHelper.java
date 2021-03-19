package com.hedera.hashgraph.sdk;

import com.google.common.base.Splitter;
import com.google.errorprone.annotations.Var;
import java8.lang.FunctionalInterface;
import org.bouncycastle.util.encoders.DecoderException;
import org.bouncycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

class EntityIdHelper {
    /**
     * The length of a Solidity address in bytes.
     */
    static final int SOLIDITY_ADDRESS_LEN = 20;

    /**
     * The length of a hexadecimal-encoded Solidity address, in ASCII characters (bytes).
     */
    static final int SOLIDITY_ADDRESS_LEN_HEX = SOLIDITY_ADDRESS_LEN * 2;

    static <R> R fromString(String id, WithIdNums<R> withIdNums) {
        R newId;

        try {
            var result = parseAddress("", id);
            verify(result.status);
            newId = withIdNums.apply(result.num1, result.num2, result.num3);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Id format, should be in format {shardNum}.{realmNum}.{idNum}", e);
        }

        return newId;
    }

    static <R> R fromSolidityAddress(String address, WithIdNums<R> withAddress) {
        return fromSolidityAddress(decodeSolidityAddress(address), withAddress);
    }

    private static <R> R fromSolidityAddress(byte[] address, WithIdNums<R> withAddress) {
        if (address.length != SOLIDITY_ADDRESS_LEN) {
            throw new IllegalArgumentException(
                "Solidity addresses must be 20 bytes or 40 hex chars");
        }

        var buf = ByteBuffer.wrap(address);
        return withAddress.apply(buf.getInt(), buf.getLong(), buf.getLong());
    }

    private static byte[] decodeSolidityAddress(@Var String address) {
        address = address.startsWith("0x") ? address.substring(2) : address;

        if (address.length() != SOLIDITY_ADDRESS_LEN_HEX) {
            throw new IllegalArgumentException(
                "Solidity addresses must be 20 bytes or 40 hex chars");
        }

        try {
            return Hex.decode(address);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("failed to decode Solidity address as hex", e);
        }
    }

    static String toSolidityAddress(long shard, long realm, long num) {
        if (Long.highestOneBit(shard) > 32) {
            throw new IllegalStateException("shard out of 32-bit range " + shard);
        }

        return Hex.toHexString(
            ByteBuffer.allocate(20)
                .putInt((int) shard)
                .putLong(realm)
                .putLong(num)
                .array());
    }

    static void verify(int status) {
        switch (status) {
            case 0: // Syntax error
                throw new Error(
                    "Invalid ID: format should look like 0.0.123 or 0.0.123-laujm"
                );
            case 1: // An invalid with-checksum address
                throw new Error("Invalid ID: checksum does not match");
            case 2: // A valid no-checksum address
            break;
            case 3: // A valid with-checksum address
            break;
        }
    }

    static ParseAddressResult parseAddress(String ledgerId, String addr ) {
        Pattern regex1 = Pattern.compile("(0|(?:[1-9]\\d*))\\.(0|(?:[1-9]\\d*))\\.(0|(?:[1-9]\\d*))(?:-([a-z]{5}))?$");
        var match = regex1.matcher(addr);
        if(!match.find()){
            return new ParseAddressResult(0);
        }

        var a = new Long[]{Long.parseLong(match.group(1)), Long.parseLong(match.group(2)), Long.parseLong(match.group(3))};
        var ad = a[0].toString() + "." + a[1].toString() + "." + a[2].toString();
        var c = checksum(ledgerId, ad);
        var s = match.group(4) == null ? 2 : c.equals(match.group(4)) ? 3 : 1;
        return new ParseAddressResult(s, a[0], a[1], a[2], c, match.group(4), ad,ad + "-" + c);
    }

    static String checksum(String ledgerId, String addr ) {
        StringBuilder answer = new StringBuilder();
        List<Integer> d = new ArrayList<>(); // Digits with 10 for ".", so if addr == "0.0.123" then d == [0, 10, 0, 10, 1, 2, 3]
        var s0 = 0; // Sum of even positions (mod 11)
        var s1 = 0; // Sum of odd positions (mod 11)
        var s = 0; // Weighted sum of all positions (mod p3)
        var sh = 0; // Hash of the ledger ID
        var c = 0; // The checksum, as a single number
        var p3 = 26 * 26 * 26; // 3 digits in base 26
        var p5 = 26 * 26 * 26 * 26 * 26; // 5 digits in base 26
        var ascii_a = Character.codePointAt("a", 0); // 97
        var w = 31; // Sum s of digit values weights them by powers of w. Should be coprime to p5.

        var id = ledgerId + "000000000000";
        List<Integer> h = new ArrayList<>();

        for (var i = 0; i < id.length(); i += 2) {
            h.add(Integer.parseInt(id.substring(i, i+2), 16));
        }
        for (var i = 0; i < addr.length(); i++) {
            d.add(addr.charAt(i) == '.' ? 10 : Integer.parseInt(String.valueOf(addr.charAt(i)), 10));
        }
        for (var i = 0; i < d.size(); i++) {
            s = (w * s + d.get(i)) % p3;
            if (i % 2 == 0) {
                s0 = (s0 + d.get(i)) % 11;
            } else {
                s1 = (s1 + d.get(i)) % 11;
            }
        }
        for (Integer integer : h) {
            sh = (w * sh + integer) % p5;
        }
        c = ((((addr.length() % 5) * 11 + s0) * 11 + s1) * p3 + s + sh) % p5;

        for (var i = 0; i < 5; i++) {
            answer.append(Character.toChars( ascii_a + (c % 26)));
            c /= 26;
        }

        return answer.reverse().toString();
    }

    @FunctionalInterface
    interface WithIdNums<R> {
        R apply(long shard, long realm, long num);
    }
}
