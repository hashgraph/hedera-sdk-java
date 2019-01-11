package com.hedera.sdk.query;

import java.io.Serializable;
import org.slf4j.LoggerFactory;
import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaContractID;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.node.HederaNode;
import com.hedera.sdk.query.HederaQuery.QueryType;
import com.hedera.sdk.query.HederaQueryHeader.QueryResponseType;
import com.hedera.sdk.transaction.HederaTransaction;
import com.hederahashgraph.api.proto.java.*;
/**
 * This class handles queries by solidity ID
 *
 */
public class HederaQueryBySolidityID implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaQueryBySolidityID.class);
	private static final long serialVersionUID = 1;
	private ResponseCodeEnum precheckResult = ResponseCodeEnum.UNKNOWN;
	private long cost = 0;
	private byte[] stateProof = new byte[0];
	private HederaNode node = null;
	private HederaAccountID accountID = null;
	private HederaContractID contractID = null;
	/**
	 * returns a {@link HederaAccountID} as a result of running the query
	 * note: this may be null
	 * @return {@link HederaAccountID}
	 */
	public HederaAccountID accountID() {
		return this.accountID;
	}
	/**
	 * returns a {@link HederaContractID} as a result of running the query
	 * note: this may be null
	 * @return {@link HederaContractID}
	 */
	public HederaContractID contractID() {
		return this.contractID;
	}
	/**
	 * Sets the node object to use for communication with a node
	 * @param node the {@link HederaNode} to use
	 */
	public void setNode (HederaNode node) {
		this.node = node;
	}
	
	/**
	 * Default constructor
	 */
	public HederaQueryBySolidityID() {


	}
	/**
	 * Returns the precheck result for a query
	 * @return {@link ResponseCodeEnum}
	 */
	public ResponseCodeEnum getPrecheckResult() {
		return this.precheckResult;
	}
	/**
	 * Returns the cost of running a query (after this has been enquired for)
	 * @return long
	 */
	public long getCost() {
		return this.cost;
	}
	/**
	 * Returns the state proof for the query if requested
	 * @return byte[]
	 */
	public byte[] getStateProof() {
		return this.stateProof;
	}

	/**
	 * Runs the query to get results from a solidityIDQuery 
	 * If successful, the method populates the properties this object depending on the type of answer requested
	 * @param payment a {@link HederaTransaction} message to indicate how this query will be paid for, this can be null for Cost queries
	 * @param responseType the type of response requested from the query
	 * @param solidityID the solidityID being queried against
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication with the node
	 */
	public boolean query(HederaTransaction payment, HederaQueryHeader.QueryResponseType responseType, String solidityID) throws InterruptedException {
		boolean result = true;
		

		// build the query
	   	// Header
		HederaQueryHeader queryHeader = new HederaQueryHeader();
		if (payment != null) {
			queryHeader.payment = payment;
			queryHeader.responseType = responseType;
		}
		
		// get solidity id query
		GetBySolidityIDQuery.Builder getBySolidityIDQuery = GetBySolidityIDQuery.newBuilder();
		getBySolidityIDQuery.setSolidityID(solidityID);
		getBySolidityIDQuery.setHeader(queryHeader.getProtobuf());
		
		// the query itself
		HederaQuery query = new HederaQuery();
		query.queryType = QueryType.GETBYSOLIDITYID;
		query.queryData = getBySolidityIDQuery.build();
		
		// query now set, send to network
		Utilities.throwIfNull("Node", this.node);
		Response response = this.node.getContractBySolidityId(query);

		GetBySolidityIDResponse.Builder getBySolidityIDResponse = response.getGetBySolidityID().toBuilder();
		
		// check response header first
		ResponseHeader.Builder responseHeader = getBySolidityIDResponse.getHeaderBuilder();
		
		this.precheckResult = responseHeader.getNodeTransactionPrecheckCode();

		if (this.precheckResult == ResponseCodeEnum.OK) {

			this.cost = responseHeader.getCost();
			this.stateProof = responseHeader.getStateProof().toByteArray();
			this.contractID = null;
			this.accountID = null;
			
			if (getBySolidityIDResponse.hasAccountID()) {
				this.accountID = new HederaAccountID(getBySolidityIDResponse.getAccountID());
			}
			if (getBySolidityIDResponse.hasContractID()) {
				this.contractID = new HederaContractID(getBySolidityIDResponse.getContractID());
			}
		} else {
			result = false;
		}
		

	   	return result;
	}
	/**
	 * Runs the solidityIDQuery query requesting only an answer
	 * If successful, the method populates the properties of this object
	 * @param payment a {@link HederaTransaction} message to indicate how this query will be paid for, this can be null for Cost queries
	 * @param solidityID the solidityID of the entity to query
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication with the node
	 */
	public boolean queryAnswerOnly(HederaTransaction payment, String solidityID) throws InterruptedException {

	   	return query(payment, QueryResponseType.ANSWER_ONLY, solidityID);
	}
	/**
	 * Runs the solidityIDQuery query requesting a state proof
	 * If successful, the method populates the properties of this object
	 * @param payment a {@link HederaTransaction} message to indicate how this query will be paid for, this can be null for Cost queries
	 * @param solidityID the solidityID of the entity to query
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication with the node
	 */
	public boolean queryStateProof(HederaTransaction payment, String solidityID) throws InterruptedException {

		return query(payment, HederaQueryHeader.QueryResponseType.ANSWER_STATE_PROOF, solidityID);
	}
	/**
	 * Queries the cost of running the solidityIDQuery query without a state proof
	 * If successful, the method populates the cost property of this object
	 * @param solidityID the solidityID of the entity to query
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication with the node
	 */
	public boolean queryCostAnswer(String solidityID) throws InterruptedException {

		return query(null, HederaQueryHeader.QueryResponseType.COST_ANSWER, solidityID);
	}
	/**
	 * Queries the cost of running the solidityIDQuery query with a state proof
	 * If successful, the method populates the cost property of this object
	 * @param solidityID the solidityID of the entity to query
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication with the node
	 */
	public boolean queryCostAnswerStateProof(String solidityID) throws InterruptedException {

		return query(null, HederaQueryHeader.QueryResponseType.COST_ANSWER_STATE_PROOF, solidityID);
	}
	
}
