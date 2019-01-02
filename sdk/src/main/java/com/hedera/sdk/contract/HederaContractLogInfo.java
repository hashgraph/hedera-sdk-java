package com.hedera.sdk.contract;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.hedera.sdk.common.HederaContractID;
import com.hederahashgraph.api.proto.java.ContractLoginfo;

/**
 * The log information for an event returned by a smart contract function call. One function call may return several such events
 */
public class HederaContractLogInfo implements Serializable {
	final static Logger logger = LoggerFactory.getLogger(HederaContractLogInfo.class);
	private static final long serialVersionUID = 1;
	private HederaContractID contractID = new HederaContractID();
	private byte[] bloom = new byte[0];
	private byte[][] topics = new byte[0][0];
	private byte[] data = new byte[0];

	/**
	 * The {@link HederaContractID} to which these results relate
	 * @return {@link HederaContractID}
	 */
	public HederaContractID contractID() {
		return this.contractID;
	}

	/**
	 * the bloom filter for the log
	 * @return byte[]
	 */
	public byte[] bloom() {
		return this.bloom;
	}

	/**
	 * topics of a particular event
	 * @return byte[][]
	 */
	public byte[][] topics() {
		return this.topics;
	}

	/**
	 * event data
	 * @return byte[]
	 */
	public byte[] data() {
		return this.data;
	}

	/**
	 * Default constructor
	 */
	public HederaContractLogInfo() {
	   	logger.trace("Start - Object init");
	   	logger.trace("End - Object init");
	}

	/**
	 * Construct from a {@link ContractLoginfo} protobuf stream
	 * @param contractLogInfoProtobuf the contract log information in protobuf
	 */
	public HederaContractLogInfo(ContractLoginfo contractLogInfoProtobuf) {
		logger.trace("Start - Object init in contractLogInfoProtobuf {}", contractLogInfoProtobuf);
		this.contractID = new HederaContractID(contractLogInfoProtobuf.getContractID());
		this.bloom = contractLogInfoProtobuf.getBloom().toByteArray();
		this.data = contractLogInfoProtobuf.getData().toByteArray();
		this.topics = new byte[contractLogInfoProtobuf.getTopicCount()][0];
		for (int i=0; i < contractLogInfoProtobuf.getTopicCount(); i++) {
			this.topics[i] = contractLogInfoProtobuf.getTopic(i).toByteArray();
		}
		logger.trace("End - Object init");
	}

	/**
	 * Generate a {@link ContractLoginfo} protobuf payload for this object
	 * @return {@link ContractLoginfo} 
	 */
	public ContractLoginfo getProtobuf() {
		logger.trace("Start - getProtobuf");
	
		ContractLoginfo.Builder contractLogInfo = ContractLoginfo.newBuilder();
		
		contractLogInfo.setContractID(this.contractID.getProtobuf());
		contractLogInfo.setBloom(ByteString.copyFrom(this.bloom));
		contractLogInfo.setData(ByteString.copyFrom(this.data));
		for (int i=0; i < this.topics.length; i++) {
			contractLogInfo.addTopic(ByteString.copyFrom(this.topics[i]));
		}
		
		return contractLogInfo.build();
	}
}