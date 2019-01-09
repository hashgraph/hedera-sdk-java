//package com.hedera.sdk.cryptography;
//
//import com.hedera.sdk.common.AbstractKeyPair;
//import com.hedera.sdk.common.HederaKeyPair;
//import com.hedera.sdk.common.HederaKeyPair.KeyType;
//
//import java.io.Serializable;
//import java.security.NoSuchAlgorithmException;
//import java.security.spec.InvalidKeySpecException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import org.bouncycastle.util.encoders.Hex;
///**
// * Implements public/private(secret) key management functions
// */
//public class HederaCryptoKeyPair implements Serializable {
//	private static final long serialVersionUID = 1L;
//	
//	private AbstractKeyPair keyPair = null;
//	private Seed seed = null;
//	protected HederaKeyPair.KeyType keyType = KeyType.NOTSET;
//
//	/**
//	 * sets the public key as a byte[]
//	 * @param publicKey the public key
//	 */
//	public void setPublicKey(byte[] publicKey)  {
//		keyPair.setPublicKey(publicKey);
//	}
//
//	/**
//	 * sets the encoded public key as a byte[]
//	 * @param encodedPublicKey the encoded public key
//	 */
//	public void setPublicKeyEncoded(byte[] encodedPublicKey) {
//		keyPair.setPublicKeyEncoded(encodedPublicKey);
//	}
//
//	/**
//	 * gets the public key as a byte[]
//	 * @return byte[] 
//	 */
//	public byte[] getPublicKey() {
//		return keyPair.publicKey;
//	}
//	
//	/**
//	 * gets the type of key
//	 * @return {@link KeyType}
//	 */
//	public KeyType getKeyType() {
//		return keyType;
//	}
//
//	/**
//	 * gets the encoded public key as a byte[]
//	 * @return byte[0] if not set
//	 */
//	public byte[] getPublicKeyEncoded() {
//		if (this.keyPair != null) {
//			return this.keyPair.getPublicKeyEncoded();
//		} else {
//			return new byte[0];
//		}
//	}
//    
//	/**
//	 * gets the encoded public key as a Hex String
//	 * @return empty string if not set
//	 */
//	public String getPublicKeyEncodedHex() {
//		if (this.keyPair != null) {
//			return Hex.toHexString(this.keyPair.getPublicKey().getEncoded());
//		} else {
//			return "";
//		}
//	}
//
//	/**
//	 * sets the secret key as a byte[]
//	 * @param secretKey the secret key
//	 */
//	public void setSecretKey(byte[] secretKey) {
//		keyPair.setSecretKey(secretKey);
//	}
//
//	/**
//	 * gets the secret key as a byte[]
//	 * @return  byte[] or null if not set
//	 */
////	public byte[] getSecretKey() {
////		if (this.keyPair != null) {
////			return keyPair.privateKey;
////		} else {
////			return null;
////		}
////	}
//
//	/**
//	 * gets the secret key as a String
//	 * @return empty string if not set
//	 */
//	public String getSecretKeyHex() {
//		if (this.keyPair != null) {
//			return Hex.toHexString(this.keyPair.getPrivateKey().getEncoded());
//		} else {
//			return "";
//		}
//	}
//
//	/**
//	 * default constructor, does nothing other than instantiate this object
//	 */
//	public HederaCryptoKeyPair() {
//	}
//
//	/**
//	 * Constructor from a key type and recoveryWords as an array of String
//	 * Creates or Recovers a public/private keypair from the list of words
//	 * @param keyType the type of key to recover
//	 * @param recoveryWords String[] the list of words to recover from
//	 * @throws NoSuchAlgorithmException in the event of an error
//	 */
//	public HederaCryptoKeyPair(HederaKeyPair.KeyType keyType, String[] recoveryWords) throws NoSuchAlgorithmException  {
//		this(keyType, Arrays.asList(recoveryWords));
//	}
//
//	/**
//	 * Constructor from a key type and recoveryWords as an List of String
//	 * Creates or Recovers a public/private keypair from the list of words
//	 * @param keyType the type of key to recover
//	 * @param recoveryWords the words to recover from
//	 * @throws NoSuchAlgorithmException in the event of an error
//	 */
//	public HederaCryptoKeyPair(HederaKeyPair.KeyType keyType, List<String> recoveryWords) throws NoSuchAlgorithmException  {
//		this.keyType = keyType;
//
//		byte[] privateKey;
//		this.seed = Seed.fromWordList(recoveryWords);
//		privateKey = CryptoUtils.deriveKey(seed.toBytes(), -1, 32);
//		keyPair = new EDKeyPair(privateKey);
//	}
//
//	/**
//	 * Constructor from a key type and seed 
//	 * @param keyType the type of key to generate
//	 * @param seed the seed to generate with
//	 */
//	public HederaCryptoKeyPair(HederaKeyPair.KeyType keyType, byte[] seed) {
//		
//		this.keyType = keyType;
//		
//		if (seed == null) {
//			seed = CryptoUtils.getSecureRandomData(32);
//		}
//		byte[] privateKey;
//		if (seed.length != 32) {
//			throw new IllegalStateException(String.format("Seed size of %d is invalid, should be 32", seed.length));
//		}
//		this.seed = Seed.fromEntropy(seed);
//		privateKey = CryptoUtils.deriveKey(this.seed.toBytes(), -1, 32);
//		keyPair = new EDKeyPair(privateKey);
//  }
// 
//	/** 
//	 * Constructs a key pair of the given key type, the seed is randomly generated
//	 * @param keyType the type of key to create
//	 */
//	public HederaCryptoKeyPair(HederaKeyPair.KeyType keyType) {
//		this(keyType, (byte[])null);
//	}
//
//	/**
//	 * Constructs from a known pair of public and optional private key
//	 * If the private key is null or has a length of 0, only a public key will be created
//	 * @param keyType {@link HederaKeyPair.KeyType}
//	 * @param publicKey {@link byte} array
//	 * @param secretKey {@link byte} array
//	 * @throws InvalidKeySpecException 
//	 */
//	public HederaCryptoKeyPair(HederaKeyPair.KeyType keyType, byte[] publicKey, byte[] secretKey) throws InvalidKeySpecException {
//		this.keyType = keyType;
//
//		switch (this.keyType) {
////			case ECDSA384:
////				throw new IllegalStateException("ECDSA384 keys are not supported");
//			case ED25519:
//				keyPair = null;
//				if (secretKey != null) {
//					if (secretKey.length != 0) {
//						keyPair = new EDKeyPair(publicKey, secretKey);
//					}
//				}
//				if (keyPair == null) {
//					keyPair = EDKeyPair.buildFromPublicKey(publicKey);
//				}
//				break;
////			case RSA3072:
////				throw new IllegalStateException("RSA3072 keys are not supported");
//			default:
//				throw new IllegalStateException("Key Type not set");
//		}
//	} 
//    
//	/**
//	 * Constructs from a known pair of public and optional private key
//	 * If the private key supplied is null or empty (""), only a public key is created
//	 * @param keyType {@link HederaKeyPair.KeyType}
//	 * @param publicKey {@link String} as a hex encoded string
//	 * @param secretKey {@link String} as a hex encoded string
//	 * @throws IllegalStateException if the key type is invalid 
//	 */
//	public HederaCryptoKeyPair(HederaKeyPair.KeyType keyType, String publicKey, String secretKey) throws InvalidKeySpecException {
//		this.keyType = keyType;
//		
//		switch (this.keyType) {
////			case ECDSA384:
////				throw new IllegalStateException("ECDSA384 keys are not supported");
//			case ED25519:
//				byte[] pub = Hex.decode(publicKey);
//				
//				keyPair = null;
//				if (secretKey != null) {
//					if (secretKey.isEmpty()) {
//						byte[] secret = Hex.decode(secretKey);
//						keyPair = new EDKeyPair(pub, secret);
//					}
//				}
//				if (keyPair == null) {
//					keyPair = EDKeyPair.buildFromPublicKey(pub);
//				}
//
//				break;
////			case RSA3072:
////				throw new IllegalStateException("RSA3072 keys are not supported");
//			default:
//				throw new IllegalStateException("Key Type not set");
//		}
//	} 
//    
//	/**
//	 * Returns the list of recoveryWords for a key pair
//	 * @return list of Strings
//	 * @throws IllegalStateException if the key type is invalid
//	 */
//	public List<String> recoveryWordsList() {
//		
//		switch (this.keyType) {
////			case ECDSA384:
////				throw new IllegalStateException("ECDSA384 keys are not supported");
//			case ED25519:
//				if (this.seed != null) {
//					return this.seed.toWords();
//				} else {
//					return new ArrayList<String>();
//				}
////			case RSA3072:
////				throw new IllegalStateException("RSA3072 keys are not supported");
//			default:
//				throw new IllegalStateException("This type of key is not supported");
//		}
//	}
//
//	/**
//	 * Returns the list of recoveryWords for a key pair
//	 * @return String[]
//	 * @throws IllegalStateException if the key type is invalid
//	 */
//	public String[] recoveryWordsArray() {
//		switch (this.keyType) {
////			case ECDSA384:
////				throw new IllegalStateException("ECDSA384 keys are not supported");
//			case ED25519:
//				List<String> wordList = new ArrayList<String>();
//				if (this.seed != null) {
//					wordList = this.seed.toWords();
//					return wordList.toArray(new String[0]);
//				} else {
//					return new String[0];
//				}
////			case RSA3072:
////				throw new IllegalStateException("RSA3072 keys are not supported");
//			default:
//				throw new IllegalStateException("This type of key is not supported");
//		}
//	}
//
//	/**
//	 * signs a message with the private key
//	 * @param message byte[]
//	 * @return byte[]
//	 */
//	public byte[] signMessage(byte[] message)  {
//		return keyPair.signMessage(message);
//	}
//
//	/**
//	 * verifies a message against a signature
//	 * @param message byte[]
//	 * @param signature byte[]
//	 * @return {@link Boolean}
//	 * @throws Exception 
//	 */
//	public boolean verifySignature(byte[] message, byte[] signature) throws Exception {
//		return keyPair.verifySignature(message, signature);
//	}
//}
