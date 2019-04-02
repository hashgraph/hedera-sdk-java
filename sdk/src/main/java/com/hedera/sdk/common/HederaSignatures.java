package com.hedera.sdk.common;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.google.protobuf.ByteString;
import com.hederahashgraph.api.proto.java.SignatureMap;
import com.hederahashgraph.api.proto.java.SignaturePair;
import com.hederahashgraph.api.proto.java.Transaction;

public class HederaSignatures {

	private Hashtable<byte[], byte[]> signaturePairs  = new Hashtable<byte[], byte[]>();
	private int maxLength = 0; 
	
	public void addSignature(byte[] pubKey, byte[] signature) {
		this.signaturePairs.put(pubKey, signature);
		// probably not necessary, but pub keys could be encoded/non encoded resulting in different lengths
		if (pubKey.length > maxLength) {
			maxLength = pubKey.length;
		}
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
		int minSize = 0;
        SignaturePair.Builder signaturePair = SignaturePair.newBuilder();
		
		if (this.signaturePairs.size() > 1) {
			// reduce public keys
			for (int keySize = maxLength - 1; keySize > 0; keySize--) {
				// loop through key size in reducing order
				Hashtable<byte[], String> reducedPairs = new Hashtable<byte[], String>();
				// loop through keys
				Iterator it = this.signaturePairs.entrySet().iterator();
//				System.out.println("");
//				System.out.println(keySize);
//				System.out.println("");
			    while (it.hasNext()) {
			        Map.Entry pair = (Map.Entry)it.next();
//			        System.out.println(pair.getKey() + " => " + pair.getKey().toString().substring(0, keySize));
			        reducedPairs.put(Arrays.copyOfRange((byte[]) pair.getKey(), 0, keySize), "");
			    }
		    	minSize = keySize;
			    if (reducedPairs.size() != this.signaturePairs.size()) {
			    	minSize = keySize + 1;
			    	break;
			    }
			}
		}
		Iterator it = this.signaturePairs.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        byte[] signature = (byte[])pair.getValue();
	        if (signature.length == 0) {
	        	// 0 length signature is a contract
	        	signaturePair.setContract(ByteString.copyFrom(signature));
	        } else {
	        	// it's ED25519
	        	signaturePair.setEd25519(ByteString.copyFrom(signature));
	        }
	        // add the reduced public key
	        byte[] key = new byte[0]; 
	        if (minSize != 0) {
	        	key = Arrays.copyOfRange((byte[]) pair.getKey(), 0, minSize);
	        }
        	signaturePair.setPubKeyPrefix(ByteString.copyFrom(key));
	        signatureMap.addSigPair(signaturePair);
	    }
		
		return signatureMap.build();
	}
	public Transaction transactionAddSignatures(Transaction transaction) {
		
		Transaction.Builder newTransaction = Transaction.newBuilder();
		newTransaction.setBody(transaction.getBody());
		newTransaction.setBodyBytes(transaction.getBodyBytes());

		int minSize = 0;
		
		if (this.signaturePairs.size() > 1) {
			// reduce public keys
			for (int keySize = maxLength - 1; keySize > 0; keySize--) {
				// loop through key size in reducing order
				Hashtable<byte[], String> reducedPairs = new Hashtable<byte[], String>();
				// loop through keys
				Iterator it = this.signaturePairs.entrySet().iterator();
//				System.out.println("");
//				System.out.println(keySize);
//				System.out.println("");
			    while (it.hasNext()) {
			        Map.Entry pair = (Map.Entry)it.next();
//			        System.out.println(pair.getKey() + " => " + pair.getKey().toString().substring(0, keySize));
			        reducedPairs.put(Arrays.copyOfRange((byte[]) pair.getKey(), 0, keySize), "");
			    }
		    	minSize = keySize;
			    if (reducedPairs.size() != this.signaturePairs.size()) {
			    	minSize = keySize + 1;
			    	break;
			    }
			}
		}
    	SignatureMap.Builder signatureMap = SignatureMap.newBuilder();
    	// add the existing signatures
    	for (SignaturePair sigPair: newTransaction.getSigMap().getSigPairList()) {
    		signatureMap.addSigPair(sigPair);
    	}

    	SignaturePair.Builder signaturePair = SignaturePair.newBuilder();

		Iterator it = this.signaturePairs.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        byte[] signature = (byte[])pair.getValue();
	        if (signature.length == 0) {
	        	// 0 length signature is a contract
	        	signaturePair.setContract(ByteString.copyFrom(signature));
	        } else {
	        	// it's ED25519
	        	signaturePair.setEd25519(ByteString.copyFrom(signature));
	        }
	        // add the reduced public key
	        byte[] key = new byte[0]; 
	        if (minSize != 0) {
	        	key = Arrays.copyOfRange((byte[]) pair.getKey(), 0, minSize);
	        }
        	signaturePair.setPubKeyPrefix(ByteString.copyFrom(key));
	        signatureMap.addSigPair(signaturePair);
	    }
	    newTransaction.setSigMap(signatureMap);
		
		return newTransaction.build();
	}
}
