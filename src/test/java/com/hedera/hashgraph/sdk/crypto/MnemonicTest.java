package com.hedera.hashgraph.sdk.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MnemonicTest {
    // happy paths are covered by Ed25519PrivateKeyTest

    @Test
    @DisplayName("Mnemonic.validate() throws on short word list")
    void shortWordList() {
        Mnemonic mnemonic = new Mnemonic(Arrays.asList("lorem", "ipsum", "dolor"));

        assertEquals(
            "expected 24-word mnemonic, got 3 words",
            assertThrows(BadMnemonicException.class, mnemonic::validate).getMessage());
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

        BadMnemonicException error = assertThrows(BadMnemonicException.class, mnemonic::validate);
        assertEquals(Arrays.asList(6, 12, 17), error.unknownIndices);
        assertEquals(
            "the following words in the mnemonic were not in the word list: adsorb, acount, acquired",
            error.getMessage());
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

        BadMnemonicException error = assertThrows(BadMnemonicException.class, mnemonic::validate);
        assertNull(error.unknownIndices);
        assertEquals("mnemonic failed checksum, expected 0xff, got 0x02", error.getMessage());
    }
}
