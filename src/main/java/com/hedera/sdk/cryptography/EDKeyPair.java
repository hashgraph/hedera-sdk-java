package com.hedera.sdk.cryptography;

import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;

public class EDKeyPair extends AbstractKeyPair {

    private EdDSAPrivateKey edPrivateKey;
    private EdDSAPublicKey edPublicKey;

    public EDKeyPair(byte[] seed) {
        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
        EdDSAPrivateKeySpec privateKeySpec = new EdDSAPrivateKeySpec(seed, spec);
        this.edPrivateKey = new EdDSAPrivateKey(privateKeySpec);
        this.privateKey = edPrivateKey.getEncoded();
        EdDSAPublicKeySpec pubKeySpec = new EdDSAPublicKeySpec(privateKeySpec.getA(), spec);
        this.edPublicKey = new EdDSAPublicKey(pubKeySpec);
        this.publicKey = edPublicKey.getAbyte();
    }

    public EDKeyPair(byte[] publicKey, byte[] privateKey) throws InvalidKeySpecException {
        PKCS8EncodedKeySpec encodedPrivKey = new PKCS8EncodedKeySpec(privateKey);
    	X509EncodedKeySpec encodedPubKey = new X509EncodedKeySpec(publicKey);
		this.edPrivateKey = new EdDSAPrivateKey(encodedPrivKey);
		this.privateKey = edPrivateKey.getEncoded();
        this.edPublicKey = new EdDSAPublicKey(encodedPubKey);
        this.publicKey = edPublicKey.getAbyte();
    }

    @Override
    public void setPublicKey(byte[] publicKey) throws InvalidKeySpecException {
    	X509EncodedKeySpec encodedPubKey = new X509EncodedKeySpec(publicKey);
        this.edPublicKey = new EdDSAPublicKey(encodedPubKey);
        this.publicKey = edPublicKey.getAbyte();
    }
    
    @Override
    public byte[] getPublicKey() {
        return this.edPublicKey.getAbyte();      
    }

    public byte[] getPublicKeyEncoded() {
        return this.edPublicKey.getEncoded();     
    }
    public void setSecretKey(byte[] secretKey) throws InvalidKeySpecException {
    	PKCS8EncodedKeySpec encodedPrivKey = new PKCS8EncodedKeySpec(privateKey);
		this.edPrivateKey = new EdDSAPrivateKey(encodedPrivKey);
		this.privateKey = edPrivateKey.getEncoded();
    }
    
    @Override
    public byte[] signMessage(byte[] message) throws Exception {
        Signature sgr = new EdDSAEngine();
        sgr.initSign(edPrivateKey);
        sgr.update(message);
        byte[] signedMessage = sgr.sign();
        return signedMessage;

    }

    @Override
    public boolean verifySignature(byte[] message, byte[] signature) throws Exception {
        Signature sgr = new EdDSAEngine();
        sgr.initVerify(edPublicKey);
        sgr.update(message);
        return sgr.verify(signature);
    }
}
