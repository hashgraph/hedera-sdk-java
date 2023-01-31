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

import com.hedera.hashgraph.sdk.Utils.Bip32Utils;
import org.bouncycastle.util.encoders.Hex;
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

    @Test
    @DisplayName("Mnemonic passphrase test")
    void mnemonicPassphraseTest() throws Exception {
        // Test if mnemonic passphrase is BIP-39 compliant which requires unicode phrases to be NFKD normalized.
        // Use unicode string as a passphrase. If it is properly normalized to NFKD,
        // it should generate the expectedPrivateKey bellow:
        String passphrase = "\u03B4\u03BF\u03BA\u03B9\u03BC\u03AE";
        String expectedPrivateKey = "302e020100300506032b6570042204203fefe1000db9485372851d542453b07e7970de4e2ecede7187d733ac037f4d2c";

        Mnemonic mnemonic = Mnemonic.fromString(MNEMONIC_STRING);
        PrivateKey key = mnemonic.toPrivateKey(passphrase);
        assertThat(key.toString()).isEqualTo(expectedPrivateKey);
    }

    @Test
    @DisplayName("BIP39 test vector")
    void bip39() throws Exception {
        final String passphrase = "TREZOR";

        // The 18-word mnemonics are not supported by the SDK
        final String[] MNEMONIC_STRINGS = {
            "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about",
            "legal winner thank year wave sausage worth useful legal winner thank yellow",
            "letter advice cage absurd amount doctor acoustic avoid letter advice cage above",
            "zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo wrong",
//            "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon agent",
//            "legal winner thank year wave sausage worth useful legal winner thank year wave sausage worth useful legal will",
//            "letter advice cage absurd amount doctor acoustic avoid letter advice cage absurd amount doctor acoustic avoid letter always",
//            "zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo when",
            "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon art",
            "legal winner thank year wave sausage worth useful legal winner thank year wave sausage worth useful legal winner thank year wave sausage worth title",
            "letter advice cage absurd amount doctor acoustic avoid letter advice cage absurd amount doctor acoustic avoid letter advice cage absurd amount doctor acoustic bless",
            "zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo vote",
            "ozone drill grab fiber curtain grace pudding thank cruise elder eight picnic",
//            "gravity machine north sort system female filter attitude volume fold club stay feature office ecology stable narrow fog",
            "hamster diagram private dutch cause delay private meat slide toddler razor book happy fancy gospel tennis maple dilemma loan word shrug inflict delay length",
            "scheme spot photo card baby mountain device kick cradle pact join borrow",
//            "horn tenant knee talent sponsor spell gate clip pulse soap slush warm silver nephew swap uncle crack brave",
            "panda eyebrow bullet gorilla call smoke muffin taste mesh discover soft ostrich alcohol speed nation flash devote level hobby quick inner drive ghost inside",
            "cat swing flag economy stadium alone churn speed unique patch report train",
//            "light rule cinnamon wrap drastic word pride squirrel upgrade then income fatal apart sustain crack supply proud access",
            "all hour make first leader extend hole alien behind guard gospel lava path output census museum junior mass reopen famous sing advance salt reform",
            "vessel ladder alter error federal sibling chat ability sun glass valve picture",
//            "scissors invite lock maple supreme raw rapid void congress muscle digital elegant little brisk hair mango congress clump",
            "void come effort suffer camp survey warrior heavy shoot primary clutch crush open amazing screen patrol group space point ten exist slush involve unfold",
        };

        final String[] EXPECTED_SEEDS = {
            "c55257c360c07c72029aebc1b53c05ed0362ada38ead3e3e9efa3708e53495531f09a6987599d18264c1e1c92f2cf141630c7a3c4ab7c81b2f001698e7463b04",
            "2e8905819b8723fe2c1d161860e5ee1830318dbf49a83bd451cfb8440c28bd6fa457fe1296106559a3c80937a1c1069be3a3a5bd381ee6260e8d9739fce1f607",
            "d71de856f81a8acc65e6fc851a38d4d7ec216fd0796d0a6827a3ad6ed5511a30fa280f12eb2e47ed2ac03b5c462a0358d18d69fe4f985ec81778c1b370b652a8",
            "ac27495480225222079d7be181583751e86f571027b0497b5b5d11218e0a8a13332572917f0f8e5a589620c6f15b11c61dee327651a14c34e18231052e48c069",
//            "035895f2f481b1b0f01fcf8c289c794660b289981a78f8106447707fdd9666ca06da5a9a565181599b79f53b844d8a71dd9f439c52a3d7b3e8a79c906ac845fa",
//            "035895f2f481b1b0f01fcf8c289c794660b289981a78f8106447707fdd9666ca06da5a9a565181599b79f53b844d8a71dd9f439c52a3d7b3e8a79c906ac845fa",
//            "107d7c02a5aa6f38c58083ff74f04c607c2d2c0ecc55501dadd72d025b751bc27fe913ffb796f841c49b1d33b610cf0e91d3aa239027f5e99fe4ce9e5088cd65",
//            "0cd6e5d827bb62eb8fc1e262254223817fd068a74b5b449cc2f667c3f1f985a76379b43348d952e2265b4cd129090758b3e3c2c49103b5051aac2eaeb890a528",
            "bda85446c68413707090a52022edd26a1c9462295029f2e60cd7c4f2bbd3097170af7a4d73245cafa9c3cca8d561a7c3de6f5d4a10be8ed2a5e608d68f92fcc8",
            "bc09fca1804f7e69da93c2f2028eb238c227f2e9dda30cd63699232578480a4021b146ad717fbb7e451ce9eb835f43620bf5c514db0f8add49f5d121449d3e87",
            "c0c519bd0e91a2ed54357d9d1ebef6f5af218a153624cf4f2da911a0ed8f7a09e2ef61af0aca007096df430022f7a2b6fb91661a9589097069720d015e4e982f",
            "dd48c104698c30cfe2b6142103248622fb7bb0ff692eebb00089b32d22484e1613912f0a5b694407be899ffd31ed3992c456cdf60f5d4564b8ba3f05a69890ad",
            "274ddc525802f7c828d8ef7ddbcdc5304e87ac3535913611fbbfa986d0c9e5476c91689f9c8a54fd55bd38606aa6a8595ad213d4c9c9f9aca3fb217069a41028",
//            "628c3827a8823298ee685db84f55caa34b5cc195a778e52d45f59bcf75aba68e4d7590e101dc414bc1bbd5737666fbbef35d1f1903953b66624f910feef245ac",
            "64c87cde7e12ecf6704ab95bb1408bef047c22db4cc7491c4271d170a1b213d20b385bc1588d9c7b38f1b39d415665b8a9030c9ec653d75e65f847d8fc1fc440",
            "ea725895aaae8d4c1cf682c1bfd2d358d52ed9f0f0591131b559e2724bb234fca05aa9c02c57407e04ee9dc3b454aa63fbff483a8b11de949624b9f1831a9612",
//            "fd579828af3da1d32544ce4db5c73d53fc8acc4ddb1e3b251a31179cdb71e853c56d2fcb11aed39898ce6c34b10b5382772db8796e52837b54468aeb312cfc3d",
            "72be8e052fc4919d2adf28d5306b5474b0069df35b02303de8c1729c9538dbb6fc2d731d5f832193cd9fb6aeecbc469594a70e3dd50811b5067f3b88b28c3e8d",
            "deb5f45449e615feff5640f2e49f933ff51895de3b4381832b3139941c57b59205a42480c52175b6efcffaa58a2503887c1e8b363a707256bdd2b587b46541f5",
//            "4cbdff1ca2db800fd61cae72a57475fdc6bab03e441fd63f96dabd1f183ef5b782925f00105f318309a7e9c3ea6967c7801e46c8a58082674c860a37b93eda02",
            "26e975ec644423f4a4c4f4215ef09b4bd7ef924e85d1d17c4cf3f136c2863cf6df0a475045652c57eb5fb41513ca2a2d67722b77e954b4b3fc11f7590449191d",
            "2aaa9242daafcee6aa9d7269f17d4efe271e1b9a529178d7dc139cd18747090bf9d60295d0ce74309a78852a9caadf0af48aae1c6253839624076224374bc63f",
//            "7b4a10be9d98e6cba265566db7f136718e1398c71cb581e1b2f464cac1ceedf4f3e274dc270003c670ad8d02c4558b2f8e39edea2775c9e232c7cb798b069e88",
            "01f5bced59dec48e362f2c45b5de68b9fd6c92c6634f44d6d40aab69056506f0e35524a518034ddc1192e1dacd32c1ed3eaa3c3b131c88ed8e7e54c49a5d0998",
        };

        for (int i = 0; i < MNEMONIC_STRINGS.length; i++) {
            byte[] seed = Mnemonic.fromString(MNEMONIC_STRINGS[i]).toSeed(passphrase);
            assertThat(Hex.toHexString(seed)).isEqualTo(EXPECTED_SEEDS[i]);
        }
    }

    @Test
    @DisplayName("Mnemonic.toStandardED25519PrivateKey() validation test")
    void toStandardED25519PrivateKey() throws BadMnemonicException {
        Mnemonic mnemonic = Mnemonic.fromString(MNEMONIC_STRING);

        PrivateKey privateKey1 = mnemonic.toStandardEd25519PrivateKey("", 0);
        assertThat(Hex.toHexString(privateKey1.toBytesRaw())).isEqualTo("f8dcc99a1ced1cc59bc2fee161c26ca6d6af657da9aa654da724441343ecd16f");

        PrivateKey privateKey2 = mnemonic.toStandardEd25519PrivateKey("", 2147483647);
        assertThat(Hex.toHexString(privateKey2.toBytesRaw())).isEqualTo("e978a6407b74a0730f7aeb722ad64ab449b308e56006c8bff9aad070b9b66ddf");

        PrivateKey privateKey3 = mnemonic.toStandardEd25519PrivateKey("some pass", 0);
        assertThat(Hex.toHexString(privateKey3.toBytesRaw())).isEqualTo("abeca64d2337db386e289482a252334c68c7536daaefff55dc169ddb77fbae28");

        PrivateKey privateKey4 = mnemonic.toStandardEd25519PrivateKey("some pass", 2147483647);
        assertThat(Hex.toHexString(privateKey4.toBytesRaw())).isEqualTo("9a601db3e24b199912cec6573e6a3d01ffd3600d50524f998b8169c105165ae5");
    }

    @Test
    @DisplayName("Mnemonic.toStandardED25519PrivateKey() should fail when index is pre-hardened")
    void toStandardED25519PrivateKeyShouldFailWhenIndexIsPreHardened() throws BadMnemonicException {
        Mnemonic mnemonic = Mnemonic.fromString(MNEMONIC_STRING);
        int hardenedIndex = Bip32Utils.toHardenedIndex(10);

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
            () -> mnemonic.toStandardEd25519PrivateKey("", hardenedIndex)
        ).satisfies(error -> assertThat(error.getMessage()).isEqualTo("the index should not be pre-hardened"));

    }

    @Test
    @DisplayName("Mnemonic.toStandardECDSAsecp256k1PrivateKey() validation test")
    void toStandardECDSAsecp256k1PrivateKey() throws BadMnemonicException {
        Mnemonic mnemonic = Mnemonic.fromString(MNEMONIC_STRING);

        PrivateKey privateKey1 = mnemonic.toStandardECDSAsecp256k1PrivateKey("", 0);
        assertThat(Hex.toHexString(privateKey1.toBytesRaw())).isEqualTo("0fde7bfd57ae6ec310bdd8b95967d98e8762a2c02da6f694b152cf9860860ab8");

        PrivateKey privateKey2 = mnemonic.toStandardECDSAsecp256k1PrivateKey("", Bip32Utils.toHardenedIndex(0));
        assertThat(Hex.toHexString(privateKey2.toBytesRaw())).isEqualTo("aab7d720a32c2d1ea6123f58b074c865bb07f6c621f14cb012f66c08e64996bb");

        PrivateKey privateKey3 = mnemonic.toStandardECDSAsecp256k1PrivateKey("some pass", 0);
        assertThat(Hex.toHexString(privateKey3.toBytesRaw())).isEqualTo("6df5ed217cf6d5586fdf9c69d39c843eb9d152ca19d3e41f7bab483e62f6ac25");

        PrivateKey privateKey4 = mnemonic.toStandardECDSAsecp256k1PrivateKey("some pass", Bip32Utils.toHardenedIndex(0));
        assertThat(Hex.toHexString(privateKey4.toBytesRaw())).isEqualTo("80df01f79ee1b1f4e9ab80491c592c0ef912194ccca1e58346c3d35cb5b7c098");

        PrivateKey privateKey5 = mnemonic.toStandardECDSAsecp256k1PrivateKey("some pass", 2147483647);
        assertThat(Hex.toHexString(privateKey5.toBytesRaw())).isEqualTo("60cb2496a623e1201d4e0e7ce5da3833cd4ec7d6c2c06bce2bcbcbc9dfef22d6");

        PrivateKey privateKey6 = mnemonic.toStandardECDSAsecp256k1PrivateKey("some pass", Bip32Utils.toHardenedIndex(2147483647));
        assertThat(Hex.toHexString(privateKey6.toBytesRaw())).isEqualTo("100477c333028c8849250035be2a0a166a347a5074a8a727bce1db1c65181a50");
    }
}
