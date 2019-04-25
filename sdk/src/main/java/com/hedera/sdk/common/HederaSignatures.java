package com.hedera.sdk.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;

import com.google.protobuf.ByteString;
import com.hederahashgraph.api.proto.java.SignatureMap;
import com.hederahashgraph.api.proto.java.SignaturePair;
import com.hederahashgraph.api.proto.java.Transaction;

public class HederaSignatures {

	private Hashtable<byte[], byte[]> signaturePairs = new Hashtable<byte[], byte[]>();

	public void addSignature(byte[] pubKey, byte[] signature) {
		this.signaturePairs.put(pubKey, signature);
	}

	public byte[] getSignature(byte[] pubKey) throws Exception {
		if (this.signaturePairs.containsKey(pubKey)) {
			return this.signaturePairs.get(pubKey);
		} else {
			throw new Exception("Public key " + pubKey + " not found");
		}
	}

	public SignatureMap getProtobuf() {
		SignatureMap.Builder signatureMap = SignatureMap.newBuilder();
		SignaturePair.Builder signaturePair = SignaturePair.newBuilder();

		Iterator it = this.signaturePairs.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			byte[] signature = (byte[]) pair.getValue();
			if (signature.length == 0) {
				// 0 length signature is a contract
				signaturePair.setContract(ByteString.copyFrom(signature));
			} else {
				// it's ED25519
				signaturePair.setEd25519(ByteString.copyFrom(signature));
			}
			signaturePair.setPubKeyPrefix(ByteString.copyFrom((byte[]) pair.getKey()));
			signatureMap.addSigPair(signaturePair);
		}

		return signatureMap.build();
	}

	public SignatureMap getProtobuf(int prefixLen) {
		SignatureMap.Builder signatureMap = SignatureMap.newBuilder();
		SignaturePair.Builder signaturePair = SignaturePair.newBuilder();

		Iterator it = this.signaturePairs.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			byte[] signature = (byte[]) pair.getValue();
			if (signature.length == 0) {
				// 0 length signature is a contract
				signaturePair.setContract(ByteString.copyFrom(signature));
			} else {
				// it's ED25519
				signaturePair.setEd25519(ByteString.copyFrom(signature));
			}

			byte[] pubKeyBytes = (byte[]) pair.getKey();
			byte[] prefixBytes = pubKeyBytes;
			if (prefixLen != -1)
				prefixBytes = copyBytes(0, prefixLen, pubKeyBytes);

			signaturePair.setPubKeyPrefix(ByteString.copyFrom(prefixBytes));
			signatureMap.addSigPair(signaturePair);
		}

		return signatureMap.build();
	}

	public Transaction transactionAddSignatures(Transaction transaction) {

		Transaction.Builder newTransaction = Transaction.newBuilder();
		newTransaction.setBodyBytes(transaction.getBodyBytes());

		SignatureMap.Builder signatureMap = SignatureMap.newBuilder();
		// add the existing signatures
		for (SignaturePair sigPair : newTransaction.getSigMap().getSigPairList()) {
			signatureMap.addSigPair(sigPair);
		}

		SignaturePair.Builder signaturePair = SignaturePair.newBuilder();

		Iterator it = this.signaturePairs.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			byte[] signature = (byte[]) pair.getValue();
			if (signature.length == 0) {
				// 0 length signature is a contract
				signaturePair.setContract(ByteString.copyFrom(signature));
			} else {
				// it's ED25519
				signaturePair.setEd25519(ByteString.copyFrom(signature));
			}
			signaturePair.setPubKeyPrefix(ByteString.copyFrom((byte[]) pair.getKey()));
			signatureMap.addSigPair(signaturePair);
		}
		newTransaction.setSigMap(signatureMap);

		return newTransaction.build();
	}

	/**
	 * Finds the minimum prefix length in term of bytes.
	 * 
	 * @param keys set of keys to process
	 * @return found minimum prefix length
	 */
	public int findMinPrefixLength() {
		if (this.signaturePairs.size() == 1)
			return 0;

		int rv = 0;
		int numKeys = this.signaturePairs.size();
		// convert set to list of key hex strings
		// find max string length
		List<String> keyHexes = new ArrayList<>();
		int maxBytes = 0;
		Iterator it = this.signaturePairs.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			byte[] bytes = (byte[]) pair.getKey();
			if (bytes.length > maxBytes)
				maxBytes = bytes.length;
			String hex = Hex.encodeHexString(bytes);
			keyHexes.add(hex);
		}

		rv = maxBytes;

		// starting from first byte (each byte is 2 hex chars) to max/2 and loop with
		// step of 2
		for (int i = 1; i <= maxBytes; i++) {
			// get all the prefixes and form a set (unique ones), check if size of the set
			// is reduced.
			Set<String> prefixSet = new HashSet<>();
			for (String khex : keyHexes) {
				prefixSet.add(khex.substring(0, i * 2));
			}
			// if not reduced, the current prefix size is the answer, stop
			if (prefixSet.size() == numKeys) {
				rv = i;
				break;
			}
		}
		return rv;
	}

	/**
	 * Copy bytes.
	 *
	 * @param start  from position
	 * @param length number of bytes to copy
	 * @param bytes  source byte array
	 */
	private byte[] copyBytes(int start, int length, byte[] bytes) {
		byte[] rv = new byte[length];
		for (int i = 0; i < length; i++) {
			rv[i] = bytes[start + i];
		}
		return rv;
	}
}
