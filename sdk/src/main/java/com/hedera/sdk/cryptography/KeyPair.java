package com.hedera.sdk.cryptography;

public interface KeyPair {
	byte[] getPrivateKey();
	byte[] getPublicKey();
	byte[] signMessage(byte[] message);
	boolean verifySignature(byte[] message, byte[] signature);
	byte[] getSeedAndPublicKey();
	byte[] getPrivateKeyEncoded();
	byte[] getPublicKeyEncoded();
	String getPrivateKeyEncodedHex();
	String getPublicKeyEncodedHex();
	String getPrivateKeyHex();
	String getPublicKeyHex();
	byte[] getPrivateKeySeed();
	String getPrivateKeySeedHex();
}
