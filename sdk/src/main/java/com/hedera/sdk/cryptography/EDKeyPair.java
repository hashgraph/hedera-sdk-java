package com.hedera.sdk.cryptography;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import com.hedera.sdk.common.AbstractKeyPair;

public class EDKeyPair extends AbstractKeyPair {

	private EdDSAPrivateKey edPrivateKey;
	private EdDSAPublicKey edPublicKey;

	public EDKeyPair(final byte[] seed) {
		final EdDSAParameterSpec spec = getEdDSAParameterSpec();
		final EdDSAPrivateKeySpec privateKeySpec = new EdDSAPrivateKeySpec(seed, spec);
		this.edPrivateKey = new EdDSAPrivateKey(privateKeySpec);
		this.privateKey = edPrivateKey.geta();
		final EdDSAPublicKeySpec pubKeySpec = new EdDSAPublicKeySpec(privateKeySpec.getA(), spec);
		this.edPublicKey = new EdDSAPublicKey(pubKeySpec);
		this.publicKey = edPublicKey.getAbyte();
	}

	public EDKeyPair(final byte[] publicKey, final byte[] privateKey) {
		try {
			if (publicKey.length  == 44) {
				final X509EncodedKeySpec encodedPubKey = new X509EncodedKeySpec(publicKey);
				this.edPublicKey = new EdDSAPublicKey(encodedPubKey);
				this.publicKey = edPublicKey.getAbyte();
			} else if (publicKey.length == 32) {
				final EdDSAPublicKeySpec encodedPubKey = new EdDSAPublicKeySpec(publicKey, EdDSANamedCurveTable.ED_25519_CURVE_SPEC);
				this.edPublicKey = new EdDSAPublicKey(encodedPubKey);
				this.publicKey = edPublicKey.getAbyte();
			}

			if (privateKey != null) {
				if (privateKey.length != 0) {
					final PKCS8EncodedKeySpec encodedPrivKey = new PKCS8EncodedKeySpec(privateKey);
					this.edPrivateKey = new EdDSAPrivateKey(encodedPrivKey);
					this.privateKey = edPrivateKey.geta();
				}
			}

		} catch (final InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}

	public static EDKeyPair buildFromPrivateKey(final byte[] privateKey) {
		try {
			final EDKeyPair edKeyPair = new EDKeyPair();
			final PKCS8EncodedKeySpec encodedPrivKey = new PKCS8EncodedKeySpec(privateKey);
			edKeyPair.edPrivateKey = new EdDSAPrivateKey(encodedPrivKey);
			edKeyPair.edPublicKey = new EdDSAPublicKey(
					new EdDSAPublicKeySpec(edKeyPair.edPrivateKey.getAbyte(), getEdDSAParameterSpec()));
			return edKeyPair;
		} catch (final Exception exception) {
			throw new RuntimeException(exception);
		}
	}

//	public static EDKeyPair buildFromPublicKey(final byte[] publicKey) {
//		try {
//			final X509EncodedKeySpec encodedPubKey = new X509EncodedKeySpec(publicKey);
//			this.edPublicKey = new EdDSAPublicKey(encodedPubKey);
//			this.publicKey = edPublicKey.getAbyte();
//		} catch (final Exception exception) {
//			throw new RuntimeException(exception);
//		}
//	}

	@Override
	public void setPublicKey(final byte[] publicKey) {
		try {
			final X509EncodedKeySpec encodedPubKey = new X509EncodedKeySpec(publicKey);
			this.edPublicKey = new EdDSAPublicKey(encodedPubKey);
			this.publicKey = edPublicKey.getAbyte();
		} catch (final InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public PrivateKey getPrivateKey() {
		return this.edPrivateKey;
	}

	@Override
	public PublicKey getPublicKey() {
		return this.edPublicKey;
	}

	public byte[] getPublicKeyEncoded() {
		return this.edPublicKey.getEncoded();
	}

	public void setSecretKey(final byte[] secretKey) {
		try {
			final PKCS8EncodedKeySpec encodedPrivKey = new PKCS8EncodedKeySpec(privateKey);
			this.edPrivateKey = new EdDSAPrivateKey(encodedPrivKey);
			this.privateKey = edPrivateKey.getEncoded();
		} catch (final InvalidKeySpecException e) {
			throw new RuntimeException(e);
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

	private EDKeyPair() {
	}

	private static EdDSAParameterSpec getEdDSAParameterSpec() {
		return EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
	}
}
