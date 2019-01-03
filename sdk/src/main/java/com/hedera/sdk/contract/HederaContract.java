package com.hedera.sdk.contract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;
import com.google.protobuf.ByteString;
import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaContractID;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaFileID;
import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaRealmID;
import com.hedera.sdk.common.HederaTransactionRecord;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.node.HederaNode;
import com.hedera.sdk.common.HederaShardID;
import com.hedera.sdk.common.HederaSignatureList;
import com.hedera.sdk.common.HederaTimeStamp;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.common.HederaTransactionID;
import com.hedera.sdk.query.HederaQuery;
import com.hedera.sdk.query.HederaQueryHeader;
import com.hedera.sdk.query.HederaQuery.QueryType;
import com.hedera.sdk.query.HederaQueryHeader.QueryResponseType;
import com.hedera.sdk.transaction.HederaTransaction;
import com.hedera.sdk.transaction.HederaTransactionBody;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hedera.sdk.transaction.HederaTransactionBody.TransactionType;
import com.hederahashgraph.api.proto.java.ContractCallLocalQuery;
import com.hederahashgraph.api.proto.java.ContractCallLocalResponse;
import com.hederahashgraph.api.proto.java.ContractCallTransactionBody;
import com.hederahashgraph.api.proto.java.ContractCreateTransactionBody;
import com.hederahashgraph.api.proto.java.ContractGetBytecodeQuery;
import com.hederahashgraph.api.proto.java.ContractGetBytecodeResponse;
import com.hederahashgraph.api.proto.java.ContractGetInfoQuery;
import com.hederahashgraph.api.proto.java.ContractGetInfoResponse;
import com.hederahashgraph.api.proto.java.ContractUpdateTransactionBody;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
import com.hederahashgraph.api.proto.java.ResponseHeader;
import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.ContractGetInfoResponse.ContractInfo;

/**
 * This class manages all aspects of interacting with a Smart Contract on Hedera Hashgraph 
 */
