package test.hedera.sdk.cryptography;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.Arrays;

import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.cryptography.CryptoUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HederaCryptoKeyPairTest2 {

	HederaKeyPair firstPair;
	HederaKeyPair secondPair;
	KeyPair thirdPair;
	byte[] seed01;
	SecureRandom secure01;

	@BeforeEach
	void setUp() throws Exception {
		seed01 = new String("firstStringfirstStringfirstStrin").getBytes();
		secure01 = new SecureRandom(seed01);

		firstPair = new HederaKeyPair(KeyType.ED25519, seed01);
	}

	@Test
	final void testKeyGenBasedOnSeed() {
		secondPair = new HederaKeyPair(KeyType.ED25519, seed01);
		assertEquals(Arrays.toString(firstPair.getPublicKey()), Arrays.toString(secondPair.getPublicKey()));
	}

	@Test
    final void testDeriveKey() {
        byte[] seed = seed01;
        long index = 0;
        int length = seed.length;
		byte[] deriveKey = CryptoUtils.deriveKey(seed, index, length);
	}

}
