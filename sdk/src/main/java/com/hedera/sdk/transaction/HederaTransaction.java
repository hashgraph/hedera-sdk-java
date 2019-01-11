package com.hedera.sdk.transaction;

import java.io.Serializable;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.account.HederaAccountAmount;
import com.hedera.sdk.common.HederaTransactionRecord;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.node.HederaNode;
import com.hedera.sdk.common.HederaSignature;
import com.hedera.sdk.common.HederaSignatureList;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.common.HederaTransactionID;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.query.HederaQuery;
import com.hedera.sdk.query.HederaQueryHeader;
import com.hedera.sdk.query.HederaQuery.QueryType;
import com.hedera.sdk.query.HederaQueryHeader.QueryResponseType;
import com.hedera.sdk.transaction.HederaTransactionBody.TransactionType;
import com.hederahashgraph.api.proto.java.*;
/**
 * Class to handle transactions on Hedera Hashgraph
 *
 */
public class HederaTransaction implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaTransaction.class);
	private static final long serialVersionUID = 1;
	private ResponseCodeEnum precheckResult = ResponseCodeEnum.UNKNOWN;
	private long cost = 0;
	private byte[] stateProof = new byte[0];
	private HederaNode node = null;
	private HederaTransactionReceipt transactionReceipt = null;
	private HederaTransactionRecord transactionRecord = null;
	
	/**
	 * get or set the body ({@link HederaTransactionBody} of the transaction
	 */
	public HederaTransactionBody body = new HederaTransactionBody();
	/**
	 * get or set the signatures for this transaction as a {@link HederaSignatureList}
	 * note: if keySignatureList is not null, this signatureList will be ignored, keySignatureList takes priority
	 */
	public HederaSignatureList signatureList = null;

	/**
	 * sets the node object to use for communication with the node
	 * @param node the node to communicate with
	 */
	public void setNode (HederaNode node) {
		this.node = node;
	}
	/**
	 * returns the {@link HederaTransactionReceipt}
	 * note: can be null
	 * @return {@link HederaTransactionReceipt}
	 */
	public HederaTransactionReceipt transactionReceipt() {
		return this.transactionReceipt;
	}
	/**
	 * returns the {@link HederaTransactionRecord}
	 * note: can be null
	 * @return {@link HederaTransactionRecord}
	 */
	public HederaTransactionRecord transactionRecord() {
		return this.transactionRecord;
	}
	/**
	 * gets the cost of running the transaction
	 * @return long
	 */
	public long getCost() {
		return this.cost;
	}
	/**
	 * gets the stateproof for the transaction if requested
	 * @return byte[]
	 */
	public byte[] getStateProof() {
		return this.stateProof;
	}
	
	/**
	 * Default constructor
	 */
	public HederaTransaction() {


	}
	/**
	 * Constructs from a {@link HederaTransactionBody} body and {@link HederaSignatureList} signatures
	 * @param body {@link HederaTransactionBody}
	 * @param sigs {@link HederaSignatureList}
	 */
	public HederaTransaction(HederaTransactionBody body, HederaSignatureList sigs) {

		this.body = body;
		this.signatureList = sigs;

	}
	/**
	 * returns the protobuf for this transaction
	 * @return {@link Transaction}
	 */
	public Transaction getProtobuf() {

		// Generates the protobuf payload for this class
		Transaction.Builder transactionProtobuf = Transaction.newBuilder();
		
		transactionProtobuf.setBody(this.body.getProtobuf());
		// if we have key signature pairs, use these\
		transactionProtobuf.setSigs(this.signatureList.getProtobuf());

		
		return transactionProtobuf.build();
	}
	/**
	 * Adds a signature to the list
	 * @param signature {@link HederaSignature}
	 */
	public void addSignature(HederaSignature signature) {

		this.signatureList.addSignature(signature);
		// can't do anything to keysignatureList here, we don't have a key

	}

	/**
	 * Gets a receipt for a transaction
	 * Get the receipt of a transaction, given its transaction ID. Once a transaction reaches consensus, 
	 * then information about whether it succeeded or failed will be available until the end of the receipt period. 
	 * Before and after the receipt period, and for a transaction that was never submitted, the receipt is unknown. 
	 * This query is free (the payment field is left empty).
	 * @param transactionID the transactionID the receipt is requested for
	 * @return true if successful
	 * @throws InterruptedException in the event of a node communication issue
	 */
	public boolean getReceipt(HederaTransactionID transactionID) throws InterruptedException {
		boolean result = false;
		final long milliConversion = 1000000;
		long startTime = System.nanoTime() / milliConversion;
		long endTime = startTime + 2000;

		Response response = null;
		while(!result) {

			// build the query
		   	// Header
			HederaQueryHeader queryHeader = new HederaQueryHeader();
			queryHeader.responseType = QueryResponseType.ANSWER_ONLY;
	
			// get receipt query
			TransactionGetReceiptQuery.Builder getReceiptQuery = TransactionGetReceiptQuery.newBuilder();
			getReceiptQuery.setTransactionID(transactionID.getProtobuf());
	
			getReceiptQuery.setHeader(queryHeader.getProtobuf());
			
			// the query itself
			HederaQuery query = new HederaQuery();
			query.queryType = QueryType.TRANSACTIONGETRECEIPT;
			query.queryData = getReceiptQuery.build();
			
			// query now set, send to network
			Utilities.throwIfNull("Node", this.node);
			response = this.node.getTransactionReceipt(query);
			if (response != null &&  response.getTransactionGetReceipt() != null ) {
				break;
			}
			if (System.nanoTime() / milliConversion >= endTime) { return false; }
		}
		this.transactionReceipt = new HederaTransactionReceipt(response.getTransactionGetReceipt());


	   	return true;
	}
	/**
	 * Get a fast record for a transaction. These only last 180s and are free 
	 * If the transaction created an account, file, or smart contract instance, then the record will contain the ID for what it created. 
	 * If the transaction called a smart contract function, then the record contains the result of that call. 
	 * If the transaction was a cryptocurrency transfer, then the record includes the TransferList which gives the details of that transfer. 
	 * If the transaction didn't return anything that should be in the record, then the results field will be set to nothing.
	 * @param transactionID the {@link HederaTransactionID} for which the record is requested
	 * @param responseType the type of response required
	 * @return true if successful
	 * @throws InterruptedException in the event of a node communication error
	 */
	public boolean getFastRecord(HederaTransactionID transactionID, HederaQueryHeader.QueryResponseType responseType) throws InterruptedException {
		boolean result = true;
		

		// build the query
	   	// Header
		HederaQueryHeader queryHeader = new HederaQueryHeader();
		
		TransactionGetFastRecordQuery.Builder getQuery = TransactionGetFastRecordQuery.newBuilder();
		getQuery.setTransactionID(transactionID.getProtobuf());
		getQuery.setHeader(queryHeader.getProtobuf());
		
		// the query itself
		HederaQuery query = new HederaQuery();
		query.queryType = QueryType.TRANSACTIONGETFASTRECORD;
		query.queryData = getQuery.build();
		
		// query now set, send to network
		Utilities.throwIfNull("Node", this.node);
		Response response = this.node.getTransactionFastRecord(query);
		TransactionGetFastRecordResponse getResponse = response.getTransactionGetFastRecord();

		// check response header first
		ResponseHeader responseHeader = getResponse.getHeader();

		this.precheckResult = responseHeader.getNodeTransactionPrecheckCode();
				
		if (this.precheckResult == ResponseCodeEnum.OK) {

			// cost
			this.cost = responseHeader.getCost();
			//state proof
			this.stateProof = responseHeader.getStateProof().toByteArray();
			
			this.transactionRecord = new HederaTransactionRecord(getResponse.getTransactionRecord());
			
		} else {
			result = false;
		}
		

	   	return result;
	}
	/**
	 * Get the record for a transaction. 
	 * If the transaction requested a record, then the record lasts for one hour, and a state proof is available for it. 
	 * If the transaction created an account, file, or smart contract instance, then the record will contain the ID for what it created. 
	 * If the transaction called a smart contract function, then the record contains the result of that call. 
	 * If the transaction was a cryptocurrency transfer, then the record includes the TransferList which gives the details of that transfer. 
	 * If the transaction didn't return anything that should be in the record, then the results field will be set to nothing.
	 * @param payment the {@link HederaTransaction} payment for requesting the record
	 * @param transactionID the {@link HederaTransactionID} for which the record is requested
	 * @param responseType the type of response required
	 * @return true if successful
	 * @throws InterruptedException in the event of a node communication error
	 */
	public boolean getRecord(HederaTransaction payment, HederaTransactionID transactionID, HederaQueryHeader.QueryResponseType responseType) throws InterruptedException {
		boolean result = true;
		

		// build the query
	   	// Header
		HederaQueryHeader queryHeader = new HederaQueryHeader();
		if (payment != null) {
			queryHeader.payment = payment;
			queryHeader.responseType = responseType;
		}
		
		TransactionGetRecordQuery.Builder getQuery = TransactionGetRecordQuery.newBuilder();
		getQuery.setTransactionID(transactionID.getProtobuf());
		getQuery.setHeader(queryHeader.getProtobuf());
		
		
		// the query itself
		HederaQuery query = new HederaQuery();
		query.queryType = QueryType.TRANSACTIONGETRECORD;
		query.queryData = getQuery.build();
		
		// query now set, send to network
		Utilities.throwIfNull("Node", this.node);
		Response response = this.node.getTransactionRecord(query);
		TransactionGetRecordResponse getResponse = response.getTransactionGetRecord();

		// check response header first
		ResponseHeader responseHeader = getResponse.getHeader();

		this.precheckResult = responseHeader.getNodeTransactionPrecheckCode();
				
		if (this.precheckResult == ResponseCodeEnum.OK) {

			// cost
			this.cost = responseHeader.getCost();
			//state proof
			this.stateProof = responseHeader.getStateProof().toByteArray();
			
			this.transactionRecord = new HederaTransactionRecord(getResponse.getTransactionRecord());
			
		} else {
			result = false;
		}
		

	   	return result;
	}
	/**
	 * Get the record for a transaction without a state proof 
	 * If the transaction requested a record, then the record lasts for one hour, and a state proof is available for it. 
	 * If the transaction created an account, file, or smart contract instance, then the record will contain the ID for what it created. 
	 * If the transaction called a smart contract function, then the record contains the result of that call. 
	 * If the transaction was a cryptocurrency transfer, then the record includes the TransferList which gives the details of that transfer. 
	 * If the transaction didn't return anything that should be in the record, then the results field will be set to nothing.
	 * @param payment the {@link HederaTransaction} payment for requesting the record
	 * @param transactionID the {@link HederaTransactionID} for which the record is requested
	 * @return true if successful
	 * @throws InterruptedException in the event of a node communication error
	 */
	public boolean getRecordAnswerOnly(HederaTransaction payment, HederaTransactionID transactionID) throws InterruptedException {

	   	return getRecord(payment, transactionID, QueryResponseType.ANSWER_ONLY);
	}
	/**
	 * Get the record for a transaction with a state proof 
	 * If the transaction requested a record, then the record lasts for one hour, and a state proof is available for it. 
	 * If the transaction created an account, file, or smart contract instance, then the record will contain the ID for what it created. 
	 * If the transaction called a smart contract function, then the record contains the result of that call. 
	 * If the transaction was a cryptocurrency transfer, then the record includes the TransferList which gives the details of that transfer. 
	 * If the transaction didn't return anything that should be in the record, then the results field will be set to nothing.
	 * @param payment the {@link HederaTransaction} payment for requesting the record
	 * @param transactionID the {@link HederaTransactionID} for which the record is requested
	 * @return true if successful
	 * @throws InterruptedException in the event of a node communication error
	 */
	public boolean getRecordStateProof(HederaTransaction payment, HederaTransactionID transactionID) throws InterruptedException {

		return getRecord(payment, transactionID, HederaQueryHeader.QueryResponseType.ANSWER_STATE_PROOF);
	}
	/**
	 * Get cost of obtaining a record for a transaction without a state proof 
	 * If the transaction requested a record, then the record lasts for one hour, and a state proof is available for it. 
	 * If the transaction created an account, file, or smart contract instance, then the record will contain the ID for what it created. 
	 * If the transaction called a smart contract function, then the record contains the result of that call. 
	 * If the transaction was a cryptocurrency transfer, then the record includes the TransferList which gives the details of that transfer. 
	 * If the transaction didn't return anything that should be in the record, then the results field will be set to nothing.
	 * @param transactionID the {@link HederaTransactionID} for which the record is requested
	 * @return true if successful
	 * @throws InterruptedException in the event of a node communication error
	 */
	public boolean getRecordCostAnswer(HederaTransactionID transactionID) throws InterruptedException {

		return getRecord(null, transactionID, HederaQueryHeader.QueryResponseType.COST_ANSWER);
	}
	/**
	 * Get cost of obtaining a record for a transaction with a state proof 
	 * If the transaction requested a record, then the record lasts for one hour, and a state proof is available for it. 
	 * If the transaction created an account, file, or smart contract instance, then the record will contain the ID for what it created. 
	 * If the transaction called a smart contract function, then the record contains the result of that call. 
	 * If the transaction was a cryptocurrency transfer, then the record includes the TransferList which gives the details of that transfer. 
	 * If the transaction didn't return anything that should be in the record, then the results field will be set to nothing.
	 * @param transactionID the {@link HederaTransactionID} for which the record is requested
	 * @return true if successful
	 * @throws InterruptedException in the event of a node communication error
	 */
	public boolean getRecordCostAnswerStateProof(HederaTransactionID transactionID) throws InterruptedException {

		return getRecord(null, transactionID, HederaQueryHeader.QueryResponseType.COST_ANSWER_STATE_PROOF);
	}
	
	/**
	 * Generates a transfer transaction to enable payments for queries
	 * @param txQueryDefaults the defaults for transactions and queries
	 * @param queryFee the fee paid for the query
	 * @throws Exception in the event of an error 
	 */
	public HederaTransaction(HederaTransactionAndQueryDefaults txQueryDefaults, long queryFee) throws Exception {

		
		// create a transaction ID (starts now with accountID of the paying account id)
		HederaTransactionID hederaTransactionID = new HederaTransactionID(txQueryDefaults.payingAccountID);

		ArrayList<HederaAccountAmount> accountAmounts = new ArrayList<HederaAccountAmount>();
		// add the two accounts and amounts to the array list
		HederaAccountAmount fromAccountAmount = null;
		// negative amount from the account for the query fee
		fromAccountAmount = new HederaAccountAmount(txQueryDefaults.payingAccountID.shardNum, txQueryDefaults.payingAccountID.realmNum, txQueryDefaults.payingAccountID.accountNum, -queryFee);
		 // positive amount to the Node's account to pay for the query
		HederaAccountAmount toAccountAmount = new HederaAccountAmount(txQueryDefaults.node.getAccountID(), queryFee);
		
		accountAmounts.add(fromAccountAmount);
		accountAmounts.add(toAccountAmount);
		
		HederaAccount account = new HederaAccount();
		// validate inputs
		Utilities.throwIfNull("txQueryDefaults", txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", txQueryDefaults.node);
		Utilities.throwIfAccountIDInvalid("txQueryDefaults.node.getAccountID()", txQueryDefaults.node.getAccountID());
		Utilities.throwIfNull("txQueryDefaults.payingKeyPair", txQueryDefaults.payingKeyPair);
		
		account.txQueryDefaults = txQueryDefaults;
		account.setNode(txQueryDefaults.node);
		
		// get the body for the transaction so we can sign it
		TransactionBody transferBody = account.bodyToSignForTransfer(
				hederaTransactionID
				, txQueryDefaults.node.getAccountID()
				, txQueryDefaults.node.accountTransferTransactionFee // this is the transaction fee
				, txQueryDefaults.transactionValidDuration
				, txQueryDefaults.generateRecord
				, txQueryDefaults.memo
				, accountAmounts);

		HederaSignatureList sigsForTransaction = new HederaSignatureList();
		//paying signature
		sigsForTransaction.addSignature(txQueryDefaults.payingKeyPair.getSignature(transferBody.toByteArray()));
		sigsForTransaction.addSignature(txQueryDefaults.payingKeyPair.getSignature(transferBody.toByteArray()));

		this.body = new HederaTransactionBody(
				TransactionType.CRYPTOTRANSFER
				, hederaTransactionID
				, txQueryDefaults.node.getAccountID()
				, txQueryDefaults.node.accountTransferTransactionFee // this is the transaction fee
				, txQueryDefaults.transactionValidDuration
				, txQueryDefaults.generateRecord
				, txQueryDefaults.memo
				, account.getTransferTransactionBody(accountAmounts));
		
		// add the signatures
		this.signatureList = sigsForTransaction;

	}
}
