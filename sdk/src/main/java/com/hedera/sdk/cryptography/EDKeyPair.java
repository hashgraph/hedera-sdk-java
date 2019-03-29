package com.hedera.sdk.cryptography;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;

import static org.junit.Assert.assertNotNull;

import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.util.encoders.Hex;

import com.hedera.sdk.cryptography.KeyPair;

public class EDKeyPair implements KeyPair {

	private EdDSAPrivateKey edPrivateKey;
	private EdDSAPublicKey edPublicKey;

	private EDKeyPair() {
	}

	public EDKeyPair(final byte[] seed) {
        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
        EdDSAPrivateKeySpec privateKeySpec = new EdDSAPrivateKeySpec(seed, spec);
        this.edPrivateKey = new EdDSAPrivateKey(privateKeySpec);
        EdDSAPublicKeySpec pubKeySpec = new EdDSAPublicKeySpec(privateKeySpec.getA(), spec);
        this.edPublicKey = new EdDSAPublicKey(pubKeySpec);		
	}

	public EDKeyPair(final byte[] publicKey, final byte[] privateKey) {
		try {
			// try encoded key first
			X509EncodedKeySpec encodedPubKey = new X509EncodedKeySpec(publicKey);
			this.edPublicKey = new EdDSAPublicKey(encodedPubKey);
		} catch (InvalidKeySpecException e) {
			// key is invalid (likely not encoded)
			// try non encoded
			final EdDSAPublicKeySpec pubKeySpec = new EdDSAPublicKeySpec(publicKey, EdDSANamedCurveTable.ED_25519_CURVE_SPEC);
			this.edPublicKey = new EdDSAPublicKey(pubKeySpec);
		}

		if (privateKey != null) {
			if (privateKey.length != 0) {
				// try encoded first
				try {
					final PKCS8EncodedKeySpec encodedPrivKey = new PKCS8EncodedKeySpec(privateKey);
					this.edPrivateKey = new EdDSAPrivateKey(encodedPrivKey);
				} catch (InvalidKeySpecException e) {
					// key is invalid (likely not encoded)
					final EdDSAPrivateKeySpec  privKeySpec = new EdDSAPrivateKeySpec(privateKey,EdDSANamedCurveTable.ED_25519_CURVE_SPEC);
					this.edPrivateKey = new EdDSAPrivateKey(privKeySpec);
				}
			}
		}
	}

	public static EDKeyPair buildFromPrivateKey(final byte[] privateKey) {

		final EDKeyPair edKeyPair = new EDKeyPair();
		try {
			// try encoded first
	 		final PKCS8EncodedKeySpec encodedPrivKey = new PKCS8EncodedKeySpec(privateKey);
			edKeyPair.edPrivateKey = new EdDSAPrivateKey(encodedPrivKey);
		} catch (InvalidKeySpecException e) {
			// key is invalid (likely not encoded)
			// try non encoded
			final EdDSAPrivateKeySpec  privKey = new EdDSAPrivateKeySpec(privateKey,EdDSANamedCurveTable.ED_25519_CURVE_SPEC);
			edKeyPair.edPrivateKey = new EdDSAPrivateKey(privKey);
		}

		EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
			
		edKeyPair.edPublicKey = new EdDSAPublicKey(
				new EdDSAPublicKeySpec(edKeyPair.edPrivateKey.getAbyte(), spec));
		return edKeyPair;
	}

	@Override
	public byte[] getPrivateKey() {
		if (this.edPrivateKey != null) {
			return this.edPrivateKey.getSeed();
		} else {
			return null;
		}
	}
	
	@Override
	public byte[] getPrivateKeyEncoded() {
		if (this.edPrivateKey != null) {
			return edPrivateKey.getEncoded();
		} else {
			return null;
		}
	}

	@Override
	public String getPrivateKeyEncodedHex() {
		if (this.edPrivateKey != null) {
			return Hex.toHexString(this.getPrivateKeyEncoded());
		} else {
			return "";
		}
	}
	
	@Override
	public String getPrivateKeyHex() {
		if (this.edPrivateKey != null) {
			return Hex.toHexString(this.getPrivateKey());
		} else {
			return "";
		}
	}
	@Override
	public byte[] getPublicKey() {
		if (this.edPublicKey != null) {
			return this.edPublicKey.getAbyte();
		} else {
			return null;
		}
	}
	public byte[] getPublicKeyEncoded() {
		if (this.edPublicKey != null) {
			return this.edPublicKey.getEncoded();
		} else {
			return null;
		}
	}
	@Override
	public String getPublicKeyEncodedHex() {
		if (this.edPublicKey != null) {
			return Hex.toHexString(this.getPublicKeyEncoded());
		} else {
			return "";
		}
	}
	
	@Override
	public String getPublicKeyHex() {
		if (this.edPublicKey != null) {
			return Hex.toHexString(this.getPublicKey());
		} else {
			return "";
		}
	}
	@Override
	public byte[] getSeedAndPublicKey() {
		
		if ((this.edPrivateKey != null)&& (this.edPublicKey != null)) {
			byte[] seed = this.edPrivateKey.getSeed();
	        byte[] publicKey = getPublicKey();
	
	        byte[] key = new byte[seed.length + publicKey.length];
	        System.arraycopy(seed, 0, key, 0, seed.length);
	        System.arraycopy(publicKey, 0, key, seed.length, publicKey.length);
	        return key;
		} else {
			return null;
		}
	}
	
	@Override
	public byte[] signMessage(final byte[] message) {
		try {
			final Signature sgr = new EdDSAEngine();
			sgr.initSign(edPrivateKey);
			sgr.update(message);
			return sgr.sign();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean verifySignature(byte[] message, byte[] signature) {
		try {
			final Signature sgr = new EdDSAEngine();
			sgr.initVerify(edPublicKey);
			sgr.update(message);
			return sgr.verify(signature);

		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

}
