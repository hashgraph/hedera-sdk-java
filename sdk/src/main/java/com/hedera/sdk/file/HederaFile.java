package com.hedera.sdk.file;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.google.protobuf.ByteString;
import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaFileID;
import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyList;
import com.hedera.sdk.common.HederaRealmID;
import com.hedera.sdk.common.HederaShardID;
import com.hedera.sdk.common.HederaSignature;
import com.hedera.sdk.common.HederaSignatureList;
import com.hedera.sdk.common.HederaTimeStamp;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.common.HederaTransactionID;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.node.HederaNode;
import com.hedera.sdk.query.HederaQuery;
import com.hedera.sdk.query.HederaQuery.QueryType;
import com.hedera.sdk.query.HederaQueryHeader;
import com.hedera.sdk.query.HederaQueryHeader.QueryResponseType;
import com.hedera.sdk.transaction.HederaTransaction;
import com.hedera.sdk.transaction.HederaTransactionBody;
import com.hedera.sdk.transaction.HederaTransactionBody.TransactionType;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hederahashgraph.api.proto.java.FileAppendTransactionBody;
import com.hederahashgraph.api.proto.java.FileCreateTransactionBody;
import com.hederahashgraph.api.proto.java.FileDeleteTransactionBody;
import com.hederahashgraph.api.proto.java.FileGetContentsQuery;
import com.hederahashgraph.api.proto.java.FileGetContentsResponse;
import com.hederahashgraph.api.proto.java.FileGetInfoQuery;
import com.hederahashgraph.api.proto.java.FileGetInfoResponse;
import com.hederahashgraph.api.proto.java.FileGetInfoResponse.FileInfo;
import com.hederahashgraph.api.proto.java.FileID;
import com.hederahashgraph.api.proto.java.FileUpdateTransactionBody;
import com.hederahashgraph.api.proto.java.KeyList;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
import com.hederahashgraph.api.proto.java.ResponseHeader;
import com.hederahashgraph.api.proto.java.TransactionBody;
import org.slf4j.LoggerFactory;

/**
 * Class to manage files on Hedera Hashgraph
 *
 */
