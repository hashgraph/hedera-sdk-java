package com.hedera.hashgraph.sdk.crypto.ed25519;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

class Ed25519SignatureTest {
    private static final String messageStr = "This is a message about the world.";
    private static final byte[] messageBytes = messageStr.getBytes(StandardCharsets.UTF_8);
    private static final String sigStr = "73bea53f31ca9c42a422ecb7516ec08d0bbd1a6bfd630ccf10ec1872454814d29f4a8011129cd007eab544af01a75f508285b591e5bed24b68f927751e49e30e";

    private static Stream<String> privKeyStrings() {
        return Stream.of(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10",
            // raw hex (concatenated private + public key)
            "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10" +
                "e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7",
            // raw hex (just private key)
            "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10"
        );
    }

    @ParameterizedTest
    @DisplayName("reproducible signature can be computed")
    @MethodSource("privKeyStrings")
    void computeSignature(String keyStr) {
        final var key = Ed25519PrivateKey.fromString(keyStr);
        final var signature = Ed25519Signature.forMessage(key, messageBytes);

        Assertions.assertEquals(
            sigStr,
            signature.toString()
        );
    }

    @ParameterizedTest
    @DisplayName("signature can be verified")
    @MethodSource("privKeyStrings")
    void verifySignature(String keyStr) {
        final var privKey = Ed25519PrivateKey.fromString(keyStr);
        final var signature = Ed25519Signature.fromString(sigStr);

        Assertions.assertTrue(
            signature.verify(privKey.getPublicKey(), messageBytes)
        );
    }
}
