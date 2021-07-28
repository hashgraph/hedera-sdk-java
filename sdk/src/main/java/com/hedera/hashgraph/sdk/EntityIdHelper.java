package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import java8.lang.FunctionalInterface;
import org.bouncycastle.util.encoders.DecoderException;
import org.bouncycastle.util.encoders.Hex;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
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

    private static final Pattern ENTITY_ID_REGEX = Pattern.compile("(0|(?:[1-9]\\d*))\\.(0|(?:[1-9]\\d*))\\.(0|(?:[1-9]\\d*))(?:-([a-z]{5}))?$");

    private EntityIdHelper() {}

    static <R> R fromString(String idString, WithIdNums<R> constructObjectWithIdNums) {
        var match = ENTITY_ID_REGEX.matcher(idString);
        if(!match.find()){
            throw new IllegalArgumentException(
                "Invalid ID \"" + idString + "\": format should look like 0.0.123 or 0.0.123-vfmkw"
            );
        }
        return constructObjectWithIdNums.apply(
            Long.parseLong(match.group(1)),
            Long.parseLong(match.group(2)),
            Long.parseLong(match.group(3)),
            match.group(4));
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
        return withAddress.apply(buf.getInt(), buf.getLong(), buf.getLong(), null);
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

    static String checksum(String ledgerId, String addr) {
        StringBuilder answer = new StringBuilder();
        List<Integer> d = new ArrayList<>(); // Digits with 10 for ".", so if addr == "0.0.123" then d == [0, 10, 0, 10, 1, 2, 3]
        @Var
        long s0 = 0; // Sum of even positions (mod 11)
        @Var
        long s1 = 0; // Sum of odd positions (mod 11)
        @Var
        long s = 0; // Weighted sum of all positions (mod p3)
        @Var
        long sh = 0; // Hash of the ledger ID
        @SuppressWarnings("UnusedVariable")
        @Var
        long c = 0; // The checksum, as a single number
        long p3 = 26 * 26 * 26; // 3 digits in base 26
        long p5 = 26 * 26 * 26 * 26 * 26; // 5 digits in base 26
        long asciiA = Character.codePointAt("a", 0); // 97
        long m = 1_000_003; //min prime greater than a million. Used for the final permutation.
        long w = 31; // Sum s of digit values weights them by powers of w. Should be coprime to p5.

        var id = ledgerId + "000000000000";
        List<Integer> h = new ArrayList<>();

        for (var i = 0; i < id.length(); i += 2) {
            h.add(Integer.parseInt(id.substring(i, Math.min(i + 2, id.length())), 16));
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
        c = (c * m) % p5;

        for (var i = 0; i < 5; i++) {
            answer.append((char)  (asciiA + (c % 26)));
            c /= 26;
        }

        return answer.reverse().toString();
    }

    @FunctionalInterface
    interface WithIdNums<R> {
        R apply(long shard, long realm, long num, @Nullable String checksum);
    }

    static void validate(long shard, long realm, long num, Client client, @Nullable String checksum) throws InvalidChecksumException {
        if(client.network.networkName != null && checksum != null) {
            String expectedChecksum = EntityIdHelper.checksum(
                Integer.toString(client.network.networkName.id),
                EntityIdHelper.toString(shard, realm, num)
            );
            if (!checksum.equals(expectedChecksum)) {
                throw new InvalidChecksumException(shard, realm, num, checksum, expectedChecksum);
            }
        }
    }

    static String toString(long shard, long realm, long num) {
        return "" + shard + "." + realm + "." + num;
    }

    static String toStringWithChecksum(long shard, long realm, long num, Client client, @Nullable String checksum) {
        if(checksum != null) {
            return "" + shard + "." + realm + "." + num + "-" + checksum;
        } else if (client.getNetworkName() != null) {
            return "" + shard + "." + realm + "." + num + "-" + checksum(Integer.toString(client.getNetworkName().id), EntityIdHelper.toString(shard, realm, num));
        } else {
            throw new IllegalStateException("Attempted to convert an entity ID to a string with a checksum, but it was impossible to derive the checksum.  Make sure the network name is set on the client.");
        }
    }
}