public class HederaFile implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaFile.class);
	private static final long serialVersionUID = 1;
	// default file duration set to 1 day (60 seconds x 60 minutes x 24h)
	private static final long EXPIRATIONDEFAULT = 86400;
	private HederaNode node = null;
	private long size = 0;
	private boolean deleted = false;
	private ResponseCodeEnum precheckResult = ResponseCodeEnum.UNKNOWN;
	private long cost = 0;
	private byte[] stateProof = new byte[0];
	/**
	 * Default parameters for a transaction or query
	 */
	public HederaTransactionAndQueryDefaults txQueryDefaults = new HederaTransactionAndQueryDefaults();
	/**
	 * new keys to manage the file with
	 */
	private List<HederaKeyPair> newKeys = new ArrayList<HederaKeyPair>();
	/**
	 * keys to manage the file with 
	 */
	private List<HederaKeyPair> keys = new ArrayList<HederaKeyPair>();
	// the time at which this file should expire (unless updated before then to
	// extend its life)
	// value is defaulted to now + EXPIRATIONDEFAULT
	/**
	 * the file's expiration time, defaults to 1 day
	 */
	public Instant expirationTime = Instant.now().plusSeconds(EXPIRATIONDEFAULT);
	/**
	 * the ID of a transaction in relation to the file
	 */
	public HederaTransactionID hederaTransactionID = null;
	/**
	 * the files's contents, either set or get
	 */
	public byte[] contents = new byte[0];
	/**
	 * the contents to append to the file defaults to null
	 */
	public byte[] appendContents = null;
	/**
	 * the file's shard number
	 */
	public long shardNum = 0;
	/**
	 * the file's realm number
	 */
	public long realmNum = 0;
	/**
	 * the file's number
	 */
	public long fileNum = 0;
	/**
	 * The new realm administration key {@link HederaKeyPair} for the file
	 */
	public HederaKeyPair newRealmAdminKey = null;
	/**
	 * sets the object to use for communication with the node
	 * 
	 * @param node the node to use for communication
	 */
	public void setNode(HederaNode node) {
		this.node = node;
	}

	/**
	 * Default constructor, this returns a blank file object.
	 */
	public HederaFile() {


	}

	/**
	 * Constructor from shard, realm and file number
	 * 
	 * @param shardNum the shard number for the file
	 * @param realmNum the realm number for the file
	 * @param fileNum the file number for the file
	 */
	public HederaFile(long shardNum, long realmNum, long fileNum) {

		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.fileNum = fileNum;

	}

	/**
	 * Constructor from a {@link HederaTransactionID}
	 * 
	 * @param transactionID the transaction ID to create the file from
	 */
	public HederaFile(HederaTransactionID transactionID) {

		this.hederaTransactionID = transactionID;

	}

	/**
	 * returns the size of the file as reported (result of a get info query)
	 * 
	 * @return long
	 */
	public long getSize() {
		return this.size;
	}
	/**
	 * returns a {@link HederaFileID} from shard, realm and file numbers
	 * @return {@link HederaFileID}
	 */
	public HederaFileID getFileID() {
		
		return new HederaFileID(this.shardNum, this.realmNum,  this.fileNum);
	}
	/**
	 * sets the shard, realm and file numbers from a {@link HederaFileID}
	 * @param fileID - The {@link HederaFileID}
	 */
	public void setFileID(HederaFileID fileID) {
		this.shardNum = fileID.shardNum;
		this.realmNum = fileID.realmNum;
		this.fileNum = fileID.fileNum;
	}
	/**
	 * returns file was deleted (result of a get info query)
	 * 
	 * @return boolean
	 */
	public boolean getDeleted() {
		return this.deleted;
	}

	/**
	 * results of the Transaction
	 * 
	 * @return {@link ResponseCodeEnum}
	 */
	public ResponseCodeEnum getPrecheckResult() {
		return this.precheckResult;
	}

	/**
	 * cost of a query
	 * 
	 * @return long
	 */
	public long getCost() {
		return this.cost;
	}

	/**
	 * state proof if requested
	 * 
	 * @return byte[]
	 */
	public byte[] getStateProof() {
		return this.stateProof;
	}

	/**
	 * This method returns the body of a transaction to create a file so that it can
	 * be signed
	 * 
	 * @param transactionID            the {@link HederaTransactionID} for the
	 *                                 transaction
	 * @param nodeAccount              the {@link HederaAccountID} of the account of
	 *                                 the node to which the transaction is
	 *                                 submitted
	 * @param transactionFee           the fee paid by the client to run the
	 *                                 transaction
	 * @param transactionValidDuration the duration of the transaction's validity as
	 *                                 {@link HederaDuration}
	 * @param generateRecord           boolean to indicate if a record should be
	 *                                 generated as a result of this transaction
	 * @param memo                     String memo to include in the transaction
	 * @return {@link TransactionBody}
	 */
	public TransactionBody bodyToSignForCreate(HederaTransactionID transactionID, HederaAccountID nodeAccount,
			long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, String memo) {

		HederaTransactionBody transactionBody = new HederaTransactionBody(TransactionType.FILECREATE, transactionID,
				nodeAccount, transactionFee, transactionValidDuration, generateRecord, memo, this.getCreateTransactionBody());


		return transactionBody.getProtobuf();
	}

	/**
	 * This method returns the body of a transaction to delete a file so that it can
	 * be signed
	 * 
	 * @param transactionID            the {@link HederaTransactionID} for the
	 *                                 transaction
	 * @param nodeAccount              the {@link HederaAccountID} of the account of
	 *                                 the node to which the transaction is
	 *                                 submitted
	 * @param transactionFee           the fee paid by the client to run the
	 *                                 transaction
	 * @param transactionValidDuration the duration of the transaction's validity as
	 *                                 {@link HederaDuration}
	 * @param generateRecord           boolean to indicate if a record should be
	 *                                 generated as a result of this transaction
	 * @param memo                     String memo to include in the transaction
	 * @return {@link TransactionBody}
	 */
	public TransactionBody bodyToSignForDelete(HederaTransactionID transactionID, HederaAccountID nodeAccount,
			long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, String memo) {

		HederaTransactionBody transactionBody = new HederaTransactionBody(TransactionType.FILEDELETE, transactionID,
				nodeAccount, transactionFee, transactionValidDuration, generateRecord, memo, this.getDeleteTransactionBody());


		return transactionBody.getProtobuf();
	}

	/**
	 * This method returns the body of a transaction to update a file so that it can
	 * be signed
	 * 
	 * @param transactionID            the {@link HederaTransactionID} for the
	 *                                 transaction
	 * @param nodeAccount              the {@link HederaAccountID} of the account of
	 *                                 the node to which the transaction is
	 *                                 submitted
	 * @param transactionFee           the fee paid by the client to run the
	 *                                 transaction
	 * @param transactionValidDuration the duration of the transaction's validity as
	 *                                 {@link HederaDuration}
	 * @param generateRecord           boolean to indicate if a record should be
	 *                                 generated as a result of this transaction
	 * @param memo                     String memo to include in the transaction
	 * @return {@link TransactionBody}
	 */
	public TransactionBody bodyToSignForUpdate(HederaTransactionID transactionID, HederaAccountID nodeAccount,
			long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, String memo) {

		HederaTransactionBody transactionBody = new HederaTransactionBody(TransactionType.FILEUPDATE, transactionID,
				nodeAccount, transactionFee, transactionValidDuration, generateRecord, memo, this.getUpdateTransactionBody());


		return transactionBody.getProtobuf();
	}

	/**
	 * This method returns the body of a transaction to append to a file so that it
	 * can be signed
	 * 
	 * @param transactionID            the {@link HederaTransactionID} for the
	 *                                 transaction
	 * @param nodeAccount              the {@link HederaAccountID} of the account of
	 *                                 the node to which the transaction is
	 *                                 submitted
	 * @param transactionFee           the fee paid by the client to run the
	 *                                 transaction
	 * @param transactionValidDuration the duration of the transaction's validity as
	 *                                 {@link HederaDuration}
	 * @param generateRecord           boolean to indicate if a record should be
	 *                                 generated as a result of this transaction
	 * @param memo                     String memo to include in the transaction
	 * @return {@link TransactionBody}
	 */
	public TransactionBody bodyToSignForAppend(HederaTransactionID transactionID, HederaAccountID nodeAccount,
			long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, String memo) {

		HederaTransactionBody transactionBody = new HederaTransactionBody(TransactionType.FILEAPPEND, transactionID,
				nodeAccount, transactionFee, transactionValidDuration, generateRecord, memo, this.getAppendTransactionBody());


		return transactionBody.getProtobuf();
	}

	/**
	 * This method runs a transaction to create a file
	 * 
	 * @param transactionID            the {@link HederaTransactionID} for the
	 *                                 transaction
	 * @param nodeAccount              the {@link HederaAccountID} of the account of
	 *                                 the node to which the transaction is
	 *                                 submitted
	 * @param transactionFee           the fee paid by the client to run the
	 *                                 transaction
	 * @param transactionValidDuration the duration of the transaction's validity as
	 *                                 {@link HederaDuration}
	 * @param generateRecord           boolean to indicate if a record should be
	 *                                 generated as a result of this transaction
	 * @param memo                     String memo to include in the transaction
	 * @param sigsForTransaction       The signatures for the transaction as a
	 *                                 {@link HederaSignatureList}
	 * @return {@link HederaTransactionResult}
	 * @throws InterruptedException in the event that communication to the node
	 *                              resulted in an error
	 */
	public HederaTransactionResult create(HederaTransactionID transactionID, HederaAccountID nodeAccount,
			long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, String memo,
			HederaSignatureList sigsForTransaction) throws InterruptedException {

		// build the body
		HederaTransaction transaction = new HederaTransaction();
		transaction.body = new HederaTransactionBody(TransactionType.FILECREATE, transactionID, nodeAccount, transactionFee,
				transactionValidDuration, generateRecord, memo, this.getCreateTransactionBody());
		// add the signatures
		transaction.signatureList = sigsForTransaction;

		// issue the transaction
		Utilities.throwIfNull("Node", this.node);
		
		HederaTransactionResult hederaTransactionResult = this.node.fileCreate(transaction);
		hederaTransactionResult.hederaTransactionID = transactionID;
		// return

		return hederaTransactionResult;

	}

	/**
	 * This method runs a transaction to delete a file
	 * 
	 * @param transactionID            the {@link HederaTransactionID} for the
	 *                                 transaction
	 * @param nodeAccount              the {@link HederaAccountID} of the account of
	 *                                 the node to which the transaction is
	 *                                 submitted
	 * @param transactionFee           the fee paid by the client to run the
	 *                                 transaction
	 * @param transactionValidDuration the duration of the transaction's validity as
	 *                                 {@link HederaDuration}
	 * @param generateRecord           boolean to indicate if a record should be
	 *                                 generated as a result of this transaction
	 * @param memo                     String memo to include in the transaction
	 * @param sigsForTransaction       The signatures for the transaction as a
	 *                                 {@link HederaSignatureList}
	 * @return {@link HederaTransactionResult}
	 * @throws InterruptedException in the event that communication to the node
	 *                              resulted in an error
	 */
	public HederaTransactionResult delete(HederaTransactionID transactionID, HederaAccountID nodeAccount,
			long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, String memo,
			HederaSignatureList sigsForTransaction) throws InterruptedException {

		// build the body
		HederaTransaction transaction = new HederaTransaction();
		transaction.body = new HederaTransactionBody(TransactionType.FILEDELETE, transactionID, nodeAccount, transactionFee,
				transactionValidDuration, generateRecord, memo, getDeleteTransactionBody());
		// add the signatures
		transaction.signatureList = sigsForTransaction;

		// issue the transaction
		Utilities.throwIfNull("Node", this.node);

		HederaTransactionResult hederaTransactionResult = this.node.fileDelete(transaction);
		hederaTransactionResult.hederaTransactionID = transactionID;


		// return
		return hederaTransactionResult;
	}

	/**
	 * This method runs a transaction to update a file
	 * 
	 * @param transactionID            the {@link HederaTransactionID} for the
	 *                                 transaction
	 * @param nodeAccount              the {@link HederaAccountID} of the account of
	 *                                 the node to which the transaction is
	 *                                 submitted
	 * @param transactionFee           the fee paid by the client to run the
	 *                                 transaction
	 * @param transactionValidDuration the duration of the transaction's validity as
	 *                                 {@link HederaDuration}
	 * @param generateRecord           boolean to indicate if a record should be
	 *                                 generated as a result of this transaction
	 * @param memo                     String memo to include in the transaction
	 * @param sigsForTransaction       The signatures for the transaction as a
	 *                                 {@link HederaSignatureList}
	 * @return {@link HederaTransactionResult}
	 * @throws InterruptedException in the event that communication to the node
	 *                              resulted in an error
	 */
	public HederaTransactionResult update(HederaTransactionID transactionID, HederaAccountID nodeAccount,
			long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, String memo,
			HederaSignatureList sigsForTransaction) throws InterruptedException {

		// build the body
		HederaTransaction transaction = new HederaTransaction();
		transaction.body = new HederaTransactionBody(TransactionType.FILEUPDATE, transactionID, nodeAccount, transactionFee,
				transactionValidDuration, generateRecord, memo, this.getUpdateTransactionBody());
		// add the signatures
		transaction.signatureList = sigsForTransaction;

		// issue the transaction
		Utilities.throwIfNull("Node", this.node);

		HederaTransactionResult hederaTransactionResult = this.node.fileUpdate(transaction);
		hederaTransactionResult.hederaTransactionID = transactionID;


		// return
		return hederaTransactionResult;
	}

	/**
	 * This method runs a transaction to append to a file
	 * 
	 * @param transactionID            the {@link HederaTransactionID} for the
	 *                                 transaction
	 * @param nodeAccount              the {@link HederaAccountID} of the account of
	 *                                 the node to which the transaction is
	 *                                 submitted
	 * @param transactionFee           the fee paid by the client to run the
	 *                                 transaction
	 * @param transactionValidDuration the duration of the transaction's validity as
	 *                                 {@link HederaDuration}
	 * @param generateRecord           boolean to indicate if a record should be
	 *                                 generated as a result of this transaction
	 * @param memo                     String memo to include in the transaction
	 * @param sigsForTransaction       The signatures for the transaction as a
	 *                                 {@link HederaSignatureList}
	 * @return {@link HederaTransactionResult}
	 * @throws InterruptedException in the event that communication to the node
	 *                              resulted in an error
	 */
	public HederaTransactionResult append(HederaTransactionID transactionID, HederaAccountID nodeAccount,
			long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, String memo,
			HederaSignatureList sigsForTransaction) throws InterruptedException {

		// build the body
		HederaTransaction transaction = new HederaTransaction();
		transaction.body = new HederaTransactionBody(TransactionType.FILEAPPEND, transactionID, nodeAccount, transactionFee,
				transactionValidDuration, generateRecord, memo, this.getAppendTransactionBody());
		// add the signatures
		transaction.signatureList = sigsForTransaction;

		// issue the transaction
		Utilities.throwIfNull("Node", this.node);

		HederaTransactionResult hederaTransactionResult = this.node.fileAppend(transaction);
		hederaTransactionResult.hederaTransactionID = transactionID;


		// return
		return hederaTransactionResult;

	}

	/**
	 * This method runs a transaction to create a file with a record, default values
	 * are used to create the file with
	 * 
	 * @param accountShardNum the shard of the account paying for the file creation
	 * @param accountRealmNum the realm of the account paying for the file creation
	 * @param accountNum      the account number of the account paying for the file
	 *                        creation
	 * @param nodeShardNum    the shard of the node the transaction is sent to
	 * @param nodeRealmNum    the realm of the node the transaction is sent to
	 * @param nodeAccountNum  the account number of the node the transaction is sent
	 *                        to
	 * @param transactionFee  the fee paid by the client to run the transaction
	 * @param memo            String memo to include in the transaction
	 * @param expirationTime  the expiration time for the file
	 * @param contents        the contents of the file to be created
	 * @param keyPair         the keypair owning the file
	 * @return {@link HederaTransactionResult}
	 * @throws InterruptedException in the event that communication to the node
	 *                              resulted in an error
	 */
	public HederaTransactionResult createWithRecord(long accountShardNum, long accountRealmNum, long accountNum,
			long nodeShardNum, long nodeRealmNum, long nodeAccountNum, long transactionFee, String memo,
			Instant expirationTime, byte[] contents, HederaKeyPair keyPair) throws Exception {

		return createSimple(accountShardNum, accountRealmNum, accountNum, nodeShardNum, nodeRealmNum, nodeAccountNum,
				transactionFee, true, memo, expirationTime, contents, keyPair);
	}

	/**
	 * This method runs a transaction to create a file without a record, default
	 * values are used to create the file with
	 * 
	 * @param accountShardNum the shard of the account paying for the file creation
	 * @param accountRealmNum the realm of the account paying for the file creation
	 * @param accountNum      the account number of the account paying for the file
	 *                        creation
	 * @param nodeShardNum    the shard of the node the transaction is sent to
	 * @param nodeRealmNum    the realm of the node the transaction is sent to
	 * @param nodeAccountNum  the account number of the node the transaction is sent
	 *                        to
	 * @param transactionFee  the fee paid by the client to run the transaction
	 * @param memo            String memo to include in the transaction
	 * @param expirationTime  the expiration time for the file
	 * @param contents        the contents of the file to be created
	 * @param keyPair         the keypair owning the file
	 * @return {@link HederaTransactionResult}
	 * @throws InterruptedException in the event that communication to the node
	 *                              resulted in an error
	 */
	public HederaTransactionResult createNoRecord(long accountShardNum, long accountRealmNum, long accountNum,
			long nodeShardNum, long nodeRealmNum, long nodeAccountNum, long transactionFee, String memo,
			Instant expirationTime, byte[] contents, HederaKeyPair keyPair) throws Exception {

		return createSimple(accountShardNum, accountRealmNum, accountNum, nodeShardNum, nodeRealmNum, nodeAccountNum,
				transactionFee, false, memo, expirationTime, contents, keyPair);
	}

	private HederaTransactionResult createSimple(long accountShardNum, long accountRealmNum, long accountNum,
			long nodeShardNum, long nodeRealmNum, long nodeAccountNum, long transactionFee, boolean generateRecord,
			String memo, Instant expirationTime, byte[] contents, HederaKeyPair keyPair) throws Exception {

		// create a node account id
		HederaAccountID nodeAccount = new HederaAccountID(nodeShardNum, nodeRealmNum, nodeAccountNum);
		// my account id
		HederaAccountID myAccountId = new HederaAccountID(accountShardNum, accountRealmNum, accountNum);

		// Create a transaction id
		// Timestamp in the transactionID here will be defaulted to now
		HederaTransactionID transactionId = new HederaTransactionID(myAccountId);

		// duration defaults to 3 minutes if not set
		HederaDuration transactionValidDuration = new HederaDuration();
		
		// add the wACL keys
		this.addKey(keyPair);

		// set file contents
		this.contents = contents;

		this.expirationTime = expirationTime;

		// File Create Transaction Body
		byte[] body = bodyToSignForCreate(transactionId, nodeAccount, transactionFee, transactionValidDuration,
				generateRecord, memo).toByteArray();

		// Sign the body with the key(s) for the file wACL
		HederaSignature fileSig = Utilities.getSignature(body, keyPair);

		// build signature List
		HederaSignatureList transactionSignatures = new HederaSignatureList();

		// first the key+sig for the account
		transactionSignatures.addSignature(fileSig);
		// then file wACL
		transactionSignatures.addSignature(fileSig);

		/*
		 * Generate transaction to send
		 */
		HederaTransactionResult hederaTransactionResult = create(transactionId, nodeAccount, transactionFee,
				transactionValidDuration, generateRecord, memo, transactionSignatures);

		hederaTransactionResult.hederaTransactionID = transactionId;


		return hederaTransactionResult;
	}

	/**
	 * Runs a query to get the contents of a given file If successful, the method
	 * populates the properties this object depending on the type of answer
	 * requested
	 * 
	 * @param payment      a {@link HederaTransaction} message to indicate how this
	 *                     query will be paid for, this can be null for Cost queries
	 * @param responseType the type of response requested from the query
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication
	 *                              with the node
	 */
	public boolean getContents(HederaTransaction payment, HederaQueryHeader.QueryResponseType responseType)
			throws InterruptedException {
		boolean result = true;


		// build the query
		// Header
		HederaQueryHeader queryHeader = new HederaQueryHeader();
		if (payment != null) {
			queryHeader.payment = payment;
			queryHeader.responseType = responseType;
		}

		// get contents query
		FileGetContentsQuery.Builder fileGetContents = FileGetContentsQuery.newBuilder();
		fileGetContents.setFileID(new HederaFileID(this.shardNum, this.realmNum, this.fileNum).getProtobuf());
		fileGetContents.setHeader(queryHeader.getProtobuf());

		// the query itself
		HederaQuery query = new HederaQuery();
		query.queryType = QueryType.FILEGETCONTENTS;
		query.queryData = fileGetContents.build();

		// query now set, send to network
		Utilities.throwIfNull("Node", this.node);

		Response response = this.node.getFileContents(query);

		FileGetContentsResponse.Builder fileContentsResponse = response.getFileGetContents().toBuilder();

		// check response header first
		ResponseHeader.Builder responseHeader = fileContentsResponse.getHeaderBuilder();

		this.precheckResult = responseHeader.getNodeTransactionPrecheckCode();

		if (this.precheckResult == ResponseCodeEnum.OK) {
			// contents
			this.contents = fileContentsResponse.getFileContents().getContents().toByteArray();
			// cost
			this.cost = responseHeader.getCost();
			// state proof
			this.stateProof = responseHeader.getStateProof().toByteArray();
		} else {
			result = false;
		}


		return result;
	}

	/**
	 * Runs a query to get the contents of a given file without a state proof If
	 * successful, the method populates the cost and contents of this object
	 * 
	 * @param payment      a {@link HederaTransaction} message to indicate how this
	 *                     query will be paid for, this can be null for Cost queries
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication
	 *                              with the node
	 */
	public boolean getContentsAnswerOnly(HederaTransaction payment) throws InterruptedException {

		return getContents(payment, QueryResponseType.ANSWER_ONLY);
	}

	/**
	 * Runs a query to get the contents of a given file with a state proof If
	 * successful, the method populates the cost, stateproof and contents of this
	 * object
	 * 
	 * @param payment      a {@link HederaTransaction} message to indicate how this
	 *                     query will be paid for, this can be null for Cost queries
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication
	 *                              with the node
	 */
	public boolean getContentsStateProof(HederaTransaction payment) throws InterruptedException {

		return getContents(payment, HederaQueryHeader.QueryResponseType.ANSWER_STATE_PROOF);
	}

	/**
	 * Runs a query to get cost of getting the contents of a given file without a
	 * state proof If successful, the method populates the cost property of this
	 * object
	 * 
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication
	 *                              with the node
	 */
	public boolean getContentsCostAnswer() throws InterruptedException {

		return getContents(null, HederaQueryHeader.QueryResponseType.COST_ANSWER);
	}

	/**
	 * Runs a query to get cost of getting the contents of a given file with a state
	 * proof If successful, the method populates the cost property of this object
	 * 
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication
	 *                              with the node
	 */
	public boolean getContentsAnswerStateProof() throws InterruptedException {

		return getContents(null, HederaQueryHeader.QueryResponseType.COST_ANSWER_STATE_PROOF);
	}

	/**
	 * Runs a query to get the information about a given file If successful, the
	 * method populates the properties this object depending on the type of answer
	 * requested
	 * 
	 * @param payment      a {@link HederaTransaction} message to indicate how this
	 *                     query will be paid for, this can be null for Cost queries
	 * @param responseType the type of response requested from the query
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication
	 *                              with the node
	 */
	public boolean getInfo(HederaTransaction payment, HederaQueryHeader.QueryResponseType responseType)
			throws InterruptedException {
		boolean result = true;


		// build the query
		// Header
		HederaQueryHeader queryHeader = new HederaQueryHeader();
		if (payment != null) {
			queryHeader.payment = payment;
			queryHeader.responseType = responseType;
		}

		// get info query
		FileGetInfoQuery.Builder fileGetInfoQuery = FileGetInfoQuery.newBuilder();
		fileGetInfoQuery.setFileID(new HederaFileID(this.shardNum, this.realmNum, this.fileNum).getProtobuf());
		fileGetInfoQuery.setHeader(queryHeader.getProtobuf());

		// the query itself
		HederaQuery query = new HederaQuery();
		query.queryType = QueryType.FILEGETINFO;
		query.queryData = fileGetInfoQuery.build();

		// query now set, send to network
		Utilities.throwIfNull("Node", this.node);

		Response response = this.node.getFileInfo(query);

		FileGetInfoResponse.Builder fileGetInfoResponse = response.getFileGetInfo().toBuilder();

		// check response header first
		ResponseHeader.Builder responseHeader = fileGetInfoResponse.getHeaderBuilder();

		this.precheckResult = responseHeader.getNodeTransactionPrecheckCode();

		if (this.precheckResult == ResponseCodeEnum.OK) {
			FileInfo fileInfo = fileGetInfoResponse.getFileInfo();
			// fileID
			// no need to set, it is what we used to issue the query in the first place
			this.cost = responseHeader.getCost();
			this.stateProof = responseHeader.getStateProof().toByteArray();
			this.size = fileInfo.getSize();
			HederaTimeStamp timestamp = new HederaTimeStamp(fileInfo.getExpirationTime());
			this.expirationTime = timestamp.time;
			this.deleted = fileInfo.getDeleted();
			this.keys.clear();
			
			KeyList protoKeys = fileInfo.getKeys();

			for (int i = 0; i < protoKeys.getKeysCount(); i++) {
				HederaKeyPair key = new HederaKeyPair(protoKeys.getKeys(i));
				this.addKey(key);
			}
		} else {
			result = false;
		}


		return result;
	}

	/**
	 * Runs a query to get the contents of a given file without a state proof If
	 * successful, the method populates the properties of this object
	 * 
	 * @param payment      a {@link HederaTransaction} message to indicate how this
	 *                     query will be paid for, this can be null for Cost queries
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication
	 *                              with the node
	 */
	public boolean getInfoAnswerOnly(HederaTransaction payment) throws InterruptedException {

		return getInfo(payment, QueryResponseType.ANSWER_ONLY);
	}

	/**
	 * Runs a query to get the contents of a given file with a state proof If
	 * successful, the method populates the properties of this object
	 * 
	 * @param payment      a {@link HederaTransaction} message to indicate how this
	 *                     query will be paid for, this can be null for Cost queries
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication
	 *                              with the node
	 */
	public boolean getInfoStateProof(HederaTransaction payment) throws InterruptedException {

		return getInfo(payment, HederaQueryHeader.QueryResponseType.ANSWER_STATE_PROOF);
	}

	/**
	 * Runs a query to get the cost of getting the contents of a given file without
	 * a state proof If successful, the method populates the cost property of this
	 * object
	 * 
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication
	 *                              with the node
	 */
	public boolean getInfoCostAnswer() throws InterruptedException {

		return getInfo(null, HederaQueryHeader.QueryResponseType.COST_ANSWER);
	}

	/**
	 * Runs a query to get the cost of getting the contents of a given file with a
	 * state proof If successful, the method populates the cost property of this
	 * object
	 * 
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication
	 *                              with the node
	 */
	public boolean getInfoCostAnswerStateProof() throws InterruptedException {

		return getInfo(null, HederaQueryHeader.QueryResponseType.COST_ANSWER_STATE_PROOF);
	}

	/**
	 * This method returns the {FileCreateTransactionBody} body for a transaction to
	 * create a file
	 * 
	 * @return {@link FileCreateTransactionBody}
	 */
	public FileCreateTransactionBody getCreateTransactionBody() {


		FileCreateTransactionBody.Builder fileCreateTransaction = FileCreateTransactionBody.newBuilder();

		if (this.expirationTime != null) {
			HederaTimeStamp timestamp = new HederaTimeStamp(this.expirationTime);
			fileCreateTransaction.setExpirationTime(timestamp.getProtobuf());
		}
		
		if (this.keys.size() > 0) {
			fileCreateTransaction.setKeys(Utilities.getProtoKeyList(this.keys));
		}

		if (this.contents != null) {
			logger.info("Setting the contents body");
			ByteString fileContents = ByteString.copyFrom(this.contents);
			fileCreateTransaction.setContents(fileContents);
		}

		if (this.shardNum > 0) {
			fileCreateTransaction.setShardID(new HederaShardID(this.shardNum).getProtobuf());
		}
		if (this.realmNum > 0) {
			fileCreateTransaction.setRealmID(new HederaRealmID(this.shardNum, this.realmNum).getProtobuf());
		}

		if (this.newRealmAdminKey != null) {
			fileCreateTransaction.setNewRealmAdminKey(newRealmAdminKey.getProtobuf());
		}


		return fileCreateTransaction.build();
	}

	/**
	 * This method returns the {FileUpdateTransactionBody} body for a transaction to
	 * update a file
	 * 
	 * @return {@link FileUpdateTransactionBody}
	 */
	public FileUpdateTransactionBody getUpdateTransactionBody() {

		FileUpdateTransactionBody.Builder fileUpdateTransaction = FileUpdateTransactionBody.newBuilder();
		
		fileUpdateTransaction.setFileID(this.getFileID().getProtobuf());

		if (this.expirationTime != null) {
			HederaTimeStamp timestamp = new HederaTimeStamp(this.expirationTime);
			fileUpdateTransaction.setExpirationTime(timestamp.getProtobuf());
		}
		HederaKeyList hederaKeyList = new HederaKeyList();

		if (this.newKeys.size() > 0) {
			for (HederaKeyPair key : this.newKeys) {
				hederaKeyList.addKey(key);
			}
			fileUpdateTransaction.setKeys(hederaKeyList.getProtobuf());
		}

		if (this.contents != null) {
			ByteString fileContents = ByteString.copyFrom(this.contents);
			fileUpdateTransaction.setContents(fileContents);
		}


		return fileUpdateTransaction.build();
	}

	/**
	 * This method returns the {FileDeleteTransactionBody} body for a transaction to
	 * delete a file
	 * 
	 * @return {@link FileDeleteTransactionBody}
	 */
	public FileDeleteTransactionBody getDeleteTransactionBody() {

		// Generates the protobuf payload for this class
		FileDeleteTransactionBody.Builder fileDeleteTransactionBody = FileDeleteTransactionBody.newBuilder();

		FileID.Builder fileId = FileID.newBuilder();
		fileId.setFileNum(this.fileNum);
		if (this.realmNum != -1) {
			// if realmnum is -1, create a new realm
			fileId.setRealmNum(this.realmNum);
		}
		fileId.setShardNum(this.shardNum);

		fileDeleteTransactionBody.setFileID(fileId);


		return fileDeleteTransactionBody.build();
	}

	/**
	 * This method returns the {FileAppendTransactionBody} body for a transaction to
	 * append to a file
	 * 
	 * @return {@link FileAppendTransactionBody}
	 */
	public FileAppendTransactionBody getAppendTransactionBody() {

		FileAppendTransactionBody.Builder fileAppendTransaction = FileAppendTransactionBody.newBuilder();

		// file Id or transactionId
		FileID.Builder fileId = FileID.newBuilder();
		fileId.setFileNum(this.fileNum);
		if (this.realmNum != -1) {
			// if realmnum is -1, create a new realm
			fileId.setRealmNum(this.realmNum);
		}
		fileId.setShardNum(this.shardNum);

		fileAppendTransaction.setFileID(fileId);

		if (this.appendContents != null) {
			ByteString fileContents = ByteString.copyFrom(this.appendContents);
			fileAppendTransaction.setContents(fileContents);
		}


		return fileAppendTransaction.build();
	}

	/**
	 * Adds a {@link HederaKeyPair} to the list
	 * 
	 * @param key the key to add to the list
	 */
	public void addKey(HederaKeyPair key) {

		this.keys.add(key);

	}

	/**
	 * Adds a {@link HederaKeyPair} to the list of new keys
	 * 
	 * @param key the key to add to the list
	 */
	public void addNewKey(HederaKeyPair key) {

		this.newKeys.add(key);

	}

	/**
	 * Deletes a {@link HederaKeyPair} from the list
	 * 
	 * @param key the key to delete
	 * @return {@link Boolean} true if successful
	 */
	public boolean deleteKey(HederaKeyPair key) {

		return this.keys.remove(key);
	}

	/**
	 * Deletes a {@link HederaKeyPair} from the list
	 * 
	 * @param key the key to remove
	 * @return {@link Boolean} true if successful
	 */
	public boolean deleteNewKey(HederaKeyPair key) {

		return this.newKeys.remove(key);
	}

	/**
	 * returns the list of {@link HederaKeyPair}
	 * 
	 * @return {@link List} of {@link HederaKeyPair}
	 */
	public List<HederaKeyPair> getNewKeys() {

		return this.newKeys;
	}

	/**
	 * returns the list of {@link HederaKeyPair}
	 * 
	 * @return {@link List} of {@link HederaKeyPair}
	 */
	public List<HederaKeyPair> getKeys() {

		return this.keys;
	}

	// SIMPLIFICATION
	/**
	 * Creates a file in the simplest possible way
	 * 
	 * @param shardNum the shard in which to create the file
	 * @param realmNum the realm in which to create the file
	 * @param contents the file contents in bytes
	 * @param defaults {@link HederaFileCreateDefaults} default parameters for
	 *                 setting up a file, if null the {@link HederaFile} class
	 *                 defaults will be used
	 * @return {@link HederaTransactionResult}
	 * @throws Exception in the event of an error 
	 */
	public HederaTransactionResult create(long shardNum, long realmNum, byte[] contents,
			HederaFileCreateDefaults defaults) throws Exception {

		// setup defaults if necessary
		if (defaults != null) {
			this.expirationTime = Instant.now().plusSeconds(defaults.expirationTimeSeconds)
					.plusNanos(defaults.expirationTimeNanos);
			this.newRealmAdminKey = defaults.getNewRealmAdminPublicKey();
		}

		// initialise the result
		HederaTransactionResult transactionResult = new HederaTransactionResult();

		// required
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.contents = contents.clone();

		// validate inputs
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", this.txQueryDefaults.node);
		Utilities.throwIfNull("txQueryDefaults.payingKeyPair", this.txQueryDefaults.payingKeyPair);
		Utilities.throwIfAccountIDInvalid("Node", this.txQueryDefaults.node.getAccountID());
		Utilities.throwIfAccountIDInvalid("txQueryDefaults.payingAccountID", this.txQueryDefaults.payingAccountID);

		// set transport
		this.node = this.txQueryDefaults.node;

		// create a transaction ID (starts now with accountID of the paying account id)
		this.hederaTransactionID = new HederaTransactionID(this.txQueryDefaults.payingAccountID);

		if (this.txQueryDefaults.fileWacl != null) {
			this.addKey(this.txQueryDefaults.fileWacl);
		}
		
		// get the body for the transaction so we can sign it
		TransactionBody createBody = this.bodyToSignForCreate(hederaTransactionID, this.node.getAccountID(),
				this.node.fileCreateTransactionFee, this.txQueryDefaults.transactionValidDuration,
				this.txQueryDefaults.generateRecord, this.txQueryDefaults.memo);

		// Signatures
		HederaSignatureList sigsForTransaction = new HederaSignatureList();
		//paying signature
		sigsForTransaction.addSignature(this.txQueryDefaults.payingKeyPair.getSignature(createBody.toByteArray()));
		// new realm if set
		if (this.newRealmAdminKey != null) {
			sigsForTransaction.addSignature(this.newRealmAdminKey.getSignature(createBody.toByteArray()));
		}
		// FILE WACL
		if (this.txQueryDefaults.fileWacl != null) {
			sigsForTransaction.addSignature(this.txQueryDefaults.fileWacl.getSignature(createBody.toByteArray()));
		}

		// create the file
		transactionResult = this.create(hederaTransactionID, this.node.getAccountID(), this.node.fileCreateTransactionFee,
				this.txQueryDefaults.transactionValidDuration, this.txQueryDefaults.generateRecord, this.txQueryDefaults.memo,
				sigsForTransaction);


		return transactionResult;
	}

	/**
	 * Deletes a file in the simplest possible way
	 * 
	 * @return {@link HederaTransactionResult}
	 * @throws Exception in the event of an error 
	 */
	public HederaTransactionResult delete() throws Exception {


		// initialise the result
		HederaTransactionResult transactionResult = new HederaTransactionResult();

		// validate inputs
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", this.txQueryDefaults.node);
		Utilities.throwIfAccountIDInvalid("Node", this.txQueryDefaults.node.getAccountID());
		Utilities.throwIfNull("txQueryDefaults.payingKeyPair", this.txQueryDefaults.payingKeyPair);
		Utilities.throwIfAccountIDInvalid("txQueryDefaults.payingKeyPair", this.txQueryDefaults.payingAccountID);

		// set transport
		this.node = this.txQueryDefaults.node;

		// create a transaction ID (starts now with accountID of the paying account id)
		this.hederaTransactionID = new HederaTransactionID(this.txQueryDefaults.payingAccountID);

		// get the body for the transaction so we can sign it
		TransactionBody deleteBody = this.bodyToSignForDelete(hederaTransactionID, this.node.getAccountID(),
				this.node.fileDeleteTransactionFee, this.txQueryDefaults.transactionValidDuration,
				this.txQueryDefaults.generateRecord, this.txQueryDefaults.memo);

		// Signatures
		HederaSignatureList sigsForTransaction = new HederaSignatureList();
		//paying signature
		sigsForTransaction.addSignature(this.txQueryDefaults.payingKeyPair.getSignature(deleteBody.toByteArray()));
		// FILE WACL
		if (this.txQueryDefaults.fileWacl != null) {
			sigsForTransaction.addSignature(this.txQueryDefaults.fileWacl.getSignature(deleteBody.toByteArray()));
		}
		
		// delete the file
		transactionResult = this.delete(hederaTransactionID, this.node.getAccountID(), this.node.fileDeleteTransactionFee,
				this.txQueryDefaults.transactionValidDuration, this.txQueryDefaults.generateRecord, this.txQueryDefaults.memo,
				sigsForTransaction);


		return transactionResult;
	}

	/**
	 * Deletes a file in the simplest possible way
	 * 
	 * @param shardNum, the shard in which the file exists
	 * @param realmNum, the realm in which the file exists
	 * @param fileNum, the file number
	 * @return {@link HederaTransactionResult}
	 * @throws Exception in the event of an error 
	 */
	public HederaTransactionResult delete(long shardNum, long realmNum, long fileNum) throws Exception {
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.fileNum = fileNum;
		return delete();
	}

	/**
	 * Appends to a file in the simplest possible way
	 * 
	 * @param contents the file contents in bytes to append
	 * @return {@link HederaTransactionResult}
	 * @throws Exception in the event of an error 
	 */
	public HederaTransactionResult append(byte[] contents) throws Exception {


		// initialise the result
		HederaTransactionResult transactionResult = new HederaTransactionResult();

		// validate inputs
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", this.txQueryDefaults.node);
		Utilities.throwIfAccountIDInvalid("Node", this.txQueryDefaults.node.getAccountID());
		Utilities.throwIfNull("txQueryDefaults.payingKeyPair", this.txQueryDefaults.payingKeyPair);
		Utilities.throwIfAccountIDInvalid("txQueryDefaults.payingKeyPair", this.txQueryDefaults.payingAccountID);
		
		// set transport
		this.node = this.txQueryDefaults.node;

		// append contents
		this.appendContents = contents.clone();

		// create a transaction ID (starts now with accountID of the paying account id)
		this.hederaTransactionID = new HederaTransactionID(this.txQueryDefaults.payingAccountID);

		// get the body for the transaction so we can sign it
		TransactionBody appendBody = this.bodyToSignForAppend(hederaTransactionID, this.node.getAccountID(),
				this.node.fileAppendTransactionFee, this.txQueryDefaults.transactionValidDuration,
				this.txQueryDefaults.generateRecord, this.txQueryDefaults.memo);

		// Signatures
		HederaSignatureList sigsForTransaction = new HederaSignatureList();
		//paying signature
		sigsForTransaction.addSignature(this.txQueryDefaults.payingKeyPair.getSignature(appendBody.toByteArray()));
		// FILE WACL
		if (this.txQueryDefaults.fileWacl != null) {
			sigsForTransaction.addSignature(this.txQueryDefaults.fileWacl.getSignature(appendBody.toByteArray()));
		}
		
		// add to the file
		transactionResult = this.append(hederaTransactionID, this.node.getAccountID(), this.node.fileAppendTransactionFee,
				this.txQueryDefaults.transactionValidDuration, this.txQueryDefaults.generateRecord, this.txQueryDefaults.memo,
				sigsForTransaction);


		return transactionResult;
	}

	/**
	 * Appends to a file in the simplest possible way
	 * 
	 * @param shardNum, the shard in which the file exists
	 * @param realmNum, the realm in which the file exists
	 * @param fileNum, the file number
	 * @param contents, the contents to add to the file
	 * @return {@link HederaTransactionResult}
	 * @throws Exception in the event of an error 
	 */
	public HederaTransactionResult append(long shardNum, long realmNum, long fileNum, byte[] contents)
			throws Exception {
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.fileNum = fileNum;
		return append(contents);
	}

	/**
	 * Updates a file in the simplest possible way Set the expiration times to -1 if
	 * you don't wish to update them Note: if seconds or nanos are supplied and the
	 * other set to -1, it will be set to 0 likewise, leave the contents null if you
	 * don't wish to update them
	 * 
	 * @param expirationTimeSeconds the new file expiration seconds (leave null for
	 *                              no change)
	 * @param expirationTimeNanos   the file expiration time nanos (leave null for
	 *                              no change)
	 * @param contents              the file contents in bytes to append
	 * @return {@link HederaTransactionResult}
	 * @throws Exception in the event of an error 
	 */
	public HederaTransactionResult update(long expirationTimeSeconds, int expirationTimeNanos, byte[] contents)
			throws Exception {

		// initialise the result
		HederaTransactionResult transactionResult = new HederaTransactionResult();

		if ((expirationTimeSeconds != -1) || (expirationTimeNanos != -1)) {
			if (expirationTimeNanos == -1) {
				expirationTimeNanos = 0;
			}
			if (expirationTimeSeconds == -1) {
				expirationTimeSeconds = 0;
			}
			this.expirationTime = Instant.now().plusSeconds(expirationTimeSeconds).plusNanos(expirationTimeNanos);
		} else {
			this.expirationTime = null;
		}

		if (contents != null) {
			this.contents = contents.clone();
		} else {
			this.contents = null;
		}

		// validate inputs
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", this.txQueryDefaults.node);
		Utilities.throwIfAccountIDInvalid("Node", this.txQueryDefaults.node.getAccountID());
		Utilities.throwIfNull("txQueryDefaults.payingKeyPair", this.txQueryDefaults.payingKeyPair);
		Utilities.throwIfAccountIDInvalid("txQueryDefaults.payingKeyPair", this.txQueryDefaults.payingAccountID);

		// set transport
		this.node = this.txQueryDefaults.node;

		// create a transaction ID (starts now with accountID of the paying account id)
		this.hederaTransactionID = new HederaTransactionID(this.txQueryDefaults.payingAccountID);

		// get the body for the transaction so we can sign it
		TransactionBody updateBody = this.bodyToSignForUpdate(hederaTransactionID, this.node.getAccountID(),
				this.node.fileUpdateTransactionFee, this.txQueryDefaults.transactionValidDuration,
				this.txQueryDefaults.generateRecord, this.txQueryDefaults.memo);

		// Signatures
		HederaSignatureList sigsForTransaction = new HederaSignatureList();
		//paying signature
		sigsForTransaction.addSignature(this.txQueryDefaults.payingKeyPair.getSignature(updateBody.toByteArray()));
		// FILE WACL
		if (this.txQueryDefaults.fileWacl != null) {
			sigsForTransaction.addSignature(this.txQueryDefaults.fileWacl.getSignature(updateBody.toByteArray()));
		}
		
		// update the file
		transactionResult = this.update(hederaTransactionID, this.node.getAccountID(), this.node.fileUpdateTransactionFee,
				this.txQueryDefaults.transactionValidDuration, this.txQueryDefaults.generateRecord, this.txQueryDefaults.memo,
				sigsForTransaction);


		return transactionResult;
	}

	/**
	 * Updates a file in the simplest possible way Set the expiration times to -1 if
	 * you don't wish to update them Note: if seconds or nanos are supplied and the
	 * other set to -1, it will be set to 0 likewise, leave the contents null if you
	 * don't wish to update them
	 * 
	 * @param                       shardNum, the shard in which the file exists
	 * @param                       realmNum, the realm in which the file exists
	 * @param                       fileNum, the file number
	 * @param expirationTimeSeconds the new file expiration seconds (leave null for
	 *                              no change)
	 * @param expirationTimeNanos   the file expiration time nanos (leave null for
	 *                              no change)
	 * @param contents              the file contents in bytes to append
	 * @return {@link HederaTransactionResult}
	 * @throws Exception in the event of an error 
	 */
	public HederaTransactionResult update(long shardNum, long realmNum, long fileNum, long expirationTimeSeconds,
			int expirationTimeNanos, byte[] contents) throws Exception {
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.fileNum = fileNum;
		return update(expirationTimeSeconds, expirationTimeNanos, contents);
	}

	/**
	 * Gets the contents of the file, returns null if an error occurred in the event
	 * of an error, check the value of this.precheckResult to determine the cause of
	 * the error
	 * 
	 * @return {@link byte} array
	 * @throws Exception in the event of an error 
	 */
	public byte[] getContents() throws Exception {

		
		// validate inputs
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", this.txQueryDefaults.node);
		
		// set transport
		this.node = this.txQueryDefaults.node;
		
		HederaTransaction transferTransaction = new HederaTransaction(this.txQueryDefaults,
				this.node.fileGetContentsQueryFee);

		if (this.getContentsAnswerOnly(transferTransaction)) {

			return this.contents;
		} else {

			return null;
		}
	}

	/**
	 * Gets the contents of the file, returns null if an error occurred in the event
	 * of an error, check the value of this.precheckResult to determine the cause of
	 * the error
	 * 
	 * @param shardNum, the shard in which the file exists
	 * @param realmNum, the realm in which the file exists
	 * @param fileNum, the file number
	 * @return {@link byte} array
	 * @throws Exception in the event of an error 
	 */
	public byte[] getContents(long shardNum, long realmNum, long fileNum) throws Exception {
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.fileNum = fileNum;
		return getContents();
	}

	/**
	 * Get info for the file which is specified by the shardNum, realmNum and
	 * fileNum properties of this class in the event of an error, check the value of
	 * this.precheckResult to determine the cause of the error Note: You may perform
	 * a "getInfoCostAnswer" in order to ascertain the cost of the query first The
	 * cost could be cached and refreshed from time to time, there is no need to
	 * look it up before each getInfo query
	 * 
	 * @return {@link boolean}
	 * @throws Exception in the event of an error 
	 */
	public boolean getInfo() throws Exception {

		// validate inputs
		Utilities.throwIfNull("txQueryDefaults", this.txQueryDefaults);
		Utilities.throwIfNull("txQueryDefaults.node", this.txQueryDefaults.node);
		
		// set transport
		this.node = this.txQueryDefaults.node;

		HederaTransaction transferTransaction = new HederaTransaction(this.txQueryDefaults, this.node.fileGetInfoQueryFee);

		return this.getInfoAnswerOnly(transferTransaction);
	}

	/**
	 * Get info for the file in the event of an error, check the value of
	 * this.precheckResult to determine the cause of the error
	 * 
	 * @param shardNum, the shard in which the file exists
	 * @param realmNum, the realm in which the file exists
	 * @param fileNum, the file number
	 * @return {@link boolean}
	 * @throws Exception in the event of an error 
	 */
	public boolean getInfo(long shardNum, long realmNum, long fileNum) throws Exception {
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.fileNum = fileNum;
		return getInfo();
	}
}