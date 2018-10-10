package com.hedera.sdk.common;

import com.hedera.sdk.common.HederaSignature;
import com.hederahashgraph.api.proto.java.Signature;
import com.hederahashgraph.api.proto.java.SignatureList;
import com.hederahashgraph.api.proto.java.ThresholdSignature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A signature corresponding to a ThresholdKey. For an N-of-M threshold key,
 * this is a list of M signatures, at least N of which must be non-null.
 *
 */
public class HederaSignatureThreshold implements Serializable {
	final Logger logger = LoggerFactory.getLogger(HederaSignatureThreshold.class);
	private static final long serialVersionUID = 1;
	/**
	 * The list of {@link HederaSignature} initially empty
	 */
	public List<HederaSignature> signatures = new ArrayList<HederaSignature>();
	/**
	 * Default constructor
	 */
	public HederaSignatureThreshold() {
		logger.trace("Start - Object init");
		logger.trace("End - Object init");
	}
	/**
	 * Constructs from a List of {@link HederaSignature}
	 * @param signatures List of {@link HederaSignature}
	 */
	public HederaSignatureThreshold(List<HederaSignature> signatures) {

   	logger.trace("Start - Object init signatures {}", signatures);
		
		for (HederaSignature hederaSignature : signatures) {
			this.signatures.add(hederaSignature);
		}
		logger.trace("End - Object init");
	}
	/**
	 * Constructs from a {@link ThresholdSignature} protobuf
	 * @param thresholdSignature threshold signature in protobuf
	 */
	public HederaSignatureThreshold(ThresholdSignature thresholdSignature) {
		logger.trace("Start - Object init protobuf {}", thresholdSignature);
		// convert a protobuf payload into class data
		this.signatures.clear();
		
		SignatureList protoSigs = thresholdSignature.getSigs();

		for (Signature signature : protoSigs.getSigsList()) {
			this.signatures.add(new HederaSignature(signature));
		}
		logger.trace("End - Object init");
	}
	/**
	 * Gets the {@link ThresholdSignature} protobuf for this object
	 * @return {@link ThresholdSignature}
	 */
	public ThresholdSignature getProtobuf() {
		logger.trace("Start - getProtobuf");
		// Generates the protobuf payload for this class
		ThresholdSignature.Builder signaturesProtobuf = ThresholdSignature.newBuilder();
		SignatureList protoKeyList;
		
		if (!this.signatures.isEmpty()) {
			protoKeyList = Utilities.getProtoSignatureList(this.signatures);
			signaturesProtobuf.setSigs(protoKeyList);
		} else {
			logger.trace("End - getKeyProtobuf: return NULL");
			return null;
		}
		
		logger.trace("End - getProtobuf");
		return signaturesProtobuf.build();
	}
	/**
	 * Adds a {@link HederaSignature} to the list
	 * @param signature the signature to delete
	 */
	public void addSignature(HederaSignature signature) {
		logger.trace("Start - addSignature signature {}", signature);
		this.signatures.add(signature);
		logger.trace("End - addSignature");
	}
	/**
	 * Deletes a {@link HederaSignature} from the list
	 * @param signature the signature to delete
	 * @return true if found and deleted
	 */
	public boolean deleteSignature(HederaSignature signature) {
		logger.trace("deleteSignature signature {}", signature);
		return this.signatures.remove(signature);
	}
}