public class HederaContract implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaContract.class);
	private static final long serialVersionUID = 1;
	private HederaNode node = null;
	private ResponseCodeEnum precheckResult = ResponseCodeEnum.UNKNOWN;
	private byte[] stateProof = new byte[0];
	// keys for signatures
	private List<HederaKeyPair> keys = new ArrayList<HederaKeyPair>();
	private String solidityContractAccountID = "";
	private long storage = 0;
	private byte[] byteCode = new byte[0];
	private List<HederaTransactionRecord> transactionRecords = new ArrayList<HederaTransactionRecord>();
	private long cost = 0;
	private HederaContractFunctionResult hederaContractFunctionResult = null;
	/**
	 * Default parameters for a transaction or query
	 */
	public HederaTransactionAndQueryDefaults txQueryDefaults = new HederaTransactionAndQueryDefaults();
	/**
	 * The {@link HederaTransactionID} of the last transaction to be run here
	 */
	public HederaTransactionID hederaTransactionID = null;
	/** 
	 * the shard number for the smart contract
	 */
	public long shardNum = 0;
	/**
	 * the realm number for the smart contract
	 * realm in which to create this (leave this null to create a new realm)
	 */
	public long realmNum = 0;
	/**
	 * the smart contract number
	 */
	public long contractNum = 0;
	/**
	 * The contract's account's shard number
	 */
	public long contractAccountShardNum = 0;
	/**
	 * The contract's account's realm number
	 */
	public long contractAccountRealmNum = 0;
	/**
	 * The contract's account number
	 */
	public long contractAccountAccountNum = 0;
	/**
	 * The {@link HederaKeyPair} representing the realm's administration key
	 * initially null
	 * if realmID is null, then this the admin key for the new realm that will be created
	 */
	public HederaKeyPair newRealmAdminKey = null;
	/**
	 * The {@link HederaKeyPair} representing the contract's administration key
	 * initially null
	 * the state of the instance and its fields can be modified arbitrarily if this key signs a transaction to modify it. 
	 * If this is null, then such modifications are not possible, and there is no administrator that can override the normal 
	 * operation of this smart contract instance. Note that if it is created with no admin keys, then there is no administrator to 
	 * authorize changing the admin keys, so there can never be any admin keys for that instance.
	 */
	public HederaKeyPair adminKey = null;
	/**
	 * the gas to pay to run a smart contract or smart contract function
	 */
	public long gas = 0;
	/**
	 * initial number of tinybars to put into the cryptocurrency account associated with and owned by the smart contract
	 */
	public long amount = 0;
	/**
	 * Parameters supplied when running a function 
	 * (in the solidity format)
	 */
	public byte[] functionParameters = new byte[0];
	/**
	 * The {@link HederaFileID} containing the smart contract's bytecode
	 * initially null
	 * A copy will be made and held by the contract instance, and have the same expiration time as the instance.
	 */
	public HederaFileID fileID = null;
	/**
	 * The initial balance of the smart contract's account
	 */
	public long initialBalance = 0;
	/**
	 * ID of the account to which this account is proxy staked. If proxyAccountID is null, or is an invalid account, 
	 * or is an account that isn't a node, then this account is automatically proxy staked to a node chosen by the network, 
	 * but without earning payments. If the proxyAccountID account refuses to accept proxy staking at the given fraction, 
	 * or if it is not currently running a node, then it will behave as if both proxyAccountID and proxyFraction were null.	 
	 * initially null
	 */
	public HederaAccountID proxyAccountID = null;
	/**
	 * payments earned from proxy staking are shared between the node and this instance's account, with proxyFraction / 10000 going to this account
	 */
	public int proxyFraction = 0;
	/**
	 * A {@link HederaDuration} representing the auto renew period of a smart contract
	 * initially null
	 * the instance will charge its account every this many seconds to renew for this long
	 */
	public HederaDuration autoRenewPeriod = null;
	/**
	 * A {@link HederaTimeStamp} containing the expiration time for a smart contract
	 * initially null
	 * extend the expiration of the instance and its account to this time (no effect if it already is this time or later)
	 */
	public HederaTimeStamp expirationTime = null;
	/**
	 * Parameters supplied when creating a smart contract 
	 * (in the solidity format)
	 */
	public byte[] constructionParameters = new byte[0];
	/**
	 * The smart contract's account ID in the solidity format
	 * @return {@link String}
	 */
	public String getSolidityContractAccountID() {
		return this.solidityContractAccountID;
	}
	/**
	 * max number of bytes that the a smart contract call result might include. The run will fail if it would have returned more than this number of bytes.
	 */
	public long maxResultSize = 100;
	/**
	 * Gets the amount of storage used by the smart contract
	 * @return long
	 */
	public long getStorage() {
		return this.storage;
	}
	/** 
	 * Gets the bytecode for the smart contract
	 * @return byte[]
	 */
	public byte[] byteCode() {
		return this.byteCode;
	}
	/**
	 * A List of {@link HederaTransactionRecord} for a transaction run on the smart contract
	 * @return {@link List} of {@link HederaTransactionRecord}
	 */
	public List<HederaTransactionRecord> getTransactionRecords() {
		return transactionRecords;
	}
	/**
	 * The last result of running a local smart contract function 
	 * Note: May be null
	 * @return {@link HederaContractFunctionResult}
	 */
	public HederaContractFunctionResult hederaContractFunctionResult() {
		return this.hederaContractFunctionResult;
	}

	/**
	 * Default constructor, this returns a blank contract object with default values set.
	 */
	public HederaContract() {


	}
	/**
	 * Constructor from shard, realm and contract numbers
	 * @param shardNum the shard number for the contract
	 * @param realmNum the realm number for the contract
	 * @param contractNum the contract number
	 */
	public HederaContract(long shardNum, long realmNum, long contractNum) {

		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.contractNum = contractNum;

	}
	/**
	 * Constructor from a {@link HederaTransactionID}
	 * @param transactionID the HederaTransactionID
	 */
	public HederaContract(HederaTransactionID transactionID) {

		this.hederaTransactionID = transactionID;

	}
	/**
	 * The precheck result {@link ResponseCodeEnum} of a transaction
	 * @return {@link ResponseCodeEnum}
	 */
	public ResponseCodeEnum getPrecheckResult() {
		return this.precheckResult;
	}
	/**
	 * Gets the cost as returned by a query
	 * @return long
	 */
	public long getCost() {
		return this.cost;
	}
	/**
	 * Gets the stateproof requested in a query
	 * @return byte[]
	 */
	public byte[] getStateProof() {
		return this.stateProof;
	}

	/**
	 * This method runs a transaction to execute a function on a smart contract
	 * @param transactionID the {@link HederaTransactionID} for the transaction
	 * @param nodeAccount the {@link HederaAccountID} of the account of the node to which the transaction is submitted
	 * @param transactionFee the fee paid by the client to run the transaction
	 * @param transactionValidDuration the duration of the transaction's validity as {@link HederaDuration}
	 * @param generateRecord boolean to indicate if a record should be generated as a result of this transaction
	 * @param memo String memo to include in the transaction
	 * @param sigsForTransaction The signatures for the transaction as a {@link HederaSignatureList}
	 * @return {@link HederaTransactionResult}
	 * @throws InterruptedException in the event that communication with the node wasn't successful
	 */
	public HederaTransactionResult call(HederaTransactionID transactionID, HederaAccountID nodeAccount
			, long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord
			, String memo, HederaSignatureList sigsForTransaction) throws InterruptedException {

		// build the body
		HederaTransaction transaction = new HederaTransaction();
		transaction.body = new HederaTransactionBody(
				TransactionType.CONTRACTCALL
				, transactionID
				, nodeAccount
				, transactionFee
				, transactionValidDuration
				, generateRecord
				, memo
				, this.getCallTransactionBody());
		// add the signatures
		transaction.signatureList = sigsForTransaction;
		
		// issue the transaction
		Utilities.throwIfNull("Node", this.node);
		HederaTransactionResult hederaTransactionResult = this.node.contractCall(transaction);
		hederaTransactionResult.hederaTransactionID = transactionID;
		// return

		return hederaTransactionResult;
	}

	/**
	 * This method returns the body of a function call transaction so that it can be signed
	 * @param transactionID the {@link HederaTransactionID} for the transaction
	 * @param nodeAccount the {@link HederaAccountID} of the account of the node to which the transaction is submitted
	 * @param transactionFee the fee paid by the client to run the transaction
	 * @param transactionValidDuration the duration of the transaction's validity as {@link HederaDuration}
	 * @param generateRecord boolean to indicate if a record should be generated as a result of this transaction
	 * @param memo String memo to include in the transaction
	 * @return {@link TransactionBody}
	 */
	public TransactionBody bodyToSignForCall(HederaTransactionID transactionID, HederaAccountID nodeAccount, 
		long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, 
		String memo) {

		HederaTransactionBody transactionBody = new HederaTransactionBody(
				TransactionType.CONTRACTCALL
				, transactionID
				, nodeAccount
				, transactionFee
				, transactionValidDuration
				, generateRecord
				, memo
				, this.getCallTransactionBody());


		return transactionBody.getProtobuf();
	}

	/**
	 * This method returns the protobuf payload for a contract call transaction
	 * @return {@link ContractCallTransactionBody}
	 */
	public ContractCallTransactionBody getCallTransactionBody() {

		
		ContractCallTransactionBody.Builder transactionBody = ContractCallTransactionBody.newBuilder();

		transactionBody.setAmount(this.amount);
		transactionBody.setContractID(new HederaContractID(this.shardNum, this.realmNum, this.contractNum).getProtobuf());
		ByteString parameters = ByteString.copyFrom(this.functionParameters);
		transactionBody.setFunctionParameters(parameters);
		transactionBody.setGas(this.gas);
	

		return transactionBody.build();
	}

	/**
	 * This method runs a transaction to create a smart contract instance 
	 * @param transactionID the {@link HederaTransactionID} for the transaction
	 * @param nodeAccount the {@link HederaAccountID} of the account of the node to which the transaction is submitted
	 * @param transactionFee the fee paid by the client to run the transaction
	 * @param transactionValidDuration the duration of the transaction's validity as {@link HederaDuration}
	 * @param generateRecord boolean to indicate if a record should be generated as a result of this transaction
	 * @param memo String memo to include in the transaction
	 * @param sigsForTransaction The signatures for the transaction as a {@link HederaSignatureList}
	 * @return {@link HederaTransactionResult}
	 * @throws InterruptedException in the event that communication to the node resulted in an error
	 */
	public HederaTransactionResult create(HederaTransactionID transactionID, HederaAccountID nodeAccount
			, long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord
			, String memo, HederaSignatureList sigsForTransaction) throws InterruptedException {

		// build the body
		HederaTransaction transaction = new HederaTransaction();
		transaction.body = new HederaTransactionBody(
				TransactionType.CONTRACTCREATEINSTANCE
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
		HederaTransactionResult hederaTransactionResult = this.node.contractCreate(transaction);
		hederaTransactionResult.hederaTransactionID = transactionID;
		// return

		return hederaTransactionResult;
	}

	/**
	 * This method returns the body of a transaction to instantiate a smart contract so that it can be signed
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

		HederaTransactionBody transactionBody = new HederaTransactionBody(
				TransactionType.CONTRACTCREATEINSTANCE
				, transactionID
				, nodeAccount
				, transactionFee
				, transactionValidDuration
				, generateRecord
				, memo
				, this.getCreateTransactionBody());


		return transactionBody.getProtobuf();
	}

	/**
	 * This method returns the protobuf payload for a contract create transaction
	 * @return {@link ContractCreateTransactionBody}
	 */
	public ContractCreateTransactionBody getCreateTransactionBody() {

		
		ContractCreateTransactionBody.Builder transactionBody = ContractCreateTransactionBody.newBuilder();

		if (this.adminKey != null) {
			transactionBody.setAdminKey(adminKey.getProtobuf());
		}
		transactionBody.setAutoRenewPeriod(this.autoRenewPeriod.getProtobuf());
		ByteString parameters = ByteString.copyFrom(this.constructionParameters);
		transactionBody.setConstructorParameters(parameters);
		transactionBody.setFileID(this.fileID.getProtobuf());
		transactionBody.setGas(this.gas);
		if (this.initialBalance != 0) {
			transactionBody.setInitialBalance(this.initialBalance);
		}
		if (this.proxyAccountID != null) {
			transactionBody.setProxyAccountID(this.proxyAccountID.getProtobuf());
		}
		transactionBody.setProxyFraction(this.proxyFraction);
		if (this.realmNum != -1) {
			transactionBody.setRealmID(new HederaRealmID(this.shardNum, this.realmNum).getProtobuf());
		}
		transactionBody.setShardID(new HederaShardID(this.shardNum).getProtobuf());	   	
		
	

		return transactionBody.build();
	}

	/**
	 * This method runs a transaction to update an existing smart contract instance 
	 * @param transactionID the {@link HederaTransactionID} for the transaction
	 * @param nodeAccount the {@link HederaAccountID} of the account of the node to which the transaction is submitted
	 * @param transactionFee the fee paid by the client to run the transaction
	 * @param transactionValidDuration the duration of the transaction's validity as {@link HederaDuration}
	 * @param generateRecord boolean to indicate if a record should be generated as a result of this transaction
	 * @param memo String memo to include in the transaction
	 * @param sigsForTransaction The signatures for the transaction as a {@link HederaSignatureList}
	 * @return {@link HederaTransactionResult}
	 * @throws InterruptedException in the event that communication to the node resulted in an error
	 */
	public HederaTransactionResult update(HederaTransactionID transactionID, HederaAccountID nodeAccount
			, long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord
			, String memo, HederaSignatureList sigsForTransaction) throws InterruptedException {
		
		// build the body
		HederaTransaction transaction = new HederaTransaction();
		transaction.body = new HederaTransactionBody(
			TransactionType.CONTRACTUPDATEINSTANCE
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
		HederaTransactionResult hederaTransactionResult = this.node.contractUpdate(transaction);
		hederaTransactionResult.hederaTransactionID = transactionID;
		// return

		return hederaTransactionResult;
	}

	/**
	 * This method returns the body of a transaction to update an existing smart contract so that it can be signed
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

		HederaTransactionBody transactionBody = new HederaTransactionBody(
			TransactionType.CONTRACTUPDATEINSTANCE
			, transactionID
			, nodeAccount
			, transactionFee
			, transactionValidDuration
			, generateRecord
			, memo
			, this.getUpdateTransactionBody());


		return transactionBody.getProtobuf();
	}
	/**
	 * This method returns the protobuf payload for a contract create transaction
	 * @return {@link ContractUpdateTransactionBody}
	 */
	public ContractUpdateTransactionBody getUpdateTransactionBody() {

		
		ContractUpdateTransactionBody.Builder transactionBody = ContractUpdateTransactionBody.newBuilder();

		transactionBody.setContractID(new HederaContractID(this.shardNum, this.realmNum, this.contractNum).getProtobuf());

		if (this.adminKey != null) {
			transactionBody.setAdminKey(adminKey.getProtobuf());
		}
		if (this.autoRenewPeriod != null) {
			transactionBody.setAutoRenewPeriod(this.autoRenewPeriod.getProtobuf());
		}
		if (this.expirationTime != null) {
			transactionBody.setExpirationTime(this.expirationTime.getProtobuf());
		}
		if (this.proxyAccountID != null) {
			transactionBody.setProxyAccountID(this.proxyAccountID.getProtobuf());
		}
	

		return transactionBody.build();
	}

	/**
	 * Runs a query to get the bytecode for a given smart contract 
	 * If successful, the method populates the bytecode, cost and stateProof for this object depending on the type of answer requested
	 * @param payment a {@link HederaTransaction} message to indicate how this query will be paid for, this can be null for Cost queries
	 * @param responseType the type of response requested from the query
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication with the node
	 */
	public boolean getByteCode(HederaTransaction payment, HederaQueryHeader.QueryResponseType responseType) throws InterruptedException {
		boolean result = true;
		

		// build the query
	  // Header
		HederaQueryHeader queryHeader = new HederaQueryHeader();
		if (payment != null) {
			queryHeader.payment = payment;
			queryHeader.responseType = responseType;
		}
		
		// get bytecode query
		ContractGetBytecodeQuery.Builder getInfoQuery = ContractGetBytecodeQuery.newBuilder();
		getInfoQuery.setContractID(new HederaContractID(this.shardNum, this.realmNum, this.contractNum).getProtobuf());
		getInfoQuery.setHeader(queryHeader.getProtobuf());
		
		
		// the query itself
		HederaQuery query = new HederaQuery();
		query.queryType = QueryType.CONTRACTGETBYTECODE;
		query.queryData = getInfoQuery.build();
		
		// query now set, send to network
		Utilities.throwIfNull("Node", this.node);
		Response response = this.node.getContractByteCode(query);
		if (response == null) {
			Utilities.printResponseFailure("HederaContrat.getByteCode");
			return false;
		}
		ContractGetBytecodeResponse getInfoResponse = response.getContractGetBytecodeResponse();

		// check response header first
		ResponseHeader responseHeader = getInfoResponse.getHeader();

		this.precheckResult = responseHeader.getNodeTransactionPrecheckCode();
				
		if (this.precheckResult == ResponseCodeEnum.OK) {
			// cost
			this.cost = responseHeader.getCost();
			//state proof
			this.stateProof = responseHeader.getStateProof().toByteArray();
			
			this.byteCode = getInfoResponse.getBytecode().toByteArray();
		} else {
			result = false;
		}

	   	return result;
	}

	/**
	 * Gets the bytecode for the smart contract, requesting only an answer
	 * If successful, the method populates the bytecode and cost for this object
	 * @param payment the {@link HederaTransaction} payload containing payment information for the query
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getByteCodeAnswerOnly(HederaTransaction payment) throws InterruptedException {

	   	return getByteCode(payment, QueryResponseType.ANSWER_ONLY);
	}

	/**
	 * Gets the bytecode for the smart contract, requesting a state proof
	 * If successful, the method populates the bytecode, stateproof and cost for this object
	 * @param payment the {@link HederaTransaction} payload containing payment information for the query
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getByteCodeStateProof(HederaTransaction payment) throws InterruptedException {

		return getByteCode(payment, HederaQueryHeader.QueryResponseType.ANSWER_STATE_PROOF);
	}

	/**
	 * Requests only a cost for a simple answer for a bytecode query
	 * If successful, the method populates the cost for this object
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getByteCodeCostAnswer() throws InterruptedException {

		return getByteCode(null, HederaQueryHeader.QueryResponseType.COST_ANSWER);
	}

	/**
	 * Requests only a cost for an answer with stateproof for a bytecode query
	 * If successful, the method populates the cost for this object
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getByteCodeCostAnswerStateProof() throws InterruptedException {

		return getByteCode(null, HederaQueryHeader.QueryResponseType.COST_ANSWER_STATE_PROOF);
	}

	/**
	 * Runs a query to get information for a given smart contract 
	 * If successful, the method populates the properties this object depending on the type of answer requested
	 * @param payment a {@link HederaTransaction} message to indicate how this query will be paid for, this can be null for Cost queries
	 * @param responseType the type of response requested from the query
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication with the node
	 */
	public boolean getInfo(HederaTransaction payment, HederaQueryHeader.QueryResponseType responseType) throws InterruptedException {
		boolean result = true;
		

		// build the query
	  // Header
		HederaQueryHeader queryHeader = new HederaQueryHeader(); 
		if (payment != null) {
			queryHeader.payment = payment;
			queryHeader.responseType = responseType;
		}
		
		// get info query
		ContractGetInfoQuery.Builder getInfoQuery = ContractGetInfoQuery.newBuilder();
		getInfoQuery.setContractID(new HederaContractID(this.shardNum, this.realmNum, this.contractNum).getProtobuf());
		getInfoQuery.setHeader(queryHeader.getProtobuf());
		
		// the query itself
		HederaQuery query = new HederaQuery();
		query.queryType = QueryType.CONTRACTGETINFO;
		query.queryData = getInfoQuery.build();
		
		// query now set, send to network
		Utilities.throwIfNull("Node", this.node);
		Response response = this.node.getContractInfo(query);
		if (response == null) {
			Utilities.printResponseFailure("HederaContrat.getInfo");
			return false;
		}
		ContractGetInfoResponse.Builder getInfoResponse = response.getContractGetInfo().toBuilder();
		
		// check response header first
		ResponseHeader.Builder responseHeader = getInfoResponse.getHeaderBuilder();

		this.precheckResult = responseHeader.getNodeTransactionPrecheckCode();
				
		if (this.precheckResult == ResponseCodeEnum.OK) {
			ContractInfo info = getInfoResponse.getContractInfo();
			// cost
			this.cost = responseHeader.getCost();
			//state proof
			this.stateProof = responseHeader.getStateProof().toByteArray();
			
			this.realmNum = info.getContractID().getRealmNum();
			this.shardNum = info.getContractID().getShardNum();
			this.contractNum = info.getContractID().getContractNum();

			this.contractAccountShardNum = info.getAccountID().getShardNum();
			this.contractAccountRealmNum = info.getAccountID().getRealmNum();
			this.contractAccountAccountNum = info.getAccountID().getAccountNum();
			
			if (info.hasAdminKey()) {
				this.adminKey = new HederaKeyPair(info.getAdminKey());
			} else {
				this.adminKey = null;
			}
			
			this.autoRenewPeriod = new HederaDuration(info.getAutoRenewPeriod());
			this.solidityContractAccountID = info.getContractAccountID();
			this.expirationTime = new HederaTimeStamp(info.getExpirationTime());
			this.storage = info.getStorage();
		} else {
			result = false;
		}
		

	   	return result;
	}

	/**
	 * Runs a query to get information for a given smart contract, requesting only an answer
	 * If successful, the method populates the properties this object
	 * @param payment the {@link HederaTransaction} payload containing payment information for the query
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getInfoAnswerOnly(HederaTransaction payment) throws InterruptedException {

		return getInfo(payment, QueryResponseType.ANSWER_ONLY);
	}

	/**
	 * Runs a query to get information for a given smart contract, requesting a stateproof
	 * If successful, the method populates the properties this object including a state proof
	 * @param payment the {@link HederaTransaction} payload containing payment information for the query
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getInfoStateProof(HederaTransaction payment) throws InterruptedException {

		return getInfo(payment, HederaQueryHeader.QueryResponseType.ANSWER_STATE_PROOF);
	}

	/**
	 * Runs a query to get the cost of getting information for a given smart contract without a state proof
	 * If successful, the method populates the cost property of this object
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getInfoCostAnswer() throws InterruptedException {

		return getInfo(null, HederaQueryHeader.QueryResponseType.COST_ANSWER);
	}

	/**
	 * Runs a query to get the cost of getting information for a given smart contract with a state proof
	 * If successful, the method populates the cost property of this object
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getInfoCostAnswerStateProof() throws InterruptedException {

		return getInfo(null, HederaQueryHeader.QueryResponseType.COST_ANSWER_STATE_PROOF);
	}

	/**
	 * Runs a query to call a smart contract function locally (on a node)
	 * If successful, the method populates the properties this object depending on the type of answer requested
	 * @param payment a {@link HederaTransaction} message to indicate how this query will be paid for, this can be null for Cost queries
	 * @param responseType the type of response requested from the query
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication with the node
	 */
	public boolean callLocal(HederaTransaction payment, HederaQueryHeader.QueryResponseType responseType) throws InterruptedException {
		boolean result = true;


		// build the query
	  // Header
		HederaQueryHeader queryHeader = new HederaQueryHeader(); 
		if (payment != null) {
			queryHeader.payment = payment;
			queryHeader.responseType = responseType;
		}
		
		// call local query
		ContractCallLocalQuery.Builder callLocalQuery = ContractCallLocalQuery.newBuilder();
		callLocalQuery.setHeader(queryHeader.getProtobuf());
		callLocalQuery.setContractID(new HederaContractID(this.shardNum, this.realmNum, this.contractNum).getProtobuf());

		callLocalQuery.setGas(this.gas);
		callLocalQuery.setFunctionParameters(ByteString.copyFrom(this.functionParameters));
		callLocalQuery.setMaxResultSize(this.maxResultSize);
		
		// the query itself
		HederaQuery query = new HederaQuery();
		query.queryType = QueryType.CONTRACTCALLLOCAL;
		query.queryData = callLocalQuery.build();
		
		// query now set, send to network
		Utilities.throwIfNull("Node", this.node);
		Response response = this.node.contractCallLocal(query);
		if (response == null) {
			Utilities.printResponseFailure("HederaContrat.callLocal");
			return false;
		}
		ContractCallLocalResponse.Builder getCallLocalResponse = response.getContractCallLocal().toBuilder();
		
		// check response header first
		ResponseHeader.Builder responseHeader = getCallLocalResponse.getHeaderBuilder();

		this.precheckResult = responseHeader.getNodeTransactionPrecheckCode();
		this.hederaContractFunctionResult = null;		
		
		if (this.precheckResult == ResponseCodeEnum.OK) {
			// result of function call
			this.hederaContractFunctionResult = new HederaContractFunctionResult(getCallLocalResponse.getFunctionResult());
			// cost
			this.cost = responseHeader.getCost();
			//state proof
			this.stateProof = responseHeader.getStateProof().toByteArray();
		} else {
			result = false;
		}
		

		return result;
	}

	/**
	 * Runs a query to call a smart contract function locally (on a node), requesting only an answer
	 * If successful, the method populates the properties this object
	 * @param payment the {@link HederaTransaction} payload containing payment information for the query
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean callLocalAnswerOnly(HederaTransaction payment) throws InterruptedException {

		return callLocal(payment, QueryResponseType.ANSWER_ONLY);
	}

	/**
	 * Runs a query to call a smart contract function locally (on a node), requesting a stateproof
	 * If successful, the method populates the properties this object including a state proof
	 * @param payment the {@link HederaTransaction} payload containing payment information for the query
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean callLocalStateProof(HederaTransaction payment) throws InterruptedException {

		return callLocal(payment, HederaQueryHeader.QueryResponseType.ANSWER_STATE_PROOF);
	}

	/**
	 * Runs a query to call a smart contract function locally (on a node), without a state proof
	 * If successful, the method populates the cost property of this object
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean callLocalCostAnswer() throws InterruptedException {

		return callLocal(null, HederaQueryHeader.QueryResponseType.COST_ANSWER);
	}

	/**
	 * Runs a query to call a smart contract function locally (on a node), with a state proof
	 * If successful, the method populates the cost property of this object
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean callLocalCostAnswerStateProof() throws InterruptedException {

		return callLocal(null, HederaQueryHeader.QueryResponseType.COST_ANSWER_STATE_PROOF);
	}
	

	/**
	 * Adds a {@link HederaKeyPair} to this object
	 * @param key the key to add
	 */
	public void addKey(HederaKeyPair key) {

		this.keys.add(key);

	}

	/**
	 * Deletes a {@link HederaKeyPair} from this object
	 * @param key the key to delete
	 * @return true if successfully deleted
	 */
	public boolean deleteKey(HederaKeyPair key) {

		return this.keys.remove(key);
	}

	/**
	 * Gets a list of {@link HederaKeyPair}
	 * @return List of {@link HederaKeyPair}
	 */
	public List<HederaKeyPair> getKeys() {

		return this.keys;
	}

	/**
	 * Creates a smart contract in the simplest possible way
	 * @param shardNum the shard in which to create the smart contract
	 * @param realmNum the realm in which to create the smart contract
	 * @param fileID the {@link HederaFileID} identifying the file containing the bytecode from which to create the smart contract instance
	 * @param initialBalance the initial balance for the smart contract's account
	 * @param gas the maximum amount of gas to use for the creation
	 * @param constructorParameters, a byte array containing the parameters for the construction of the smart contract
	 * @param autoRenewPeriod, a {@link HederaDuration} to specify how often the smart contract should renew itself
	 * @return {@link HederaTransactionResult}
	 * @throws Exception in the event of an error 
	 */
	public HederaTransactionResult create(long shardNum, long realmNum, HederaFileID fileID, long initialBalance, long gas, byte[] constructorParameters, HederaDuration autoRenewPeriod) throws Exception {

		// setup defaults if necessary
		
		// initialise the result
		HederaTransactionResult transactionResult = new HederaTransactionResult();

		// required
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.initialBalance = initialBalance;
		this.gas = gas;
		this.fileID = fileID;
		this.constructionParameters = constructorParameters;
		this.autoRenewPeriod = autoRenewPeriod;
		
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

		// get the body for the transaction so we can sign it
		TransactionBody createBody = this.bodyToSignForCreate(
			this.hederaTransactionID
			, this.node.getAccountID()
			, this.node.contractCreateTransactionFee
			, this.txQueryDefaults.transactionValidDuration
			, this.txQueryDefaults.generateRecord
			, this.txQueryDefaults.memo);

		HederaSignatureList sigsForTransaction = new HederaSignatureList();
		//paying signature
		sigsForTransaction.addSignature(this.txQueryDefaults.payingKeyPair.getSignature(createBody.toByteArray()));
		// new realm admin key
		if (this.newRealmAdminKey != null) {
			sigsForTransaction.addSignature(this.newRealmAdminKey.getSignature(createBody.toByteArray()));
		}
		// admin key 
		if (this.adminKey != null) {
			sigsForTransaction.addSignature(this.adminKey.getSignature(createBody.toByteArray()));
		}
		
		// create the contract
		transactionResult = this.create(
			this.hederaTransactionID
			, this.node.getAccountID()
			, this.node.contractCreateTransactionFee
			, this.txQueryDefaults.transactionValidDuration
			, this.txQueryDefaults.generateRecord
			, this.txQueryDefaults.memo
			, sigsForTransaction);
		

		return transactionResult;
	}

	/**
	 * Updates a smart contract in the simplest possible way
	 * @param expirationTime, a {@link HederaTimeStamp} update the expiration time of the smart contract
	 * @param autoRenewPeriod, a {@link HederaDuration} to specify how often the smart contract should renew itself
	 * @return {@link HederaTransactionResult}
	 * @throws Exception in the event of an error 
	 */
	public HederaTransactionResult update(HederaTimeStamp expirationTime, HederaDuration autoRenewPeriod) throws Exception {

		// setup defaults if necessary
		
		// initialise the result
		HederaTransactionResult transactionResult = new HederaTransactionResult();

		// required
		this.expirationTime = expirationTime;
		this.autoRenewPeriod = autoRenewPeriod;
		
		// validate inputs
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", this.txQueryDefaults.node);
		Utilities.throwIfNull("txQueryDefaults.payingKeyPair", this.txQueryDefaults.payingKeyPair);
		Utilities.throwIfAccountIDInvalid("node.AccountID", this.node.getAccountID());

		// set transport
		this.node = this.txQueryDefaults.node;
		
		// create a transaction ID (starts now with accountID of the paying account id)
		this.hederaTransactionID = new HederaTransactionID(this.txQueryDefaults.payingAccountID);

		// get the body for the transaction so we can sign it
		TransactionBody updateBody = this.bodyToSignForUpdate(
			this.hederaTransactionID
			, this.node.getAccountID()
			, this.node.contractUpdateTransactionFee
			, this.txQueryDefaults.transactionValidDuration
			, this.txQueryDefaults.generateRecord
			, this.txQueryDefaults.memo);

		HederaSignatureList sigsForTransaction = new HederaSignatureList();
		//paying signature
		sigsForTransaction.addSignature(this.txQueryDefaults.payingKeyPair.getSignature(updateBody.toByteArray()));
		// admin key for change
		if (this.adminKey != null) {
			sigsForTransaction.addSignature(this.adminKey.getSignature(updateBody.toByteArray()));
		}

		// update the contract
		transactionResult = this.update(
			this.hederaTransactionID
			, this.node.getAccountID()
			, this.node.contractUpdateTransactionFee
			, this.txQueryDefaults.transactionValidDuration
			, this.txQueryDefaults.generateRecord
			, this.txQueryDefaults.memo
			, sigsForTransaction);
		

		return transactionResult;
	}

	/**
	 * Updates a smart contract in the simplest possible way
	 * @param shardNum, the shard number of the smart contract 
	 * @param realmNum, the realm number of the smart contract
	 * @param contractNum, the account number of the smart contract
	 * @param expirationTime, a {@link HederaTimeStamp} update the expiration time of the smart contract
	 * @param autoRenewPeriod, a {@link HederaDuration} to specify how often the smart contract should renew itself
	 * @return {@link HederaTransactionResult}
	 * @throws Exception in the event of an error 
	 */
	public HederaTransactionResult update(long shardNum, long realmNum, long contractNum, HederaTimeStamp expirationTime, HederaDuration autoRenewPeriod) throws Exception {
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.contractNum = contractNum;
		return update(expirationTime, autoRenewPeriod);
	}

	/**
	 * Calls a smart contract function in the simplest possible way
	 * @param gas, the maximum amount of gas to use for the call
	 * @param amount, number of tinybars sent (the function must be payable if this is nonzero)
	 * @param functionParameters, which function to call, and the parameters to pass to the function
	 * @return {@link HederaTransactionResult}
	 * @throws Exception in the event of an error 
	 */
	public HederaTransactionResult call(long gas, long amount, byte[] functionParameters) throws Exception {

		// setup defaults if necessary
		
		// initialise the result
		HederaTransactionResult transactionResult = new HederaTransactionResult();

		// required
		this.gas = gas;
		this.amount = amount;
		this.functionParameters = functionParameters.clone();
				
		// validate inputs
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", this.txQueryDefaults.node);
		Utilities.throwIfNull("txQueryDefaults.payingKeyPair", this.txQueryDefaults.payingKeyPair);
		Utilities.throwIfAccountIDInvalid("txQueryDefaults.payingAccountID", this.txQueryDefaults.payingAccountID);
		Utilities.throwIfAccountIDInvalid("node.AccountID", this.node.getAccountID());

		// set transport
		this.node = this.txQueryDefaults.node;

		// create a transaction ID (starts now with accountID of the paying account id)
		this.hederaTransactionID = new HederaTransactionID(this.txQueryDefaults.payingAccountID);

		// get the body for the transaction so we can sign it
		TransactionBody callBody = this.bodyToSignForCall(
			this.hederaTransactionID
			, this.node.getAccountID()
			, this.node.contractCallTransactionFee
			, this.txQueryDefaults.transactionValidDuration
			, this.txQueryDefaults.generateRecord
			, this.txQueryDefaults.memo);

		HederaSignatureList sigsForTransaction = new HederaSignatureList();
		//paying signature
		sigsForTransaction.addSignature(this.txQueryDefaults.payingKeyPair.getSignature(callBody.toByteArray()));
		
		// call the contract function
		transactionResult = this.call(
			this.hederaTransactionID
			, this.node.getAccountID()
			, this.node.contractCallTransactionFee
			, this.txQueryDefaults.transactionValidDuration
			, this.txQueryDefaults.generateRecord
			, this.txQueryDefaults.memo
			, sigsForTransaction);
	

		return transactionResult;
	}

	/**
	 * Calls a smart contract function in the simplest possible way
	 * @param shardNum, the shard number of the smart contract 
	 * @param realmNum, the realm number of the smart contract
	 * @param contractNum, the account number of the smart contract
	 * @param gas, the maximum amount of gas to use for the call
	 * @param amount, number of tinybars sent (the function must be payable if this is nonzero)
	 * @param functionParameters, which function to call, and the parameters to pass to the function
	 * @return {@link HederaTransactionResult}
	 * @throws Exception in the event of an error 
	 */
	public HederaTransactionResult call(long shardNum, long realmNum, long contractNum, long gas, long amount, byte[] functionParameters) throws Exception {
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.contractNum = contractNum;
		return call(gas, amount, functionParameters);
	}

	/**
	 * Get info for the contract which is specified by the
	 * shardNum, realmNum and contractNum properties of this class
	 * in the event of an error, check the value of this.precheckResult to determine the 
	 * cause of the error
	 * Note: You may perform a "getInfoCostAnswer" in order to ascertain the cost of the query first
	 * The cost could be cached and refreshed from time to time, there is no need to look it up 
	 * before each getInfo query
	 * @return {@link boolean}
	 * @throws Exception in the event of an error 
	 */
	public boolean getInfo() throws Exception {

		// set transport
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("Node", this.txQueryDefaults.node);
		this.node = this.txQueryDefaults.node;
		Utilities.throwIfNull("Node", this.node);
		HederaTransaction transferTransaction = new HederaTransaction(this.txQueryDefaults, this.node.contractGetInfoQueryFee);

		return this.getInfoAnswerOnly(transferTransaction);
	}

	/**
	 * Get info for a smart contract 
	 * in the event of an error, check the value of this.precheckResult to determine the 
	 * cause of the error
	 * Note: You may perform a "getInfoCostAnswer" in order to ascertain the cost of the query first
	 * The cost could be cached and refreshed from time to time, there is no need to look it up 
	 * before each getInfo query
	 * @param shardNum, the shard number of the smart contract 
	 * @param realmNum, the realm number of the smart contract
	 * @param contractNum, the account number of the smart contract
	 * @return {@link boolean}
	 * @throws Exception in the event of an error 
	 */
	public boolean getInfo(long shardNum, long realmNum, long contractNum) throws Exception {
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.contractNum = contractNum;
		return getInfo();
	}	

	/** 
	 * Gets the bytecode of the smart contract specified by the shardNum, realmNum and contractNum properties of this class, 
	 * returns null if an error occurred.
	 * in the event of an error, check the value of this.precheckResult to determine the 
	 * cause of the error
	 * @return {@link byte} array
	 * @throws Exception in the event of an error 
	 */
	public byte[] getByteCode() throws Exception {

		// set transport
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("Node", this.txQueryDefaults.node);
		this.node = this.txQueryDefaults.node;
		
		HederaTransaction transferTransaction = new HederaTransaction(this.txQueryDefaults,this.node.contractGetByteCodeQueryFee);

		if (this.getByteCodeAnswerOnly(transferTransaction)) {

			return this.byteCode;
		} else {

			return null;
		}
	}

	/** 
	 * Gets the bytecode of a smart contract  
	 * returns null if an error occurred.
	 * in the event of an error, check the value of this.precheckResult to determine the 
	 * cause of the error
	 * @param shardNum, the shard number of the smart contract 
	 * @param realmNum, the realm number of the smart contract
	 * @param contractNum, the account number of the smart contract
	 * @return {@link byte} array
	 * @throws Exception in the event of an error 
	 */
	public byte[] getByteCode(long shardNum, long realmNum, long contractNum) throws Exception {
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.contractNum = contractNum;
		return getByteCode();
	}

	/** 
	 * Runs a smart contract function on a single node and returns the result as a {@link HederaContractFunctionResult} object 
	 * returns null if an error occurred
	 * in the event of an error, check the value of this.precheckResult to determine the cause of the error
	 * @param gas, the maximum amount of gas to pay for this function execution
	 * @param functionParameters, parameters for running the function
	 * @param maxResultSize, max number of bytes that the result might include. The run will fail if it would have returned more than this number of bytes.
	 * @return {@link HederaContractFunctionResult}
	 * @throws Exception in the event of an error 
	 */
	public HederaContractFunctionResult callLocal(long gas, byte[] functionParameters, long maxResultSize) throws Exception {


		// validate inputs
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", this.txQueryDefaults.node);

		// set transport
		this.node = this.txQueryDefaults.node;
		
		this.gas = gas;
		this.functionParameters = functionParameters;
		this.maxResultSize = maxResultSize;

		HederaTransaction transferTransaction = new HederaTransaction(this.txQueryDefaults,this.node.contractCallLocalQueryFee);

		if (this.callLocalAnswerOnly(transferTransaction)) {

			return this.hederaContractFunctionResult;
		} else {

			return null;
		}
	}
	
	/** 
	 * Runs a smart contract function on a single node and returns the result as a {@link HederaContractFunctionResult} object 
	 * returns null if an error occurred
	 * in the event of an error, check the value of this.precheckResult to determine the cause of the error
	 * @param shardNum, the shard number of the smart contract 
	 * @param realmNum, the realm number of the smart contract
	 * @param contractNum, the account number of the smart contract
	 * @param gas, the maximum amount of gas to pay for this function execution
	 * @param functionParameters, parameters for running the function
	 * @param maxResultSize, max number of bytes that the result might include. The run will fail if it would have returned more than this number of bytes.
	 * @return {@link HederaContractFunctionResult}
	 * @throws Exception in the event of an error 
	 */
	public HederaContractFunctionResult callLocal(long shardNum, long realmNum, long contractNum, long gas, byte[] functionParameters, long maxResultSize) throws Exception {
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.contractNum = contractNum;
		return callLocal(gas, functionParameters, maxResultSize);
	}
}