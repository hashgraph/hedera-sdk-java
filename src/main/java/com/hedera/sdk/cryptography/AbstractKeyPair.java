package com.hedera.sdk.cryptography;

abstract class AbstractKeyPair implements KeyPair {
	
	 protected Seed seed = null;

	 protected byte[] publicKey = new byte[0]; 
	 protected byte[] publicKeyEncoded = new byte[0]; 
	 protected byte[] privateKey = new byte[0];
	 
	 
	 public void setPublicKey(byte[] publicKey) {
		 this.publicKey = publicKey;
	 }

	 public void setPublicKeyEncoded(byte[] encodedPublicKey) {
		 this.publicKeyEncoded = encodedPublicKey;
	 }
	 
	 public void setSecretKey(byte[] secretKey) {
		 this.privateKey = secretKey;
	 }

	@Override
	public byte[] getPrivateKey() {
		return this.privateKey;
	}

	@Override
	public byte[] getPublicKey() {
		return this.publicKey;
	}
	@Override
	public byte[] getPublicKeyEncoded() {
		return this.publicKeyEncoded;
	}

	@Override
	abstract public byte[] signMessage(byte[] message);

	@Override
	abstract public boolean verifySignature(byte[] message, byte[] signature);

}
