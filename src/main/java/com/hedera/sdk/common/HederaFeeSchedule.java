package com.hedera.sdk.common;

import java.io.Serializable;

import com.hederahashgraph.api.proto.java.FeeComponents;


/**
 * The fee schedule for a specific hedera functionality and the time period this fee will be valid for the transaction
 */
public class HederaFeeSchedule implements Serializable {
	private static final long serialVersionUID = 1;

	/**
	 * 	the minimum fees that needs to be paid
	 */
	public long min	= 0;
	/**
	 * the maximum fees that can be submitted
	 */
	public long max	= 0;
	/**
	 * a constant determined by the business to calculate the fees
	 */
	public long constant = 0;
	/**
	 * bytes per transaction
	 */
	public long	bpt	= 0;	 	
	/**
	 * 	verifications per transaction
	 */
	public long vpt	= 0;
	/**
	 * 	ram byte seconds
	 */
	public long rbs	= 0;
	/**
	 * 	storage byte seconds
	 */
	public long sbs	= 0;
	/**
	 * ethereum gas
	 */
	public long gas	= 0;
	/**
	 * transaction value (crypto transfers amount, tv is in tiny bars divided by 1000, rounded down)
	 */
	public long tv = 0;
	/**
	 * bytes per response 
	 */
	public long bpr = 0;
	/**
	 * storage bytes per response
	 */
	public long sbpr = 0;	 	
	
	/**
	 * Default constructor, creates a HederaContractID with default values
	 */
	public HederaFeeSchedule() {
	}

	/**
	 * Generate a protobuf payload for this object
	 * @return a protobuf FeeComponents 
	 */
	public FeeComponents getProtobuf() {
		
		FeeComponents.Builder feeComponents = FeeComponents.newBuilder();
		
		feeComponents.setMin(this.min);
		feeComponents.setMax(this.max);
		feeComponents.setConstant(this.constant);
		feeComponents.setBpt(this.bpt);
		feeComponents.setVpt(this.vpt);
		feeComponents.setRbs(this.rbs);
		feeComponents.setSbs(this.sbs);
		feeComponents.setGas(this.gas);
		feeComponents.setTv(this.tv);
		feeComponents.setBpr(this.bpr);
		feeComponents.setSbpr(this.sbpr);

		return feeComponents.build();
	}
}