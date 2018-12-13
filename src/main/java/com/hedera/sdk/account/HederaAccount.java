package com.hedera.sdk.account;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaKey;
import com.hedera.sdk.common.HederaKeySignature;
import com.hedera.sdk.common.HederaKeySignatureList;
import com.hedera.sdk.common.HederaRealmID;
import com.hedera.sdk.common.HederaTransactionRecord;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.common.HederaShardID;
import com.hedera.sdk.common.HederaSignature;
import com.hedera.sdk.common.HederaSignatureList;
import com.hedera.sdk.common.HederaTimeStamp;
import com.hedera.sdk.common.HederaTransactionID;
import com.hedera.sdk.cryptography.HederaCryptoKeyPair;
import com.hedera.sdk.node.HederaNode;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.common.HederaKey.KeyType;
import com.hedera.sdk.query.HederaQuery;
import com.hedera.sdk.query.HederaQueryHeader;
import com.hedera.sdk.query.HederaQuery.QueryType;
import com.hedera.sdk.query.HederaQueryHeader.QueryResponseType;
import com.hedera.sdk.transaction.HederaTransaction;
import com.hedera.sdk.transaction.HederaTransactionBody;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hedera.sdk.transaction.HederaTransactionBody.TransactionType;
import com.hederahashgraph.api.proto.java.CryptoAddClaimTransactionBody;
import com.hederahashgraph.api.proto.java.CryptoCreateTransactionBody;
import com.hederahashgraph.api.proto.java.CryptoDeleteClaimTransactionBody;
import com.hederahashgraph.api.proto.java.CryptoDeleteTransactionBody;
import com.hederahashgraph.api.proto.java.CryptoGetAccountBalanceQuery;
import com.hederahashgraph.api.proto.java.CryptoGetAccountBalanceResponse;
import com.hederahashgraph.api.proto.java.CryptoGetAccountRecordsQuery;
import com.hederahashgraph.api.proto.java.CryptoGetAccountRecordsResponse;
import com.hederahashgraph.api.proto.java.CryptoGetInfoQuery;
import com.hederahashgraph.api.proto.java.CryptoGetInfoResponse;
import com.hederahashgraph.api.proto.java.CryptoTransferTransactionBody;
import com.hederahashgraph.api.proto.java.CryptoUpdateTransactionBody;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
import com.hederahashgraph.api.proto.java.ResponseHeader;
import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransferList;
/**
 * Class to manage a cryptocurrency account
 */
public class HederaAccount implements Serializable {
	final Logger logger = LoggerFactory.getLogger(HederaAccount.class);
	private static final long serialVersionUID = 1;
	private HederaNode node = null;
	// keys for signatures
	private List<HederaKeySignature> keySignatures = new ArrayList<HederaKeySignature>();
	private List<HederaKey> keys = new ArrayList<HederaKey>();
	private List<HederaTransactionRecord> records = null;
	private ResponseCodeEnum precheckResult = ResponseCodeEnum.UNKNOWN;
	private String solidityContractAccountID = "";
	private boolean deleted = false;
	private long proxyReceived = 0;
	private long cost = 0;
	private byte[] stateProof = new byte[0];
	private HederaProxyStakers stakers = new HederaProxyStakers();
	private long balance = 0;

