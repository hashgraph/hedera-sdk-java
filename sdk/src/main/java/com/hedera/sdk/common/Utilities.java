package com.hedera.sdk.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import org.slf4j.LoggerFactory;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.node.HederaNode;
import com.hederahashgraph.api.proto.java.KeyList;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
import com.hederahashgraph.api.proto.java.SignatureList;

public class Utilities {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Utilities.class);
	/**
	 * Serializes an object into a byte array
	 * @param object any object which is serializable
	 * @return byte[]
	 * @throws IOException in the event of an error
	 */
	public static byte[] serialize(Object object) throws IOException {

		byte[] serialData = new byte[0];
		// Serialise
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(bos);
	    os.writeObject(object);
	    serialData = bos.toByteArray();
	    os.close();

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

		Object returnObject = new Object();
	
	    // de-serialise
	    ByteArrayInputStream bis = new ByteArrayInputStream(serialData);
	    ObjectInputStream oInputStream = new ObjectInputStream(bis);
	    returnObject = oInputStream.readObject();
	    oInputStream.close();

	    return returnObject;
	}

	/**
	 * Generates a random long number using java's SecureRandom class
	 * Use this to generate a random account number, contract number or file number.
	 * @return {@link Long}
	 */
	public static long getLongRandom() {

		Random random = new SecureRandom();
		byte[] randomInt64 = new byte[16]; // 16 bytes = 64 bits = Int64 
		random.nextBytes(randomInt64);

		return random.nextLong();
	}
	/**
	 * Helper function to generate a {@link HederaKeySignature} for a given 
	 * payload (body) and keypair
	 * @param payload the payload to sign
	 * @param keyPair the keypair to use for signing
	 * @return {@link HederaKeySignature}
	 * @throws Exception in the event of an error 
	 */
	public static HederaKeySignature getKeySignature(byte[] payload, HederaKeyPair keyPair) throws Exception {

		byte[] signedBody = keyPair.signMessage(payload);
		// create a Hedera Signature for it
		HederaSignature signature = new HederaSignature(keyPair.getKeyType(), signedBody);

		return new HederaKeySignature(keyPair.getKeyType(), keyPair.getPublicKeyEncoded(), signature.getSignature());
	}
	/**
	 * Helper function to generate a {@link HederaKeySignature} for a given 
	 * payload (body) key type, private and public key
	 * @param payload the payload to sign
	 * @param keyType the type of key
	 * @param publicKey the public key
	 * @param privateKey the private key
	 * @return {@link HederaKeySignature}
	 * @throws Exception in the event of an error 
	 */
	public static HederaKeySignature getKeySignature(byte[] payload, KeyType keyType, byte[] publicKey, byte[] privateKey) throws Exception {
		// create new keypair
		HederaKeyPair keyPair = new HederaKeyPair(keyType, publicKey, privateKey);
		return getKeySignature(payload, keyPair);
	}
	/**
	 * Helper function to generate a {@link HederaSignature} for a given 
	 * payload (body) and keypair
	 * @param payload the payload to sign
	 * @param keyPair the keypair to use for the signature
	 * @return {@link HederaSignature}
	 * @throws Exception in the event of an error 
	 */
	public static HederaSignature getSignature(byte[] payload, HederaKeyPair keyPair) throws Exception {

		byte[] signedBody = keyPair.signMessage(payload);
		// create a Hedera Signature for it
		HederaSignature signature = new HederaSignature(keyPair.getKeyType(), signedBody);

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
	 * @throws Exception in the event of an error 
	 */
	public static HederaSignature getSignature(byte[] payload, KeyType keyType, byte[] publicKey, byte[] privateKey) throws Exception {
		// create new keypair
		HederaKeyPair keyPair = new HederaKeyPair(keyType, publicKey, privateKey);
		return getSignature(payload, keyPair);
	}
	/**
	 * retrieves a receipt for a transaction, sleeps 1 second between attempts
	 * returns the last {@link HederaTransactionReceipt} received.
	 * tries 10 times and aborts if not successful
	 * @param hederaTransactionID the transaction id to get a receipt for
	 * @param node the node to communicate with
	 * @return {@link HederaTransactionReceipt}
	 * @throws InterruptedException in the event of a node communication issue 
	 */
	public static HederaTransactionReceipt getReceipt (HederaTransactionID hederaTransactionID, HederaNode node) throws InterruptedException {

		final int MAX_CALL_COUNT = 50;
		final int DELAY_BASE = 550; // base delay in milliseconds
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
	 * @throws InterruptedException in the event of a node communication issue 
	 */
	public static HederaTransactionReceipt getReceipt (HederaTransactionID hederaTransactionID, HederaNode node, int maxRetries, int firstDelay, int increaseDelay) throws InterruptedException {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaTransactionReceipt.class);

		long sleepTime = firstDelay;
		boolean keepGoing = true;
		
		int callCount = 1;
		HederaTransactionReceipt receipt = new HederaTransactionReceipt();
		receipt.transactionStatus = ResponseCodeEnum.UNKNOWN;
		while ((callCount <= maxRetries) && keepGoing) {
			
			Thread.sleep(sleepTime);
			sleepTime += increaseDelay;
			logger.info("Fetching receipt");
			receipt = new HederaTransactionReceipt(hederaTransactionID, node);
			// was that successful ?
			callCount += 1;
			switch (receipt.nodePrecheck) {
				case ACCOUNT_UPDATE_FAILED:
					logger.info("precheck=ACCOUNT_UPDATE_FAILED");
					keepGoing = false;
					break;
				case BAD_ENCODING:
					logger.info("precheck=BAD_ENCODING");
					keepGoing = false;
					break;
				case BUSY:
					logger.info("precheck=BUSY");
					break;
				case CONTRACT_EXECUTION_EXCEPTION:
					logger.info("precheck=CONTRACT_EXECUTION_EXCEPTION");
					keepGoing = false;
					break;
				case CONTRACT_REVERT_EXECUTED:
					logger.info("precheck=CONTRACT_REVERT_EXECUTED");
					keepGoing = false;
					break;
				case CONTRACT_SIZE_LIMIT_EXCEEDED:
					logger.info("precheck=CONTRACT_SIZE_LIMIT_EXCEEDED");
					keepGoing = false;
					break;
				case CONTRACT_UPDATE_FAILED:
					logger.info("precheck=CONTRACT_UPDATE_FAILED");
					keepGoing = false;
					break;
				case DUPLICATE_TRANSACTION:
					logger.info("precheck=DUPLICATE_TRANSACTION");
					keepGoing = false;
					break;
				case EMPTY_TRANSACTION_BODY:
					logger.info("precheck=EMPTY_TRANSACTION_BODY");
					keepGoing = false;
					break;
				case FAIL_BALANCE:
					logger.info("precheck=FAIL_BALANCE");
					keepGoing = false;
					break;
				case FAIL_FEE:
					logger.info("precheck=FAIL_FEE");
					keepGoing = false;
					break;
				case FAIL_INVALID:
					logger.info("precheck=FAIL_INVALID");
					keepGoing = false;
					break;
				case FILE_CONTENT_EMPTY:
					logger.info("precheck=FILE_CONTENT_EMPTY");
					keepGoing = false;
					break;
				case INSUFFICIENT_ACCOUNT_BALANCE:
					logger.info("precheck=INSUFFICIENT_ACCOUNT_BALANCE");
					keepGoing = false;
					break;
				case INSUFFICIENT_GAS:
					logger.info("precheck=INSUFFICIENT_GAS");
					keepGoing = false;
					break;
				case INSUFFICIENT_PAYER_BALANCE:
					logger.info("precheck=INSUFFICIENT_PAYER_BALANCE");
					keepGoing = false;
					break;
				case INSUFFICIENT_TX_FEE:
					logger.info("precheck=INSUFFICIENT_TX_FEE");
					keepGoing = false;
					break;
				case INVALID_ACCOUNT_AMOUNTS:
					logger.info("precheck=INVALID_ACCOUNT_AMOUNTS");
					keepGoing = false;
					break;
				case INVALID_ACCOUNT_ID:
					logger.info("precheck=INVALID_ACCOUNT_ID");
					keepGoing = false;
					break;
				case INVALID_CONTRACT_ID:
					logger.info("precheck=INVALID_CONTRACT_ID");
					keepGoing = false;
					break;
				case INVALID_EXPIRATION_TIME:
					logger.info("precheck=INVALID_EXPIRATION_TIME");
					keepGoing = false;
					break;
				case INVALID_FEE_SUBMITTED:
					logger.info("precheck=INVALID_FEE_SUBMITTED");
					keepGoing = false;
					break;
				case INVALID_FILE_ID:
					logger.info("precheck=INVALID_FILE_ID");
					keepGoing = false;
					break;
				case INVALID_KEY_ENCODING:
					logger.info("precheck=INVALID_KEY_ENCODING");
					keepGoing = false;
					break;
				case INVALID_NODE_ACCOUNT:
					logger.info("precheck=INVALID_NODE_ACCOUNT");
					keepGoing = false;
					break;
				case INVALID_PAYER_SIGNATURE:
					logger.info("precheck=INVALID_PAYER_SIGNATURE");
					keepGoing = false;
					break;
				case INVALID_QUERY_HEADER:
					logger.info("precheck=INVALID_QUERY_HEADER");
					keepGoing = false;
					break;
				case INVALID_RECEIVING_NODE_ACCOUNT:
					logger.info("precheck=INVALID_RECEIVING_NODE_ACCOUNT");
					keepGoing = false;
					break;
				case INVALID_SIGNATURE:
					logger.info("precheck=INVALID_SIGNATURE");
					keepGoing = false;
					break;
				case INVALID_SOLIDITY_ADDRESS:
					logger.info("precheck=INVALID_SOLIDITY_ADDRESS");
					keepGoing = false;
					break;
				case INVALID_SOLIDITY_ID:
					logger.info("precheck=INVALID_SOLIDITY_ID");
					keepGoing = false;
					break;
				case INVALID_TRANSACTION: 
					// do nothing
					logger.info("precheck=INVALID_TRANSACTION");
					break;
				case INVALID_TRANSACTION_BODY:
					logger.info("precheck=INVALID_TRANSACTION_BODY");
					keepGoing = false;
					break;
				case INVALID_TRANSACTION_DURATION:
					logger.info("precheck=INVALID_TRANSACTION_DURATION");
					keepGoing = false;
					break;
				case INVALID_TRANSACTION_ID:
					logger.info("precheck=INVALID_TRANSACTION_ID");
					keepGoing = false;
					break;
				case INVALID_TRANSACTION_START:
					logger.info("precheck=INVALID_TRANSACTION_START");
					keepGoing = false;
					break;
				case KEY_NOT_PROVIDED:
					logger.info("precheck=KEY_NOT_PROVIDED");
					keepGoing = false;
					break;
				case KEY_REQUIRED: 
					logger.info("precheck=KEY_REQUIRED");
					keepGoing = false;
					break;
				case LOCAL_CALL_MODIFICATION_EXCEPTION:
					logger.info("precheck=LOCAL_CALL_MODIFICATION_EXCEPTION");
					keepGoing = false;
					break;
				case MEMO_TOO_LONG: 
					logger.info("precheck=MEMO_TOO_LONG");
					keepGoing = false;
					break;
				case MISSING_QUERY_HEADER:
					logger.info("precheck=MISSING_QUERY_HEADER");
					keepGoing = false;
					break;
				case NO_WACL_KEY:
					logger.info("precheck=NO_WACL_KEY");
					keepGoing = false;
					break;
				case NOT_SUPPORTED:
					logger.info("precheck=NOT_SUPPORTED");
					keepGoing = false;
					break;
				case NULL_SOLIDITY_ADDRESS:
					logger.info("precheck=NULL_SOLIDITY_ADDRESS");
					keepGoing = false;
					break;
				case OK:
					logger.info("precheck=OK");
					if (receipt.transactionStatus != ResponseCodeEnum.UNKNOWN) {
						logger.info("took " + callCount + " call, about " + sleepTime * callCount + " milliseconds");
						keepGoing = false;
						break;
					} else {
						logger.info("Transaction status=" + receipt.transactionStatus.name());
						break;
					}
				case PAYER_ACCOUNT_NOT_FOUND:
					logger.info("precheck=PAYER_ACCOUNT_NOT_FOUND");
					keepGoing = false;
					break;
				case RECEIPT_NOT_FOUND:
					logger.info("precheck=RECEIPT_NOT_FOUND");
					keepGoing = false;
					break;
				case RECORD_NOT_FOUND: 
					logger.info("precheck=RECORD_NOT_FOUND");
					keepGoing = false;
					break;
				case SUCCESS:
					logger.info("precheck=SUCCESS");
					if (receipt.transactionStatus == ResponseCodeEnum.SUCCESS) {
						logger.info("took " + callCount + " call, about " + sleepTime * callCount + " milliseconds");
						keepGoing = false;
						break;
					} else {
						logger.info("Transaction status=" + receipt.transactionStatus.name());
						break;
					}
				case TRANSACTION_EXPIRED:
					logger.info("precheck=TRANSACTION_EXPIRED");
					keepGoing = false;
					break;
				case UNKNOWN:
					logger.info("precheck=UNKNOWN");
					keepGoing = false;
					break;
				case UNRECOGNIZED: 
					logger.info("precheck=UNRECOGNIZED");
					keepGoing = false;
					break;
			}
		}
		return receipt;
	}

	public static KeyList getProtoKeyList(List<HederaKeyPair> keys) {
		
		com.hederahashgraph.api.proto.java.KeyList.Builder keyListBuilder = KeyList.newBuilder();
		
		if (!keys.isEmpty()) {
			for (HederaKeyPair key : keys) {
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
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Utilities.class);
		logger.error("***** " + location + " FAILED to get response *****");
	}
	/**
	 * Throws an exception if the supplied node object is null
	 * @param objectType a string describing the object being checked which will be included in the error message
	 * @param object the object to check
	 * @throws IllegalStateException thrown if the object is null
	 */
	public static void throwIfNull(String objectType, Object object) {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Utilities.class);

		if (object == null) {
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
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Utilities.class);

		if (accountID == null) {
		   	String error = accountType + " AccountID is null.";
		   	logger.error(error);
			throw new IllegalStateException(error);
		} else {
			if ((accountID.shardNum < 0) || (accountID.realmNum < 0) || (accountID.accountNum <= 0)) {

			   	String error = accountType + " AccountID shard and realm must be greater or equal to 0 and accountNum greater than 0.";
			   	logger.error(error);
				throw new IllegalStateException(error);
			}
		}
	}
}