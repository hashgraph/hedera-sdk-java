package com.hedera.sdk.common;

import java.io.Serializable;

import com.hederahashgraph.api.proto.java.HederaFunctionality;
import com.hederahashgraph.api.proto.java.TransactionFeeSchedule;


/**
 * The fees for a specific transaction or query based on the fee data
 */
public class HederaTransactionFeeSchedule implements Serializable {
	private static final long serialVersionUID = 1;

	/**
	 * 	the minimum fees that needs to be paid
	 */
	public HederaFunctionality hederaFunctionality = HederaFunctionality.UNRECOGNIZED;
	public HederaFeeData feeData = new HederaFeeData();
	
	/**
	 * Default constructor, creates a HederaContractID with default values
	 */
	public HederaTransactionFeeSchedule() {
	}

	/**
	 * Generate a protobuf payload for this object
	 * @return a protobuf TransactionFeeSchedule 
	 */
	public TransactionFeeSchedule getProtobuf() {
		
		TransactionFeeSchedule.Builder transactionFeeSchedule = TransactionFeeSchedule.newBuilder();
		
		transactionFeeSchedule.setHederaFunctionality(this.hederaFunctionality);
		transactionFeeSchedule.setFeeData(this.feeData.getProtobuf());

		return transactionFeeSchedule.build();
	}
}