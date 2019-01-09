package com.hedera.sdk.cryptography;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface KeyPair {

	PrivateKey getPrivateKey();

	PublicKey getPublicKey();

	byte[] getPublicKeyEncoded();

	byte[] signMessage(byte[] message);

	boolean verifySignature(byte[] message, byte[] signature);
}
