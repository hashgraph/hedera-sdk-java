package com.hedera.sdk.query;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.transaction.HederaTransaction;
import com.hederahashgraph.api.proto.java.*;
/**
 * Class to manage setting up Query Headers
 *
 */
public class HederaQueryHeader implements Serializable {
	final Logger logger = LoggerFactory.getLogger(HederaQuery.class);
	private static final long serialVersionUID = 1;
	/**
	 * Allowed types of responses required by the query
	 */
	public enum QueryResponseType {
		ANSWER_ONLY,
		ANSWER_STATE_PROOF,
		COST_ANSWER,
		COST_ANSWER_STATE_PROOF,
		NOTSET
	}
	/**
	 * The payment for the query
	 */
	public HederaTransaction payment = null;
	/**
	 * The response type requested for the query
	 */
	public QueryResponseType responseType = QueryResponseType.ANSWER_ONLY;

	/**
	 * Default constructor 
	 */
	public HederaQueryHeader() {
	   	logger.trace("Start - Object init");
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructor with payment and response type
	 * @param payment {@link HederaTransaction}
	 * @param responseType {@link QueryResponseType}
	 */
	public HederaQueryHeader(HederaTransaction payment, QueryResponseType responseType) {
	   	logger.trace("Start - Object init payment {}, responseType {}", payment, responseType);
		this.payment = payment;
		this.responseType = responseType;
	   	logger.trace("End - Object init");
	}
	/**
	 * returns the protobuf for a {@link QueryHeader}
	 * @return {@link QueryHeader}
	 */
	public QueryHeader getProtobuf() {
	   	logger.trace("Start - getProtobuf");
		// Generates the protobuf payload for this class
		QueryHeader.Builder queryHeader = QueryHeader.newBuilder();

		if (this.payment != null) {
			queryHeader.setPayment(this.payment.getProtobuf());
		}
		switch (this.responseType) {
			case ANSWER_ONLY:
				queryHeader.setResponseType(ResponseType.ANSWER_ONLY);
				break;
			case ANSWER_STATE_PROOF:
				queryHeader.setResponseType(ResponseType.ANSWER_STATE_PROOF);
				break;
			case COST_ANSWER:
				queryHeader.setResponseType(ResponseType.COST_ANSWER);
				break;
			case COST_ANSWER_STATE_PROOF:
				queryHeader.setResponseType(ResponseType.COST_ANSWER_STATE_PROOF);
				break;
			case NOTSET:
	    	   	logger.trace("End - getProtobuf");
	            throw new IllegalArgumentException("Response type not set. Unable to generate data.");			
		}
	   	logger.trace("End - getProtobuf");
		
		return queryHeader.build();
	}
}
	