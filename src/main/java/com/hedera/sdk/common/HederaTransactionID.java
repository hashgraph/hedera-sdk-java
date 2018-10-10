package com.hedera.sdk.common;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hederahashgraph.api.proto.java.TransactionID;

/**
 * The ID for a transaction. This is used for retrieving receipts and records for a transaction, for appending to a file right after creating it, 
 * for instantiating a smart contract with bytecode in a file just created, and internally by the network for detecting when duplicate 
 * transactions are submitted. A user might get a transaction processed faster by submitting it to N nodes, each with a different node account, 
 * but all with the same TransactionID. Then, the transaction will take effect when the first of all those nodes submits the transaction and it 
 * reaches consensus. The other transactions will not take effect. So this could make the transaction take effect faster, if any given node 
 * might be slow. However, the full transaction fee is charged for each transaction, so the total fee is N times as much if the transaction 
 * is sent to N nodes.
 */
public class HederaTransactionID implements Serializable {
	final Logger logger = LoggerFactory.getLogger(HederaTimeStamp.class);
	private static final long serialVersionUID = 1;

	/**
	 * The {@link HederaAccountID} for this {@link HederaTransactionID}
	 */
	public HederaAccountID accountID = new HederaAccountID();
	/**
	 * The {@link HederaTimeStamp} for this {@link HederaTransactionID}
	 * defaults to now
	 */
	public HederaTimeStamp transactionValidStart = new HederaTimeStamp();

	/**
	 * Default constructor
	 */
	public HederaTransactionID() {
	   	logger.trace("Start - Object init");
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructs from a {@link HederaAccountID}
	 * transactionValidStart defaults to now
	 * @param accountID the account ID to initialise the object with
	 */
	public HederaTransactionID(HederaAccountID accountID) {
	   	logger.trace("Start - Object init accountID {}", accountID);
		this.accountID = accountID;
		// timestamp defaults to now if not specified
		this.transactionValidStart = new HederaTimeStamp();
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructs from a {@link HederaAccountID} and {@link HederaTimeStamp}
	 * @param accountID the account ID to initialise the object with
	 * @param transactionValidStart the transaction valid start date/time
	 */
	public HederaTransactionID(HederaAccountID accountID, HederaTimeStamp transactionValidStart) {
	   	logger.trace("Start - Object init accountID {}, transactionValidStart {}", accountID, transactionValidStart);
		this.accountID = accountID;
		this.transactionValidStart = transactionValidStart;
	   	logger.trace("End - Object init");
	}

	/**
	 * Construct from a {@link HederaTransactionID} protobuf stream
	 * @param transactionIDProtobuf the transactinID in protobuf format
	 */
	public HederaTransactionID(TransactionID transactionIDProtobuf) {
	   	logger.trace("Start - Object init transactionIDProtobuf {}", transactionIDProtobuf);
		this.transactionValidStart = new HederaTimeStamp(transactionIDProtobuf.getTransactionValidStart());
		this.accountID = new HederaAccountID(transactionIDProtobuf.getAccountID());
	   	logger.trace("End - Object init");
	}

	/**
	 * Generate a {@link HederaTransactionID} protobuf payload for this object 
	 * @return {@link TransactionID}
	 */
	public TransactionID getProtobuf() {
	   	logger.trace("Start - getProtobuf");
		TransactionID.Builder transactionID = TransactionID.newBuilder();
		
		transactionID.setAccountID(this.accountID.getProtobuf());
		transactionID.setTransactionValidStart(this.transactionValidStart.getProtobuf());
		
	   	logger.trace("End - getProtobuf");
		return transactionID.build();
	}
}