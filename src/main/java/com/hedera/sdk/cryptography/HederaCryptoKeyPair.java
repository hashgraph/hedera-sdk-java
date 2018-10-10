package com.hedera.sdk.cryptography;

import com.hedera.sdk.common.HederaKey;
import com.hedera.sdk.common.HederaKey.KeyType;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
/**
 * Implements public/private(secret) key management functions
 */
public class HederaCryptoKeyPair implements Serializable {
	private static final long serialVersionUID = 1L;
	
	final static Logger logger = LoggerFactory.getLogger(HederaCryptoKeyPair.class);

	private AbstractKeyPair keyPair = null;
	protected HederaKey.KeyType keyType = KeyType.NOTSET;

  private Seed seed = null;

	/**
	 * sets the public key as a byte[]
	 * @param publicKey the public key
	 */
	public void setPublicKey(byte[] publicKey) {
		logger.trace("Start - setPublicKey publicKey {}", publicKey);
		keyPair.setPublicKey(publicKey);
		logger.trace("End - setPublicKey");
	}

	/**
	 * sets the encoded public key as a byte[]
	 * @param encodedPublicKey the encoded public key
	 */
	public void setPublicKeyEncoded(byte[] encodedPublicKey) {
		logger.trace("Start - setPublicKeyEncoded publicKey {}", encodedPublicKey);
		keyPair.setPublicKeyEncoded(encodedPublicKey);
		logger.trace("End - setPublicKeyEncoded");
	}

	/**
	 * gets the public key as a byte[]
	 * @return byte[0] if not set
	 */
	public byte[] getPublicKey() {
		logger.trace("Start - getPublicKey");
		if (this.keyPair != null) {
			logger.trace("End - getPublicKey");
			return keyPair.getPublicKey();
		} else {
			logger.trace("End - getPublicKey");
			return new byte[0];
		}
	}
	
	/**
	 * gets the type of key
	 * @return {@link KeyType}
	 */
	public KeyType getKeyType() {
		return keyType;
	}

	/**
	 * gets the encoded public key as a byte[]
	 * @return byte[0] if not set
	 */
	public byte[] getPublicKeyEncoded() {
		logger.trace("Start - getPublicKeyEncoded");
		if (this.keyPair != null) {
				logger.trace("End - getPublicKeyEncoded");
			return this.keyPair.getPublicKeyEncoded();
		} else {
				logger.trace("End - getPublicKeyEncoded");
			return new byte[0];
		}
	}
    
	/**
	 * gets the encoded public key as a Hex String
	 * @return empty string if not set
	 */
	public String getPublicKeyEncodedHex() {
		logger.trace("Start - getPublicKeyEncodedHex");
		if (this.keyPair != null) {
				logger.trace("End - getPublicKeyEncodedHex");
			return Hex.toHexString(this.keyPair.getPublicKeyEncoded());
		} else {
				logger.trace("End - getPublicKeyEncodedHex");
			return "";
		}
	}

	/**
	 * sets the secret key as a byte[]
	 * @param secretKey the secret key
	 */
	public void setSecretKey(byte[] secretKey) {
		logger.trace("Start - setSecretKey secretKey {}", secretKey);
		keyPair.setSecretKey(secretKey);  	
		logger.trace("End - setSecretKey");
	}

	/**
	 * gets the secret key as a byte[]
	 * @return  byte[0] if not set
	 */
  public byte[] getSecretKey() {
		logger.trace("Start - getSecretKey");
		if (this.keyPair != null) {
				logger.trace("End - getSecretKey");
				return this.keyPair.getPrivateKey();
		} else {
				logger.trace("End - getSecretKey");
			return new byte[0];
		}
  }

	/**
	 * gets the secret key as a String
	 * @return empty string if not set
	 */
	public String getSecretKeyHex() {
		logger.trace("Start - getSecretKeyHex");
		if (this.keyPair != null) {
				logger.trace("End - getSecretKeyHex");
				return Hex.toHexString(this.keyPair.getPrivateKey());
		} else {
				logger.trace("End - getSecretKeyHex");
			return "";
		}
	}

	/**
	 * default constructor, does nothing other than instantiate this object
	 */
	public HederaCryptoKeyPair() {
		logger.trace("Start - Object init");
		logger.trace("End - Object init");
	}

