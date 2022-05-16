/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class MnemonicTest {
    private static final String MNEMONIC3_STRING = "obvious favorite remain caution remove laptop base vacant increase video erase pass sniff sausage knock grid argue salt romance way alone fever slush dune";

    private static final String MNEMONIC_LEGACY_STRING = "jolly kidnap tom lawn drunk chick optic lust mutter mole bride galley dense member sage neural widow decide curb aboard margin manure";

    private static final String MNEMONIC_STRING = "inmate flip alley wear offer often piece magnet surge toddler submit right radio absent pear floor belt raven price stove replace reduce plate home";
    private static final String MNEMONIC_PRIVATE_KEY = "302e020100300506032b657004220420853f15aecd22706b105da1d709b4ac05b4906170c2b9c7495dff9af49e1391da";

    @Test
    @DisplayName("Mnemonic.generate() creates a valid mnemonic")
    void generateValidMnemonic() {
        Mnemonic.generate24();
        Mnemonic.generate12();
    }

    @ParameterizedTest
    @DisplayName("Mnemonic.validate() passes on known-good mnemonics")
    @ValueSource(strings = {
        "inmate flip alley wear offer often piece magnet surge toddler submit right radio absent pear floor belt raven price stove replace reduce plate home",
        "tiny denial casual grass skull spare awkward indoor ethics dash enough flavor good daughter early hard rug staff capable swallow raise flavor empty angle",
        "ramp april job flavor surround pyramid fish sea good know blame gate village viable include mixed term draft among monitor swear swing novel track",
        "evoke rich bicycle fire promote climb zero squeeze little spoil slight damage",
    })
    void knownGoodMnemonics(String mnemonicStr) throws Exception {
        Mnemonic.fromString(mnemonicStr);
    }

    @Test
    @DisplayName("Mnemonic.validate() throws on short word list")
    void shortWordList() {
        assertThatExceptionOfType(BadMnemonicException.class).isThrownBy(
            () -> Mnemonic.fromWords(Arrays.asList("lorem", "ipsum", "dolor"))
        ).satisfies(
            error -> {
                assertThat(error.reason).isEqualTo(BadMnemonicReason.BadLength);
                assertThat(error.unknownWordIndices).isNull();
            }
        );
    }

    @Test
    @DisplayName("Mnemonic.validate() throws on long word list")
    void longWordList() {
        assertThatExceptionOfType(BadMnemonicException.class).isThrownBy(
            () -> Mnemonic.fromWords(Arrays.asList(
                "lorem",
                "ipsum",
                "dolor",
                "ramp",
                "april",
                "job",
                "flavor",
                "surround",
                "pyramid",
                "fish",
                "sea",
                "good",
                "know",
                "blame",
                "gate",
                "village",
                "viable",
                "include",
                "mixed",
                "term",
                "draft",
                "among",
                "monitor",
                "swear",
                "swing",
                "novel",
                "track"
            ))
        ).satisfies(
            error -> {
                assertThat(error.reason).isEqualTo(BadMnemonicReason.BadLength);
                assertThat(error.unknownWordIndices).isNull();
            }
        );
    }

    @Test
    @DisplayName("Mnemonic.validate() throws on 12-24 words")
    void betweenWordList() {
        assertThatExceptionOfType(BadMnemonicException.class).isThrownBy(
            () -> Mnemonic.fromWords(Arrays.asList("" +
                "lorem",
                "ipsum",
                "dolor",
                "ramp",
                "april",
                "job",
                "flavor",
                "surround",
                "pyramid",
                "fish",
                "sea",
                "good",
                "know",
                "blame"
            ))
        ).satisfies(
            error -> {
                assertThat(error.reason).isEqualTo(BadMnemonicReason.BadLength);
                assertThat(error.unknownWordIndices).isNull();
            }
        );
    }

    @Test
    @DisplayName("Mnemonic.validate() throws on unknown words")
    void unknownWords() {
        assertThatExceptionOfType(BadMnemonicException.class).isThrownBy(
            () -> Mnemonic.fromWords(Arrays.asList(
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
            ))
        ).satisfies(
            error -> {
                assertThat(error.reason).isEqualTo(BadMnemonicReason.UnknownWords);
                assertThat(error.unknownWordIndices).containsExactly(6, 12, 17);
            }
        );
    }

    @Test
    @DisplayName("Mnemonic.validate() throws on checksum mismatch, 24 words")
    void checksumMismatch() {
        // this mnemonic was just made up, the checksum should definitely not match
        assertThatExceptionOfType(BadMnemonicException.class).isThrownBy(
            () -> Mnemonic.fromWords(Arrays.asList(
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
            ))
        ).satisfies(
            error -> {
                assertThat(error.reason).isEqualTo(BadMnemonicReason.ChecksumMismatch);
                assertThat(error.unknownWordIndices).isNull();
            }
        );
    }

    @Test
    @DisplayName("Mnemonic.validate() throws on checksum mismatch, 12 words")
    void checksumMismatch12() {
        // this mnemonic was just made up, the checksum should definitely not match
        assertThatExceptionOfType(BadMnemonicException.class).isThrownBy(
            () -> Mnemonic.fromWords(Arrays.asList(
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
            ))
        ).satisfies(
            error -> {
                assertThat(error.reason).isEqualTo(BadMnemonicReason.ChecksumMismatch);
                assertThat(error.unknownWordIndices).isNull();
            }
        );
    }

    @Test
    @DisplayName("Invalid Mnemonic can still be used to generate a private key")
    void invalidToPrivateKey() {
        assertThatExceptionOfType(BadMnemonicException.class).isThrownBy(() -> Mnemonic.fromWords(Arrays.asList(
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
        ))).satisfies(error -> assertThat(error.mnemonic).isNotNull());
    }

    @Test
    @DisplayName("Mnemonic 3 test")
    void thirdMnemonicTest() throws Exception {
        Mnemonic mnemonic = Mnemonic.fromString(MNEMONIC3_STRING);
        PrivateKey key = mnemonic.toLegacyPrivateKey();
        PrivateKey derivedKey = key.legacyDerive(0);
        PrivateKey derivedKey2 = key.legacyDerive(-1);
        assertThat(derivedKey.toString()).isEqualTo(
            "302e020100300506032b6570042204202b7345f302a10c2a6d55bf8b7af40f125ec41d780957826006d30776f0c441fb"
        );
        assertThat(derivedKey2.toString()).isEqualTo(
            "302e020100300506032b657004220420caffc03fdb9853e6a91a5b3c57a5c0031d164ce1c464dea88f3114786b5199e5"
        );
    }

    @Test
    @DisplayName("Legacy mnemonic test")
    void legacyMnemonicTest() throws Exception {
        Mnemonic mnemonic = Mnemonic.fromString(MNEMONIC_LEGACY_STRING);
        PrivateKey key = mnemonic.toLegacyPrivateKey();
        PrivateKey derivedKey = key.legacyDerive(0);
        PrivateKey derivedKey2 = key.legacyDerive(-1);
        assertThat(derivedKey.toString()).isEqualTo(
            "302e020100300506032b657004220420fae0002d2716ea3a60c9cd05ee3c4bb88723b196341b68a02d20975f9d049dc6"
        );
        assertThat(derivedKey2.toString()).isEqualTo(
            "302e020100300506032b657004220420882a565ad8cb45643892b5366c1ee1c1ef4a730c5ce821a219ff49b6bf173ddf"
        );
    }

    @Test
    @DisplayName("should match MyHbarWallet v1")
    void myHbarWalletV1Test() throws Exception {
        Mnemonic mnemonic = Mnemonic.fromString(MNEMONIC_LEGACY_STRING);
        PrivateKey key = mnemonic.toLegacyPrivateKey();
        PrivateKey derivedKey = key.legacyDerive(1099511627775L);
        assertThat(derivedKey.getPublicKey().toString()).isEqualTo(
            "302a300506032b657003210045f3a673984a0b4ee404a1f4404ed058475ecd177729daa042e437702f7791e9"
        );
    }

    @Test
    @DisplayName("Mnemonic test")
    void mnemonicTest() throws Exception {
        Mnemonic mnemonic = Mnemonic.fromString(MNEMONIC_STRING);
        PrivateKey key = mnemonic.toPrivateKey();
        assertThat(key.toString()).isEqualTo(MNEMONIC_PRIVATE_KEY);
    }
}
