package com.hedera.sdk.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;
import com.google.protobuf.ByteString;
import com.hedera.sdk.account.HederaAccountAmount;
import com.hedera.sdk.contract.HederaContractFunctionResult;
import com.hedera.sdk.query.HederaQueryHeader.QueryResponseType;
import com.hedera.sdk.transaction.HederaTransaction;
import com.hederahashgraph.api.proto.java.TransactionRecord;
import com.hederahashgraph.api.proto.java.TransferList;

/**
 * Hedera Record for a transaction
 */
public class HederaTransactionRecord implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaTransactionRecord.class);
	private static final long serialVersionUID = 1;
	private BodyType bodyType = null;
	private enum BodyType {
		CONTRACTCALLRESULT,
		CONTRACTCREATERESULT,
		TRANSFERLIST
	}

	/**
	 * the receipt for the transaction, initially null
	 */
	public HederaTransactionReceipt transactionReceipt = null;
	/**
	 * the hash of the transaction
	 */
	public byte[] transactionHash = new byte[0];
	/**
	 * the transaction's consensus timestamp, initially null
	 */
	public HederaTimeStamp consensusTimeStamp = null;
	/**
	 * the transaction's ID {@link HederaTransactionID}, initially null
	 */
	public HederaTransactionID transactionId = null;
	/**
	 * the transaction's memo
	 */
	public String memo = "";
	/**
	 * the transaction's fee
	 */
	public long transactionFee = 0;
	/**
	 * transaction contract call result, initially null
	 */
	public HederaContractFunctionResult contractCallResult = null;
	/**
	 * transaction contract create result, initially null
	 */
	public HederaContractFunctionResult contractCreateResult = null;
	/**
	 * transaction transfer list, initially null
	 */
	public List<HederaAccountAmount> transferList = new ArrayList<HederaAccountAmount>();
	
	public void setBodyContractCallResult() {
		this.bodyType = BodyType.CONTRACTCALLRESULT;
	}
	public void setBodyContractCreateResult() {
		this.bodyType = BodyType.CONTRACTCREATERESULT;
	}
	public void setBodyTransferList() {
		this.bodyType = BodyType.TRANSFERLIST;
	}
	/**
	 * Default constructor
	 */
	public HederaTransactionRecord() {


	}
	/**
	 * Constructor from a {@link TransactionRecord} protobuf
	 * @param transactionRecordProtobuf the transaction record
	 */
	public HederaTransactionRecord(TransactionRecord transactionRecordProtobuf) {

	   	if (transactionRecordProtobuf.hasReceipt()) {
	   		this.transactionReceipt = new HederaTransactionReceipt(transactionRecordProtobuf.getReceipt());
	   	} else {
	   		this.transactionReceipt = null;
	   	}
	   	this.transactionHash = transactionRecordProtobuf.getTransactionHash().toByteArray();
	   	if (transactionRecordProtobuf.hasConsensusTimestamp()) {
	   		this.consensusTimeStamp = new HederaTimeStamp(transactionRecordProtobuf.getConsensusTimestamp());
	   	} else {
	   		this.consensusTimeStamp = null;
	   	}
	   	if (transactionRecordProtobuf.hasTransactionID()) {
	   		this.transactionId = new HederaTransactionID(transactionRecordProtobuf.getTransactionID());
	   	} else {
	   		this.transactionId = null;
	   	}
	   	this.memo = transactionRecordProtobuf.getMemo();
	   	this.transactionFee = transactionRecordProtobuf.getTransactionFee();

   		if (transactionRecordProtobuf.hasContractCallResult()) {
   			this.bodyType = BodyType.CONTRACTCALLRESULT;
   			this.contractCallResult = new HederaContractFunctionResult(transactionRecordProtobuf.getContractCallResult());
   		} else if (transactionRecordProtobuf.hasContractCreateResult()) {
   			this.bodyType = BodyType.CONTRACTCREATERESULT;
   			this.contractCreateResult = new HederaContractFunctionResult(transactionRecordProtobuf.getContractCreateResult());
   		} else if (transactionRecordProtobuf.hasTransferList()) {
   			this.bodyType = BodyType.TRANSFERLIST;
			TransferList transferListPB = transactionRecordProtobuf.getTransferList();
			this.transferList.clear();
			for (int i=0; i < transferListPB.getAccountAmountsCount(); i++) {
				HederaAccountAmount accountAmount = new HederaAccountAmount(transferListPB.getAccountAmounts(i));
				this.transferList.add(accountAmount);
			} 
	   	}
	}

	/**
	 * Generate a protobuf payload for this object 
	 * @return {@link TransactionRecord}
	 */
	public TransactionRecord getProtobuf() {

		
		TransactionRecord.Builder transactionRecord = TransactionRecord.newBuilder();
		if (this.consensusTimeStamp != null) {
			transactionRecord.setConsensusTimestamp(this.consensusTimeStamp.getProtobuf());
		}
		
		switch (this.bodyType) {
		case CONTRACTCALLRESULT:
			transactionRecord.setContractCallResult(contractCallResult.getProtobuf());
			break;
		case CONTRACTCREATERESULT:
			transactionRecord.setContractCreateResult(contractCreateResult.getProtobuf());
			break;
		case TRANSFERLIST:
			TransferList.Builder transferList = TransferList.newBuilder();
			for (int i=0; i < this.transferList.size(); i++) {
				transferList.addAccountAmounts(this.transferList.get(i).getProtobuf());
			}
			transactionRecord.setTransferList(transferList);
			break;
		}

		transactionRecord.setMemo(this.memo);
		transactionRecord.setReceipt(this.transactionReceipt.getProtobuf());
		transactionRecord.setTransactionFee(this.transactionFee);
		transactionRecord.setTransactionHash(ByteString.copyFrom(this.transactionHash));
		transactionRecord.setTransactionID(this.transactionId.getProtobuf());
		
		return transactionRecord.build();
	}
	/** 
	 * Gets a record for a transaction
	 * @param transactionID the transaction ID against which to get the record
	 * @param queryFee the fee being paid for the query
	 * @param txQueryDefaults - default parameters for running the query (inc. node)
	 * @throws Exception in the event of an error 
	 */
	public HederaTransactionRecord(HederaTransactionID transactionID, Long queryFee, HederaTransactionAndQueryDefaults txQueryDefaults) throws Exception {
		HederaTransaction transaction = new HederaTransaction();
		Utilities.throwIfNull("txQueryDefaults", txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", txQueryDefaults.node);
		HederaTransaction payment = new HederaTransaction(txQueryDefaults, queryFee);
		
		transaction.setNode(txQueryDefaults.node);
		
		if (transaction.getRecord(payment, transactionID, QueryResponseType.ANSWER_ONLY)) {
			this.consensusTimeStamp = transaction.transactionRecord().consensusTimeStamp;
			this.contractCallResult = transaction.transactionRecord().contractCallResult;
			this.contractCreateResult = transaction.transactionRecord().contractCreateResult;
			this.memo = transaction.transactionRecord().memo;
			this.transactionFee = transaction.transactionRecord().transactionFee;
			this.transactionHash = transaction.transactionRecord().transactionHash;
			this.transactionId = transaction.transactionRecord().transactionId;
			this.transactionReceipt = transaction.transactionRecord().transactionReceipt;
		}
	}
	//
	/** 
	 * Gets a fast (free) record for a transaction (lasts 180 seconds)
	 * @param transactionID the transaction ID against which to get the record
	 * @param txQueryDefaults - default parameters for running the query (inc. node)
	 * @throws Exception in the event of an error 
	 */
	public HederaTransactionRecord(HederaTransactionID transactionID, HederaTransactionAndQueryDefaults txQueryDefaults) throws Exception {
		HederaTransaction transaction = new HederaTransaction();
		Utilities.throwIfNull("txQueryDefaults", txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", txQueryDefaults.node);
		
		transaction.setNode(txQueryDefaults.node);
		
		if (transaction.getFastRecord(transactionID, QueryResponseType.ANSWER_ONLY)) {
			this.consensusTimeStamp = transaction.transactionRecord().consensusTimeStamp;
			this.contractCallResult = transaction.transactionRecord().contractCallResult;
			this.contractCreateResult = transaction.transactionRecord().contractCreateResult;
			this.memo = transaction.transactionRecord().memo;
			this.transactionFee = transaction.transactionRecord().transactionFee;
			this.transactionHash = transaction.transactionRecord().transactionHash;
			this.transactionId = transaction.transactionRecord().transactionId;
			this.transactionReceipt = transaction.transactionRecord().transactionReceipt;
		}
	}
}