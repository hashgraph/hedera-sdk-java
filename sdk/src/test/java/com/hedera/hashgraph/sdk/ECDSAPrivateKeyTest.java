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

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ECDSAPrivateKeyTest {
    @Test
    @DisplayName("private key generates successfully")
    void keyGenerates() {
        PrivateKey key = PrivateKey.generateECDSA();

        assertThat(key).isNotNull();
        assertThat(key.toBytes()).isNotNull();
    }

    @Test
    @DisplayName("private key can be recovered from bytes")
    void keySerialization() {
        PrivateKey key1 = PrivateKey.generateECDSA();
        byte[] key1Bytes = key1.toBytes();
        PrivateKey key2 = PrivateKey.fromBytes(key1Bytes);
        byte[] key2Bytes = key2.toBytes();

        assertThat(key2Bytes).containsExactly(key1Bytes);
    }

    @Test
    @DisplayName("private key can be recovered from raw bytes")
    void keySerialization2() {
        PrivateKey key1 = PrivateKey.generateECDSA();
        byte[] key1Bytes = key1.toBytesRaw();
        PrivateKey key2 = PrivateKey.fromBytesECDSA(key1Bytes);
        byte[] key2Bytes = key2.toBytesRaw();
        // cannot use PrivateKey.fromBytes() to parse raw ECDSA bytes
        // because they're indistinguishable from ED25519 raw bytes

        assertThat(key2Bytes).containsExactly(key1Bytes);
    }

    @Test
    @DisplayName("private key can be recovered from DER bytes")
    void keySerialization3() {
        PrivateKey key1 = PrivateKey.generateECDSA();
        byte[] key1Bytes = key1.toBytesDER();
        PrivateKey key2 = PrivateKey.fromBytesDER(key1Bytes);
        byte[] key2Bytes = key2.toBytesDER();
        PrivateKey key3 = PrivateKey.fromBytes(key1Bytes);
        byte[] key3Bytes = key3.toBytesDER();

        assertThat(key2Bytes).containsExactly(key1Bytes);
        assertThat(key3Bytes).containsExactly(key1Bytes);
    }

    @Test
    @DisplayName("private key can be recovered from string")
    void keyStringification() {
        PrivateKey key1 = PrivateKey.generateECDSA();
        String key1String = key1.toString();
        PrivateKey key2 = PrivateKey.fromString(key1String);
        String key2String = key2.toString();

        assertThat(key2String).isEqualTo(key1String);
    }

    @Test
    @DisplayName("private key can be recovered from raw string")
    void keyStringification2() {
        PrivateKey key1 = PrivateKey.generateECDSA();
        String key1String = key1.toStringRaw();
        PrivateKey key2 = PrivateKey.fromStringECDSA(key1String);
        String key2String = key2.toStringRaw();
        // cannot use PrivateKey.fromString() to parse raw ECDSA string
        // because it's indistinguishable from ED25519 raw string

        assertThat(key2String).isEqualTo(key1String);
    }

    @Test
    @DisplayName("private key can be recovered from DER string")
    void keyStringification3() {
        PrivateKey key1 = PrivateKey.generateECDSA();
        String key1String = key1.toStringDER();
        PrivateKey key2 = PrivateKey.fromStringDER(key1String);
        String key2String = key2.toStringDER();
        PrivateKey key3 = PrivateKey.fromString(key1String);
        String key3String = key3.toStringDER();

        assertThat(key2String).isEqualTo(key1String);
        assertThat(key3String).isEqualTo(key1String);
    }

    @Test
    @DisplayName("private key is is ECDSA")
    void keyIsECDSA() {
        PrivateKey key = PrivateKey.generateECDSA();

        assertThat(key.isECDSA()).isTrue();
    }

    @Test
    @DisplayName("private key is is not Ed25519")
    void keyIsNotEd25519() {
        PrivateKey key = PrivateKey.generateECDSA();

        assertThat(key.isED25519()).isFalse();
    }

    // TODO: replace with HexFormat.of().parseHex when the required Java version is 17
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private static int toHardened(int index) {
        return index | 0x80000000;
    }

    @Test
    @DisplayName("SLIP10 test vector 1")
    void slip10TestVector1() {
        // https://github.com/satoshilabs/slips/blob/master/slip-0010.md#test-vector-1-for-secp256k1
        final String CHAIN_CODE1 = "873dff81c02f525623fd1fe5167eac3a55a049de3d314bb42ee227ffed37d508";
        final String PRIVATE_KEY1 = "e8f32e723decf4051aefac8e2c93c9c5b214313817cdb01a1494b917c8436b35";
        final String PUBLIC_KEY1 = "0339a36013301597daef41fbe593a02cc513d0b55527ec2df1050e2e8ff49c85c2";

        final String CHAIN_CODE2 = "47fdacbd0f1097043b78c63c20c34ef4ed9a111d980047ad16282c7ae6236141";
        final String PRIVATE_KEY2 = "edb2e14f9ee77d26dd93b4ecede8d16ed408ce149b6cd80b0715a2d911a0afea";
        final String PUBLIC_KEY2 = "035a784662a4a20a65bf6aab9ae98a6c068a81c52e4b032c0fb5400c706cfccc56";

        final String CHAIN_CODE3 = "2a7857631386ba23dacac34180dd1983734e444fdbf774041578e9b6adb37c19";
        final String PRIVATE_KEY3 = "3c6cb8d0f6a264c91ea8b5030fadaa8e538b020f0a387421a12de9319dc93368";
        final String PUBLIC_KEY3 = "03501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711c";

        final String CHAIN_CODE4 = "04466b9cc8e161e966409ca52986c584f07e9dc81f735db683c3ff6ec7b1503f";
        final String PRIVATE_KEY4 = "cbce0d719ecf7431d88e6a89fa1483e02e35092af60c042b1df2ff59fa424dca";
        final String PUBLIC_KEY4 = "0357bfe1e341d01c69fe5654309956cbea516822fba8a601743a012a7896ee8dc2";

        final String CHAIN_CODE5 = "cfb71883f01676f587d023cc53a35bc7f88f724b1f8c2892ac1275ac822a3edd";
        final String PRIVATE_KEY5 = "0f479245fb19a38a1954c5c7c0ebab2f9bdfd96a17563ef28a6a4b1a2a764ef4";
        final String PUBLIC_KEY5 = "02e8445082a72f29b75ca48748a914df60622a609cacfce8ed0e35804560741d29";

        final String CHAIN_CODE6 = "c783e67b921d2beb8f6b389cc646d7263b4145701dadd2161548a8b078e65e9e";
        final String PRIVATE_KEY6 = "471b76e389e528d6de6d816857e012c5455051cad6660850e58372a6c3e6e7c8";
        final String PUBLIC_KEY6 = "022a471424da5e657499d1ff51cb43c47481a03b1e77f951fe64cec9f5a48f7011";

        var seed = hexStringToByteArray("000102030405060708090a0b0c0d0e0f");

        // Chain m
        PrivateKey key1 = PrivateKey.fromSeedECDSA(seed);
        assertThat(Hex.toHexString(key1.getChainCode().getKey())).isEqualTo(CHAIN_CODE1);
        assertThat(key1.toStringRaw()).isEqualTo(PRIVATE_KEY1);
        assertThat(key1.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY1);

        // Chain m/0'
        PrivateKey key2 = key1.derive(toHardened(0));
        assertThat(Hex.toHexString(key2.getChainCode().getKey())).isEqualTo(CHAIN_CODE2);
        assertThat(key2.toStringRaw()).isEqualTo(PRIVATE_KEY2);
        assertThat(key2.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY2);

        // Chain m/0'/1
        PrivateKey key3 = key2.derive(1);
        assertThat(Hex.toHexString(key3.getChainCode().getKey())).isEqualTo(CHAIN_CODE3);
        assertThat(key3.toStringRaw()).isEqualTo(PRIVATE_KEY3);
        assertThat(key3.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY3);

        // Chain m/0'/1/2'
        PrivateKey key4 = key3.derive(toHardened(2));
        assertThat(Hex.toHexString(key4.getChainCode().getKey())).isEqualTo(CHAIN_CODE4);
        assertThat(key4.toStringRaw()).isEqualTo(PRIVATE_KEY4);
        assertThat(key4.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY4);

        // Chain m/0'/1/2'/2
        PrivateKey key5 = key4.derive(2);
        assertThat(Hex.toHexString(key5.getChainCode().getKey())).isEqualTo(CHAIN_CODE5);
        assertThat(key5.toStringRaw()).isEqualTo(PRIVATE_KEY5);
        assertThat(key5.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY5);

        // Chain m/0'/1/2'/2/1000000000
        PrivateKey key6 = key5.derive(1000000000);
        assertThat(Hex.toHexString(key6.getChainCode().getKey())).isEqualTo(CHAIN_CODE6);
        assertThat(key6.toStringRaw()).isEqualTo(PRIVATE_KEY6);
        assertThat(key6.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY6);
    }

    @Test
    @DisplayName("SLIP10 test vector 2")
    void slip10TestVector2() {
        // https://github.com/satoshilabs/slips/blob/master/slip-0010.md#test-vector-2-for-secp256k1
        final String CHAIN_CODE1 = "60499f801b896d83179a4374aeb7822aaeaceaa0db1f85ee3e904c4defbd9689";
        final String PRIVATE_KEY1 = "4b03d6fc340455b363f51020ad3ecca4f0850280cf436c70c727923f6db46c3e";
        final String PUBLIC_KEY1 = "03cbcaa9c98c877a26977d00825c956a238e8dddfbd322cce4f74b0b5bd6ace4a7";

        final String CHAIN_CODE2 = "f0909affaa7ee7abe5dd4e100598d4dc53cd709d5a5c2cac40e7412f232f7c9c";
        final String PRIVATE_KEY2 = "abe74a98f6c7eabee0428f53798f0ab8aa1bd37873999041703c742f15ac7e1e";
        final String PUBLIC_KEY2 = "02fc9e5af0ac8d9b3cecfe2a888e2117ba3d089d8585886c9c826b6b22a98d12ea";

        final String CHAIN_CODE3 = "be17a268474a6bb9c61e1d720cf6215e2a88c5406c4aee7b38547f585c9a37d9";
        final String PRIVATE_KEY3 = "877c779ad9687164e9c2f4f0f4ff0340814392330693ce95a58fe18fd52e6e93";
        final String PUBLIC_KEY3 = "03c01e7425647bdefa82b12d9bad5e3e6865bee0502694b94ca58b666abc0a5c3b";

        final String CHAIN_CODE4 = "f366f48f1ea9f2d1d3fe958c95ca84ea18e4c4ddb9366c336c927eb246fb38cb";
        final String PRIVATE_KEY4 = "704addf544a06e5ee4bea37098463c23613da32020d604506da8c0518e1da4b7";
        final String PUBLIC_KEY4 = "03a7d1d856deb74c508e05031f9895dab54626251b3806e16b4bd12e781a7df5b9";

        final String CHAIN_CODE5 = "637807030d55d01f9a0cb3a7839515d796bd07706386a6eddf06cc29a65a0e29";
        final String PRIVATE_KEY5 = "f1c7c871a54a804afe328b4c83a1c33b8e5ff48f5087273f04efa83b247d6a2d";
        final String PUBLIC_KEY5 = "02d2b36900396c9282fa14628566582f206a5dd0bcc8d5e892611806cafb0301f0";

        final String CHAIN_CODE6 = "9452b549be8cea3ecb7a84bec10dcfd94afe4d129ebfd3b3cb58eedf394ed271";
        final String PRIVATE_KEY6 = "bb7d39bdb83ecf58f2fd82b6d918341cbef428661ef01ab97c28a4842125ac23";
        final String PUBLIC_KEY6 = "024d902e1a2fc7a8755ab5b694c575fce742c48d9ff192e63df5193e4c7afe1f9c";

        var seed = hexStringToByteArray("fffcf9f6f3f0edeae7e4e1dedbd8d5d2cfccc9c6c3c0bdbab7b4b1aeaba8a5a29f9c999693908d8a8784817e7b7875726f6c696663605d5a5754514e4b484542");

        // Chain m
        PrivateKey key1 = PrivateKey.fromSeedECDSA(seed);;
        assertThat(Hex.toHexString(key1.getChainCode().getKey())).isEqualTo(CHAIN_CODE1);
        assertThat(key1.toStringRaw()).isEqualTo(PRIVATE_KEY1);
        assertThat(key1.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY1);

        // Chain m/0
        PrivateKey key2 = key1.derive(0);
        assertThat(Hex.toHexString(key2.getChainCode().getKey())).isEqualTo(CHAIN_CODE2);
        assertThat(key2.toStringRaw()).isEqualTo(PRIVATE_KEY2);
        assertThat(key2.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY2);

        // Chain m/0/2147483647'
        PrivateKey key3 = key2.derive(toHardened(2147483647));
        assertThat(Hex.toHexString(key3.getChainCode().getKey())).isEqualTo(CHAIN_CODE3);
        assertThat(key3.toStringRaw()).isEqualTo(PRIVATE_KEY3);
        assertThat(key3.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY3);

        // Chain m/0/2147483647'/1
        PrivateKey key4 = key3.derive(1);
        assertThat(Hex.toHexString(key4.getChainCode().getKey())).isEqualTo(CHAIN_CODE4);
        assertThat(key4.toStringRaw()).isEqualTo(PRIVATE_KEY4);
        assertThat(key4.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY4);

        // Chain m/0/2147483647'/1/2147483646'
        PrivateKey key5 = key4.derive(toHardened(2147483646));
        assertThat(Hex.toHexString(key5.getChainCode().getKey())).isEqualTo(CHAIN_CODE5);
        assertThat(key5.toStringRaw()).isEqualTo(PRIVATE_KEY5);
        assertThat(key5.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY5);

        // Chain m/0/2147483647'/1/2147483646'/2
        PrivateKey key6 = key5.derive(2);
        assertThat(Hex.toHexString(key6.getChainCode().getKey())).isEqualTo(CHAIN_CODE6);
        assertThat(key6.toStringRaw()).isEqualTo(PRIVATE_KEY6);
        assertThat(key6.getPublicKey().toStringRaw()).isSubstringOf(PUBLIC_KEY6);
    }

    // TODO: get fromPem working, and test it
}
