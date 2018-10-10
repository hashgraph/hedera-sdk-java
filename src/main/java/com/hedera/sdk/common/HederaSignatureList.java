package com.hedera.sdk.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hederahashgraph.api.proto.java.Signature;
import com.hederahashgraph.api.proto.java.SignatureList;;
/**
 * The signatures corresponding to a KeyList of the same length
 *
 */
public class HederaSignatureList implements Serializable {
	final Logger logger = LoggerFactory.getLogger(HederaSignatureList.class);
	private static final long serialVersionUID = 1;
	/**
	 * the List of {@link HederaSignature}
	 * empty to start with
	 */
	public List<HederaSignature> signatures  = new ArrayList<HederaSignature>();
	/**
	 * Default constructor
	 */
	public HederaSignatureList() {
	   	logger.trace("Start - Object init");
	   	logger.trace("End - Object init");
	}
	/**
	 * Construct from a List of {@link HederaSignature}
	 * @param signatures the signatures to construct from
	 */
	public HederaSignatureList(List<HederaSignature> signatures) {
	   	logger.trace("Start - Object init signatures {}", signatures);
		for (HederaSignature hederaSignature : signatures) {
			this.signatures.add(hederaSignature);
		}
	   	logger.trace("End - Object init");
	}
	/**
	 * Construct from a {@link SignatureList} protobuf
	 * @param protobuf signatures in protobuf format
	 */
	public HederaSignatureList(SignatureList protobuf) {
	   	logger.trace("Start - Object init protobuf {}", protobuf);
		// convert a protobuf payload into class data
		this.signatures.clear();
		
		for (Signature signature : protobuf.getSigsList()) {
			this.signatures.add(new HederaSignature(signature));
		}
	   	logger.trace("End - Object init");
	}
	/**
	 * Get the {@link SignatureList} protobuf for this object
	 * @return {@link SignatureList}
	 */
	public SignatureList getProtobuf() {
	   	logger.trace("Start - getProtobuf");
		// Generates the protobuf payload for this class
		SignatureList.Builder signatureListProtobuf = SignatureList.newBuilder();
		for (HederaSignature signature : this.signatures) {
			signatureListProtobuf.addSigs(signature.getProtobuf());
		}
	   	logger.trace("End - getProtobuf");
		return signatureListProtobuf.build();
	}
	/**
	 * Add a {@link HederaSignature} to the list
	 * @param signature {@link HederaSignature}
	 */
	public void addSignature(HederaSignature signature) {
	   	logger.trace("Start - addSignature signature {}", signature);
		this.signatures.add(signature);
	   	logger.trace("End - addSignature");
	}
	/** 
	 * Delete a {@link HederaSignature} from the list
	 * @param signature {@link HederaSignature}
	 * @return true if found and deleted
	 */
	public boolean deleteSignature(HederaSignature signature) {
	   	logger.trace("deleteSignature signature {}", signature);
		return this.signatures.remove(signature);
	}
}
