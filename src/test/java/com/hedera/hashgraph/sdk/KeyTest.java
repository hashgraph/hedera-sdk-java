package com.hedera.hashgraph.sdk;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeyTest {
    @Test
    void signatureVerified() {
        byte[] message = "Hello, World".getBytes();
        PrivateKey privateKey = PrivateKey.generateEd25519();
        PublicKey publicKey = privateKey.getPublicKey();
        byte[] signature = privateKey.sign(message);

        assertThat(publicKey.verify(message, signature)).isTrue();
    }
}
