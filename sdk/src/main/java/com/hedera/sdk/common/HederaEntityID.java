package com.hedera.sdk.common;

import java.io.Serializable;
import org.slf4j.LoggerFactory;
import com.hedera.sdk.account.HederaClaim;
import com.hederahashgraph.api.proto.java.EntityID;

/**
 * The ID for a single entity (account, claim, file, or smart contract instance)
 * either one of accountID, claim, fileID or contractID will be set, others will be null
 */
public class HederaEntityID implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(EntityID.class);
	private static final long serialVersionUID = 1;

	/**
	 * the HederaAccountID, initialises with null
	 */
	public HederaAccountID accountID = null;
	/**
	 * the HederaClaim, initialises with null
	 */
	public HederaClaim claim = null;
	/** 
	 * the HederaFileId, initialises with null
	 */
	public HederaFileID fileID = null;
	/**
	 * the HederaContractID, initialises with null
	 */
	public HederaContractID contractID = null;

	/**
	 * Default constructor for a HederaEntityId
	 * with default values
	 */
	public HederaEntityID() {


	}
	/**
	 * Construct from an EntityID protobuf stream
	 * @param entityIDProtobuf the protofbuf to generate the object from
	 */
	public HederaEntityID(EntityID entityIDProtobuf) {

	   	
	   	this.accountID = null;
	   	this.claim = null;
	   	this.contractID = null;
	   	this.fileID = null;
	   	
	   	if (entityIDProtobuf.hasAccountID()) {
	   		this.accountID = new HederaAccountID(entityIDProtobuf.getAccountID());
	   	}
	   	if (entityIDProtobuf.hasClaim()) {
	   		this.claim = new HederaClaim(entityIDProtobuf.getClaim());
	   	}
	   	if (entityIDProtobuf.hasContractID()) {
	   		this.contractID = new HederaContractID(entityIDProtobuf.getContractID());
	   	}
	   	if (entityIDProtobuf.hasFileID()) {
	   		this.fileID = new HederaFileID(entityIDProtobuf.getFileID());
	   	}


	}
}