	/**
	 * Constructor from a key type and recoveryWords as an array of String
	 * Creates or Recovers a public/private keypair from the list of words
	 * @param keyType the type of key to recover
	 * @param recoveryWords String[] the list of words to recover from
	 * @throws NoSuchAlgorithmException in the event of an error
	 */
	public HederaCryptoKeyPair(HederaKey.KeyType keyType, String[] recoveryWords) throws NoSuchAlgorithmException  {
		this(keyType, Arrays.asList(recoveryWords));
	}

	/**
	 * Constructor from a key type and recoveryWords as an List of String
	 * Creates or Recovers a public/private keypair from the list of words
	 * @param keyType the type of key to recover
	 * @param recoveryWords the words to recover from
	 * @throws NoSuchAlgorithmException in the event of an error
	 */
	public HederaCryptoKeyPair(HederaKey.KeyType keyType, List<String> recoveryWords) throws NoSuchAlgorithmException  {
		logger.trace("Start - Object init keyType {}, recoveryWords {}", keyType, recoveryWords);
		
		this.keyType = keyType;
		byte[] privateKey;
		switch (keyType) {
			case ECDSA384:
				this.seed = Seed.fromWordList(recoveryWords);
				privateKey = CryptoUtils.deriveKey(seed.toBytes(), 0, 48);
				keyPair = ECKeyPair.fromPrivate(privateKey);
				break;
			case ED25519:
				this.seed = Seed.fromWordList(recoveryWords);
				privateKey = CryptoUtils.deriveKey(seed.toBytes(), 0, 32);
				keyPair = new EDKeyPair(privateKey);
				break;
			case RSA3072:
				throw new IllegalStateException("Cannot seed RSA3072 keys");
			default:
				throw new IllegalStateException("Key Type not set");
		}
	  logger.trace("End - Object init");
	}

	/**
	 * Constructor from a key type and seed 
	 * @param keyType the type of key to generate
	 * @param seed the seed to generate with
	 */
	public HederaCryptoKeyPair(HederaKey.KeyType keyType, byte[] seed) {
		logger.trace("Start - Object init keyType {}, seed {}", keyType, seed);
		
		this.keyType = keyType;
		
		if (seed == null) {
			// empty seed, need to generate a new one
			if (keyType == KeyType.ECDSA384) {
				seed = CryptoUtils.getSecureRandomData(48);
			} else if (keyType == KeyType.ED25519) {
					seed = CryptoUtils.getSecureRandomData(32);
			} else {
				seed = CryptoUtils.getSecureRandomData(48);
			}
		}
		byte[] privateKey;
		switch (keyType) {
			case ECDSA384:
				if (seed.length != 48) {
					throw new IllegalStateException(String.format("Seed size of %d is invalid, should be 48", seed.length));
				}
				this.seed = Seed.fromEntropy(seed);
				privateKey = CryptoUtils.deriveKey(this.seed.toBytes(), 0, 48);
				keyPair = ECKeyPair.fromPrivate(privateKey);
	   		break;
	   	case ED25519:
	   		if (seed.length != 32) {
	   			throw new IllegalStateException(String.format("Seed size of %d is invalid, should be 32", seed.length));
	   		}
	   		this.seed = Seed.fromEntropy(seed);
				privateKey = CryptoUtils.deriveKey(this.seed.toBytes(), 0, 32);
				keyPair = new EDKeyPair(privateKey);
	      break;
	   	case RSA3072:
				throw new IllegalStateException("Cannot seed RSA3072 keys");
			default:
				throw new IllegalStateException("Key Type not set");
	  }
		logger.trace("End - Object init");
  }
 
	/** 
	 * Constructs a key pair of the given key type, the seed is randomly generated
	 * @param keyType the type of key to create
	 */
	public HederaCryptoKeyPair(HederaKey.KeyType keyType) {
		this(keyType, (byte[])null);
	}

