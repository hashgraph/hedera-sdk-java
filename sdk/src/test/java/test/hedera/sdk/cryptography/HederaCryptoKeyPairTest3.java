package test.hedera.sdk.cryptography;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;

import org.apache.commons.codec.DecoderException;
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
		byte[] privateSeed = firstPair.getSecretKey();
		byte[] publicKey = firstPair.getPublicKey();
		
		// regenerate using public/private - NON ENCODED
		secondPair = new HederaKeyPair(KeyType.ED25519, publicKey, privateSeed);

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
		byte[] privateKey = firstPair.getSecretKeyEncoded();
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
		
		// generate a key from a known encoded pair
		try {
			HederaKeyPair encoded = new HederaKeyPair(KeyType.ED25519,"302a300506032b65700321006fbef2c87c5f8f6af5f93d028cbcbc78fb7206ef9ac3615e6236462e3e279cf8","302e020100300506032b657004220420358aa8e5ae5d8a87fc7d2eb346c75028ef759ba043194c51d5b14ef38a249e41");
			signedMessage1 = encoded.signMessage(message);
			assertTrue(encoded.verifySignature(message, signedMessage1));
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DecoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// generate a key from a known non encoded pair
		try {
			HederaKeyPair notEncoded = new HederaKeyPair(KeyType.ED25519,"48e161d91dcc9a14b9c63bcb4eebdb0055da267e0ab37b3396ca724894e05add","2c4b4dfdd0846ee80187c1c2227edca9307291f3ac94482ced125b51e65c1811");
			signedMessage1 = notEncoded.signMessage(message);
			assertTrue(notEncoded.verifySignature(message, signedMessage1));
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DecoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// recover from words at index -1
		String[] recoveryWords = "insist,cement,tenant,proper,lurid,seem,soften,clause,pork,want,order,tribal,picnic,remote,bow,Lewis,bulky,land,Cajun,flurry,sodium,aunt".split(",");
		try {
			HederaKeyPair recoverWords = new HederaKeyPair(KeyType.ED25519, recoveryWords);
			
			assertEquals("41e6416b8d92bce7818efdc0534e7a5ffd653b5a71ebfa3b4cdb482a2b34108a",recoverWords.getPublicKeyHex());
			assertEquals("302a300506032b657003210041e6416b8d92bce7818efdc0534e7a5ffd653b5a71ebfa3b4cdb482a2b34108a",recoverWords.getPublicKeyEncodedHex());
			assertEquals("58e14ddef4af53809022c4cc26dac3382e3256a76768a44e643b27427fe95c79",recoverWords.getSecretKeyHex());
			assertEquals("302e020100300506032b657004220420e0bf24678f5fe9fc6168d2519c73f3fb11722ef61382554b355cf63b4c8db988",recoverWords.getSecretKeyEncodedHex());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		Your generated key pair for index -1 is:
//			************************************************************************
//			Public key (hex)    : 41e6416b8d92bce7818efdc0534e7a5ffd653b5a71ebfa3b4cdb482a2b34108a
//			Public key(enc hex) : 302a300506032b657003210041e6416b8d92bce7818efdc0534e7a5ffd653b5a71ebfa3b4cdb482a2b34108a
//
//			Secret key (hex)    : 58e14ddef4af53809022c4cc26dac3382e3256a76768a44e643b27427fe95c79
//			Secret key(enc hex) : 302e020100300506032b657004220420e0bf24678f5fe9fc6168d2519c73f3fb11722ef61382554b355cf63b4c8db988
//
//			Secret Seed (hex)   : e0bf24678f5fe9fc6168d2519c73f3fb11722ef61382554b355cf63b4c8db988
//			Seed+PubKey         : e0bf24678f5fe9fc6168d2519c73f3fb11722ef61382554b355cf63b4c8db98841e6416b8d92bce7818efdc0534e7a5ffd653b5a71ebfa3b4cdb482a2b34108a
//
//			Recovery words  : insist,cement,tenant,proper,lurid,seem,soften,clause,pork,want,order,tribal,picnic,remote,bow,Lewis,bulky,land,Cajun,flurry,sodium,aunt
//			************************************************************************

		
		
		// recover from words at index 0
		recoveryWords = "merry,remark,goat,dare,write,locate,tense,cable,erect,object,Latin,hick,barren,only,omen,clear,stuff,sect,slug,detail,affect,behalf".split(",");
		try {
			HederaKeyPair recoverWords = new HederaKeyPair(KeyType.ED25519, recoveryWords, 0);
			assertEquals("9d69c4dc81c64bf92554b61ae902cd726b9b90bd46ae506c4696c895b50515cf",recoverWords.getPublicKeyHex());
			assertEquals("302a300506032b65700321009d69c4dc81c64bf92554b61ae902cd726b9b90bd46ae506c4696c895b50515cf",recoverWords.getPublicKeyEncodedHex());
			assertEquals("20c4e87cab52b8a8ca7f20fe1e2c2fabb8f64093d6766f6892fc18dc5a15ac74",recoverWords.getSecretKeyHex());
			assertEquals("302e020100300506032b6570042204203644412da259d1ee964f78a07928b59a3d9602819d120fb4bfecc64e63935bdf",recoverWords.getSecretKeyEncodedHex());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		Your generated key pair for index 0 is:
//			************************************************************************
//			Public key (hex)    : 9d69c4dc81c64bf92554b61ae902cd726b9b90bd46ae506c4696c895b50515cf
//			Public key(enc hex) : 302a300506032b65700321009d69c4dc81c64bf92554b61ae902cd726b9b90bd46ae506c4696c895b50515cf
//
//			Secret key (hex)    : 20c4e87cab52b8a8ca7f20fe1e2c2fabb8f64093d6766f6892fc18dc5a15ac74
//			Secret key(enc hex) : 302e020100300506032b6570042204203644412da259d1ee964f78a07928b59a3d9602819d120fb4bfecc64e63935bdf
//
//			Secret Seed (hex)   : 3644412da259d1ee964f78a07928b59a3d9602819d120fb4bfecc64e63935bdf
//			Seed+PubKey         : 3644412da259d1ee964f78a07928b59a3d9602819d120fb4bfecc64e63935bdf9d69c4dc81c64bf92554b61ae902cd726b9b90bd46ae506c4696c895b50515cf
//
//			Recovery words  : merry,remark,goat,dare,write,locate,tense,cable,erect,object,Latin,hick,barren,only,omen,clear,stuff,sect,slug,detail,affect,behalf
//			************************************************************************
	}
}
