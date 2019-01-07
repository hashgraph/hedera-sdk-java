package com.hedera.sdk.contract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;
import com.google.protobuf.ByteString;
import com.hedera.sdk.common.HederaContractID;
import com.hederahashgraph.api.proto.java.ContractFunctionResult;

/**
 * The result returned by a call to a smart contract function. This is part of the response to a ContractCallLocal query, 
 * and is in the record for a ContractCall or ContractCreateInstance transaction. 
 * The ContractCreateInstance transaction record has the results of the call to the constructor.
 */
public class HederaContractFunctionResult implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaContractFunctionResult.class);
	private static final long serialVersionUID = 1;
	private HederaContractID contractID = new HederaContractID();
	private byte[] contractCallResult = new byte[0];
	private String errorMessage = "";
	private byte[] bloom = new byte[0];
	private long gasUsed = 0;
	private List<HederaContractLogInfo> contractLogInfo = new ArrayList<HederaContractLogInfo>();

	/**
	 * The {@link HederaContractID} to which these results relate
	 * @return {@link HederaContractID}
	 */
	public HederaContractID contractID() {
		return this.contractID;
	}

	/**
	 * the result of the contract function call in the solidity format
	 * @return byte[]
	 */
	public byte[] contractCallResult() {
		return this.contractCallResult;
	}

	/**
	 * An error message if present
	 * @return String
	 */
	public String errorMessage() {
		return this.errorMessage;
	}

	/**
	 * the amount of gas the function call consumed
	 * @return long
	 */
	public long gasUsed() {
		return this.gasUsed;
	}

	/**
	 * A list of {@link HederaContractLogInfo}
	 * @return List HederaContractLogInfo
	 */
	public List<HederaContractLogInfo> contractLogInfo() {
		return this.contractLogInfo;
	}

	/**
	 * the bloom filter for the record
	 * @return byte[]
	 */
	public byte[] bloom() {
		return this.bloom;
	}

	/**
	 * Default constructor
	 */
	public HederaContractFunctionResult() {


	}

	/**
	 * Construct from a {@link ContractFunctionResult} protobuf stream
	 * @param contractFunctionResultProtobuf the result of a contract function execution
	 */
	public HederaContractFunctionResult(ContractFunctionResult contractFunctionResultProtobuf) {

		this.contractID = new HederaContractID(contractFunctionResultProtobuf.getContractID());
		this.contractCallResult = contractFunctionResultProtobuf.getContractCallResult().toByteArray();
		this.errorMessage = contractFunctionResultProtobuf.getErrorMessage();
		this.bloom = contractFunctionResultProtobuf.getBloom().toByteArray();
		this.gasUsed = contractFunctionResultProtobuf.getGasUsed();
		this.contractLogInfo.clear();
		for (int i=0; i < contractFunctionResultProtobuf.getLogInfoCount(); i++) {
			HederaContractLogInfo contractLogInfo = new HederaContractLogInfo(contractFunctionResultProtobuf.getLogInfo(i));
			this.contractLogInfo.add(contractLogInfo);
		}

	}

	/**
	 * Generate a {@link ContractFunctionResult} protobuf payload for this object
	 * @return {@link ContractFunctionResult} 
	 */
	public ContractFunctionResult getProtobuf() {

	
		ContractFunctionResult.Builder contractFunctionResultProtobuf = ContractFunctionResult.newBuilder();
		
		contractFunctionResultProtobuf.setContractID(this.contractID.getProtobuf());
		contractFunctionResultProtobuf.setContractCallResult(ByteString.copyFrom(this.contractCallResult));
		contractFunctionResultProtobuf.setErrorMessage(this.errorMessage);
		contractFunctionResultProtobuf.setBloom(ByteString.copyFrom(this.bloom));
		contractFunctionResultProtobuf.setGasUsed(this.gasUsed);

		for (int i=0; i < this.contractLogInfo.size(); i++) {
			contractFunctionResultProtobuf.addLogInfo(this.contractLogInfo.get(i).getProtobuf());
		}
		
		return contractFunctionResultProtobuf.build();
	}
}