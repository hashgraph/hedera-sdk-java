package com.hedera.sdk.common;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.google.protobuf.ByteString;
import com.hederahashgraph.api.proto.java.SignatureMap;
import com.hederahashgraph.api.proto.java.SignaturePair;

public class HederaSignatures {

	private Hashtable<String, byte[]> signaturePairs  = new Hashtable<String, byte[]>();
	private int maxLength = 0; 
	
	public void addSignature(String pubKey, byte[] signature) {
		this.signaturePairs.put(pubKey, signature);
		// probably not necessary, but pub keys could be encoded/non encoded resulting in different lenghts
		if (pubKey.length() > maxLength) {
			maxLength = pubKey.length();
		}
	}

	public byte[] getSignature(String pubKey) throws Exception {
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
				Hashtable<String, String> reducedPairs = new Hashtable<String, String>();
				// loop through keys
				Iterator it = this.signaturePairs.entrySet().iterator();
//				System.out.println("");
//				System.out.println(keySize);
//				System.out.println("");
			    while (it.hasNext()) {
			        Map.Entry pair = (Map.Entry)it.next();
//			        System.out.println(pair.getKey() + " => " + pair.getKey().toString().substring(0, keySize));
			        reducedPairs.put(pair.getKey().toString().substring(0, keySize), "");
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
	        if (minSize == 0) {
	        	signaturePair.setPubKeyPrefix(ByteString.copyFrom(new byte[0]));
	        } else {
	        	signaturePair.setPubKeyPrefix(ByteString.copyFromUtf8(pair.getKey().toString().substring(0, minSize)));
	        }
	        signatureMap.addSigPair(signaturePair);
	    }
		
		return signatureMap.build();
	}
}
