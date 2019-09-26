package com.hedera.hashgraph.sdk.crypto;

import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import io.github.novacrypto.bip39.MnemonicGenerator;
import io.github.novacrypto.bip39.wordlists.English;

/**
 * BIP-39 mnemonic phrases compatible with the Android and iOS mobile wallets.
 */
public final class Mnemonic {
    /**
     * The list of words in this mnemonic.
     */
    public final List<CharSequence> wordList;

    @Nullable
    private String asString;

    private static final MnemonicGenerator generator = new MnemonicGenerator(English.INSTANCE);

    private static final SecureRandom secureRandom = new SecureRandom();

    public Mnemonic(List<CharSequence> wordList) {
        if (wordList.size() != 24) {
            throw new IllegalArgumentException("wordList must have length 24");
        }

        this.wordList = Collections.unmodifiableList(wordList);
    }

    public static Mnemonic fromString(String mnemonicString) {
        return new Mnemonic(Arrays.asList(mnemonicString.split(" ")));
    }

    public static Mnemonic generate() {
        final byte[] entropy = new byte[32];
        secureRandom.nextBytes(entropy);

        final ArrayList<CharSequence> wordList = new ArrayList<>(24);

        generator.createMnemonic(entropy, word -> {
            // the generator spits out spaces whether you want them or not
            if (!word.toString().equals(" ")) {
                wordList.add(word);
            }
        });

        return new Mnemonic(wordList);
    }

    @Override
    public String toString() {
        if (asString == null) {
            asString = String.join(" ", wordList);
        }

        return asString;
    }

    /**
     * Derive a 64 byte seed from this mnemonic using the given passphrase, which can be empty.
     */
    public byte[] toSeed(String passphrase) {
        final String salt = "mnemonic" + passphrase;

        // BIP-39 seed generation
        final PKCS5S2ParametersGenerator pbkdf2 = new PKCS5S2ParametersGenerator(new SHA512Digest());
        pbkdf2.init(
            toString().getBytes(StandardCharsets.UTF_8),
            salt.getBytes(StandardCharsets.UTF_8),
            2048);

        final KeyParameter key = (KeyParameter) pbkdf2.generateDerivedParameters(512);
        return key.getKey();
    }
}
