package test.hedera.sdk.cryptography;

import java.security.KeyPair;

import net.i2p.crypto.eddsa.KeyPairGenerator;
import java.security.SecureRandom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HederaCryptoKeyPairTest {
	
	KeyPair firstPair;
	KeyPair secondPair;
	KeyPair thirdPair;
	byte[] seed01;
	SecureRandom secure01;

	@BeforeEach
	void setUp() throws Exception {
		seed01 = new String("firstString").getBytes();
		secure01 = new SecureRandom(seed01);
		KeyPairGenerator KP = new KeyPairGenerator();
		KP.initialize(256, secure01);
		firstPair = KP.generateKeyPair();
        // secondPair = new KeyPairGenerator().generateKeyPair();
        // thirdPair = new KeyPairGenerator().generateKeyPair();
	}
	
	@Test
	final void testKeyGenBasedOnSeed() {
		KeyPairGenerator KP = new KeyPairGenerator();
		KP.initialize(256, secure01);
		KeyPair testFirstPair = KP.generateKeyPair();
	}
}
