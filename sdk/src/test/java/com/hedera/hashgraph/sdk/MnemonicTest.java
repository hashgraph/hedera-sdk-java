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

import com.hedera.hashgraph.sdk.utils.Bip32Utils;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class MnemonicTest {
    private static final String MNEMONIC_LEGACY_V1_STRING = "jolly kidnap tom lawn drunk chick optic lust mutter mole bride galley dense member sage neural widow decide curb aboard margin manure";
    private static final String MNEMONIC_LEGACY_V2_STRING = "obvious favorite remain caution remove laptop base vacant increase video erase pass sniff sausage knock grid argue salt romance way alone fever slush dune";
    private static final String MNEMONIC_24_WORD_STRING = "inmate flip alley wear offer often piece magnet surge toddler submit right radio absent pear floor belt raven price stove replace reduce plate home";
    private static final String MNEMONIC_12_WORD_STRING = "finish furnace tomorrow wine mass goose festival air palm easy region guilt";
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
    @DisplayName("Legacy V1 mnemonic test")
    void legacyV1MnemonicTest() throws Exception {
        // TODO: add link to reference test vectors
        final String PRIVATE_KEY1 = "00c2f59212cb3417f0ee0d38e7bd876810d04f2dd2cb5c2d8f26ff406573f2bd";
        final String PUBLIC_KEY1 = "0c5bb4624df6b64c2f07a8cb8753945dd42d4b9a2ed4c0bf98e87ef154f473e9";

        final String PRIVATE_KEY2 = "fae0002d2716ea3a60c9cd05ee3c4bb88723b196341b68a02d20975f9d049dc6";
        final String PUBLIC_KEY2 = "f40f9fdb1f161c31ed656794ada7af8025e8b5c70e538f38a4dfb46a0a6b0392";

        final String PRIVATE_KEY3 = "882a565ad8cb45643892b5366c1ee1c1ef4a730c5ce821a219ff49b6bf173ddf";
        final String PUBLIC_KEY3 = "53c6b451e695d6abc52168a269316a0d20deee2331f612d4fb8b2b379e5c6854";

        final String PRIVATE_KEY4 = "6890dc311754ce9d3fc36bdf83301aa1c8f2556e035a6d0d13c2cccdbbab1242";
        final String PUBLIC_KEY4 = "45f3a673984a0b4ee404a1f4404ed058475ecd177729daa042e437702f7791e9";

        Mnemonic mnemonic = Mnemonic.fromString(MNEMONIC_LEGACY_V1_STRING);

        // Chain m
        PrivateKey key1 = mnemonic.toLegacyPrivateKey();
        assertThat(key1.toStringRaw()).isEqualTo(PRIVATE_KEY1);
        assertThat(key1.getPublicKey().toStringRaw()).isEqualTo(PUBLIC_KEY1);

        // Chain m/0
        PrivateKey key2 = key1.legacyDerive(0);
        assertThat(key2.toStringRaw()).isEqualTo(PRIVATE_KEY2);
        assertThat(key2.getPublicKey().toStringRaw()).isEqualTo(PUBLIC_KEY2);

        // Chain m/-1
        PrivateKey key3 = key1.legacyDerive(-1);
        assertThat(key3.toStringRaw()).isEqualTo(PRIVATE_KEY3);
        assertThat(key3.getPublicKey().toStringRaw()).isEqualTo(PUBLIC_KEY3);

        // Chain m/1099511627775
        PrivateKey key4 = key1.legacyDerive(1099511627775L);
        assertThat(key4.toStringRaw()).isEqualTo(PRIVATE_KEY4);
        assertThat(key4.getPublicKey().toStringRaw()).isEqualTo(PUBLIC_KEY4);
    }

    @Test
    @DisplayName("Legacy V2 mnemonic test")
    void legacyV2MnemonicTest() throws Exception {
        // TODO: add link to reference test vectors
        final String PRIVATE_KEY1 = "98aa82d6125b5efa04bf8372be7931d05cd77f5ef3330b97d6ee7c006eaaf312";
        final String PUBLIC_KEY1 = "e0ce688d614f22f96d9d213ca513d58a7d03d954fe45790006e6e86b25456465";

        final String PRIVATE_KEY2 = "2b7345f302a10c2a6d55bf8b7af40f125ec41d780957826006d30776f0c441fb";
        final String PUBLIC_KEY2 = "0e19f99800b007cc7c82f9d85b73e0f6e48799469450caf43f253b48c4d0d91a";

        final String PRIVATE_KEY3 = "caffc03fdb9853e6a91a5b3c57a5c0031d164ce1c464dea88f3114786b5199e5";
        final String PUBLIC_KEY3 = "9fe11da3fcfba5d28a6645ecb611a9a43dbe6014b102279ba1d34506ea86974b";

        Mnemonic mnemonic = Mnemonic.fromString(MNEMONIC_LEGACY_V2_STRING);

        // Chain m
        PrivateKey key1 = mnemonic.toLegacyPrivateKey();
        assertThat(key1.toStringRaw()).isEqualTo(PRIVATE_KEY1);
        assertThat(key1.getPublicKey().toStringRaw()).isEqualTo(PUBLIC_KEY1);

        // Chain m/0
        PrivateKey key2 = key1.legacyDerive(0);
        assertThat(key2.toStringRaw()).isEqualTo(PRIVATE_KEY2);
        assertThat(key2.getPublicKey().toStringRaw()).isEqualTo(PUBLIC_KEY2);

        // Chain m/-1
        PrivateKey key3 = key1.legacyDerive(-1);
        assertThat(key3.toStringRaw()).isEqualTo(PRIVATE_KEY3);
        assertThat(key3.getPublicKey().toStringRaw()).isEqualTo(PUBLIC_KEY3);
    }

    @Test
    @DisplayName("Mnemonic test")
    void mnemonicTest() throws Exception {
        Mnemonic mnemonic = Mnemonic.fromString(MNEMONIC_24_WORD_STRING);
        PrivateKey key = mnemonic.toPrivateKey();
        assertThat(key).hasToString(
            "302e020100300506032b657004220420853f15aecd22706b105da1d709b4ac05b4906170c2b9c7495dff9af49e1391da"
        );
    }

    @Test
    @DisplayName("Mnemonic passphrase test")
    void mnemonicPassphraseTest() throws Exception {
        // Test if mnemonic passphrase is BIP-39 compliant which requires unicode phrases to be NFKD normalized.
        // Use unicode string as a passphrase. If it is properly normalized to NFKD,
        // it should generate the expectedPrivateKey bellow:
        String passphrase = "\u03B4\u03BF\u03BA\u03B9\u03BC\u03AE";
        String expectedPrivateKey = "302e020100300506032b6570042204203fefe1000db9485372851d542453b07e7970de4e2ecede7187d733ac037f4d2c";

        Mnemonic mnemonic = Mnemonic.fromString(MNEMONIC_24_WORD_STRING);
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
    @DisplayName("Mnemonic.toStandardED25519PrivateKey() test vector")
    void toStandardED25519PrivateKey() throws BadMnemonicException {
        // https://github.com/hashgraph/hedera-sdk-reference/issues/73#issuecomment-1422330626
        final String CHAIN_CODE1 = "404914563637c92d688deb9d41f3f25cbe8d6659d859cc743712fcfac72d7eda";
        final String PRIVATE_KEY1 = "f8dcc99a1ced1cc59bc2fee161c26ca6d6af657da9aa654da724441343ecd16f";
        final String PUBLIC_KEY1 = "2e42c9f5a5cdbde64afa65ce3dbaf013d5f9ff8d177f6ef4eb89fbe8c084ec0d";

        final String CHAIN_CODE2 = "9c2b0073ac934696cd0b52c6c521b9bd1902aac134380a737282fdfe29014bf1";
        final String PRIVATE_KEY2 = "e978a6407b74a0730f7aeb722ad64ab449b308e56006c8bff9aad070b9b66ddf";
        final String PUBLIC_KEY2 = "c4b33dca1f83509f17b69b2686ee46b8556143f79f4b9df7fe7ed3864c0c64d0";

        final String CHAIN_CODE3 = "699344acc5e07c77eb63b154b4c5c3d33cab8bf85ee21bea4cc29ab7f0502259";
        final String PRIVATE_KEY3 = "abeca64d2337db386e289482a252334c68c7536daaefff55dc169ddb77fbae28";
        final String PUBLIC_KEY3 = "fd311925a7a04b38f7508931c6ae6a93e5dc4394d83dafda49b051c0017d3380";

        final String CHAIN_CODE4 = "e5af7c95043a912af57a6e031ddcad191677c265d75c39954152a2733c750a3b";
        final String PRIVATE_KEY4 = "9a601db3e24b199912cec6573e6a3d01ffd3600d50524f998b8169c105165ae5";
        final String PUBLIC_KEY4 = "cf525500706faa7752dca65a086c9381d30d72cc67f23bf334f330579074a890";

        Mnemonic mnemonic = Mnemonic.fromString(MNEMONIC_24_WORD_STRING);

        // Chain m/44'/3030'/0'/0'/0'
        PrivateKey key1 = mnemonic.toStandardEd25519PrivateKey("", 0);
        assertThat(Hex.toHexString(key1.getChainCode().getKey())).isEqualTo(CHAIN_CODE1);
        assertThat(key1.toStringRaw()).isEqualTo(PRIVATE_KEY1);
        assertThat(key1.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY1);

        // Chain m/44'/3030'/0'/0'/2147483647'
        PrivateKey key2 = mnemonic.toStandardEd25519PrivateKey("", 2147483647);
        assertThat(Hex.toHexString(key2.getChainCode().getKey())).isEqualTo(CHAIN_CODE2);
        assertThat(key2.toStringRaw()).isEqualTo(PRIVATE_KEY2);
        assertThat(key2.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY2);

        // Chain m/44'/3030'/0'/0'/0'; Passphrase: "some pass"
        PrivateKey key3 = mnemonic.toStandardEd25519PrivateKey("some pass", 0);
        assertThat(Hex.toHexString(key3.getChainCode().getKey())).isEqualTo(CHAIN_CODE3);
        assertThat(key3.toStringRaw()).isEqualTo(PRIVATE_KEY3);
        assertThat(key3.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY3);

        // Chain m/44'/3030'/0'/0'/2147483647'; Passphrase: "some pass"
        PrivateKey key4 = mnemonic.toStandardEd25519PrivateKey("some pass", 2147483647);
        assertThat(Hex.toHexString(key4.getChainCode().getKey())).isEqualTo(CHAIN_CODE4);
        assertThat(key4.toStringRaw()).isEqualTo(PRIVATE_KEY4);
        assertThat(key4.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY4);
    }

    @Test
    @DisplayName("Mnemonic.toStandardED25519PrivateKey() test vector 2")
    void toStandardED25519PrivateKey2() throws BadMnemonicException {
        // https://github.com/hashgraph/hedera-sdk-reference/issues/73#issuecomment-1424761224
        final String CHAIN_CODE1 = "48c89d67e9920e443f09d2b14525213ff83b245c8b98d63747ea0801e6d0ff3f";
        final String PRIVATE_KEY1 = "020487611f3167a68482b0f4aacdeb02cc30c52e53852af7b73779f67eeca3c5";
        final String PUBLIC_KEY1 = "2d047ff02a2091f860633f849ea2024b23e7803cfd628c9bdd635010cbd782d3";

        final String CHAIN_CODE2 = "c0bcdbd9df6d8a4f214f20f3e5c7856415b68be34a1f406398c04690818bea16";
        final String PRIVATE_KEY2 = "d0c4484480944db698dd51936b7ecc81b0b87e8eafc3d5563c76339338f9611a";
        final String PUBLIC_KEY2 = "a1a2573c2c45bd57b0fd054865b5b3d8f492a6e1572bf04b44471e07e2f589b2";

        final String CHAIN_CODE3 = "998a156855ab5398afcde06164b63c5523ff2c8900db53962cc2af191df59e1c";
        final String PRIVATE_KEY3 = "d06630d6e4c17942155819bbbe0db8306cd989ba7baf3c29985c8455fbefc37f";
        final String PUBLIC_KEY3 = "6bd0a51e0ca6fcc8b13cf25efd0b4814978bcaca7d1cf7dbedf538eb02969acb";

        final String CHAIN_CODE4 = "19d99506a5ce2dc0080092068d278fe29b85ffb8d9c26f8956bfca876307c79c";
        final String PRIVATE_KEY4 = "a095ef77ee88da28f373246e9ae143f76e5839f680746c3f921e90bf76c81b08";
        final String PUBLIC_KEY4 = "35be6a2a37ff6bbb142e9f4d9b558308f4f75d7c51d5632c6a084257455e1461";

        Mnemonic mnemonic = Mnemonic.fromString(MNEMONIC_12_WORD_STRING);

        // Chain m/44'/3030'/0'/0'/0'
        PrivateKey key1 = mnemonic.toStandardEd25519PrivateKey("", 0);
        assertThat(Hex.toHexString(key1.getChainCode().getKey())).isEqualTo(CHAIN_CODE1);
        assertThat(key1.toStringRaw()).isEqualTo(PRIVATE_KEY1);
        assertThat(key1.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY1);

        // Chain m/44'/3030'/0'/0'/2147483647'
        PrivateKey key2 = mnemonic.toStandardEd25519PrivateKey("", 2147483647);
        assertThat(Hex.toHexString(key2.getChainCode().getKey())).isEqualTo(CHAIN_CODE2);
        assertThat(key2.toStringRaw()).isEqualTo(PRIVATE_KEY2);
        assertThat(key2.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY2);

        // Chain m/44'/3030'/0'/0'/0'; Passphrase: "some pass"
        PrivateKey key3 = mnemonic.toStandardEd25519PrivateKey("some pass", 0);
        assertThat(Hex.toHexString(key3.getChainCode().getKey())).isEqualTo(CHAIN_CODE3);
        assertThat(key3.toStringRaw()).isEqualTo(PRIVATE_KEY3);
        assertThat(key3.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY3);

        // Chain m/44'/3030'/0'/0'/2147483647'; Passphrase: "some pass"
        PrivateKey key4 = mnemonic.toStandardEd25519PrivateKey("some pass", 2147483647);
        assertThat(Hex.toHexString(key4.getChainCode().getKey())).isEqualTo(CHAIN_CODE4);
        assertThat(key4.toStringRaw()).isEqualTo(PRIVATE_KEY4);
        assertThat(key4.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY4);
    }

    @Test
    @DisplayName("Mnemonic.toStandardED25519PrivateKey() should fail when index is pre-hardened")
    void toStandardED25519PrivateKeyShouldFailWhenIndexIsPreHardened() throws BadMnemonicException {
        Mnemonic mnemonic = Mnemonic.fromString(MNEMONIC_24_WORD_STRING);
        int hardenedIndex = Bip32Utils.toHardenedIndex(10);

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
            () -> mnemonic.toStandardEd25519PrivateKey("", hardenedIndex)
        ).satisfies(error -> assertThat(error.getMessage()).isEqualTo("the index should not be pre-hardened"));

    }

    @Test
    @DisplayName("Mnemonic.toStandardECDSAsecp256k1PrivateKey() test vector")
    void toStandardECDSAsecp256k1PrivateKey() throws BadMnemonicException {
        // https://github.com/hashgraph/hedera-sdk-reference/issues/73#issuecomment-1422330626
        final String CHAIN_CODE1 = "7717bc71194c257d4b233e16cf48c24adef630052f874a262d19aeb2b527620d";
        final String PRIVATE_KEY1 = "0fde7bfd57ae6ec310bdd8b95967d98e8762a2c02da6f694b152cf9860860ab8";
        final String PUBLIC_KEY1 = "03b1c064b4d04d52e51f6c8e8bb1bff75d62fa7b1446412d5901d424f6aedd6fd4";

        final String CHAIN_CODE2 = "e333da4bd9e21b5dbd2b0f6d88bad02f0fa24cf4b70b2fb613368d0364cdf8af";
        final String PRIVATE_KEY2 = "aab7d720a32c2d1ea6123f58b074c865bb07f6c621f14cb012f66c08e64996bb";
        final String PUBLIC_KEY2 = "03a0ea31bb3562f8a309b1436bc4b2f537301778e8a5e12b68cec26052f567a235";

        final String CHAIN_CODE3 = "0ff552587f6baef1f0818136bacac0bb37236473f6ecb5a8c1cc68a716726ed1";
        final String PRIVATE_KEY3 = "6df5ed217cf6d5586fdf9c69d39c843eb9d152ca19d3e41f7bab483e62f6ac25";
        final String PUBLIC_KEY3 = "0357d69bb36fee569838fe7b325c07ca511e8c1b222873cde93fc6bb541eb7ecea";

        final String CHAIN_CODE4 = "3a5048e93aad88f1c42907163ba4dce914d3aaf2eea87b4dd247ca7da7530f0b";
        final String PRIVATE_KEY4 = "80df01f79ee1b1f4e9ab80491c592c0ef912194ccca1e58346c3d35cb5b7c098";
        final String PUBLIC_KEY4 = "039ebe79f85573baa065af5883d0509a5634245f7864ddead76a008c9e42aa758d";

        final String CHAIN_CODE5 = "e54254940db58ef4913a377062ac6e411daebf435ad592d262d5a66d808a8b94";
        final String PRIVATE_KEY5 = "60cb2496a623e1201d4e0e7ce5da3833cd4ec7d6c2c06bce2bcbcbc9dfef22d6";
        final String PUBLIC_KEY5 = "02b59f348a6b69bd97afa80115e2d5331749b3c89c61297255430c487d6677f404";

        final String CHAIN_CODE6 = "cb23165e9d2d798c85effddc901a248a1a273fab2a56fe7976df97b016e7bb77";
        final String PRIVATE_KEY6 = "100477c333028c8849250035be2a0a166a347a5074a8a727bce1db1c65181a50";
        final String PUBLIC_KEY6 = "03d10ebfa2d8ff2cd34aa96e5ef59ca2e69316b4c0996e6d5f54b6932fe51be560";

        Mnemonic mnemonic = Mnemonic.fromString(MNEMONIC_24_WORD_STRING);

        // Chain m/44'/3030'/0'/0/0
        PrivateKey key1 = mnemonic.toStandardECDSAsecp256k1PrivateKey("", 0);
        assertThat(Hex.toHexString(key1.getChainCode().getKey())).isEqualTo(CHAIN_CODE1);
        assertThat(key1.toStringRaw()).isEqualTo(PRIVATE_KEY1);
        assertThat(key1.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY1);

        // Chain m/44'/3030'/0'/0/0'
        PrivateKey key2 = mnemonic.toStandardECDSAsecp256k1PrivateKey("", Bip32Utils.toHardenedIndex(0));
        assertThat(Hex.toHexString(key2.getChainCode().getKey())).isEqualTo(CHAIN_CODE2);
        assertThat(key2.toStringRaw()).isEqualTo(PRIVATE_KEY2);
        assertThat(key2.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY2);

        // Chain m/44'/3030'/0'/0/0; Passphrase "some pass"
        PrivateKey key3 = mnemonic.toStandardECDSAsecp256k1PrivateKey("some pass", 0);
        assertThat(Hex.toHexString(key3.getChainCode().getKey())).isEqualTo(CHAIN_CODE3);
        assertThat(key3.toStringRaw()).isEqualTo(PRIVATE_KEY3);
        assertThat(key3.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY3);

        // Chain m/44'/3030'/0'/0/0'; Passphrase "some pass"
        PrivateKey key4 = mnemonic.toStandardECDSAsecp256k1PrivateKey("some pass", Bip32Utils.toHardenedIndex(0));
        assertThat(Hex.toHexString(key4.getChainCode().getKey())).isEqualTo(CHAIN_CODE4);
        assertThat(key4.toStringRaw()).isEqualTo(PRIVATE_KEY4);
        assertThat(key4.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY4);

        // Chain m/44'/3030'/0'/0/2147483647; Passphrase "some pass"
        PrivateKey key5 = mnemonic.toStandardECDSAsecp256k1PrivateKey("some pass", 2147483647);
        assertThat(Hex.toHexString(key5.getChainCode().getKey())).isEqualTo(CHAIN_CODE5);
        assertThat(key5.toStringRaw()).isEqualTo(PRIVATE_KEY5);
        assertThat(key5.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY5);

        // Chain m/44'/3030'/0'/0/2147483647'; Passphrase "some pass"
        PrivateKey key6 = mnemonic.toStandardECDSAsecp256k1PrivateKey("some pass", Bip32Utils.toHardenedIndex(2147483647));
        assertThat(Hex.toHexString(key6.getChainCode().getKey())).isEqualTo(CHAIN_CODE6);
        assertThat(key6.toStringRaw()).isEqualTo(PRIVATE_KEY6);
        assertThat(key6.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY6);
    }

    @Test
    @DisplayName("Mnemonic.toStandardECDSAsecp256k1PrivateKey() test vector 2")
    void toStandardECDSAsecp256k1PrivateKey2() throws BadMnemonicException {
        // https://github.com/hashgraph/hedera-sdk-reference/issues/73#issuecomment-1424761224
        final String CHAIN_CODE1 = "e76e0480faf2790e62dc1a7bac9dce51db1b3571fd74d8e264abc0d240a55d09";
        final String PRIVATE_KEY1 = "f033824c20dd9949ad7a4440f67120ee02a826559ed5884077361d69b2ad51dd";
        final String PUBLIC_KEY1 = "0294bf84a54806989a74ca4b76291d386914610b40b610d303162b9e495bc06416";

        final String CHAIN_CODE2 = "60c39c6a77bd68c0aaabfe2f4711dc9c2247214c4f4dae15ad4cb76905f5f544";
        final String PRIVATE_KEY2 = "962f549dafe2d9c8091ac918cb4fc348ab0767353f37501067897efbc84e7651";
        final String PUBLIC_KEY2 = "027123855357fd41d28130fbc59053192b771800d28ef47319ef277a1a032af78f";

        final String CHAIN_CODE3 = "911a1095b64b01f7f3a06198df3d618654e5ed65862b211997c67515e3167892";
        final String PRIVATE_KEY3 = "c139ebb363d7f441ccbdd7f58883809ec0cc3ee7a122ef67974eec8534de65e8";
        final String PUBLIC_KEY3 = "0293bdb1507a26542ed9c1ec42afe959cf8b34f39daab4bf842cdac5fa36d50ef7";

        final String CHAIN_CODE4 = "64173f2dcb1d65e15e787ef882fa15f54db00209e2dab16fa1661244cd98e95c";
        final String PRIVATE_KEY4 = "87c1d8d4bb0cebb4e230852f2a6d16f6847881294b14eb1d6058b729604afea0";
        final String PUBLIC_KEY4 = "03358e7761a422ca1c577f145fe845c77563f164b2c93b5b34516a8fa13c2c0888";

        final String CHAIN_CODE5 = "a7250c2b07b368a054f5c91e6a3dbe6ca3bbe01eb0489fe8778304bd0a19c711";
        final String PRIVATE_KEY5 = "2583170ee745191d2bb83474b1de41a1621c47f6e23db3f2bf413a1acb5709e4";
        final String PUBLIC_KEY5 = "03f9eb27cc73f751e8e476dd1db79037a7df2c749fa75b6cc6951031370d2f95a5";

        final String CHAIN_CODE6 = "66a1175e7690e3714d53ffce16ee6bb4eb02065516be2c2ad6bf6c9df81ec394";
        final String PRIVATE_KEY6 = "f2d008cd7349bdab19ed85b523ba218048f35ca141a3ecbc66377ad50819e961";
        final String PUBLIC_KEY6 = "027b653d04958d4bf83dd913a9379b4f9a1a1e64025a691830a67383bc3157c044";

        Mnemonic mnemonic = Mnemonic.fromString(MNEMONIC_12_WORD_STRING);

        // Chain m/44'/3030'/0'/0/0
        PrivateKey key1 = mnemonic.toStandardECDSAsecp256k1PrivateKey("", 0);
        assertThat(Hex.toHexString(key1.getChainCode().getKey())).isEqualTo(CHAIN_CODE1);
        assertThat(key1.toStringRaw()).isEqualTo(PRIVATE_KEY1);
        assertThat(key1.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY1);

        // Chain m/44'/3030'/0'/0/0'
        PrivateKey key2 = mnemonic.toStandardECDSAsecp256k1PrivateKey("", Bip32Utils.toHardenedIndex(0));
        assertThat(Hex.toHexString(key2.getChainCode().getKey())).isEqualTo(CHAIN_CODE2);
        assertThat(key2.toStringRaw()).isEqualTo(PRIVATE_KEY2);
        assertThat(key2.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY2);

        // Chain m/44'/3030'/0'/0/0; Passphrase "some pass"
        PrivateKey key3 = mnemonic.toStandardECDSAsecp256k1PrivateKey("some pass", 0);
        assertThat(Hex.toHexString(key3.getChainCode().getKey())).isEqualTo(CHAIN_CODE3);
        assertThat(key3.toStringRaw()).isEqualTo(PRIVATE_KEY3);
        assertThat(key3.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY3);

        // Chain m/44'/3030'/0'/0/0'; Passphrase "some pass"
        PrivateKey key4 = mnemonic.toStandardECDSAsecp256k1PrivateKey("some pass", Bip32Utils.toHardenedIndex(0));
        assertThat(Hex.toHexString(key4.getChainCode().getKey())).isEqualTo(CHAIN_CODE4);
        assertThat(key4.toStringRaw()).isEqualTo(PRIVATE_KEY4);
        assertThat(key4.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY4);

        // Chain m/44'/3030'/0'/0/2147483647; Passphrase "some pass"
        PrivateKey key5 = mnemonic.toStandardECDSAsecp256k1PrivateKey("some pass", 2147483647);
        assertThat(Hex.toHexString(key5.getChainCode().getKey())).isEqualTo(CHAIN_CODE5);
        assertThat(key5.toStringRaw()).isEqualTo(PRIVATE_KEY5);
        assertThat(key5.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY5);

        // Chain m/44'/3030'/0'/0/2147483647'; Passphrase "some pass"
        PrivateKey key6 = mnemonic.toStandardECDSAsecp256k1PrivateKey("some pass", Bip32Utils.toHardenedIndex(2147483647));
        assertThat(Hex.toHexString(key6.getChainCode().getKey())).isEqualTo(CHAIN_CODE6);
        assertThat(key6.toStringRaw()).isEqualTo(PRIVATE_KEY6);
        assertThat(key6.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY6);
    }
}