	/**
	 * Default parameters for a transaction or query
	 */
	public HederaTransactionAndQueryDefaults txQueryDefaults = new HederaTransactionAndQueryDefaults();
	/**
	 * The shard number for the account
	 */
	public long shardNum = 0;
	/**
	 * The realm number for the account
	 */
	public long realmNum = 0;
	/**
	 * The account number for the account
	 */
	public long accountNum = 0;
	/**
	 * The {@link HederaTransactionID} for a transaction in relation to this account
	 */
	public HederaTransactionID hederaTransactionID;
	/**
	 * The new realm administration key {@link HederaKey} for the account
	 * note: if a newRealmAdminKeySig is specified, this will be ignored
	 */
	public HederaKey newRealmAdminKey = null;
	/**
	 * The new realm administration key {@link HederaKeySignature} and signature pair for the account
	 * note: this takes priority over a newRealmAdminKey
	 */
	public HederaKeySignature newRealmAdminKeySig = null;
	/**
	 * The {@link HederaKey} for the account
	 * note: if an accountKeySig is specified, this will be ignored
	 * the key that must sign each transfer out of the account. If receiverSigRequired is true, then it must also sign any transfer into the account.
	 */
	public HederaKey accountKey = null;
	/**
	 * The {@link HederaKeySignature} for the account
	 * note: this takes priority over an accountKey 
	 * the key that must sign each transfer out of the account. If receiverSigRequired is true, then it must also sign any transfer into the account.
	 */
	public HederaKeySignature accountKeySig = null;
	/**
	 * The new {@link HederaKey} for the account
	 * the key that must sign each transfer out of the account. If receiverSigRequired is true, then it must also sign any transfer into the account.
	 */
	public HederaKey newAccountKey = null;
	/** 
	 * The initial balance when creating a new account
	 */
	public long initialBalance = 0;
	/**
	 * the balance of the account
	 * @return {@link Long}
	 */
	public long balance() {
		return this.balance;
	}
	/**
	 * the {@link HederaAccountID} to proxy tokens to
	 * If proxyAccountID is null, or is an invalid account, or is an account that isn't a node, then this account is 
	 * automatically proxy staked to a node chosen by the network, but without earning payments. 
	 * If the proxyAccountID account refuses to accept proxy staking at the given fraction, 
	 * or if it is not currently running a node, then it will behave as if both proxyAccountID and proxyFraction were null.
	 */
	public HederaAccountID proxyAccountID = new HederaAccountID(0, 0, 0);
	/** 
	 * payments earned from proxy staking are shared between the node and this account, with proxyFraction / 10000 going to this account
	 */
	public int proxyFraction = 0;
	/**
	 * when another account tries to proxy stake to this account, accept it only if the proxyFraction from that other account is at most maxReceiveProxyFraction
	 */
	public int maxReceiveProxyFraction = 0;
	/**
	 * create an account record for any transaction withdrawing more than this many tinybars
	 */
	public long sendRecordThreshold = Long.MAX_VALUE;
	/**
	 * create an account record for any transaction depositing more than this many tinybars
	 */
	public long receiveRecordThreshold = Long.MAX_VALUE;
	/**
	 * if true, this account's key must sign any transaction depositing into this account (in addition to all withdrawals). 
	 * This field is immutable; it cannot be changed by a CryptoUpdate transaction.
	 */
	public boolean receiverSigRequired = false;
	/**
	 * the account is charged to extend its expiration date every this many seconds. If it doesn't have enough, 
	 * it extends as long as possible. If it is empty when it expires, then it is deleted.
	 * Defaults to 60 * 60 * 24 * 30 = 30 days (60s * 60m * 24h * 30d) 
	 */
	public HederaDuration autoRenewPeriod = new HederaDuration(60 * 60 * 24 * 30, 0); // 30 days
	/**
	 * the list of claims attached to this account as a result of a query
	 */
	public List<HederaClaim> claims = new ArrayList<HederaClaim>();
	/**
	 * The expiration time of the account as a result of a query
	 */
	public HederaTimeStamp expirationTime = null; 
	/**
	 * sets the node to communicate with
	 * @param node the node to communicate with
	 */
	public void setNode (HederaNode node) {
		this.node = node;
	}
	/**
	 * returns the account id in solidity format for use in smart contracts
	 * @return String
	 */
	public String getSolidityContractAccountID() {
		return this.solidityContractAccountID;
	}
	/**
	 * true if the account has been deleted 
	 * set as a result of a query
	 * @return boolean
	 */
	public boolean getDeleted() {
		return this.deleted;
	}
	/**
	 * total number of tinybars proxy staked to this account populated as a result of a query
	 * @return long
	 */
	public long getProxyReceived() {
		return this.proxyReceived;
	}
	/**
	 * get the {@link HederaProxyStakers} related to this account
	 * @return {@link HederaProxyStakers}
	 */
	public HederaProxyStakers getProxyStakers() {
		return stakers;
	}
	/**
	 * Sets the hederaAccountID values (shard, realm, accountNum) 
	 * from a HederaAccountID
	 * @param accountID (The HederaAccountID from which to set the properties)
	 */
	public void setHederaAccountID(HederaAccountID accountID) {
		this.shardNum = accountID.shardNum;
		this.realmNum = accountID.realmNum;
		this.accountNum = accountID.accountNum;
	}
	/**
	 * Gets the shard, realm and accountNum of this account in the form 
	 * of a HederaAccountID
	 * @return {@link HederaAccountID}
	 */
	public HederaAccountID getHederaAccountID() {
		return new HederaAccountID(this.shardNum,  this.realmNum,  this.accountNum);
	}
	/**
	 * Default constructor
	 */
	public HederaAccount() {
	   	logger.trace("Start - Object init");
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructor from shard, realm and account number
	 * @param shardNum the shard number for the account
	 * @param realmNum the realm number for the account
	 * @param accountNum the account number for the account
	 */
	public HederaAccount(long shardNum, long realmNum, long accountNum) {
	   	logger.trace("Start - Object init shardNum {}, realmNum {}, accountNum {}", shardNum, realmNum, accountNum);
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.accountNum = accountNum;
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructor from a {@link HederaTransactionID} protobuf object
	 * @param transactionID the transactionID to construct the object from
	 */
	public HederaAccount(HederaTransactionID transactionID) {
	   	logger.trace("Start - Object init transactionID {}", transactionID);
		this.hederaTransactionID = transactionID;
		this.shardNum = transactionID.accountID.shardNum;
		this.realmNum = transactionID.accountID.realmNum;
		this.accountNum = transactionID.accountID.accountNum;
	   	logger.trace("End - Object init");
	}
	/**
	 * gets the {@link ResponseCodeEnum} as a result of a transaction
	 * @return {@link ResponseCodeEnum} 
	 */
	public ResponseCodeEnum getPrecheckResult() {
		return this.precheckResult;
	}
	/**
	 * Returns the cost of a query after it's been requested
	 * @return long
	 */
	public long getCost() {
		return this.cost;
	}
	/**
	 * Returns the state proof obtained from a query
	 * @return byte[]
	 */
	public byte[] getStateProof() {
		return this.stateProof;
	}
	/**
	 * This method returns the body of a transaction to create an account so that it can be signed
	 * @param transactionID the {@link HederaTransactionID} for the transaction
	 * @param nodeAccount the {@link HederaAccountID} of the account of the node to which the transaction is submitted
	 * @param transactionFee the fee paid by the client to run the transaction
	 * @param transactionValidDuration the duration of the transaction's validity as {@link HederaDuration}
	 * @param generateRecord boolean to indicate if a record should be generated as a result of this transaction
	 * @param memo String memo to include in the transaction
	 * @return {@link TransactionBody}
	 */
	public TransactionBody bodyToSignForCreate(HederaTransactionID transactionID, HederaAccountID nodeAccount, 
			long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, 
			String memo) {
	   	logger.trace("Start - bodyToSignForCreate transactionID {}, nodeAccount {}, transactionFee {}, transactionValidDuration {}, generateRecord {}, memo {}"
	   			, transactionID, nodeAccount, transactionFee, transactionValidDuration, generateRecord,	memo);
		
		HederaTransactionBody transactionBody = new HederaTransactionBody(
				TransactionType.CRYPTOCREATEACCOUNT
				, transactionID
				, nodeAccount
				, transactionFee
				, transactionValidDuration
				, generateRecord
				, memo
				, this.getCreateTransactionBody());

	   	logger.trace("End - bodyToSignForCreate");
		return transactionBody.getProtobuf();
	}
	/**
	 * This method returns the body of a transaction to transfer cryptocurrency between accounts so that it can be signed
	 * @param transactionID the {@link HederaTransactionID} for the transaction
	 * @param nodeAccount the {@link HederaAccountID} of the account of the node to which the transaction is submitted
	 * @param transactionFee the fee paid by the client to run the transaction
	 * @param transactionValidDuration the duration of the transaction's validity as {@link HederaDuration}
	 * @param generateRecord boolean to indicate if a record should be generated as a result of this transaction
	 * @param memo String memo to include in the transaction
	 * @param accountAmounts hte list of accounts and amounts to transfer
	 * @return {@link TransactionBody}
	 */
	public TransactionBody bodyToSignForTransfer(HederaTransactionID transactionID, HederaAccountID nodeAccount, 
			long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, 
			String memo, ArrayList<HederaAccountAmount> accountAmounts) {
	   	logger.trace("Start - bodyToSignForTransfer transactionID {}, nodeAccount {}, transactionFee {}, transactionValidDuration {}, generateRecord {}, memo {}"
	   			, transactionID, nodeAccount, transactionFee, transactionValidDuration, generateRecord,	memo);
		
		HederaTransactionBody transactionBody = new HederaTransactionBody(
				TransactionType.CRYPTOTRANSFER
				, transactionID
				, nodeAccount
				, transactionFee
				, transactionValidDuration
				, generateRecord
				, memo
				, this.getTransferTransactionBody(accountAmounts));

	   	logger.trace("End - bodyToSignForTransfer");
		return transactionBody.getProtobuf();
	}
	/**
	 * This method returns the body of a transaction to delete an account so it can be signed
	 * @param transactionID the {@link HederaTransactionID} for the transaction
	 * @param nodeAccount the {@link HederaAccountID} of the account of the node to which the transaction is submitted
	 * @param transactionFee the fee paid by the client to run the transaction
	 * @param transactionValidDuration the duration of the transaction's validity as {@link HederaDuration}
	 * @param generateRecord boolean to indicate if a record should be generated as a result of this transaction
	 * @param memo String memo to include in the transaction
	 * @param transferAccountID the {@link HederaAccountID} to transfer remaining funds to
	 * @return {@link TransactionBody}
	 */
	public TransactionBody bodyToSignForDelete(HederaTransactionID transactionID, HederaAccountID nodeAccount, 
			long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, 
			String memo, HederaAccountID transferAccountID) {
	   	logger.trace("Start - bodyToSignForDelete transactionID {}, nodeAccount {}, transactionFee {}, transactionValidDuration {}, generateRecord {}, memo {}, transferAccountID {}"
	   			, transactionID, nodeAccount, transactionFee, transactionValidDuration, generateRecord,	memo, transferAccountID);
		
		HederaTransactionBody transactionBody = new HederaTransactionBody(
				TransactionType.CRYPTODELETE
				, transactionID
				, nodeAccount
				, transactionFee
				, transactionValidDuration
				, generateRecord
				, memo
				, this.getDeleteTransactionBody(transferAccountID));

	   	logger.trace("End - bodyToSignForDelete");
		return transactionBody.getProtobuf();
	}
	/**
	 * This method returns the body of a transaction to update an account so it can be signed
	 * @param transactionID the {@link HederaTransactionID} for the transaction
	 * @param nodeAccount the {@link HederaAccountID} of the account of the node to which the transaction is submitted
	 * @param transactionFee the fee paid by the client to run the transaction
	 * @param transactionValidDuration the duration of the transaction's validity as {@link HederaDuration}
	 * @param generateRecord boolean to indicate if a record should be generated as a result of this transaction
	 * @param memo String memo to include in the transaction
	 * @return {@link TransactionBody}
	 */
	public TransactionBody bodyToSignForUpdate(HederaTransactionID transactionID, HederaAccountID nodeAccount, 
			long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, 
			String memo) {
	   	logger.trace("Start - bodyToSignForUpdate transactionID {}, nodeAccount {}, transactionFee {}, transactionValidDuration {}, generateRecord {}, memo {}"
	   			, transactionID, nodeAccount, transactionFee, transactionValidDuration, generateRecord,	memo);
		
		HederaTransactionBody transactionBody = new HederaTransactionBody(
				TransactionType.CRYPTOUPDATEACCOUNT
				, transactionID
				, nodeAccount
				, transactionFee
				, transactionValidDuration
				, generateRecord
				, memo
				, this.getUpdateTransactionBody());

	   	logger.trace("End - bodyToSignForUpdate");
		return transactionBody.getProtobuf();
	}
	/**
	 * This method returns the body of a transaction to add a claim to an account so it can be signed
	 * @param transactionID the {@link HederaTransactionID} for the transaction
	 * @param nodeAccount the {@link HederaAccountID} of the account of the node to which the transaction is submitted
	 * @param transactionFee the fee paid by the client to run the transaction
	 * @param transactionValidDuration the duration of the transaction's validity as {@link HederaDuration}
	 * @param generateRecord boolean to indicate if a record should be generated as a result of this transaction
	 * @param memo String memo to include in the transaction
	 * @param claim the {@link HederaClaim} to add
	 * @return {@link TransactionBody}
	 */
	public TransactionBody bodyToSignForAddClaim(HederaTransactionID transactionID, HederaAccountID nodeAccount, 
			long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, 
			String memo, HederaClaim claim) {
	   	logger.trace("Start - bodyToSignForAddClaim transactionID {}, nodeAccount {}, transactionFee {}, transactionValidDuration {}, generateRecord {}, memo {}, claim {}"
	   			, transactionID, nodeAccount, transactionFee, transactionValidDuration, generateRecord, memo, claim);
		
		HederaTransactionBody transactionBody = new HederaTransactionBody(
				TransactionType.CRYPTOADDCLAIM
				, transactionID
				, nodeAccount
				, transactionFee
				, transactionValidDuration
				, generateRecord
				, memo
				, this.getAddClaimTransactionBody(claim));

	   	logger.trace("End - bodyToSignForAppend");
		return transactionBody.getProtobuf();
	}
	/**
	 * This method returns the body of a transaction to delete a claim to an account so it can be signed
	 * @param transactionID the {@link HederaTransactionID} for the transaction
	 * @param nodeAccount the {@link HederaAccountID} of the account of the node to which the transaction is submitted
	 * @param transactionFee the fee paid by the client to run the transaction
	 * @param transactionValidDuration the duration of the transaction's validity as {@link HederaDuration}
	 * @param generateRecord boolean to indicate if a record should be generated as a result of this transaction
	 * @param memo String memo to include in the transaction
	 * @param claim the {@link HederaClaim} to delete
	 * @return {@link TransactionBody}
	 */
	public TransactionBody bodyToSignForDeleteClaim(HederaTransactionID transactionID, HederaAccountID nodeAccount, 
			long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, 
			String memo, HederaClaim claim) {
	   	logger.trace("Start - bodyToSignForDeleteClaim transactionID {}, nodeAccount {}, transactionFee {}, transactionValidDuration {}, generateRecord {}, memo {}, claim {}"
	   			, transactionID, nodeAccount, transactionFee, transactionValidDuration, generateRecord, memo, claim);
		
		HederaTransactionBody transactionBody = new HederaTransactionBody(
				TransactionType.CRYPTODELETECLAIM
				, transactionID
				, nodeAccount
				, transactionFee
				, transactionValidDuration
				, generateRecord
				, memo
				, this.getDeleteClaimTransactionBody(claim));

	   	logger.trace("End - bodyToSignForDeleteClaim");
		return transactionBody.getProtobuf();
	}
	/**
	 * This method runs a transaction to create an account 
	 * @param transactionID the {@link HederaTransactionID} for the transaction
	 * @param nodeAccount the {@link HederaAccountID} of the account of the node to which the transaction is submitted
	 * @param transactionFee the fee paid by the client to run the transaction
	 * @param transactionValidDuration the duration of the transaction's validity as {@link HederaDuration}
	 * @param generateRecord boolean to indicate if a record should be generated as a result of this transaction
	 * @param memo String memo to include in the transaction
	 * @param sigsForTransaction The signatures for the transaction as a {@link HederaKeySignatureList}
	 * @return {@link HederaTransactionResult}
	 * @throws InterruptedException in the event that communication to the node resulted in an error
	 */
	public HederaTransactionResult create(HederaTransactionID transactionID, HederaAccountID nodeAccount
			, long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord
			, String memo, HederaSignatureList sigsForTransaction) throws InterruptedException {
	   	logger.trace("Start - create transactionID {}, nodeAccounttransactionFee {}, transactionValidDuration {}, generateRecord {}, memo {}, sigsForTransaction {}"
	   			, transactionID, nodeAccount, transactionFee, transactionValidDuration, generateRecord, memo, sigsForTransaction);
		
		// build the body
		HederaTransaction transaction = new HederaTransaction();
		transaction.body = new HederaTransactionBody(
				TransactionType.CRYPTOCREATEACCOUNT
				, transactionID
				, nodeAccount
				, transactionFee
				, transactionValidDuration
				, generateRecord
				, memo
				, this.getCreateTransactionBody());
		// add the signatures
		transaction.signatureList = sigsForTransaction;
		
		// issue the transaction
		Utilities.throwIfNull("Node", this.node);
		HederaTransactionResult hederaTransactionResult = this.node.accountCreate(transaction);
		hederaTransactionResult.hederaTransactionID = transactionID;
		// return
	   	logger.trace("End - create");
		return hederaTransactionResult;
	}
	/**
	 * This method runs a transaction to transfer cryptocurrency between accounts 
	 * @param transactionID the {@link HederaTransactionID} for the transaction
	 * @param nodeAccount the {@link HederaAccountID} of the account of the node to which the transaction is submitted
	 * @param transactionFee the fee paid by the client to run the transaction
	 * @param transactionValidDuration the duration of the transaction's validity as {@link HederaDuration}
	 * @param generateRecord boolean to indicate if a record should be generated as a result of this transaction
	 * @param memo String memo to include in the transaction
	 * @param sigsForTransaction The signatures for the transaction as a {@link HederaKeySignatureList}
	 * @param accountAmounts the accounts and amounts to transfer currency to and from
	 * @return {@link HederaTransactionResult}
	 * @throws InterruptedException in the event that communication to the node resulted in an error
	 */
	public HederaTransactionResult transfer(HederaTransactionID transactionID, HederaAccountID nodeAccount
			, long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord
			, String memo, HederaSignatureList sigsForTransaction, ArrayList<HederaAccountAmount> accountAmounts) throws InterruptedException {
	   	logger.trace("Start - transfer transactionID {}, nodeAccounttransactionFee {}, transactionValidDuration {}, generateRecord {}, memo {}, sigsForTransaction {}, accountAmounts {}"
	   			, transactionID, nodeAccount, transactionFee, transactionValidDuration, generateRecord, memo, sigsForTransaction, accountAmounts);
		
		// build the body
		HederaTransaction transaction = new HederaTransaction();
		transaction.body = new HederaTransactionBody(
				TransactionType.CRYPTOTRANSFER
				, transactionID
				, nodeAccount
				, transactionFee
				, transactionValidDuration
				, generateRecord
				, memo
				, this.getTransferTransactionBody(accountAmounts));
		// add the signatures
		transaction.signatureList = sigsForTransaction;
		
		// issue the transaction
		Utilities.throwIfNull("Node", this.node);
		HederaTransactionResult hederaTransactionResult = this.node.accountTransfer(transaction);
		hederaTransactionResult.hederaTransactionID = transactionID;
		// return
	   	logger.trace("End - transfer");
		return hederaTransactionResult;
	}

	/**
	 * This method runs a transaction to update an account 
	 * @param transactionID the {@link HederaTransactionID} for the transaction
	 * @param nodeAccount the {@link HederaAccountID} of the account of the node to which the transaction is submitted
	 * @param transactionFee the fee paid by the client to run the transaction
	 * @param transactionValidDuration the duration of the transaction's validity as {@link HederaDuration}
	 * @param generateRecord boolean to indicate if a record should be generated as a result of this transaction
	 * @param memo String memo to include in the transaction
	 * @param sigsForTransaction The signatures for the transaction as a {@link HederaKeySignatureList}
	 * @return {@link HederaTransactionResult}
	 * @throws InterruptedException in the event that communication to the node resulted in an error
	 */
	public HederaTransactionResult update(HederaTransactionID transactionID, HederaAccountID nodeAccount
			, long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord
			, String memo, HederaSignatureList sigsForTransaction) throws InterruptedException {
	   	logger.trace("Start - update transactionID {}, nodeAccount {}, transactionFee {}, transactionValidDuration {}, generateRecord {}, memo {}, sigsForTransaction {}"
	   			, transactionID, nodeAccount, transactionFee, transactionValidDuration, generateRecord, memo, sigsForTransaction);
		
		// build the body
		HederaTransaction transaction = new HederaTransaction();
		transaction.body = new HederaTransactionBody(
				TransactionType.CRYPTOUPDATEACCOUNT
				, transactionID
				, nodeAccount
				, transactionFee
				, transactionValidDuration
				, generateRecord
				, memo
				, this.getUpdateTransactionBody());
		// add the signatures
		transaction.signatureList = sigsForTransaction;
		
		// issue the transaction
		Utilities.throwIfNull("Node", this.node);
		HederaTransactionResult hederaTransactionResult = this.node.accountUpdate(transaction);
		hederaTransactionResult.hederaTransactionID = transactionID;

	   	logger.trace("End - update");
		// return
		return hederaTransactionResult;
	}
	/**
	 * This method runs a transaction to add a claim to an account 
	 * @param transactionID the {@link HederaTransactionID} for the transaction
	 * @param nodeAccount the {@link HederaAccountID} of the account of the node to which the transaction is submitted
	 * @param transactionFee the fee paid by the client to run the transaction
	 * @param transactionValidDuration the duration of the transaction's validity as {@link HederaDuration}
	 * @param generateRecord boolean to indicate if a record should be generated as a result of this transaction
	 * @param memo String memo to include in the transaction
	 * @param sigsForTransaction The signatures for the transaction as a {@link HederaKeySignatureList}
	 * @param claim the {@link HederaClaim} to add to the account
	 * @return {@link HederaTransactionResult}
	 * @throws InterruptedException in the event that communication to the node resulted in an error
	 */
	public HederaTransactionResult addClaim(HederaTransactionID transactionID, HederaAccountID nodeAccount
			, long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord
			, String memo, HederaKeySignatureList sigsForTransaction, HederaClaim claim) throws InterruptedException {
	   	logger.trace("Start - addClaim transactionID {}, nodeAccount {}, transactionFee {}, transactionValidDuration {}, generateRecord {}, memo {}, sigsForTransaction {}"
	   			, transactionID, nodeAccount, transactionFee, transactionValidDuration, generateRecord, memo, sigsForTransaction);
		
		// build the body
		HederaTransaction transaction = new HederaTransaction();
		transaction.body = new HederaTransactionBody(
				TransactionType.CRYPTOADDCLAIM
				, transactionID
				, nodeAccount
				, transactionFee
				, transactionValidDuration
				, generateRecord
				, memo
				, this.getAddClaimTransactionBody(claim));
		// add the signatures
		transaction.keySignatureList = sigsForTransaction;
		
		// issue the transaction
		Utilities.throwIfNull("Node", this.node);
		HederaTransactionResult hederaTransactionResult = this.node.addClaim(transaction);
		hederaTransactionResult.hederaTransactionID = transactionID;

	   	logger.trace("End - addClaim");
		// return
		return hederaTransactionResult;
	}

	/**
	 * Runs a query to get the account balance of the given account 
	 * If successful, the method populates the balance property of the account, cost and state proof if requested
	 * @param payment a {@link HederaTransaction} message to indicate how this query will be paid for, this can be null for Cost queries
	 * @param responseType the type of response requested from the query
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication with the node
	 */
	public boolean getBalance(HederaTransaction payment, HederaQueryHeader.QueryResponseType responseType) throws InterruptedException {
		boolean result = true;
		
	   	logger.trace("Start - getBalance payment {}, responseType {}", payment, responseType);
		// build the query
	   	// Header
		HederaQueryHeader queryHeader = new HederaQueryHeader();
		if (payment != null) {
			queryHeader.payment = payment;
			queryHeader.responseType = responseType;
		}
		
		// get contents query
		CryptoGetAccountBalanceQuery.Builder queryBalance = CryptoGetAccountBalanceQuery.newBuilder();
		
		queryBalance.setAccountID(this.getHederaAccountID().getProtobuf());
		queryBalance.setHeader(queryHeader.getProtobuf());
		
		// the query itself
		HederaQuery query = new HederaQuery();
		query.queryType = QueryType.CRYPTOGETACCOUNTBALANCE;
		query.queryData = queryBalance.build();
		
		// query now set, send to network
		Utilities.throwIfNull("Node", this.node);
		Response response = this.node.getAccountBalance(query);

		if (response == null) {
			Utilities.printResponseFailure("HederaAccount.getBalance");
			return false;
		}

		CryptoGetAccountBalanceResponse.Builder queryResponse = response.getCryptogetAccountBalance().toBuilder();
		
		// check response header first
		ResponseHeader.Builder responseHeader = queryResponse.getHeaderBuilder();

		this.precheckResult = responseHeader.getNodeTransactionPrecheckCode();

		if (this.precheckResult == ResponseCodeEnum.OK) {
			this.balance = queryResponse.getBalance();
			this.cost = responseHeader.getCost();
			this.stateProof = responseHeader.getStateProof().toByteArray();
		} else {
			result = false;
		}
		
	   	logger.trace("End - getBalance");
	   	return result;
	}
	/**
	 * Gets the balance of the account, requesting only an answer
	 * If successful, the method populates the balance property of the account and cost 
	 * @param payment the {@link HederaTransaction} payload containing payment information for the query
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getBalanceAnswerOnly(HederaTransaction payment) throws InterruptedException {
	   	logger.trace("Start - getBalanceAnswerOnly");
	   	return getBalance(payment, QueryResponseType.ANSWER_ONLY);
	}
	/**
	 * Gets the balance of the account, requesting only an answer
	 * If successful, the method populates the balance property of the account, cost and stateproof 
	 * @param payment the {@link HederaTransaction} payload containing payment information for the query
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getBalanceStateProof(HederaTransaction payment) throws InterruptedException {
	   	logger.trace("getBalanceStateProof");
		return getBalance(payment, HederaQueryHeader.QueryResponseType.ANSWER_STATE_PROOF);
	}
	/**
	 * Gets the cost of enquiring for balance of the account, requesting only an answer
	 * If successful, the method populates the cost property 
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getBalanceCostAnswer() throws InterruptedException {
	   	logger.trace("getBalanceCostAnswer");
		return getBalance(null, HederaQueryHeader.QueryResponseType.COST_ANSWER);
	}
	/**
	 * Gets the cost of enquiring for balance of the account, requesting a state proof
	 * If successful, the method populates the cost property 
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getBalanceAnswerStateProof() throws InterruptedException {
	   	logger.trace("getBalanceAnswerStateProof");
		return getBalance(null, HederaQueryHeader.QueryResponseType.COST_ANSWER_STATE_PROOF);
	}
	
	/**
	 * Runs a query to get records attached to an account 
	 * If successful, the method sets the records property, cost and state proof if requested
	 * @param payment a {@link HederaTransaction} message to indicate how this query will be paid for, this can be null for Cost queries
	 * @param responseType the type of response requested from the query
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication with the node
	 */
	public boolean getRecords(HederaTransaction payment, HederaQueryHeader.QueryResponseType responseType) throws InterruptedException {
		return getRecords(payment, responseType, this.getHederaAccountID());
	}
	/**
	 * Runs a query to get records attached to an account 
	 * If successful, the method sets the records property, cost and state proof if requested
	 * @param payment a {@link HederaTransaction} message to indicate how this query will be paid for, this can be null for Cost queries
	 * @param responseType the type of response requested from the query
	 * @param accountID accountID of the account records being queried
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication with the node
	 */
	public boolean getRecords(HederaTransaction payment, HederaQueryHeader.QueryResponseType responseType, HederaAccountID accountID) throws InterruptedException {
		boolean result = true;
		
	   	logger.trace("Start - getRecords payment {}, responseType {}", payment, responseType);
		// build the query
	   	// Header
		HederaQueryHeader queryHeader = new HederaQueryHeader();
		if (payment != null) {
			queryHeader.payment = payment;
			queryHeader.responseType = responseType;
		}
		
		// get contents query
		CryptoGetAccountRecordsQuery.Builder queryRecords = CryptoGetAccountRecordsQuery.newBuilder();
		
		queryRecords.setAccountID(accountID.getProtobuf());
		queryRecords.setHeader(queryHeader.getProtobuf());
		
		// the query itself
		HederaQuery query = new HederaQuery();
		query.queryType = QueryType.CRYPTOGETACCOUNTRECORDS;
		query.queryData = queryRecords.build();
		
		// query now set, send to network
		Utilities.throwIfNull("Node", this.node);
		Response response = this.node.getAccountRecords(query);
		if (response == null) {
			Utilities.printResponseFailure("HederaAccount.getRecords");
			return false;
		}
		CryptoGetAccountRecordsResponse.Builder queryResponse = response.getCryptoGetAccountRecords().toBuilder();
		
		// check response header first
		ResponseHeader.Builder responseHeader = queryResponse.getHeaderBuilder();

		this.precheckResult = responseHeader.getNodeTransactionPrecheckCode();

		if (this.precheckResult == ResponseCodeEnum.OK) {
			this.records = new ArrayList<HederaTransactionRecord>();
			
			for (int i=0; i < queryResponse.getRecordsCount(); i++) {
				HederaTransactionRecord record = new HederaTransactionRecord(queryResponse.getRecords(i));
				this.records.add(record);
			}
			
			this.cost = responseHeader.getCost();
			this.stateProof = responseHeader.getStateProof().toByteArray();
		} else {
			this.records = null;
			result = false;
		}
		
	   	logger.trace("End - getRecords");
	   	return result;
	}
	/**
	 * Runs a query to get records attached to an account without a state proof 
	 * If successful, the method sets the records property and cost 
	 * @param payment a {@link HederaTransaction} message to indicate how this query will be paid for, this can be null for Cost queries
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication with the node
	 */
	public boolean getRecordsAnswerOnly(HederaTransaction payment) throws InterruptedException {
	   	logger.trace("Start - getRecordsAnswerOnly");
	   	return getRecords(payment, QueryResponseType.ANSWER_ONLY);
	}
	/**
	 * Runs a query to get records attached to an account with a state proof 
	 * If successful, the method sets the records property, cost and state proof 
	 * @param payment a {@link HederaTransaction} message to indicate how this query will be paid for, this can be null for Cost queries
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication with the node
	 */
	public boolean getRecordsStateProof(HederaTransaction payment) throws InterruptedException {
	   	logger.trace("getRecordsStateProof");
		return getRecords(payment, HederaQueryHeader.QueryResponseType.ANSWER_STATE_PROOF);
	}
	/**
	 * Runs a query to find out the cost of getting records attached to an account without a state proof 
	 * If successful, the method sets the cost 
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication with the node
	 */
	public boolean getRecordsCostAnswer() throws InterruptedException {
	   	logger.trace("getRecordsCostAnswer");
		return getRecords(null, HederaQueryHeader.QueryResponseType.COST_ANSWER);
	}
	/**
	 * Runs a query to find out the cost of getting records attached to an account with a state proof 
	 * If successful, the method sets the cost 
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication with the node
	 */
	public boolean getRecordsAnswerStateProof() throws InterruptedException {
	   	logger.trace("getRecordsAnswerStateProof");
		return getRecords(null, HederaQueryHeader.QueryResponseType.COST_ANSWER_STATE_PROOF);
	}
	/**
	 * Runs a query to get information for a given account 
	 * If successful, the method populates the properties this object depending on the type of answer requested
	 * @param payment a {@link HederaTransaction} message to indicate how this query will be paid for, this can be null for Cost queries
	 * @param responseType the type of response requested from the query
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication with the node
	 */
	public boolean getInfo(HederaTransaction payment, HederaQueryHeader.QueryResponseType responseType) throws InterruptedException {
		boolean result = true;
		
	   	logger.trace("Start - getInfo payment {}, responseType {}", payment, responseType);
		// build the query
	   	// Header
		HederaQueryHeader queryHeader = new HederaQueryHeader();
		if (payment != null) {
			queryHeader.payment = payment;
			queryHeader.responseType = responseType;
		}
		
		// get info query
		CryptoGetInfoQuery.Builder accountGetInfoQuery = CryptoGetInfoQuery.newBuilder();
		accountGetInfoQuery.setAccountID(this.getHederaAccountID().getProtobuf());
		accountGetInfoQuery.setHeader(queryHeader.getProtobuf());
		
		// the query itself
		HederaQuery query = new HederaQuery();
		query.queryType = QueryType.CRYPTOGETINFO;
		query.queryData = accountGetInfoQuery.build();
		
		// query now set, send to network
		Utilities.throwIfNull("Node", this.node);
		Response response = this.node.getAccountInfo(query);

		if (response == null) {
			Utilities.printResponseFailure("HederaAccount.getInfo");
			return false;
		}

		CryptoGetInfoResponse.Builder accountGetInfoResponse = response.getCryptoGetInfo().toBuilder();
		
		// check response header first
		ResponseHeader.Builder responseHeader = accountGetInfoResponse.getHeaderBuilder();
		
		this.precheckResult = responseHeader.getNodeTransactionPrecheckCode();

		if (this.precheckResult == ResponseCodeEnum.OK) {

			this.solidityContractAccountID = accountGetInfoResponse.getAccountInfo().getContractAccountID();
			this.deleted = accountGetInfoResponse.getAccountInfo().getDeleted();
			this.proxyAccountID = new HederaAccountID(accountGetInfoResponse.getAccountInfo().getProxyAccountID());
			this.proxyFraction = accountGetInfoResponse.getAccountInfo().getProxyFraction();
			this.proxyReceived = accountGetInfoResponse.getAccountInfo().getProxyReceived();
			this.accountKey = new HederaKey(accountGetInfoResponse.getAccountInfo().getKey());
			this.balance = accountGetInfoResponse.getAccountInfo().getBalance();
			this.receiveRecordThreshold = accountGetInfoResponse.getAccountInfo().getGenerateReceiveRecordThreshold();
			this.sendRecordThreshold = accountGetInfoResponse.getAccountInfo().getGenerateSendRecordThreshold();
			this.receiverSigRequired = accountGetInfoResponse.getAccountInfo().getReceiverSigRequired();
			this.expirationTime = new HederaTimeStamp(accountGetInfoResponse.getAccountInfo().getExpirationTime());
			this.autoRenewPeriod = new HederaDuration(accountGetInfoResponse.getAccountInfo().getAutoRenewPeriod());

			this.claims.clear();
			for (int i=0; i < accountGetInfoResponse.getAccountInfo().getClaimsCount(); i++) {
				HederaClaim claim = new HederaClaim(accountGetInfoResponse.getAccountInfo().getClaims(i));
				this.claims.add(claim);
			}

			this.cost = responseHeader.getCost();
			this.stateProof = responseHeader.getStateProof().toByteArray();

		} else {
			result = false;
		}
		
	   	logger.trace("End - getInfo");
	   	return result;
	}
	/**
	 * Runs a query to get information for a given account, requesting only an answer
	 * If successful, the method populates the properties this object
	 * @param payment the {@link HederaTransaction} payload containing payment information for the query
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getInfoAnswerOnly(HederaTransaction payment) throws InterruptedException {
	   	logger.trace("Start - getInfoAnswerOnly");
	   	return getInfo(payment, QueryResponseType.ANSWER_ONLY);
	}
	/**
	 * Runs a query to get information for a given account, requesting a stateproof
	 * If successful, the method populates the properties this object including a state proof
	 * @param payment the {@link HederaTransaction} payload containing payment information for the query
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getInfoStateProof(HederaTransaction payment) throws InterruptedException {
	   	logger.trace("getInfoStateProof");
		return getInfo(payment, HederaQueryHeader.QueryResponseType.ANSWER_STATE_PROOF);
	}
	/**
	 * Runs a query to get the cost of getting information for a given account without a state proof
	 * If successful, the method populates the cost property of this object
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getInfoCostAnswer() throws InterruptedException {
	   	logger.trace("getInfoCostAnswer");
		return getInfo(null, HederaQueryHeader.QueryResponseType.COST_ANSWER);
	}
	/**
	 * Runs a query to get the cost of getting information for a given account with a state proof
	 * If successful, the method populates the cost property of this object
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getInfoCostAnswerStateProof() throws InterruptedException {
	   	logger.trace("getInfoCostAnswerStateProof");
		return getInfo(null, HederaQueryHeader.QueryResponseType.COST_ANSWER_STATE_PROOF);
	}
	/**
	 * This method returns the {@link CryptoCreateTransactionBody} body for a transaction to create an account
	 * @return {@link CryptoCreateTransactionBody}
	 */
	public CryptoCreateTransactionBody getCreateTransactionBody() {
	   	logger.trace("Start - getCreateTransactionBody");
	   	
	   	CryptoCreateTransactionBody.Builder transactionBody = CryptoCreateTransactionBody.newBuilder();
	   	transactionBody.setAutoRenewPeriod(this.autoRenewPeriod.getProtobuf());
	   	transactionBody.setInitialBalance(this.initialBalance);
	   	transactionBody.setReceiverSigRequired(this.receiverSigRequired);
   		transactionBody.setMaxReceiveProxyFraction(this.maxReceiveProxyFraction);
   		transactionBody.setReceiveRecordThreshold(this.receiveRecordThreshold);
   		transactionBody.setSendRecordThreshold(this.sendRecordThreshold);
	   	
	   	if (this.accountKeySig != null) {
	   		transactionBody.setKey(this.accountKeySig.getKeyProtobuf());
	   	} else if (this.accountKey != null) {
	   		transactionBody.setKey(this.accountKey.getProtobuf());
	   	}
	   	
	   	if (this.realmNum > 0) {
	   		transactionBody.setRealmID(new HederaRealmID(this.shardNum, this.realmNum).getProtobuf());
	   	} else if (this.realmNum == -1) {
		   	if (this.newRealmAdminKeySig != null) {
		   		transactionBody.setNewRealmAdminKey(newRealmAdminKeySig.getKeyProtobuf());
		   	} else if (this.newRealmAdminKey != null) {
		   		transactionBody.setNewRealmAdminKey(newRealmAdminKey.getProtobuf());
		   	}
	   	}
	   	if (this.proxyAccountID != null) {
	   		transactionBody.setProxyAccountID(this.proxyAccountID.getProtobuf());
	   	}
	   	if (this.shardNum > 0) {
	   		transactionBody.setShardID(new HederaShardID(this.shardNum).getProtobuf());
	   	}

	   	logger.trace("End - getCreateTransactionBody");
		return transactionBody.build();
	}

	/**
	 * This method returns the {CryptoTransferTransactionBody} body for a transaction to transfer funds between accounts
	 * @param accountAmounts the amounts to transfer between accounts
	 * @return {@link CryptoTransferTransactionBody}
	 */
	public CryptoTransferTransactionBody getTransferTransactionBody(ArrayList<HederaAccountAmount>accountAmounts) {
	   	logger.trace("Start - getTransferTransactionBody");
	   	
	   	CryptoTransferTransactionBody.Builder transactionBody = CryptoTransferTransactionBody.newBuilder();
	   	TransferList.Builder transferList = TransferList.newBuilder();
	   	for (HederaAccountAmount accountAmount: accountAmounts) {
	   		transferList.addAccountAmounts(accountAmount.getProtobuf());
	   	}
	   	
	   	transactionBody.setTransfers(transferList);

	   	logger.trace("End - getTransferTransactionBody");
		return transactionBody.build();
	}
	/**
	 * This method returns the {CryptoUpdateTransactionBody} body for a transaction to update an account
	 * @return {@link CryptoUpdateTransactionBody}
	 */
	public CryptoUpdateTransactionBody getUpdateTransactionBody() {
	   	logger.trace("Start - getUpdateTransactionBody");
		CryptoUpdateTransactionBody.Builder updateTransaction = CryptoUpdateTransactionBody.newBuilder();
		
		updateTransaction.setAccountIDToUpdate(this.getHederaAccountID().getProtobuf());
		if (this.autoRenewPeriod != null) {
			updateTransaction.setAutoRenewPeriod(this.autoRenewPeriod.getProtobuf());
		}
		if (this.expirationTime != null) {
			updateTransaction.setExpirationTime(this.expirationTime.getProtobuf());
		}
		if (this.newAccountKey != null) {
			updateTransaction.setKey(this.newAccountKey.getProtobuf());
		}
		if (this.proxyAccountID != null) {
			updateTransaction.setProxyAccountID(this.proxyAccountID.getProtobuf());
		}
		if (this.proxyFraction != 0) {
			updateTransaction.setProxyFraction(this.proxyFraction);
		}
		if (this.receiveRecordThreshold != 0) {
			updateTransaction.setReceiveRecordThreshold(this.receiveRecordThreshold);
		}
		if (this.sendRecordThreshold != 0) {
			updateTransaction.setSendRecordThreshold(this.sendRecordThreshold);
		}
		
	   	logger.trace("End - getUpdateTransactionBody");
		return updateTransaction.build();
	}
	/**
	 * This method returns the {CryptoDeleteTransactionBody} body for a transaction to delete an account
	 * @param transferAccountID The account to transfer remaining funds to
	 * @return {@link CryptoDeleteTransactionBody}
	 */
	public CryptoDeleteTransactionBody getDeleteTransactionBody(HederaAccountID transferAccountID) {
	   	logger.trace("Start - getDeleteTransactionBody");
		// Generates the protobuf payload for this class
	   	CryptoDeleteTransactionBody.Builder transactionBody = CryptoDeleteTransactionBody.newBuilder();
		
	   	transactionBody.setDeleteAccountID(this.getHederaAccountID().getProtobuf());
	   	if (transferAccountID != null) {
	   		transactionBody.setTransferAccountID(transferAccountID.getProtobuf());
	   	}
	   	
	   	logger.trace("End - getDeleteTransactionBody");
		return transactionBody.build();
	}
	/**
	 * This method returns the {CryptoAddClaimTransactionBody} body for a transaction to add a claim to an account
	 * @param claim the claim to add
	 * @return {@link CryptoAddClaimTransactionBody}
	 */
	public CryptoAddClaimTransactionBody getAddClaimTransactionBody(HederaClaim claim) {
	   	logger.trace("Start - getAddClaimTransactionBody");
		CryptoAddClaimTransactionBody.Builder transaction = CryptoAddClaimTransactionBody.newBuilder();
		
		transaction.setAccountID(this.getHederaAccountID().getProtobuf());
		transaction.setClaim(claim.getProtobuf());
		
	   	logger.trace("End - getAddClaimTransactionBody");
		return transaction.build();
	}
	/**
	 * This method returns the {CryptoDeleteClaimTransactionBody} body for a transaction to delete a claim from an account
	 * @param claim the claim to delete
	 * @return {@link CryptoDeleteClaimTransactionBody}
	 */
	public CryptoDeleteClaimTransactionBody getDeleteClaimTransactionBody(HederaClaim claim) {
	   	logger.trace("Start - getDeleteClaimTransactionBody");
	   	CryptoDeleteClaimTransactionBody.Builder transaction = CryptoDeleteClaimTransactionBody.newBuilder();
		
		transaction.setAccountIDToDeleteFrom(this.getHederaAccountID().getProtobuf());
		transaction.setHashToDelete(ByteString.copyFrom(claim.hash));
		
	   	logger.trace("End - getDeleteClaimTransactionBody");
		return transaction.build();
	}
	/**
	 * Adds a {@link HederaKey} to the list
	 * @param key the key to add
	 */
	public void addKey(HederaKey key) {
	   	logger.trace("Start - addKey key {}", key);
		this.keys.add(key);
	   	logger.trace("End - addKey");
	}
	/**
	 * Adds a {@link HederaKeySignature} to the list
	 * @param keySigPair the key signature pair to add
	 */
	public void addKeySignaturePair(HederaKeySignature keySigPair) {
	   	logger.trace("addKey keySigPair {}", keySigPair);
		this.keySignatures.add(keySigPair);
	   	logger.trace("End - addKey");
	}
	/**
	 * Deletes a {@link HederaKey} from the list
	 * @param key the key to remove
	 * @return boolean true if successful
	 */
	public boolean deleteKey(HederaKey key) {
	   	logger.trace("deleteKey key {}", key);
		return this.keys.remove(key);
	}
	/**
	 * Deletes a {@link HederaKeySignature} from the list
	 * @param keySigPair the key signature pair to remove
	 * @return boolean true if successful
	 */
	public boolean deleteKeySignaturePair(HederaKeySignature keySigPair) {
	   	logger.trace("deleteKeySignaturePair {}", keySigPair);
		return this.keySignatures.remove(keySigPair);
	}
	/**
	 * returns the list of {@link HederaKey}
	 * @return List {@link HederaKey}
	 */
	public List<HederaKey> getKeys() {
	   	logger.trace("getKeys");
		return this.keys;
	}
	/**
	 * returns the list of {@link HederaKeySignature}
	 * @return List {@link HederaKeySignature}
	 */
	public List<HederaKeySignature> getKeySignatures() {
	   	logger.trace("getKeySignatures");
		return this.keySignatures;
	}

	/**
	 * Creates an account in the simplest possible way
	 * @param shardNum the shard number for the new account
	 * @param realmNum the realm number for the new account
	 * @param publicKey the public key for the new account
	 * @param keyType the type of the account's public key
	 * @param initialBalance the initial balance for the new account
	 * @param defaults the defaults for the account creation (can be null)
	 * @return {@link HederaTransactionResult}
	 * @throws Exception 
	 */
	public HederaTransactionResult create(long shardNum, long realmNum, byte[] publicKey, KeyType keyType, long initialBalance, HederaAccountCreateDefaults defaults) throws Exception {
	   	logger.trace("Start - createAccount shardNum {}, realmNum {}, publicKey {}, keyType {}, initialBalance {}, defaults {}"
	   			, shardNum, realmNum, publicKey, keyType, initialBalance, defaults);
		// setup defaults if necessary
		if (defaults != null) {
			this.proxyAccountID = defaults.getProxyAccountID();
			this.proxyFraction = defaults.proxyFraction;
			this.maxReceiveProxyFraction = defaults.maxReceiveProxyFraction;
			this.sendRecordThreshold = defaults.sendRecordThreshold;
			this.receiveRecordThreshold = defaults.receiveRecordThreshold;
			this.receiverSigRequired = defaults.receiverSignatureRequired;
			this.autoRenewPeriod = new HederaDuration(defaults.autoRenewPeriodSeconds, defaults.autoRenewPeriodNanos);
			this.newRealmAdminKey = defaults.getNewRealmAdminPublicKey();
		}
		
		// initialise the result
		HederaTransactionResult transactionResult = new HederaTransactionResult();

		// required
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.accountKey = new HederaKey(keyType, publicKey);
		this.initialBalance = initialBalance;

		// validate inputs
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", this.txQueryDefaults.node);
		Utilities.throwIfAccountIDInvalid("txQueryDefaults.payingAccountID", this.txQueryDefaults.payingAccountID);
		Utilities.throwIfAccountIDInvalid("txQueryDefaults.node.AccountID", this.txQueryDefaults.node.getAccountID());
		
		// set transport
		this.node = this.txQueryDefaults.node;
		
		// create a transaction ID (starts now with accountID of the paying account id)
		this.hederaTransactionID = new HederaTransactionID(this.txQueryDefaults.payingAccountID);
		

		// get the body for the transaction so we can sign it
		TransactionBody createBody = this.bodyToSignForCreate(
				this.hederaTransactionID
				, this.node.getAccountID()
				, this.node.accountCreateTransactionFee
				, this.txQueryDefaults.transactionValidDuration
				, this.txQueryDefaults.generateRecord
				, this.txQueryDefaults.memo);

		// PAYING ACCOUNT
		// get the signature for the body
		byte[] signedBody;
		signedBody = this.txQueryDefaults.payingKeyPair.signMessage(createBody.toByteArray());
		// create a Hedera Signature for it
		HederaSignature signature = new HederaSignature(this.txQueryDefaults.payingKeyPair.getKeyType(), signedBody);

		HederaSignatureList sigList = new HederaSignatureList();
		sigList.addSignature(signature);
		
		HederaSignature signatureAsList = new HederaSignature(sigList);
		
		// put the signatures in a signature list
		HederaSignatureList sigsForTransaction = new HederaSignatureList();
		sigsForTransaction.addSignature(signatureAsList);

//		sigsForTransaction.addSignature(sigsForTransaction);
		
//		sigs {
//			  sigs {
//			    signatureList {
//			      sigs {
//			        ed25519: "\2320\377knj\275\270\266]/\346{\000\251l\201\035\273\345\225\224\212\367\024k\320\266\374\213S\256\3577w\022\'8\271\332\036\034f\267\b\213\315\277\341\354\367\253\364+\261\"|\a\355W(W\256\a"
//			      }
//			    }
//			  }
//			}
		
		// create the account
		transactionResult = this.create(
				this.hederaTransactionID
				, this.node.getAccountID()
				, this.node.accountCreateTransactionFee
				, this.txQueryDefaults.transactionValidDuration
				, this.txQueryDefaults.generateRecord
				, this.txQueryDefaults.memo
				, sigsForTransaction);
		
	   	logger.trace("End - createAccount");
		return transactionResult;
	}
	
	private HederaKeySignatureList signBody(byte[] message) throws Exception {
		// get the signature for the body
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.payingKeyPair", this.txQueryDefaults.payingKeyPair);
		
		byte[] signedBody = this.txQueryDefaults.payingKeyPair.signMessage(message);
		// create a Hedera Signature for it
		HederaSignature payingSignature = new HederaSignature(this.txQueryDefaults.payingKeyPair.getKeyType(), signedBody);
		// put the signatures in a signature list
		HederaKeySignatureList sigsForTransaction = new HederaKeySignatureList();
		sigsForTransaction.addKeySignaturePair(this.txQueryDefaults.payingKeyPair.getKeyType(), this.txQueryDefaults.payingKeyPair.getPublicKey(), payingSignature.getSignature());
		
		return sigsForTransaction;
	}
	
	private void signClaim(HederaCryptoKeyPair claimKeyPair, byte[] message, HederaKeySignatureList sigList) throws Exception {
		// sign as the claim owner
		byte[] signedBody = claimKeyPair.signMessage(message);
		// create a Hedera Signature for it
		HederaSignature claimSignature = new HederaSignature(claimKeyPair.getKeyType(), signedBody);
		// put the signature in a signature list
		sigList.addKeySignaturePair(claimKeyPair.getKeyType(), claimKeyPair.getPublicKey(), claimSignature.getSignature());
	}
	
	/** Send an amount of crypto currency to an account
	 * The paying account is the same as the one paying for the transaction
	 * @param toAccountID, the accountID receiving the funds
	 * @param amount, the amount to transfer
	 * @return {@link HederaTransactionResult}
	 * @throws Exception 
	 */
	public HederaTransactionResult send(HederaAccountID toAccountID, long amount) throws Exception {
	   	logger.trace("Start - send toAccountID {}, amount {}"
	   			, toAccountID, amount);
		
		// initialise the result
		HederaTransactionResult transactionResult = new HederaTransactionResult();
		
		// validate inputs
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", this.txQueryDefaults.node);
		Utilities.throwIfNull("txQueryDefaults.payingKeyPair", this.txQueryDefaults.payingKeyPair);
		Utilities.throwIfAccountIDInvalid("txQueryDefaults.payingAccountID", this.txQueryDefaults.payingAccountID);
		Utilities.throwIfAccountIDInvalid("txQueryDefaults.node.AccountID", this.txQueryDefaults.node.getAccountID());

		// set transport
		this.node = this.txQueryDefaults.node;

		// create a transaction ID (starts now with accountID of the paying account id)
		this.hederaTransactionID = new HederaTransactionID(this.txQueryDefaults.payingAccountID);

		ArrayList<HederaAccountAmount> accountAmounts = new ArrayList<HederaAccountAmount>();
		// add the two accounts and amounts to the array list
		HederaAccountAmount fromAccountAmount = null;
		// negative amount from the account
		 fromAccountAmount = new HederaAccountAmount(this.txQueryDefaults.payingAccountID.shardNum, this.txQueryDefaults.payingAccountID.realmNum, this.txQueryDefaults.payingAccountID.accountNum, -amount);

		 // positive amount to the account
		HederaAccountAmount toAccountAmount = new HederaAccountAmount(toAccountID.shardNum, toAccountID.realmNum, toAccountID.accountNum, amount);
		
		accountAmounts.add(fromAccountAmount);
		accountAmounts.add(toAccountAmount);

		Utilities.throwIfAccountIDInvalid("Node", this.node.getAccountID());
		
		// get the body for the transaction so we can sign it
		TransactionBody transferBody = this.bodyToSignForTransfer(
				this.hederaTransactionID
				, this.node.getAccountID()
				, this.node.accountTransferTransactionFee
				, this.txQueryDefaults.transactionValidDuration
				, this.txQueryDefaults.generateRecord
				, this.txQueryDefaults.memo
				, accountAmounts);
		
		HederaSignatureList sigsForTransaction = new HederaSignatureList(); //(transferBody.toByteArray());
		
		byte[] signedBody = this.txQueryDefaults.payingKeyPair.signMessage(transferBody.toByteArray());
		// create a Hedera Signature for it
		HederaSignature payingSignature = new HederaSignature(this.txQueryDefaults.payingKeyPair.getKeyType(), signedBody);
		HederaSignatureList sigList = new HederaSignatureList();
		sigList.addSignature(payingSignature);
		HederaSignature sigForList = new HederaSignature(sigList);
		
		// put the signatures in a signature list
		sigsForTransaction.addSignature(sigForList);// .addKeySignaturePair(this.txQueryDefaults.payingKeyPair.getKeyType(), this.txQueryDefaults.payingKeyPair.getPublicKey(), payingSignature.getSignature());

		// transfer the crypto currency
		transactionResult = this.transfer(
				this.hederaTransactionID
				, this.node.getAccountID()
				, this.node.accountTransferTransactionFee
				, this.txQueryDefaults.transactionValidDuration
				, this.txQueryDefaults.generateRecord
				, this.txQueryDefaults.memo
				, sigsForTransaction
				, accountAmounts);

	   	logger.trace("End - send");
		
		return transactionResult;
	}

	/** Send an amount of crypto currency to an account
	 * The paying account is the same as the one paying for the transaction
	 * @param fromAccountShardNum, the shard number of the sending account
	 * @param fromAccountRealmNum, the realm number of the sending account
	 * @param fromAccountAccountNum, the account number of the sending account
	 * @param toAccountID, the accountID receiving the funds
	 * @param amount, the amount to transfer
	 * @return {@link HederaTransactionResult}
	 * @throws Exception 
	 */
	public HederaTransactionResult send(long fromAccountShardNum, long fromAccountRealmNum, long fromAccountAccountNum, HederaAccountID toAccountID, long amount) throws Exception {
		this.shardNum = fromAccountAccountNum;
		this.realmNum = fromAccountRealmNum;
		this.accountNum = fromAccountAccountNum;
		return send(toAccountID, amount);
	}
	
	/** Adds a claim to an account
	 * @param claimToAdd the {@link HederaClaim} to add to the account
	 * @param claimKeyPair the keypair for the claim
	 * @return {@link HederaTransactionResult}
	 * @throws Exception 
	 */
	public HederaTransactionResult addClaim(HederaClaim claimToAdd, HederaCryptoKeyPair claimKeyPair) throws Exception {
	   	logger.trace("Start - addClaim claimToAdd {}, claimKeyPair {}"
	   			, claimToAdd, claimKeyPair);
		
		// initialise the result
		HederaTransactionResult transactionResult = new HederaTransactionResult();
		
		// validate inputs
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", this.txQueryDefaults.node);
		Utilities.throwIfAccountIDInvalid("txQueryDefaults.payingAccountID", this.txQueryDefaults.payingAccountID);
		Utilities.throwIfAccountIDInvalid("txQueryDefaults.node.AccountID", this.txQueryDefaults.node.getAccountID());

		// set transport
		this.node = this.txQueryDefaults.node;
		
		// create a transaction ID (starts now with accountID of the paying account id)
		this.hederaTransactionID = new HederaTransactionID(this.txQueryDefaults.payingAccountID);

		Utilities.throwIfAccountIDInvalid("Node", this.node.getAccountID());

		// get the body for the transaction so we can sign it
		TransactionBody claimBody = this.bodyToSignForAddClaim(
				this.hederaTransactionID
				, this.node.getAccountID()
				, this.node.accountAddClaimTransactionFee
				, this.txQueryDefaults.transactionValidDuration
				, this.txQueryDefaults.generateRecord
				, this.txQueryDefaults.memo
				, claimToAdd);
		
		HederaKeySignatureList sigsForTransaction = signBody(claimBody.toByteArray());
		signClaim(claimKeyPair, claimBody.toByteArray(), sigsForTransaction);

		// transfer the crypto currency
		transactionResult = this.addClaim(
				this.hederaTransactionID
				, this.node.getAccountID()
				, this.node.accountAddClaimTransactionFee
				, this.txQueryDefaults.transactionValidDuration
				, this.txQueryDefaults.generateRecord
				, this.txQueryDefaults.memo
				, sigsForTransaction
				, claimToAdd);
		
	   	logger.trace("End - addClaim");
		return transactionResult;
	}

	/** Adds a claim to an account
	 * @param shardNum, the shard number of the account to add a claim to
	 * @param realmNum, the realm number of the account to add a claim to
	 * @param accountNum, the account number of the account to add a claim to
	 * @param claimToAdd the {@link HederaClaim} to add to the account
	 * @param claimKeyPair the keypair for the claim
	 * @return {@link HederaTransactionResult}
	 * @throws Exception 
	 */
	public HederaTransactionResult addClaim(long shardNum, long realmNum, long accountNum, HederaClaim claimToAdd, HederaCryptoKeyPair claimKeyPair) throws Exception {
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.accountNum = accountNum;
		return addClaim(claimToAdd, claimKeyPair);
	}
	
	/** 
	 * Gets the balance of the account, returns -1 if an error occurred
	 * in the event of an error, check the value of this.precheckResult to determine the 
	 * cause of the error
	 * Note: You may perform a "getBalanceCostAnswer" in order to ascertain the cost of the query first
	 * The cost could be cached and refreshed from time to time, there is no need to look it up 
	 * before each getBalance query
	 * @return {@link Long}
	 * @throws Exception 
	 */
	public long getBalance() throws Exception {
	   	logger.trace("Start - getBalance");
		// set transport
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", this.txQueryDefaults.node);
		this.node = this.txQueryDefaults.node;
		
		HederaTransaction transferTransaction = new HederaTransaction(this.txQueryDefaults,this.node.accountBalanceQueryFee);

		if (this.getBalanceAnswerOnly(transferTransaction)) {
		   	logger.trace("End - getBalance");
			return this.balance;
		} else {
		   	logger.trace("End - getBalance");
			return -1;
		}
	}
	/** 
	 * Gets the balance of the account, returns -1 if an error occurred
	 * in the event of an error, check the value of this.precheckResult to determine the 
	 * cause of the error
	 * Note: You may perform a "getBalanceCostAnswer" in order to ascertain the cost of the query first
	 * The cost could be cached and refreshed from time to time, there is no need to look it up 
	 * before each getBalance query
	 * @param shardNum, the shard number of the account 
	 * @param realmNum, the realm number of the account remove the claim from
	 * @param accountNum, the account number of the account
	 * @return {@link Long} 
	 * @throws Exception 
	 */
	public long getBalance(long shardNum, long realmNum, long accountNum) throws Exception {
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.accountNum = accountNum;
		return getBalance();
	}

	/**
	 * Get info for the account which is specified by the
	 * shardNum, realmNum and accountNum properties of this class
	 * in the event of an error, check the value of this.precheckResult to determine the 
	 * cause of the error
	 * Note: You may perform a "getInfoCostAnswer" in order to ascertain the cost of the query first
	 * The cost could be cached and refreshed from time to time, there is no need to look it up 
	 * before each getInfo query
	 * @return boolean
	 * @throws Exception 
	 */
	public boolean getInfo() throws Exception {
	   	logger.trace("Start - getInfo");
		// set transport
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", this.txQueryDefaults.node);
		this.node = this.txQueryDefaults.node;

		HederaTransaction transferTransaction = new HederaTransaction(this.txQueryDefaults, this.node.accountInfoQueryFee);
	   	logger.trace("End - getInfo");
		return this.getInfoAnswerOnly(transferTransaction);
	}

	/** 
	 * Get info for the account 
	 * in the event of an error, check the value of this.precheckResult to determine the 
	 * cause of the error
	 * Note: You may perform a "getInfoCostAnswer" in order to ascertain the cost of the query first
	 * The cost could be cached and refreshed from time to time, there is no need to look it up 
	 * before each getInfo query
	 * @param shardNum, the shard number of the account 
	 * @param realmNum, the realm number of the account remove the claim from
	 * @param accountNum, the account number of the account 
	 * @return boolean
	 * @throws Exception 
	 */
	public boolean getInfo(long shardNum, long realmNum, long accountNum) throws Exception {
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.accountNum = accountNum;
		return getInfo();
	}

	/**
	 * Updates an account in the simplest possible way
	 * Make sure the supplied HederaAccountUpdateValues (updates) contains all the necessary updates
	 * It is also recommended you create a new account object prior to running this call to ensure properties from an older instance are
	 * used to update this account's properties
	 * @param updates the updates to apply to the account
	 * @return {@link HederaTransactionResult}
	 * @throws Exception 
	 */
	public HederaTransactionResult update(HederaAccountUpdateValues updates) throws Exception {
	   	logger.trace("Start - getInfo updates {}", updates);
		// init
		// setup defaults if necessary
		if (updates == null) {
			throw new InvalidParameterException("No values to update supplied");
		} else {
			if ((updates.autoRenewPeriodSeconds != -1) && (updates.autoRenewPeriosNanos != -1)) {
				this.autoRenewPeriod = new HederaDuration(updates.autoRenewPeriodSeconds, updates.autoRenewPeriosNanos);
			}
			if ((updates.expirationTimeSeconds != -1) && (updates.expirationTimeNanos != -1)) {
				this.expirationTime = new HederaTimeStamp(updates.expirationTimeSeconds, updates.expirationTimeNanos);
			}
			if (updates.newKey != null) {
				this.newAccountKey = new HederaKey(updates.newKey.getKeyType(), updates.newKey.getPublicKey());
			} else {
				this.newAccountKey = null;
			}
			if ((updates.proxyAccountAccountNum != 0) && (updates.proxyAccountRealmNum != 0) && (updates.proxyAccountShardNum != 0)) {
				this.proxyAccountID = new HederaAccountID(updates.proxyAccountShardNum, updates.proxyAccountRealmNum, updates.proxyAccountAccountNum);
			}
			if (updates.proxyFraction != 0) {
				this.proxyFraction = updates.proxyFraction;
			}
			if (updates.receiveRecordThreshold != 0) {
				this.receiveRecordThreshold = updates.receiveRecordThreshold;
			}
			if (updates.sendRecordThreshold != 0) {
				this.sendRecordThreshold = updates.sendRecordThreshold;
			}
		}
		// initialise the result
		HederaTransactionResult transactionResult = new HederaTransactionResult();
		
		// validate inputs
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", this.txQueryDefaults.node);
		Utilities.throwIfNull("txQueryDefaults.payingKeyPair", this.txQueryDefaults.payingKeyPair);
		Utilities.throwIfAccountIDInvalid("txQueryDefaults.payingAccountID", this.txQueryDefaults.payingAccountID);
		Utilities.throwIfAccountIDInvalid("txQueryDefaults.node.AccountID", this.txQueryDefaults.node.getAccountID());

		// set transport
		this.node = this.txQueryDefaults.node;
		
		// create a transaction ID (starts now with accountID of the paying account id)
		this.hederaTransactionID = new HederaTransactionID(this.txQueryDefaults.payingAccountID);

		Utilities.throwIfAccountIDInvalid("Node", this.node.getAccountID());

		// build body
		// get the body for the transaction so we can sign it
		TransactionBody updateBody = this.bodyToSignForUpdate(
				this.hederaTransactionID
				, this.node.getAccountID()
				, this.node.accountUpdateTransactionFee
				, this.txQueryDefaults.transactionValidDuration
				, this.txQueryDefaults.generateRecord
				, this.txQueryDefaults.memo
		);

		// put the signatures in a signature list
		HederaSignatureList sigList = new HederaSignatureList();

		// sign with old key for payment
		sigList.addSignature(Utilities.getSignature(updateBody.toByteArray(), this.txQueryDefaults.payingKeyPair));

		// sign with old key for change
		sigList.addSignature(Utilities.getSignature(updateBody.toByteArray(), this.txQueryDefaults.payingKeyPair));

		//+ new key if necessary
		if (updates.newKey != null) {
			sigList.addSignature(Utilities.getSignature(updateBody.toByteArray(), updates.newKey));
		}
		
		HederaSignature sigForList = new HederaSignature(sigList);
		
		HederaSignatureList sigsForTransaction = new HederaSignatureList();
		sigsForTransaction.addSignature(sigForList);

		// send
		transactionResult = this.update(
				this.hederaTransactionID
				, this.node.getAccountID()
				, this.node.accountUpdateTransactionFee
				, this.txQueryDefaults.transactionValidDuration
				, this.txQueryDefaults.generateRecord
				, this.txQueryDefaults.memo
				, sigsForTransaction);
		
	   	logger.trace("End - Update");
		return transactionResult;
	}

	/**
	 * Updates an account in the simplest possible way
	 * Make sure the supplied HederaAccountUpdateValues (updates) contains all the necessary updates
	 * It is also recommended you create a new account object prior to running this call to ensure properties from an older instance are
	 * used to update this account's properties
	 * @param shardNum, the shard number of the account 
	 * @param realmNum, the realm number of the account remove the claim from
	 * @param accountNum, the account number of the account
	 * @param updates, a set of {@link HederaAccountUpdateValues} to update the account with
	 * @return {@link HederaTransactionResult} 
	 * @throws Exception 
	 */
	public HederaTransactionResult update(long shardNum, long realmNum, long accountNum, HederaAccountUpdateValues updates) throws Exception {
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.accountNum = accountNum;
		return update(updates);
	}
	
	/**
	 * Get records attached to this account
	 * Note: If no records are found, the function returns an empty array
	 * if however an error occurred, it will return null
	 * @return {@link List} of {@link HederaTransactionRecord}
	 * @throws Exception 
	 */
	public List<HederaTransactionRecord> getRecords() throws Exception {
	   	logger.trace("Start - getRecords");
		// set transport
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", this.txQueryDefaults.node);
		this.node = this.txQueryDefaults.node;
		
		HederaTransaction transferTransaction = new HederaTransaction(this.txQueryDefaults, this.node.accountGetRecordsQueryFee);
		getRecordsAnswerOnly(transferTransaction);
	   	logger.trace("End - getRecords");
		return this.records;
	}	
	/**
	 * Get records attached to this account
	 * Note: If no records are found, the function returns an empty array
	 * if however an error occurred, it will return null
	 * @param shardNum, the shard number of the account 
	 * @param realmNum, the realm number of the account remove the claim from
	 * @param accountNum, the account number of the account
	 * @return {@link List} of {@link HederaTransactionRecord}
	 * @throws Exception 
	 */
	public List<HederaTransactionRecord> getRecords(long shardNum, long realmNum, long accountNum) throws Exception {
		HederaAccount recordAccount = new HederaAccount(shardNum, realmNum, accountNum);
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("node", this.node);
		
		HederaTransaction transferTransaction = new HederaTransaction(this.txQueryDefaults, this.node.accountGetRecordsQueryFee);
		if (getRecords(transferTransaction, QueryResponseType.ANSWER_ONLY, recordAccount.getHederaAccountID())) {
			return this.records;
		}
		else return null;
	}	
}
