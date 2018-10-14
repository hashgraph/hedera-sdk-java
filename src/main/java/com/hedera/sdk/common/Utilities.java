package com.hedera.sdk.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.common.HederaKey.KeyType;
import com.hedera.sdk.cryptography.HederaCryptoKeyPair;
import com.hedera.sdk.node.HederaNode;
import com.hederahashgraph.api.proto.java.KeyList;
import com.hederahashgraph.api.proto.java.NodeTransactionPrecheckCode;
import com.hederahashgraph.api.proto.java.SignatureList;

public class Utilities {
	final static Logger logger = LoggerFactory.getLogger(Utilities.class);
	/**
	 * Serializes an object into a byte array
	 * @param object any object which is serializable
	 * @return byte[]
	 * @throws IOException in the event of an error
	 */
	public static byte[] serialize(Object object) throws IOException {
		logger.trace("Start - serialize");
		
		byte[] serialData = new byte[0];
		// Serialise
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(bos);
	    os.writeObject(object);
	    serialData = bos.toByteArray();
	    os.close();
		logger.trace("End - serialize");
		return serialData;
	}
	/**
	 * Rebuilds an object from its serialized representation
	 * @param serialData byte[] containing the serialized object
	 * @return {@link Object}
	 * @throws IOException in the event of an error
	 * @throws ClassNotFoundException in the event of an error
	 */
	public static Object deserialize(byte[] serialData) throws IOException, ClassNotFoundException {
		logger.trace("Start - deserialize");
		Object returnObject = new Object();
	
	    // de-serialise
	    ByteArrayInputStream bis = new ByteArrayInputStream(serialData);
	    ObjectInputStream oInputStream = new ObjectInputStream(bis);
	    returnObject = oInputStream.readObject();
	    oInputStream.close();
		logger.trace("End - deserialize");
	    return returnObject;
	}

