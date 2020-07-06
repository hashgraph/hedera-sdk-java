package com.hedera.hashgraph.sdk;

import com.google.common.base.Joiner;
import com.google.errorprone.annotations.Var;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * BIP-39 24-word mnemonic phrases compatible with the Android and iOS mobile wallets.
 */
public final class Mnemonic {
    // by storing our word list in a SoftReference, the GC is free to evict it at its discretion
    // but the implementation is meant to wait until free space is needed
    @Nullable
    private static SoftReference<List<String>> wordList;

    /**
     * The list of words in this mnemonic.
     */
    public final List<CharSequence> words;

    @Nullable
    private String asString;

    private Mnemonic(List<? extends CharSequence> words) {
        this.words = Collections.unmodifiableList(words);
    }

    /**
     * Construct a mnemonic from a 24-word list. {@link Mnemonic#validate()}
     * is called before returning, and it will throw an exception if it
     * does not pass validation. An invalid mnemonic can still create valid
     * Ed25519 private keys, so the exception will contain the mnemonic in case
     * the user wants to ignore the outcome of the validation.
     *
     * @param words the 24-word list that constitutes a mnemonic phrase.
     * @see #validate() the function that validates the mnemonic.
     * @throws BadMnemonicException if the mnemonic does not pass validation.
     * @return {@code this}
     */
    public static Mnemonic fromWords(List<? extends CharSequence> words) throws BadMnemonicException {
        Mnemonic mnemonic = new Mnemonic(words);

        mnemonic.validate();

        return mnemonic;
    }

    /**
     * Recover a mnemonic from a string, splitting on spaces.
     *
     * @param mnemonicString The string to recover the mnemonic from
     * @return {@code this}
     * @throws BadMnemonicException if the mnemonic does not pass validation.
     */
    public static Mnemonic fromString(String mnemonicString) throws BadMnemonicException {
        return Mnemonic.fromWords(Arrays.asList(mnemonicString.split(" ")));
    }

    /**
     * Returns a new random 24-word mnemonic from the BIP-39 standard English word list.
     * @return {@code this}
     */
    public static Mnemonic generate() {
        var entropy = new byte[32];
        ThreadLocalSecureRandom.current().nextBytes(entropy);

        return new Mnemonic(entropyToWords(entropy));
    }

    private static List<String> entropyToWords(byte[] entropy) {
        // we only care to support 24 word mnemonics
        if (entropy.length != 32) {
            throw new IllegalArgumentException("invalid entropy byte length: " + entropy.length);
        }

        // checksum for 256 bits is one byte
        var bytes = Arrays.copyOf(entropy, 33);
        bytes[32] = checksum(entropy);

        var wordList = getWordList();
        var words = new ArrayList<String>(24);

        @Var var scratch = 0;
        @Var var offset = 0;

        for (var b : bytes) {
            // shift `bytes` into `scratch`, popping off 11-bit indices when we can
            scratch <<= 8;
            // bitwise operations implicitly widen to `int` so mask off sign-extended bits
            scratch |= b & 0xFF;
            offset += 8;

            if (offset >= 11) {
                // pop 11 bits off the end of `scratch` and into `index`
                var index = (scratch >> (offset - 11)) & 0x7FF;
                offset -= 11;

                words.add(wordList.get(index));
            }
        }

        return words;
    }

    // hash the first 32 bytes of `entropy` and return the first byte of the digest
    private static byte checksum(byte[] entropy) {
        SHA256Digest digest = new SHA256Digest();
        // hash the first
        digest.update(entropy, 0, 32);

        byte[] checksum = new byte[digest.getDigestSize()];
        digest.doFinal(checksum, 0);

        return checksum[0];
    }

    private static int getWordIndex(CharSequence word) {
        return Collections.binarySearch(getWordList(), word, null);
    }

    private static List<String> getWordList() {
        if (wordList == null || wordList.get() == null) {
            synchronized (Mnemonic.class) {
                if (wordList == null || wordList.get() == null) {
                    List<String> words = readWordList();
                    wordList = new SoftReference<>(words);
                    // immediately return the strong reference
                    return words;
                }
            }
        }

        return wordList.get();
    }

