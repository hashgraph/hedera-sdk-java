package test.hedera.sdk.cryptography;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.security.Signature;

import org.junit.jupiter.api.Test;

import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyPair.KeyType;

import net.i2p.crypto.eddsa.EdDSAEngine;

class HederaCryptoKeyPairTest3 {
	
	HederaKeyPair firstPair;
	HederaKeyPair secondPair;

	@Test
	final void testPublicPrivateEncodedRecover() {
		// generate a new key
		firstPair = new HederaKeyPair(KeyType.ED25519);
		byte[] privateKey = firstPair.getSecretKey();
		byte[] publicKey = firstPair.getPublicKey();
		
		System.out.println(firstPair.getSecretKeyHex());
		System.out.println(firstPair.getSecretKeyEncodedHex());
		
		System.out.println(firstPair.getPublicKeyHex());
		System.out.println(firstPair.getPublicKeyEncodedHex());
		
		// regenerate using public/private - NON ENCODED
		secondPair = new HederaKeyPair(KeyType.ED25519, publicKey, privateKey);

		System.out.println("");
		System.out.println(secondPair.getSecretKeyHex());
		System.out.println(secondPair.getSecretKeyEncodedHex());
		
		System.out.println(secondPair.getPublicKeyHex());
		System.out.println(secondPair.getPublicKeyEncodedHex());
		
		assertArrayEquals(firstPair.getSecretKey(), secondPair.getSecretKey());
		assertArrayEquals(firstPair.getPublicKey(), secondPair.getPublicKey());
		
		// sign and validate
		byte[] message = "testMessage".getBytes();

		byte[] signedMessage1 = firstPair.signMessage(message);
		byte[] signedMessage2 = secondPair.signMessage(message);
		assertArrayEquals(signedMessage1, signedMessage2);
		
		try {
			assertTrue(firstPair.verifySignature(message, signedMessage1));
			assertTrue(secondPair.verifySignature(message, signedMessage2));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// try encoded
		privateKey = firstPair.getSecretKeyEncoded();
		publicKey = firstPair.getPublicKeyEncoded();
		secondPair = new HederaKeyPair(KeyType.ED25519, publicKey, privateKey);

		assertArrayEquals(firstPair.getSecretKey(), secondPair.getSecretKey());
		assertArrayEquals(firstPair.getPublicKey(), secondPair.getPublicKey());
		
		signedMessage1 = firstPair.signMessage(message);
		signedMessage2 = secondPair.signMessage(message);
		assertArrayEquals(signedMessage1, signedMessage2);
		
		try {
			assertTrue(firstPair.verifySignature(message, signedMessage1));
			assertTrue(secondPair.verifySignature(message, signedMessage2));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
