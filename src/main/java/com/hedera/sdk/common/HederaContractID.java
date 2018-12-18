package com.hedera.sdk.common;

import java.io.Serializable;
import org.json.simple.JSONObject;
import org.slf4j.LoggerFactory;

import com.hederahashgraph.api.proto.java.ContractID;


/**
 * The ID for a smart contract, it is composed
 * of a shard number, a realm number and a contract number
 */
public class HederaContractID implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaContractID.class);
	private static String JSON_SHARDNUM = "shardNum";
	private static String JSON_REALMNUM = "realmNum";
	private static String JSON_CONTRACTNUM = "contractNum";
	private static final long serialVersionUID = 1;

	/**
	 * the shard number (nonnegative)
	 */
	public long shardNum = 0;

	/**
	 * the realm number
	 * note: if set to -1, in future versions it will be automatically assigned by the Hedera network
	 */
	public long realmNum = 0;

	/**
	 * contract number (a nonnegative number unique within its realm)
	 */
	public long contractNum = 0;
	
	/**
	 * Default constructor, creates a HederaContractID with default values
	 */
	public HederaContractID() {
	}

	/**
	 * Constructor for a HederaContractID from specified parameter values
	 * @param shardNum the shard number
	 * @param realmNum the realm number
	 * @param contractNum the contract number (non negative and unique within its realm)
	 */
	public HederaContractID(long shardNum, long realmNum, long contractNum) {
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.contractNum = contractNum;
	}

	/**
	 * Constructor for a HederaContractID from protobuf
	 * @param contractIDProtobuf the protobuf from which to create the contract ID
	 */
	public HederaContractID(ContractID contractIDProtobuf) {
		this.shardNum = contractIDProtobuf.getShardNum();
		this.realmNum = contractIDProtobuf.getRealmNum();
		this.contractNum = contractIDProtobuf.getContractNum();
	}

	/**
	 * Generate a protobuf payload for this object
	 * @return a protobuf ContractID 
	 */
	public ContractID getProtobuf() {
		
		ContractID.Builder contractID = ContractID.newBuilder();
		
		contractID.setShardNum(this.shardNum);
		if (this.realmNum != -1) {
			contractID.setRealmNum(this.realmNum);
		}
		contractID.setContractNum(this.contractNum);

		return contractID.build();
	}

	/**
	 * Generate a JSON object from the HederaContractID
	 * @return a JSONObject
	 */
	@SuppressWarnings("unchecked")
	public JSONObject JSON() {
	   	JSONObject jsonContract = new JSONObject();

	   	jsonContract.put(JSON_SHARDNUM, this.shardNum);
	   	jsonContract.put(JSON_REALMNUM, this.realmNum);
	   	jsonContract.put(JSON_CONTRACTNUM, this.contractNum);

		return jsonContract;
	}
	
	/**
	 * Gets a JSON representation of the HederaContractID as a string
	 * @return a String
	 */
	public String JSONString() {
		return JSON().toJSONString();
	}

	/**
	 * Sets the HederaContractID properties from a JSONObject representation
	 * @param jsonContract JSONObject representing a HederaContractID
	 */
	public void fromJSON(JSONObject jsonContract) {
		
		if (jsonContract.containsKey(JSON_SHARDNUM)) {
			this.shardNum = (Long) jsonContract.get(JSON_SHARDNUM);
		} else {
			this.shardNum = 1;
		}
		if (jsonContract.containsKey(JSON_REALMNUM)) {
			this.realmNum = (Long) jsonContract.get(JSON_REALMNUM);
		} else {
			this.realmNum = 1;
		}
		if (jsonContract.containsKey(JSON_CONTRACTNUM)) {
			this.contractNum = (Long) jsonContract.get(JSON_CONTRACTNUM);
		} else {
			this.contractNum = 1;
		}
	}
}