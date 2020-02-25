package com.hedera.hashgraph.sdk.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MnemonicTest {
    // happy paths are covered by Ed25519PrivateKeyTest

    @Test
    @DisplayName("new Mnemonic() throws on short word list")
    void shortWordList() {
        assertEquals(
            "expected 24-word mnemonic, got 3 words",
            assertThrows(BadMnemonicError.class,
                () -> new Mnemonic(Arrays.asList("lorem", "ipsum", "dolor"))).getMessage());
    }

    private static final List<String> words = Arrays.asList(
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
    );

    @Test
    @DisplayName("new Mnemonic() throws on unknown words")
    void unknownWords() {
        BadMnemonicError error = assertThrows(BadMnemonicError.class, () -> new Mnemonic(words));
        assertEquals(Arrays.asList(6, 12, 17), error.unknownIndices);
        assertEquals(
            "the following words in the mnemonic were not in the word list: adsorb, acount, acquired",
            error.getMessage());
    }
}
