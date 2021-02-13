package com.hedera.hashgraph.sdk.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MnemonicTest {
    @Test
    @DisplayName("Mnemonic.generate() creates a valid mnemonic")
    void generateValidMnemonic() {
        Mnemonic mnemonic = Mnemonic.generate();

        MnemonicValidationResult validationResult = mnemonic.validate();

        assertEquals(validationResult.status, MnemonicValidationStatus.Ok);
        assertNull(validationResult.unknownIndices);
        assertTrue(validationResult.isOk());
    }

    @ParameterizedTest
    @DisplayName("Mnemonic Legacy")
    @ValueSource(strings = {
        "jolly kidnap tom lawn drunk chick optic lust mutter mole bride galley dense member sage neural widow decide curb aboard margin manure",
    })
    void knownGoodLegacyMnemonic(String mnemonicStr) {
        Mnemonic mnemonic = Mnemonic.fromString(mnemonicStr);

//        MnemonicValidationResult validationResult = mnemonic.validate();

        Ed25519PrivateKey key = mnemonic.toLegacyPrivateKey();

        assertEquals(key.toString(), "302e020100300506032b657004220420882a565ad8cb45643892b5366c1ee1c1ef4a730c5ce821a219ff49b6bf173ddf");
    }

    @ParameterizedTest
    @DisplayName("Mnemonic Legacy 3")
    @ValueSource(strings = {
        "obvious favorite remain caution remove laptop base vacant increase video erase pass sniff sausage knock grid argue salt romance way alone fever slush dune",
    })
    void knownGoodLegacyMnemonic3(String mnemonicStr) {
        Mnemonic mnemonic = Mnemonic.fromString(mnemonicStr);

        Ed25519PrivateKey key = mnemonic.toLegacyPrivateKey();

        assertEquals(key.toString(), "302e020100300506032b6570042204202b7345f302a10c2a6d55bf8b7af40f125ec41d780957826006d30776f0c441fb");
    }

    @ParameterizedTest
    @DisplayName("Mnemonic.validate() passes on known-good mnemonics")
    @ValueSource(strings = {
        "inmate flip alley wear offer often piece magnet surge toddler submit right radio absent pear floor belt raven price stove replace reduce plate home",
        "tiny denial casual grass skull spare awkward indoor ethics dash enough flavor good daughter early hard rug staff capable swallow raise flavor empty angle",
        "ramp april job flavor surround pyramid fish sea good know blame gate village viable include mixed term draft among monitor swear swing novel track",
    })
    void knownGoodMnemonics(String mnemonicStr) {
        Mnemonic mnemonic = Mnemonic.fromString(mnemonicStr);

        MnemonicValidationResult validationResult = mnemonic.validate();

        assertEquals(validationResult.status, MnemonicValidationStatus.Ok);
        assertNull(validationResult.unknownIndices);
        assertTrue(validationResult.isOk());
    }

    @Test
    @DisplayName("Mnemonic.validate() throws on short word list")
    void shortWordList() {
        Mnemonic mnemonic = new Mnemonic(Arrays.asList("lorem", "ipsum", "dolor"));
        MnemonicValidationResult validationResult = mnemonic.validate();

        assertEquals(MnemonicValidationStatus.BadLength, validationResult.status);
        assertNull(validationResult.unknownIndices);
        assertFalse(validationResult.isOk());
    }

    @Test
    @DisplayName("Mnemonic.validate() throws on unknown words")
    void unknownWords() {
        Mnemonic mnemonic = new Mnemonic(Arrays.asList(
            "abandon",
            "ability",
            "able",
            "about",
            "above",
            "absent",
            "adsorb", // typo from "absorb"
            "abstract",
            "absurd",
            "abuse",
            "access",
            "accident",
            "acount", // typo from "account"
            "accuse",
            "achieve",
            "acid",
            "acoustic",
            "acquired", // typo from "acquire"
            "across",
            "act",
            "action",
            "actor",
            "actress",
            "actual"
        ));

        MnemonicValidationResult validationResult = mnemonic.validate();

        assertEquals(MnemonicValidationStatus.UnknownWords, validationResult.status);
        assertEquals(Arrays.asList(6, 12, 17), validationResult.unknownIndices);
        assertFalse(validationResult.isOk());
    }

    @Test
    @DisplayName("Mnemonic.validate() throws on checksum mismatch")
    void checksumMismatch() {
        // this mnemonic was just made up, the checksum should definitely not match
        Mnemonic mnemonic = new Mnemonic(Arrays.asList(
            "abandon",
            "ability",
            "able",
            "about",
            "above",
            "absent",
            "absorb",
            "abstract",
            "absurd",
            "abuse",
            "access",
            "accident",
            "account",
            "accuse",
            "achieve",
            "acid",
            "acoustic",
            "acquire",
            "across",
            "act",
            "action",
            "actor",
            "actress",
            "actual"
        ));

        MnemonicValidationResult validationResult = mnemonic.validate();

        assertEquals(MnemonicValidationStatus.ChecksumMismatch, validationResult.status);
        assertNull(validationResult.unknownIndices);
        assertFalse(validationResult.isOk());
    }
}