	/**
	 * Generates a random long number using java's SecureRandom class
	 * Use this to generate a random account number, contract number or file number.
	 * @return {@link Long}
	 */
	public static long getLongRandom() {
		logger.trace("Start - getLongRandom");
		Random random = new SecureRandom();
		byte[] randomInt64 = new byte[16]; // 16 bytes = 64 bits = Int64 
		random.nextBytes(randomInt64);
		logger.trace("End - getLongRandom");
		return random.nextLong();
	}
	/**
	 * Helper function to generate a {@link HederaKeySignature} for a given 
	 * payload (body) and keypair
	 * @param payload the payload to sign
	 * @param keyPair the keypair to use for signing
	 * @return {@link HederaKeySignature}
	 */
	public static HederaKeySignature getKeySignature(byte[] payload, HederaCryptoKeyPair keyPair) {
		logger.trace("Start - getSignature payload {}, keyPair {}", payload, keyPair);
		byte[] signedBody = keyPair.signMessage(payload);
		// create a Hedera Signature for it
		HederaSignature signature = new HederaSignature(keyPair.getKeyType(), signedBody);
		logger.trace("End - getSignature");
		return new HederaKeySignature(keyPair.getKeyType(), keyPair.getPublicKey(), signature.getSignature());
	}
	/**
	 * Helper function to generate a {@link HederaKeySignature} for a given 
	 * payload (body) key type, private and public key
	 * @param payload the payload to sign
	 * @param keyType the type of key
	 * @param publicKey the public key
	 * @param privateKey the private key
	 * @return {@link HederaKeySignature}
	 */
	public static HederaKeySignature getKeySignature(byte[] payload, KeyType keyType, byte[] publicKey, byte[] privateKey) {
		// create new keypair
		HederaCryptoKeyPair keyPair = new HederaCryptoKeyPair(keyType, publicKey, privateKey);
		return getKeySignature(payload, keyPair);
	}
	/**
	 * Helper function to generate a {@link HederaSignature} for a given 
	 * payload (body) and keypair
	 * @param payload the payload to sign
	 * @param keyPair the keypair to use for the signature
	 * @return {@link HederaSignature}
	 */
	public static HederaSignature getSignature(byte[] payload, HederaCryptoKeyPair keyPair) {
		logger.trace("Start - getSignature payload {}, keyPair {}", payload, keyPair);
		byte[] signedBody = keyPair.signMessage(payload);
		// create a Hedera Signature for it
		HederaSignature signature = new HederaSignature(keyPair.getKeyType(), signedBody);
		logger.trace("End - getSignature");
		return new HederaSignature(keyPair.getKeyType(), signature.getSignature());
	}
	/**
	 * Helper function to generate a {@link HederaSignature} for a given 
	 * payload (body) key type and private key
	 * @param payload the payload to sign
	 * @param keyType the type of key
	 * @param publicKey the public key
	 * @param privateKey the private key
	 * @return {@link HederaSignature}
	 */
	public static HederaSignature getSignature(byte[] payload, KeyType keyType, byte[] publicKey, byte[] privateKey) {
		// create new keypair
		HederaCryptoKeyPair keyPair = new HederaCryptoKeyPair(keyType, publicKey, privateKey);
		return getSignature(payload, keyPair);
	}
	public static HederaPrecheckResult setPrecheckResult(NodeTransactionPrecheckCode nodeTransactionPrecheckCode) {
	   	logger.trace("setPrecheckResult nodeTransactionPrecheckCode {}", nodeTransactionPrecheckCode);
		switch (nodeTransactionPrecheckCode) {
		case DUPLICATE:
			return HederaPrecheckResult.DUPLICATE;
		case INSUFFICIENT_BALANCE:
			return HederaPrecheckResult.INSUFFICIENT_BALANCE;
		case INSUFFICIENT_FEE:
			return HederaPrecheckResult.INSUFFICIENT_FEE;
		case INVALID_ACCOUNT:
			return HederaPrecheckResult.INVALID_ACCOUNT;
		case INVALID_TRANSACTION:
			return HederaPrecheckResult.INVALID_TRANSACTION;
		case OK:
			return HederaPrecheckResult.OK;
		case UNRECOGNIZED:
			return HederaPrecheckResult.UNRECOGNIZED;
		default:
			return HederaPrecheckResult.NOTSET;
				
		}
	}
	/**
	 * retrieves a receipt for a transaction, sleeps 1 second between attempts
	 * returns the last {@link HederaTransactionReceipt} received.
	 * tries 10 times and aborts if not successful
	 * @param hederaTransactionID the transaction id to get a receipt for
	 * @param node the node to communicate with
	 * @return {@link HederaTransactionReceipt}
	 */
	public static HederaTransactionReceipt getReceipt (HederaTransactionID hederaTransactionID, HederaNode node) {

		final int MAX_CALL_COUNT = 500;
		final int DELAY_BASE = 50; // base delay in milliseconds
		final int DELAY_INCREASE = 0; // this determines how delay increases between calls. It is simply added to sleepTime between each call

		
		return getReceipt(hederaTransactionID, node, MAX_CALL_COUNT, DELAY_BASE, DELAY_INCREASE);
	}
	
