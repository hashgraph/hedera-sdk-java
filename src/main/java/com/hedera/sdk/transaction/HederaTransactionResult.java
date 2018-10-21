package com.hedera.sdk.transaction;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.common.HederaPrecheckResult;
import com.hedera.sdk.common.HederaTransactionID;
import com.hederahashgraph.api.proto.java.NodeTransactionPrecheckCode;

/* 
 * Holds the response from a transaction call, including the transactionID values necessary to get the transaction ID
 * for subsequent queries (this is useful if the transaction ID was generated inside the sdk).
 */
public class HederaTransactionResult implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final Logger logger = LoggerFactory.getLogger(HederaTransactionResult.class);
	private HederaPrecheckResult precheckResult = HederaPrecheckResult.OK;
	/**
	 * the {@link HederaTransactionID} for this transaction 
	 */
	public HederaTransactionID hederaTransactionID = new HederaTransactionID();
	/**
	 * Sets the precheck result of the transaction request
	 * @param nodeTransactionPrecheckCode the precheck code from the node
	 */
	public void setPrecheckResult (NodeTransactionPrecheckCode nodeTransactionPrecheckCode) {
	   	logger.trace("Start - setPrecheckResult nodeTransactionPrecheckCode{}", nodeTransactionPrecheckCode);
		switch (nodeTransactionPrecheckCode) {
		case DUPLICATE:
			this.precheckResult = HederaPrecheckResult.DUPLICATE;
			break;
		case INSUFFICIENT_BALANCE:
			this.precheckResult = HederaPrecheckResult.INSUFFICIENT_BALANCE;
			break;
		case INSUFFICIENT_FEE:
			this.precheckResult = HederaPrecheckResult.INSUFFICIENT_FEE;
			break;
		case INVALID_ACCOUNT:
			this.precheckResult = HederaPrecheckResult.INVALID_ACCOUNT;
			break;
		case INVALID_TRANSACTION:
			this.precheckResult = HederaPrecheckResult.INVALID_TRANSACTION;
			break;
		case OK:
			this.precheckResult = HederaPrecheckResult.OK;
			break;
		case BUSY:
			this.precheckResult = HederaPrecheckResult.BUSY;
			break;
		case NOT_SUPPORTED:
		   	logger.trace("End - setPrecheckResult");
            throw new IllegalArgumentException("Precheck Response not recognized. You may be using an old sdk.");			
		}
	   	logger.trace("End - setPrecheckResult");
	}
	/**
	 * Sets the transaction result in error
	 */
	public void setError() {
	   	logger.trace("setError");
		this.precheckResult = HederaPrecheckResult.ERROR;
	}
	/**
	 * Returns the {@link HederaPrecheckResult}
	 * @return {@link HederaPrecheckResult} 
	 */
	public HederaPrecheckResult getPrecheckResult() {
	   	logger.trace("getPrecheckResult");
		return precheckResult;
	}
	/**
	 * true if the transaction request was successful
	 * note: this tests for precheckResult == OK, it doesn't confirm that the transaction overall
	 * has reached consensus, a query is necessary for this.
	 * @return {@link Boolean}
	 */
	public boolean success() {
	   	logger.trace("success");
		return (this.precheckResult == HederaPrecheckResult.OK);
	}
	/**
	 * A string representation of the error that occurred if any
	 * returns "OK" if the transaction request was successful
	 * @return {@link String}
	 */
	public String errorText() {
		logger.trace("Start - errorText");
		switch (this.precheckResult) {
		case DUPLICATE:
		   	logger.trace("End - errorText");
			return "DUPLICATE TRANSACTION";
		case INSUFFICIENT_BALANCE:
		   	logger.trace("End - errorText");
			return "INSUFFICIENT BALANCE";
		case INSUFFICIENT_FEE:
		   	logger.trace("End - errorText");
			return "INSUFFICIENT FEE";
		case INVALID_ACCOUNT:
		   	logger.trace("End - errorText");
			return "INVALID ACCOUNT";
		case INVALID_TRANSACTION:
		   	logger.trace("End - errorText");
			return "INVALID TRANSACTION";
		case ERROR:
		   	logger.trace("End - errorText");
			return "ERROR";
		default:
		   	logger.trace("End - errorText");
			return "OK";
		}
	}
}
