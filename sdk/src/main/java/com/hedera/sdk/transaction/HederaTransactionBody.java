package com.hedera.sdk.transaction;

import java.io.Serializable;
import org.slf4j.LoggerFactory;
import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaTransactionID;
import com.hederahashgraph.api.proto.java.*;
/**
 * this class manages transaction bodies
 */
public class HederaTransactionBody implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaTransactionBody.class);
	private static final long serialVersionUID = 1;
	/**
	 * enumeration of allowed transaction types
	 */
	public enum TransactionType {
		CONTRACTCALL,
		CONTRACTCREATEINSTANCE,
		CONTRACTUPDATEINSTANCE,
		CRYPTOADDCLAIM,
		CRYPTOCREATEACCOUNT,
		CRYPTODELETE,
		CRYPTODELETECLAIM,
		CRYPTOTRANSFER,
		CRYPTOUPDATEACCOUNT,
		FILEAPPEND,
		FILECREATE,
		FILEDELETE,
		FILEUPDATE,
		NOTSET
	}
	/**
	 * get or set the {@link HederaTransactionID} for which this body is being built
	 */
	public HederaTransactionID transactionId = new HederaTransactionID();
	/**
	 * get or set the {@link HederaAccountID} of the node to which the transaction is being sent
	 */
	public HederaAccountID nodeAccount = new HederaAccountID();
	/**
	 * get or set the transaction fee
	 */
	public long transactionFee = 0;
	/**
	 * get or set the {@link HederaDuration} representing the transaction's valididity duration
	 */
	public HederaDuration transactionValidDuration = new HederaDuration();
	/**
	 * specifies whether a record should be generated for this transaction
	 */
	public boolean generateRecord = false;
	/**
	 * get or set the memo for the transaction
	 */
	public String memo = "";
	/**
	 * generic transaction data object
	 */
	public Object data = new Object();
	/**
	 * get or set the type of transaction
	 */
	public TransactionType transactionType = TransactionType.NOTSET;
	
	/**
	 * Default constructor
	 */
	public HederaTransactionBody() {


	}
	/**
	 * Constructor from all necessary information
	 * @param transactionType the {@link TransactionType} for this transaction
	 * @param transactionID the {@link HederaTransactionID} for this transaction
	 * @param nodeAccount the {@link HederaAccountID} of the node to which the transaction is to be sent
	 * @param transactionFee the fee to pay for the transaction {@link Long}
	 * @param transactionValidDuration the {@link HederaDuration} specifying how long the transaction is valid for
	 * @param generateRecord {@link Boolean} to specify whether a record is required or not
	 * @param memo a {@link String} to store in the transaction
	 * @param data the transaction data {@link Object}
	 */
	public HederaTransactionBody(TransactionType transactionType, HederaTransactionID transactionID, HederaAccountID nodeAccount
			, long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, String memo
			, Object data) {

		this.transactionType = transactionType;
		this.transactionId = transactionID;
		this.nodeAccount = nodeAccount;
		this.transactionFee = transactionFee;
		this.transactionValidDuration = transactionValidDuration;
		this.generateRecord = generateRecord;
		this.memo = memo;
		this.data = data;

	}
	/**
	 * returns the {@link TransactionBody} protobuf for this transaction
	 * @return {@link TransactionBody}
	 */
	public TransactionBody getProtobuf() {

		// Generates the protobuf payload for this class
		TransactionBody.Builder transactionBodyProtobuf = TransactionBody.newBuilder();
		transactionBodyProtobuf.setTransactionID(this.transactionId.getProtobuf());
		transactionBodyProtobuf.setNodeAccountID(this.nodeAccount.getProtobuf());
		transactionBodyProtobuf.setTransactionFee(this.transactionFee);
		transactionBodyProtobuf.setTransactionValidDuration(this.transactionValidDuration.getProtobuf());
		transactionBodyProtobuf.setGenerateRecord(this.generateRecord);
		transactionBodyProtobuf.setMemo(this.memo);
		switch (this.transactionType) {
			case CONTRACTCALL:
				transactionBodyProtobuf.setContractCall((ContractCallTransactionBody)this.data);
				break;
			case CONTRACTCREATEINSTANCE:
				transactionBodyProtobuf.setContractCreateInstance((ContractCreateTransactionBody)this.data);
				break;
			case CONTRACTUPDATEINSTANCE:
				transactionBodyProtobuf.setContractUpdateInstance((ContractUpdateTransactionBody)this.data);
				break;
			case CRYPTOADDCLAIM:
				transactionBodyProtobuf.setCryptoAddClaim((CryptoAddClaimTransactionBody)this.data);
				break;
			case CRYPTOCREATEACCOUNT:
				transactionBodyProtobuf.setCryptoCreateAccount((CryptoCreateTransactionBody)this.data);
				break;
			case CRYPTODELETE:
				transactionBodyProtobuf.setCryptoDelete((CryptoDeleteTransactionBody)this.data);
				break;
			case CRYPTODELETECLAIM:
				transactionBodyProtobuf.setCryptoDeleteClaim((CryptoDeleteClaimTransactionBody)this.data);
				break;
			case CRYPTOTRANSFER:
				transactionBodyProtobuf.setCryptoTransfer((CryptoTransferTransactionBody)this.data);
				break;
			case CRYPTOUPDATEACCOUNT:
				transactionBodyProtobuf.setCryptoUpdateAccount((CryptoUpdateTransactionBody)this.data);
				break;
			case FILEAPPEND:
				transactionBodyProtobuf.setFileAppend((FileAppendTransactionBody)this.data);
				break;
			case FILECREATE:
				transactionBodyProtobuf.setFileCreate((FileCreateTransactionBody)this.data);
				break;
			case FILEDELETE:
				transactionBodyProtobuf.setFileDelete((FileDeleteTransactionBody)this.data);
				break;
			case FILEUPDATE:
				transactionBodyProtobuf.setFileUpdate((FileUpdateTransactionBody)this.data);
				break;
			case NOTSET:

	            throw new IllegalArgumentException("Transaction type not set. Unable to generate data.");			
		}

		
		return transactionBodyProtobuf.build();
	}
}
