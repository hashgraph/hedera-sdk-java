package com.hedera.hashgraph.sdk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MnemonicTest {
    @Test
    @DisplayName("Mnemonic.generate() creates a valid mnemonic")
    void generateValidMnemonic() {
        assertDoesNotThrow(() -> Mnemonic.generate24());
        assertDoesNotThrow(() -> Mnemonic.generate12());
    }

    @ParameterizedTest
    @DisplayName("Mnemonic.validate() passes on known-good mnemonics")
    @ValueSource(strings = {
        "inmate flip alley wear offer often piece magnet surge toddler submit right radio absent pear floor belt raven price stove replace reduce plate home",
        "tiny denial casual grass skull spare awkward indoor ethics dash enough flavor good daughter early hard rug staff capable swallow raise flavor empty angle",
        "ramp april job flavor surround pyramid fish sea good know blame gate village viable include mixed term draft among monitor swear swing novel track",
        "evoke rich bicycle fire promote climb zero squeeze little spoil slight damage",
    })
    void knownGoodMnemonics(String mnemonicStr) {
        assertDoesNotThrow(() -> Mnemonic.fromString(mnemonicStr));
    }

    @Test
    @DisplayName("Mnemonic.validate() throws on short word list")
    void shortWordList() {
        BadMnemonicException exception = assertThrows(BadMnemonicException.class, () -> Mnemonic.fromWords(Arrays.asList("lorem", "ipsum", "dolor")));
        assertEquals(BadMnemonicReason.BadLength, exception.reason);
        assertNull(exception.unknownWordIndices);
    }

    @Test
    @DisplayName("Mnemonic.validate() throws on long word list")
    void longWordList() {
        BadMnemonicException exception = assertThrows(BadMnemonicException.class, () -> Mnemonic.fromWords(Arrays.asList("lorem", "ipsum", "dolor", "ramp", "april", "job", "flavor", "surround", "pyramid", "fish", "sea", "good", "know", "blame",
            "gate", "village", "viable", "include", "mixed", "term", "draft", "among", "monitor", "swear", "swing", "novel", "track")));
        assertEquals(BadMnemonicReason.BadLength, exception.reason);
        assertNull(exception.unknownWordIndices);
    }

    @Test
    @DisplayName("Mnemonic.validate() throws on 12-24 words")
    void betweenWordList() {
        BadMnemonicException exception = assertThrows(BadMnemonicException.class, () -> Mnemonic.fromWords(Arrays.asList("lorem", "ipsum", "dolor", "ramp", "april", "job", "flavor", "surround", "pyramid", "fish", "sea", "good", "know", "blame")));
        assertEquals(BadMnemonicReason.BadLength, exception.reason);
        assertNull(exception.unknownWordIndices);
    }

    @Test
    @DisplayName("Mnemonic.validate() throws on unknown words")
    void unknownWords() {
        BadMnemonicException exception = assertThrows(BadMnemonicException.class, () -> Mnemonic.fromWords(Arrays.asList(
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
        )));

        assertEquals(BadMnemonicReason.UnknownWords, exception.reason);
        assertEquals(Arrays.asList(6, 12, 17), exception.unknownWordIndices);
    }

    @Test
    @DisplayName("Mnemonic.validate() throws on checksum mismatch, 24 words")
    void checksumMismatch() {
        // this mnemonic was just made up, the checksum should definitely not match
        BadMnemonicException exception = assertThrows(BadMnemonicException.class, () -> Mnemonic.fromWords(Arrays.asList(
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
        )));

        assertEquals(BadMnemonicReason.ChecksumMismatch, exception.reason);
        assertNull(exception.unknownWordIndices);
    }

    @Test
    @DisplayName("Mnemonic.validate() throws on checksum mismatch, 12 words")
    void checksumMismatch12() {
        // this mnemonic was just made up, the checksum should definitely not match
        BadMnemonicException exception = assertThrows(BadMnemonicException.class, () -> Mnemonic.fromWords(Arrays.asList(
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
            "accident"
        )));

        assertEquals(BadMnemonicReason.ChecksumMismatch, exception.reason);
        assertNull(exception.unknownWordIndices);
    }

    @Test
    @DisplayName("Invalid Mnemonic can still be used to generate a private key")
    void invalidToPrivateKey() {
        Mnemonic mnemonic = assertThrows(BadMnemonicException.class, () -> Mnemonic.fromWords(Arrays.asList(
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
        ))).mnemonic;

        assertNotNull(mnemonic);
    }
}
