package com.hedera.sdk.common;

import java.io.Serializable;
import org.slf4j.LoggerFactory;
import com.google.protobuf.ByteString;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hederahashgraph.api.proto.java.Signature;
/**
 * A Signature corresponding to a Key. It is a sequence of bytes holding a public key signature from one of the three supported 
 * systems (ed25519, RSA-3072, ECDSA with p384). Or, it can be a list of signatures corresponding to a single threshold key. 
 * Or, it can be the ID of a smart contract instance, which is authorized to act as if it had a key. If an account has an ed25519 key associated with it, 
 * then the corresponding private key must sign any transaction to transfer cryptocurrency out of it. 
 * If it has a smart contract ID associated with it, then that smart contract is allowed to transfer cryptocurrency out of it. 
 * The smart contract doesn't actually have a key, and doesn't actually sign a transaction. 
 * But it's as if a virtual transaction were created, and the smart contract signed it with a private key. 
 * A key can also be a "threshold key", which means a list of M keys, any N of which must sign in order for the threshold signature to be considered valid. 
 * The keys within a threshold signature may themselves be threshold signatures, to allow complex signature requirements 
 * (this nesting is not supported in the currently, but will be supported in a future version of API). 
 * If a Signature message is missing the "signature" field, then this is considered to be a null signature. 
 * That is useful in cases such as threshold signatures, where some of the signatures can be null. 
 * The definition of Key uses mutual recursion, so it allows nesting that is arbitrarily deep. 
 * But the current API only accepts Key messages up to 3 levels deep, such as a list of threshold keys, 
 * each of which is a list of primitive keys. Therefore, the matching Signature will have the same limitation. 
 * This restriction may be relaxed in future versions of the API, to allow deeper nesting. 
 */
public class HederaSignature implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaSignature.class);
	private static final long serialVersionUID = 1;
	private byte[] signature = new byte[0];
	private KeyType signatureType = KeyType.NOTSET;
	private HederaSignatureThreshold thresholdSignature = null;
	private HederaSignatureList signatureList = null;
	
	/**
	 * Default constructor
	 */
	public HederaSignature() {


	}
	/**
	 * Constructor from a {@link KeyType} and signature
	 * @param signatureType the type of the signature
	 * @param signature a byte[] containing the signature
	 */
	public HederaSignature(KeyType signatureType, byte[] signature) {

		this.signatureType = signatureType;
		if (signatureType == KeyType.CONTRACT) {
			this.signature = new byte[0];
		} else {
			this.signature = signature.clone();
		}

	}
	/**
	 * Constructor from a {@link HederaSignatureThreshold}
	 * @param thresholdSignature the threshold signature to create the signature from 
	 */
	public HederaSignature(HederaSignatureThreshold thresholdSignature) {

		this.signatureType = KeyType.THRESHOLD;
		this.thresholdSignature = thresholdSignature;

	}
	/**
	 * Constructor from a {@link HederaSignatureList}
	 * @param signatureList the signature list to create the signature from 
	 */
	public HederaSignature(HederaSignatureList signatureList) {

		this.signatureType = KeyType.LIST;
		this.signatureList = signatureList;

	}
	/**
	 * Constructor from a {@link Signature} protobuf
	 * @param signature signature to create the signature from 
	 */
	public HederaSignature(Signature signature) {

		// convert a protobuf payload into class data
		switch (signature.getSignatureCase()) {
		case CONTRACT:
			this.signature = signature.getContract().toByteArray();
			this.signatureType = KeyType.CONTRACT;
			break;
		case ED25519:
			this.signature = signature.getEd25519().toByteArray();
			this.signatureType = KeyType.ED25519;
			break;
//		case RSA_3072:
//			this.signature = signature.getRSA3072().toByteArray();
//			this.signatureType = KeyType.RSA3072;
//			break;
//		case ECDSA_384:
//			this.signature = signature.getECDSA384().toByteArray();
//			this.signatureType = KeyType.ECDSA384;
//			break;
		case THRESHOLDSIGNATURE:
			this.thresholdSignature = new HederaSignatureThreshold(signature.getThresholdSignature());
			this.signatureType = KeyType.THRESHOLD;
			break;
		case SIGNATURELIST:
			this.signatureList = new HederaSignatureList(signature.getSignatureList());
			this.signatureType = KeyType.LIST;
			break;
		case SIGNATURE_NOT_SET:
            throw new IllegalArgumentException("Signature not set in protobuf data.");			
		default:
            throw new IllegalArgumentException("Signature type unrecognized, you may be using an old sdk.");			
		}

	}
	/**
	 * Get the type of signature
	 * @return {@link KeyType}
	 */
	public KeyType getSignatureType() {

		return this.signatureType;
	}
	/** 
	 * Get the signature
	 * note: this may be null
	 * @return byte[]
	 */
	public byte[] getSignature() {

		return this.signature;
	}
	/**
	 * Get a {@link HederaSignatureThreshold}
	 * note: this may be null
	 * @return {@link HederaSignatureThreshold}
	 */
	public HederaSignatureThreshold getThresholdSignature() {

		return this.thresholdSignature;
	}
	/**
	 * Get a {@link HederaSignatureList}
	 * note: This may be null
	 * @return {@link HederaSignatureList}
	 */
	public HederaSignatureList getSignatureList() {

		return this.signatureList;
	}
	/**
	 * Get the {@link Signature} protobuf for this object
	 * @return {@link Signature}
	 */
	public Signature getProtobuf() {

		// Generates the protobuf payload for this class
		Signature.Builder signatureProtobuf = Signature.newBuilder();
		
		switch (this.signatureType) {
		case ED25519:
			if (this.signature != null) {
				signatureProtobuf.setEd25519(ByteString.copyFrom(this.signature));
			} else {
				signatureProtobuf.setEd25519(ByteString.copyFrom(new byte[0]));
			}
			break;
//		case RSA3072:
//			if (this.signature != null) {
//				signatureProtobuf.setRSA3072(ByteString.copyFrom(this.signature));
//			} else {
//				signatureProtobuf.setRSA3072(ByteString.copyFrom(new byte[0]));
//			}
//			break;
//		case ECDSA384:
//			if (this.signature != null) {
//				signatureProtobuf.setECDSA384(ByteString.copyFrom(this.signature));
//			} else {
//				signatureProtobuf.setECDSA384(ByteString.copyFrom(new byte[0]));
//			}
//			break;
		case CONTRACT:
			signatureProtobuf.setContract(ByteString.copyFrom(new byte[0]));
			break;
		case THRESHOLD:
			if (this.thresholdSignature != null) {
				signatureProtobuf.setThresholdSignature(this.thresholdSignature.getProtobuf());
			}
			break;
		case LIST:
			if (this.signatureList != null) {
				signatureProtobuf.setSignatureList(this.signatureList.getProtobuf());
			}
			break;
		case NOTSET:
            throw new IllegalArgumentException("Signature type not set, unable to generate data.");			
		}

		return signatureProtobuf.build();
	}
}
