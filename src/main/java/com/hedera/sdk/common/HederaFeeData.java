package com.hedera.sdk.common;

import java.io.Serializable;
import com.hederahashgraph.api.proto.java.FeeData;

/**
 * The total fees charged for a trasansaction. It contains three parts namely nodedata, networkdata and servicedata
 */
public class HederaFeeData implements Serializable {
	private static final long serialVersionUID = 1;

	/**
	 * Fee charged by Node for this functionality
	 */
	public HederaFeeComponents nodeData = new HederaFeeComponents();
	/**
	 * Fee charged for network operations by Hedera
	 */
	public HederaFeeComponents networkData = new HederaFeeComponents();
	/**
	 * Fee charged for provding service by Hedera
	 */
	public HederaFeeComponents serviceData = new HederaFeeComponents();
	
	/**
	 * Default constructor, creates a HederaFeeData with default values
	 */
	public HederaFeeData() {
	}

	/**
	 * Generate a protobuf payload for this object
	 * @return a protobuf FeeData 
	 */
	public FeeData getProtobuf() {
		
		FeeData.Builder feeData = FeeData.newBuilder();
		
		feeData.setNetworkdata(this.networkData.getProtobuf());
		feeData.setNodedata(this.nodeData.getProtobuf());
		feeData.setServicedata(this.serviceData.getProtobuf());

		return feeData.build();
	}
}