	/**
	 * retrieves a receipt for a transaction
	 * returns the last {@link HederaTransactionReceipt} received.
	 * @param hederaTransactionID the transaction id to get a receipt for
	 * @param node the node to communicate with
	 * @param maxRetries the maximum number of times the method will retry
	 * @param firstDelay the time in milliseconds before the first getReceipt is sent
	 * @param increaseDelay the time in milliseconds to increase the delay by between each retry
	 * @return {@link HederaTransactionReceipt}
	 */
	public static HederaTransactionReceipt getReceipt (HederaTransactionID hederaTransactionID, HederaNode node, int maxRetries, int firstDelay, int increaseDelay) {
		final Logger logger = LoggerFactory.getLogger(HederaTransactionReceipt.class);

		long sleepTime = firstDelay;
		
		int callCount = 1;
		HederaTransactionReceipt receipt = new HederaTransactionReceipt();
		receipt.transactionStatus = HederaTransactionStatus.UNKNOWN;
		try {
			while (callCount <= maxRetries) {
				
				Thread.sleep(sleepTime);
				sleepTime += increaseDelay;
						
				receipt = new HederaTransactionReceipt(hederaTransactionID, node);
				// was that successful ?
				callCount += 1;
				if (receipt.nodePrecheck == HederaPrecheckResult.INVALID_TRANSACTION) {
					// do nothing
				} else if (receipt.transactionStatus == HederaTransactionStatus.FAIL_INVALID) {
					// force exit out of loop
					break;
				} else if (receipt.transactionStatus == HederaTransactionStatus.FAIL_BALANCE) {
					// force exit out of loop
					break;
				} else if (receipt.nodePrecheck == HederaPrecheckResult.OK) {
					if (receipt.transactionStatus == HederaTransactionStatus.SUCCESS) {
						logger.info("took " + callCount + " call, about " + sleepTime * callCount + " milliseconds");
						break;
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return receipt;
	}

	public static KeyList getProtoKeyList(List<HederaKey> keys) {
		
		com.hederahashgraph.api.proto.java.KeyList.Builder keyListBuilder = KeyList.newBuilder();
		
		if (!keys.isEmpty()) {
			for (HederaKey key : keys) {
				keyListBuilder.addKeys(key.getProtobuf());
			}
		}
		return keyListBuilder.build();
		
	}
	
	public static KeyList getProtoKeyFromKeySigList(List<HederaKeySignature> keySignatures) {
		
		com.hederahashgraph.api.proto.java.KeyList.Builder keyListBuilder = KeyList.newBuilder();
		
		if (!keySignatures.isEmpty()) {
			for (HederaKeySignature keySig : keySignatures) {
				keyListBuilder.addKeys(keySig.getKeyProtobuf());
			}
		}
		
		return keyListBuilder.build();
		
	}
	
	public static SignatureList getProtoSignatureFromKeySigList(List<HederaKeySignature> keySignatures) {
		
		com.hederahashgraph.api.proto.java.SignatureList.Builder sigListBuilder = SignatureList.newBuilder();
		
		if (!keySignatures.isEmpty()) {
			for (HederaKeySignature keySig : keySignatures) {
				sigListBuilder.addSigs(keySig.getSignatureProtobuf());
			}
		}
		
		return sigListBuilder.build();
		
	}
	public static SignatureList getProtoSignatureList(List<HederaSignature> signatures) {
		com.hederahashgraph.api.proto.java.SignatureList.Builder sigListBuilder = SignatureList.newBuilder();
		
		if (!signatures.isEmpty()) {
			for (HederaSignature sig : signatures) {
				sigListBuilder.addSigs(sig.getProtobuf());
			}
		}
		
		return sigListBuilder.build();
	}
	
	public static void printResponseFailure(String location) {
		logger.error("***** " + location + " FAILED to get response *****");
	}
	/**
	 * Throws an exception if the supplied node object is null
	 * @param objectType a string describing the object being checked which will be included in the error message
	 * @param object the object to check
	 * @throws IllegalStateException thrown if the object is null
	 */
	public static void throwIfNull(String objectType, Object object) {
		if (object == null) {
		   	logger.trace("throwIfNull");
		   	String error = objectType + " is null";
		   	logger.error(error);
			throw new IllegalStateException(error);
		}
	}
	/**
	 * Throws an exception if the supplied accountID object is invalid
	 * @param accountType a string describing the accountID being checked which will be included in the error message
	 * @param accountID {@link HederaAccountID} to check
	 * @throws IllegalStateException thrown if the accountID is invalid
	 */
	public static void throwIfAccountIDInvalid(String accountType, HederaAccountID accountID) {
		if (accountID == null) {
		   	logger.trace("throwIfAccountIDInvalid");
		   	String error = accountType + " AccountID is null.";
		   	logger.error(error);
			throw new IllegalStateException(error);
		} else {
			if ((accountID.shardNum < 0) || (accountID.realmNum < 0) || (accountID.accountNum <= 0)) {
			   	logger.trace("throwIfAccountIDInvalid");
			   	String error = accountType + " AccountID shard and realm must be greater or equal to 0 and accountNum greater than 0.";
			   	logger.error(error);
				throw new IllegalStateException(error);
			}
		}
	}
}