	/**
	 * Constructs from a known pair of public and private key
	 * @param keyType {@link HederaKey.KeyType}
	 * @param publicKey {@link byte} array
	 * @param secretKey {@link byte} array
	 */
	public HederaCryptoKeyPair(HederaKey.KeyType keyType, byte[] publicKey, byte[] secretKey) {
		logger.trace("Start - Object init keyType {}, publicKey {}, secretKey {}", keyType, publicKey, secretKey);
		this.keyType = keyType;
		logger.trace("End - Object init");

		switch (this.keyType) {
			case ECDSA384:
				keyPair = ECKeyPair.fromPrivate(secretKey);
				break;
			case ED25519:
				keyPair = new EDKeyPair(publicKey, secretKey);
				break;
			case RSA3072:
				throw new IllegalStateException("RSA3072 keys are not supported");
			default:
				throw new IllegalStateException("Key Type not set");
		}
	} 
    
	/**
	 * Constructs from a known pair of public and private key
	 * @param keyType {@link HederaKey.KeyType}
	 * @param publicKey {@link String} as a hex encoded string
	 * @param secretKey {@link String} as a hex encoded string
	 */
	public HederaCryptoKeyPair(HederaKey.KeyType keyType, String publicKey, String secretKey) {
		logger.trace("Start - Object init keyType {}, publicKey {}, secretKey {}", keyType, publicKey, secretKey);
		this.keyType = keyType;
		
		byte[] pub = Hex.decode(publicKey);
		byte[] secret = Hex.decode(secretKey);
		logger.trace("End - Object init");

		switch (this.keyType) {
			case ECDSA384:
				keyPair = ECKeyPair.fromPrivate(secret);
				break;
				case ED25519:
				logger.info("ED25519 key generation secretKey");
				logger.info(secretKey);
				logger.info("ED25519 key generation secret");
				logger.info(Arrays.toString(secret));
				keyPair = new EDKeyPair(pub, secret);
				break;
			case RSA3072:
				throw new IllegalStateException("RSA3072 keys are not supported");
			default:
				throw new IllegalStateException("Key Type not set");
		}
	} 
    
	/**
	 * Returns the list of recoveryWords for a key pair
	 * @return list of Strings
	 * @throws IllegalStateException if the key type is invalid
	 */
	public List<String> recoveryWordsList() {
		
		switch (this.keyType) {
			case ECDSA384:
				if (this.seed != null) {
					return this.seed.toWords();
				} else {
					return new ArrayList<String>();
				}
			case ED25519:
				if (this.seed != null) {
					logger.info("ED25519 key recoverywordslist");
					logger.info(Arrays.toString(this.seed.toBytes()));
					logger.info(this.seed.toWords().toString());
					return this.seed.toWords();
				} else {
					return new ArrayList<String>();
				}
			case RSA3072:
				throw new IllegalStateException("RSA3072 keys are not supported");
			default:
				throw new IllegalStateException("This type of key is not supported");
		}
	}

	/**
	 * Returns the list of recoveryWords for a key pair
	 * @return String[]
	 * @throws IllegalStateException if the key type is invalid
	 */
	public String[] recoveryWordsArray() {
		logger.trace("Start - recoveryWordsArray");
		List<String> wordList = new ArrayList<String>();
		switch (this.keyType) {
			case ECDSA384:
				if (this.seed != null) {
					wordList = this.seed.toWords();
					logger.trace("End - recoveryWordsArray");
					return wordList.toArray(new String[0]);
				} else {
					logger.trace("End - recoveryWordsArray");
					return new String[0];
				}
			case ED25519:
				if (this.seed != null) {
					wordList = this.seed.toWords();
					logger.trace("End - recoveryWordsArray");
					return wordList.toArray(new String[0]);
				} else {
					logger.trace("End - recoveryWordsArray");
					return new String[0];
				}
			case RSA3072:
				throw new IllegalStateException("RSA3072 keys are not supported");
			default:
				throw new IllegalStateException("This type of key is not supported");
		}
	}

	/**
	 * signs a message with the private key
	 * @param message byte[]
	 * @return byte[]
	 */
	public byte[] signMessage(byte[] message) {
		logger.trace("Start - signMessage message {}", message);
		logger.trace("End - signMessage");
		return keyPair.signMessage(message);
	}

	/**
	 * verifies a message against a signature
	 * @param message byte[]
	 * @param signature byte[]
	 * @return {@link Boolean}
	 */
	public boolean verifySignature(byte[] message, byte[] signature) {
		logger.trace("Start - verifySignature message {}, signature {}", message, signature);
		logger.trace("End - verifySignature");
		return keyPair.verifySignature(message, signature);
	}
}