    private static List<String> readWordList() {
        InputStream wordStream = Mnemonic.class.getClassLoader().getResourceAsStream("bip39-english.txt");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(wordStream), UTF_8))) {
            ArrayList<String> words = new ArrayList<>(2048);

            for (String word = reader.readLine(); word != null; word = reader.readLine()) {
                words.add(word);
            }
            return Collections.unmodifiableList(words);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Recover a private key from this mnemonic phrase.
     * <p>
     * This is not compatible with the phrases generated by the Android and iOS wallets;
     * use the no-passphrase version instead.
     *
     * @param passphrase the passphrase used to protect the mnemonic (not used in the
     *                   mobile wallets, use {@link #toPrivateKey()} instead.)
     * @return the recovered key; use {@link PrivateKey#derive(int)} to get a key for an
     * account index (0 for default account)
     * @see PrivateKey#fromMnemonic(Mnemonic, String)
     */
    public PrivateKey toPrivateKey(String passphrase) {
        return PrivateKey.fromMnemonic(this, passphrase);
    }

    /**
     * Recover a private key from this mnemonic phrase.
     *
     * @return the recovered key; use {@link PrivateKey#derive(int)} to get a key for an
     * account index (0 for default account)
     * @see PrivateKey#fromMnemonic(Mnemonic)
     */
    public PrivateKey toPrivateKey() {
        return toPrivateKey("");
    }

    /**
     * Validate that this is a valid BIP-39 mnemonic as generated by BIP-39's rules.
     * <p>
     * Technically, invalid mnemonics can still be used to generate valid private keys,
     * but if they became invalid due to user error then it will be difficult for the user
     * to tell the difference unless they compare the generated keys.
     * <p>
     * During validation, the following conditions are checked in order:
     * <ol>
     *     <li>{@link #words}{@code .length} is exactly 24.</li>
     *     <li>All strings in {@code #words} exist in the BIP-39 standard English word list (no normalization is done).</li>
     *     <li>The calculated checksum for the mnemonic equals the checksum encoded in the mnemonic.</li>
     * </ol>
     * <p>
     * If one of these checks do not pass, a {@link BadMnemonicException} is thrown containing the mnemonic
     * and the reason why it failed validation.
     *
     * @see BadMnemonicException
     * @see BadMnemonicReason
     * @see <a href="https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki">
     * Bitcoin Improvement Project proposal 39 (BIP-39)
     * </a>
     * @see <a href="https://github.com/bitcoin/bips/blob/master/bip-0039/english.txt">
     * BIP-39 English word list
     * </a>.
     * @throws BadMnemonicException if the mnemonic does not pass validation.
     */
    public void validate() throws BadMnemonicException {
        if (words.size() != 24) {
            throw new BadMnemonicException(this, BadMnemonicReason.BadLength);
        }

        ArrayList<Integer> unknownIndices = new ArrayList<>();

        for (int i = 0; i < words.size(); i++) {
            if (getWordIndex(words.get(i)) < 0) {
                unknownIndices.add(i);
            }
        }

        if (!unknownIndices.isEmpty()) {
            throw new BadMnemonicException(this, BadMnemonicReason.UnknownWords, unknownIndices);
        }

        // test the checksum encoded in the mnemonic
        byte[] entropyAndChecksum = wordsToEntropyAndChecksum();

        // ignores the 33rd byte
        byte expectedChecksum = checksum(entropyAndChecksum);
        byte givenChecksum = entropyAndChecksum[32];

        if (givenChecksum != expectedChecksum) {
            throw new BadMnemonicException(this, BadMnemonicReason.ChecksumMismatch);
        }
    }

    @Override
    public String toString() {
        if (asString == null) {
            asString = Joiner.on(' ').join(words);
        }

        return asString;
    }

    byte[] toSeed(String passphrase) {
        String salt = "mnemonic" + passphrase;

        // BIP-39 seed generation
        PKCS5S2ParametersGenerator pbkdf2 = new PKCS5S2ParametersGenerator(new SHA512Digest());
        pbkdf2.init(
            toString().getBytes(UTF_8),
            salt.getBytes(UTF_8),
            2048);

        KeyParameter key = (KeyParameter) pbkdf2.generateDerivedParameters(512);
        return key.getKey();
    }

    private byte[] wordsToEntropyAndChecksum() {
        if (words.size() != 24) {
            // should be checked in `validate()`
            throw new IllegalStateException(
                "(BUG) expected 24-word mnemonic, got " + words.size() + " words");
        }

        ByteBuffer buffer = ByteBuffer.allocate(33);

        // reverse algorithm of `entropyToWords()` below
        @Var int scratch = 0;
        @Var int offset = 0;
        for (CharSequence word : words) {
            int index = getWordIndex(word);

            if (index < 0) {
                // should also be checked in `validate()`
                throw new IllegalStateException("(BUG) word not in word list: " + word);
            } else if (index > 0x7FF) {
                throw new IndexOutOfBoundsException("(BUG) index out of bounds: " + index);
            }

            scratch <<= 11;
            scratch |= index;
            offset += 11;

            while (offset >= 8) {
                // truncation is what we want here
                buffer.put((byte) (scratch >> (offset - 8)));
                offset -= 8;
            }
        }

        return buffer.array();
    }
}
