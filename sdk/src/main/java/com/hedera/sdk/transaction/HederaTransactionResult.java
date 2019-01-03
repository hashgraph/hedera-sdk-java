package com.hedera.sdk.transaction;

import java.io.Serializable;
import org.slf4j.LoggerFactory;
import com.hedera.sdk.common.HederaTransactionID;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

/* 
 * Holds the response from a transaction call, including the transactionID values necessary to get the transaction ID
 * for subsequent queries (this is useful if the transaction ID was generated inside the sdk).
 */
public class HederaTransactionResult implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaTransactionResult.class);
	private ResponseCodeEnum precheckResult = ResponseCodeEnum.OK;
	/**
	 * the {@link HederaTransactionID} for this transaction 
	 */
	public HederaTransactionID hederaTransactionID = new HederaTransactionID();

	/**
	 * Returns the {@link ResponseCodeEnum}
	 * @return {@link ResponseCodeEnum} 
	 */
	public ResponseCodeEnum getPrecheckResult() {

		return this.precheckResult;
	}
	/**
	 * Sets the precheckResult
	 * @param precheckResult the responseCode value to set
	 */
	public void setPrecheckResult(ResponseCodeEnum precheckResult) {

		this.precheckResult = precheckResult;
	}
	/**
	 * true if the transaction request was successful
	 * note: this tests for precheckResult == OK, it doesn't confirm that the transaction overall
	 * has reached consensus, a query is necessary for this.
	 * @return {@link Boolean}
	 */
	public boolean success() {

		return (this.precheckResult == ResponseCodeEnum.OK);
	}
}
