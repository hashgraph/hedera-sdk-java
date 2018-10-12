package com.hedera.sdk.node;

import java.io.Serializable;
import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.query.HederaQuery;
import com.hedera.sdk.transaction.HederaTransaction;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hederahashgraph.api.proto.java.AccountID;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.TransactionResponse;
import com.hederahashgraph.service.proto.java.CryptoServiceGrpc;
import com.hederahashgraph.service.proto.java.FileServiceGrpc;
import com.hederahashgraph.service.proto.java.SmartContractServiceGrpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class HederaNode implements Serializable {
	/**
	 * Class to manage information associated with a Hedera Node 
	 */
	private static final long serialVersionUID = 1L;
	final static Logger logger = LoggerFactory.getLogger(HederaNode.class);
	private String host = "";
    private int port = 0;
    private HederaAccountID accountID = null;
	// GRPC
	private ManagedChannel grpcChannel = null; 
    
	/**
	 * The default fee associated with running an account create transaction
	 */
	public long accountCreateTransactionFee = 10;
	/**
	 * The default fee associated with running a token transaction
	 */
	public long accountTransferTransactionFee = 10;
	/**
	 * The default fee associated with running an update transaction
	 */
	public long accountUpdateTransactionFee = 10;
	/**
	 * The default fee associated with deleting an account
	 */
	public long accountDeleteTransactionFee = 10;
	/**
	 * The default fee associated with adding a claim to an account
	 */
	public long accountAddClaimTransactionFee = 10;
	/**
	 * The default fee associated with deleting a claim from an account
	 */
	public long accountDeleteClaimTransactionFee = 10;
	/**
	 * The default fee associated with querying an account balance
	 */
	public long accountBalanceQueryFee = 10;
	/**
	 * The default fee associated with an account info query
	 */
	public long accountInfoQueryFee = 10;
	/**
	 * The default fee associated with an get records query against an account
	 */
	public long accountGetRecordsQueryFee = 10;
	
	/**
	 * The default fee associated with creating a file
	 */
	public long fileCreateTransactionFee = 10;
	/**
	 * The default fee associated with deleting a file
	 */
	public long fileDeleteTransactionFee = 10;
	/**
	 * The default fee associated with updating a file
	 */
	public long fileUpdateTransactionFee = 10;
	/**
	 * The default fee associated with appending to a file
	 */
	public long fileAppendTransactionFee = 10;
	/**
	 * The default fee associated with getting file contents
	 */
	public long fileGetContentsQueryFee = 10;
	/**
	 * The default fee associated with getting file information
	 */
	public long fileGetInfoQueryFee = 10;
	/**
	 * The default fee associated with a get records query against a file
	 */
	public long fileGetRecordsQueryFee = 10;
	
	/**
	 * The default fee associated with creating a smart contract
	 */
	public long contractCreateTransactionFee = 10;
	/**
	 * The default fee associated with updating a smart contract
	 */
	public long contractUpdateTransactionFee = 10;
	/**
	 * The default fee associated with getting a smart contract's byte code
	 */
	public long contractGetByteCodeQueryFee = 10;
	/**
	 * The default fee associated with calling a smart contract function
	 */
	public long contractCallTransactionFee = 10;
	/**
	 * The default fee associated with getting smart contract information
	 */
	public long contractGetInfoQueryFee = 10;
	/**
	 * The default fee associated with running a local call function
	 */
	public long contractCallLocalQueryFee = 10;
	/**
	 * The default fee associated with querying by solidityID
	 */
	public long contractGetBySolidityId = 10;
	/**
	 * The default fee associated with a get records query against a smart contract
	 */
	public long contractGetRecordsQueryFee = 10;

	/**
	 * Default Constructor
	 */
	public HederaNode() {
		logger.trace("Start - init:");
		logger.trace("End - init");
	}
	/**
	 * Constructor with host and port
	 * @param host the host to communicate with
	 * @param port the port to use for communication
	 */
	public HederaNode(String host, int port) {
		logger.trace("Start - init: host {}, port {}", host, port);
		this.host = host;
		this.port = port;
		openChannel();
		logger.trace("End - init");
	}
	/**
	 * Constructor with host, port and accountID
	 * @param host the host to communicate with
	 * @param port the port to use for communication
	 * @param accountID the node's {@link AccountID}
	 */
	public HederaNode(String host, int port, HederaAccountID accountID) {
		logger.trace("Start - init: host {}, port {}, accountID {}", host, port, accountID);
		this.host = host;
		this.port = port;
		this.accountID = accountID;
		openChannel();
		logger.trace("End - init");
	}
	/**
	 * Sets the host and port of the specified node
	 * @param host {@link String}
	 * @param port {@link Integer}
	 * @throws InterruptedException in the event of a node communication error 
	 */
	public void setHostPort(String host, int port) throws InterruptedException {
		logger.trace("Start - setHostPort: host {}, port {}", host, port);
		if ((!host.equals(this.host)) || (port != this.port)) {
			// close the current connection if open
			shutdown();
			this.host = host;
			this.port = port;
			openChannel();
		}
		logger.trace("End - setHostPort");
    }
    
    /**
     * Gets the host for this node
	 * @return {@link String}
     */
    public String getHost() {
		logger.trace("Start - getHost");
		logger.trace("End - getHost");
        return this.host;
    }

    /**
     * Gets the port used to access this node
	 * @return {@link Integer}
     */
    public int getPort() {
		logger.trace("Start - getPort");
		logger.trace("End - getPort");
        return this.port;
    }

    /**
     * Sets the host for this node
	 * @param host {@link String}
	 * @throws InterruptedException in the event of a node communication error 
     */
    public void setHost(String host) throws InterruptedException {
		logger.trace("Start - setHost: host {}", host);
		if (!host.equals(this.host)) {
			// close the current connection if open
			shutdown();
			this.host = host;
			openChannel();
		}
		logger.trace("End - setHost");
    }

    /**
     * Sets the port used to access this node
	 * @param port {@link Integer}
	 * @throws InterruptedException in the event of a node communication error 
     */
    public void setPort(int port) throws InterruptedException {
		logger.trace("Start - setPort: port {}", port);
		this.port = port;
		if (port != this.port) {
			// close the current connection if open
			shutdown();
			this.port = port;
			openChannel();
		}
		logger.trace("End - setPort");
    }

    /**
     * Sets the account ID for this node based upon an existing HederaAccountID object
	 * @param accountID {@link HederaAccountID}
	 */
	public void setAccountID(HederaAccountID accountID) {
		logger.trace("Start - setAccountID: accountID {}", accountID);
		this.accountID = accountID;
		logger.trace("End - setAccountID");
    }
    
    /**
     * Sets the account ID for this node based upon shardNum, realmNum and accountNum
	 * @param shardNum {@link Long}
	 * @param realmNum {@link Long}
	 * @param accountNum {@link Long}
	 */
	public void setAccountID(long shardNum, long realmNum, long accountNum) {
		logger.trace("Start - setAccountID: shardNum {}, realmNum {}, accountNum {}", shardNum, realmNum, accountNum);
		HederaAccountID accountID = new HederaAccountID(shardNum, realmNum, accountNum);
		this.accountID = accountID;
		logger.trace("End - setAccountID");
    }
    
    /**
     * Gets the account ID for this node
	 * @return {@link HederaAccountID}
	 */
	public HederaAccountID getAccountID() {
		logger.trace("Start - getAccountID");
		logger.trace("End - getAccountID");

		return this.accountID;
    }

	/**
	 * Sends a transaction to a node to create an account and returns the result of the request
	 * @param transaction the {@link HederaTransaction} to send
	 * @return {@link HederaTransactionResult} the result of the transaction
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public HederaTransactionResult accountCreate(HederaTransaction transaction) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - accountCreate protobuf {}", transaction.getProtobuf().toString());
		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasCryptoCreateAccount()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.createAccount(transaction.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}

		if (transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		} else {
			transResult.setError();
		}
		logger.trace("End - accountCreate");
		return transResult;
	}	

	/**
	 * Sends a transaction to a node to add a claim to an account and returns the result of the request
	 * @param transaction the {@link HederaTransaction} to send
	 * @return {@link HederaTransactionResult} the result of the transaction
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public HederaTransactionResult addClaim(HederaTransaction transaction) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - addClaim protobuf {}", transaction.getProtobuf().toString());
		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasCryptoAddClaim()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.addClaim(transaction.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}
		if(transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		} else {
			transResult.setError();
		}
		logger.trace("End - addClaim");
		return transResult;
	}	

	/**
	 * Sends a transaction to a node to transfer tokens from an account to another and returns the result of the request
	 * @param transaction the {@link HederaTransaction} to send
	 * @return {@link HederaTransactionResult} the result of the transaction
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public HederaTransactionResult accountTransfer(HederaTransaction transaction) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - accountTransfer protobuf {}", transaction.getProtobuf().toString());
		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasCryptoTransfer()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.cryptoTransfer(transaction.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}
		if(transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		} else {
			transResult.setError();
		}
		logger.trace("End - accountTransfer");
		return transResult;
	}	

	/**
	 * Sends a transaction to a node to transfer tokens from an account to another and returns the result of the request
	 * @param transaction the {@link HederaTransaction} to send
	 * @return {@link HederaTransactionResult} the result of the transaction
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public HederaTransactionResult accountUpdate(HederaTransaction transaction) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - accountUpdate protobuf {}", transaction.getProtobuf().toString());
		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasCryptoUpdateAccount()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.updateAccount(transaction.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}
		if(transResult != null && response != null) {		
		    transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		} else {
			transResult.setError();
		}
		logger.trace("End - accountUpdate");
		return transResult;
	}	
	
	/**
	 * Sends a transaction to a node to append to a file and returns the result of the request
	 * @param transaction the {@link HederaTransaction} to send
	 * @return {@link HederaTransactionResult} the result of the transaction
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public HederaTransactionResult fileAppend(HederaTransaction transaction) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - fileAppend protobuf {}", transaction.getProtobuf().toString());
		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasFileAppend()) {
			openChannel();
			FileServiceGrpc.FileServiceBlockingStub blockingStub = FileServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.appendContent(transaction.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}
		if(transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		} else {
			transResult.setError();
		}
		logger.trace("End - fileAppend");
		return transResult;
	}	
	
	/**
	 * Sends a transaction to a node to create a file and returns the result of the request
	 * @param transaction the {@link HederaTransaction} to send
	 * @return {@link HederaTransactionResult} the result of the transaction
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public HederaTransactionResult fileCreate(HederaTransaction transaction) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - fileCreate protobuf {}", transaction.getProtobuf().toString());
		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasFileCreate()) {
			openChannel();
			FileServiceGrpc.FileServiceBlockingStub blockingStub = FileServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.createFile(transaction.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}

		if(transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		} else {
			transResult.setError();
		}
		logger.trace("End - fileCreate");
		return transResult;
	}	

	/**
	 * Sends a transaction to a node to delete a file and returns the result of the request
	 * @param transaction the {@link HederaTransaction} to send
	 * @return {@link HederaTransactionResult} the result of the transaction
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public HederaTransactionResult fileDelete(HederaTransaction transaction) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - fileDelete protobuf {}", transaction.getProtobuf().toString());
		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasFileDelete()) {
			openChannel();
			FileServiceGrpc.FileServiceBlockingStub blockingStub = FileServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.deleteFile(transaction.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}

		if(transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		} else {
			transResult.setError();
		}
		logger.trace("End - fileDelete");
		return transResult;
	}	

	/**
	 * Sends a transaction to a node to update a file and returns the result of the request
	 * @param transaction the {@link HederaTransaction} to send
	 * @return {@link HederaTransactionResult} the result of the transaction
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public HederaTransactionResult fileUpdate(HederaTransaction transaction) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - fileUpdate protobuf {}", transaction.getProtobuf().toString());
		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasFileUpdate()) {
			openChannel();
			FileServiceGrpc.FileServiceBlockingStub blockingStub = FileServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.updateFile(transaction.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}

		if(transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		} else {
			transResult.setError();
		}
		logger.trace("End - fileUpdate");
		return transResult;
	}	

	/**
	 * Sends a transaction to a node to call a smart contract function and returns the result of the request
	 * @param transaction the {@link HederaTransaction} to send
	 * @return {@link HederaTransactionResult} the result of the transaction
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public HederaTransactionResult contractCall(HederaTransaction transaction) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - contractCall protobuf {}", transaction.getProtobuf().toString());
		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasContractCall()) {
			openChannel();
			SmartContractServiceGrpc.SmartContractServiceBlockingStub blockingStub = SmartContractServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.contractCallMethod(transaction.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}

		if(transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		} else {
			transResult.setError();
		}
		logger.trace("End - contractCall");
		return transResult;
	}	

	/**
	 * Sends a transaction to a node to create a smart contract instance and returns the result of the request
	 * @param transaction the {@link HederaTransaction} to send
	 * @return {@link HederaTransactionResult} the result of the transaction
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public HederaTransactionResult contractCreate(HederaTransaction transaction) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - contractCreate protobuf {}", transaction.getProtobuf().toString());
		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasContractCreateInstance()) {
			openChannel();
			SmartContractServiceGrpc.SmartContractServiceBlockingStub blockingStub = SmartContractServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.createContract(transaction.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}

		if(transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		} else {
			transResult.setError();
		}
		logger.trace("End - contractCreate");
		return transResult;
	}	

	/**
	 * Sends a transaction to a node to create a smart contract instance and returns the result of the request
	 * @param transaction the {@link HederaTransaction} to send
	 * @return {@link HederaTransactionResult} the result of the transaction
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public HederaTransactionResult contractUpdate(HederaTransaction transaction) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - contractUpdate protobuf {}", transaction.getProtobuf().toString());
		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasContractUpdateInstance()) {
			openChannel();
			SmartContractServiceGrpc.SmartContractServiceBlockingStub blockingStub = SmartContractServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.updateContract(transaction.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}

		if(transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		} else {
			transResult.setError();
		}
		logger.trace("End - contractUpdate");
		return transResult;
	}	

	/**
	 * Sends a query to a node to call a local smart contract function and returns the result of the request
	 * @param query the {@link HederaQuery} to send
	 * @return {@link Response} the result of the query
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public Response contractCallLocal(HederaQuery query) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - contractCallLocal query {}", query);
		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;

		if (query.getProtobuf().hasContractCallLocal()) {
			openChannel();
			SmartContractServiceGrpc.SmartContractServiceBlockingStub blockingStub = SmartContractServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.contractCallLocalMethod(query.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());
		logger.trace("End - contractCallLocal");
		return response;
	}
	
	/**
	 * Sends a query to a node to get a smart contract's byte code and returns the result of the request
	 * @param query the {@link HederaQuery} to send
	 * @return {@link Response} the result of the query
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public Response getContractByteCode(HederaQuery query) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - getContractByteCode query {}", query);
		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;

		if (query.getProtobuf().hasContractGetBytecode()) {
			openChannel();
			SmartContractServiceGrpc.SmartContractServiceBlockingStub blockingStub = SmartContractServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.contractGetBytecode(query.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());
		logger.trace("End - getContractByteCode");
		return response;
	}

	/**
	 * Sends a query to a node to get a smart contract by its solidityID and returns the result of the request
	 * @param query the {@link HederaQuery} to send
	 * @return {@link Response} the result of the query
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public Response getContractBySolidityId(HederaQuery query) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - getContractBySolidityId query {}", query);
		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;
		if (query.getProtobuf().hasGetBySolidityID()) {
			openChannel();
			SmartContractServiceGrpc.SmartContractServiceBlockingStub blockingStub = SmartContractServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.getBySolidityID(query.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());
		logger.trace("End - getContractBySolidityId");
		return response;
	}

	/**
	 * Sends a query to a node to get a smart contract's info and returns the result of the request
	 * @param query the {@link HederaQuery} to send
	 * @return {@link Response} the result of the query
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public Response getContractInfo(HederaQuery query) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - getContractInfo query {}", query);
		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;

		if (query.getProtobuf().hasContractGetInfo()) {
			openChannel();
			SmartContractServiceGrpc.SmartContractServiceBlockingStub blockingStub = SmartContractServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.getContractInfo(query.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());
		logger.trace("End - getContractInfo");
		return response;
	}

	/**
	 * Sends a query to a node to get the balance of an account and returns the result of the request
	 * @param query the {@link HederaQuery} to send
	 * @return {@link Response} the result of the query
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public Response getAccountBalance(HederaQuery query) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - getAccountBalance query {}", query);
		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;

		if (query.getProtobuf().hasCryptogetAccountBalance()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.cryptoGetBalance(query.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());
		logger.trace("End - getAccountBalance");
		return response;
	}

	/**
	 * Sends a query to a node to get the records for an account and returns the result of the request
	 * @param query the {@link HederaQuery} to send
	 * @return {@link Response} the result of the query
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public Response getAccountRecords(HederaQuery query) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - getAccountRecords query {}", query);
		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;

		if (query.getProtobuf().hasCryptoGetAccountRecords()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.getAccountRecords(query.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());
		logger.trace("End - getAccountRecords");
		return response;
	}

	/**
	 * Sends a query to a node to get an account's information and returns the result of the request
	 * @param query the {@link HederaQuery} to send
	 * @return {@link Response} the result of the query
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public Response getAccountInfo(HederaQuery query) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - getAccountInfo query {}", query);
		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;

		if (query.getProtobuf().hasCryptoGetInfo()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.getAccountInfo(query.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());
		logger.trace("End - getAccountInfo");
		return response;
	}

	/**
	 * Sends a query to a node to get a transaction receipt and returns the result of the request
	 * @param query the {@link HederaQuery} to send
	 * @return {@link Response} the result of the query
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public Response getTransactionReceipt(HederaQuery query) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - getTransactionReceipt query {}", query);

		Response response = null;
		if (query.getProtobuf().hasTransactionGetReceipt()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.getTransactionReceipts(query.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.trace("End - getTransactionReceipt");
		return response;
	}

	/**
	 * Sends a query to a node to get a transaction record and returns the result of the request
	 * @param query the {@link HederaQuery} to send
	 * @return {@link Response} the result of the query
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public Response getTransactionRecord(HederaQuery query) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - getTransactionRecord query {}", query);
		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;
		if (query.getProtobuf().hasTransactionGetRecord()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.getTxRecordByTxID(query.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());
		logger.trace("End - getTransactionRecord");
		return response;
	}

	/**
	 * Sends a query to a node to get file contents and returns the result of the request
	 * @param query the {@link HederaQuery} to send
	 * @return {@link Response} the result of the query
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public Response getFileContents(HederaQuery query) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - getFileContents query {}", query);
		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;
		if (query.getProtobuf().hasFileGetContents()) {
			openChannel();
			FileServiceGrpc.FileServiceBlockingStub blockingStub = FileServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.getFileContent(query.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());
		logger.trace("End - getFileContents");
		return response;
	}

	/**
	 * Sends a query to a node to get file info and returns the result of the request
	 * @param query the {@link HederaQuery} to send
	 * @return {@link Response} the result of the query
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public Response getFileInfo(HederaQuery query) throws InterruptedException, StatusRuntimeException {
		logger.trace("Start - fileGetInfo query {}", query);
		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;
		if (query.getProtobuf().hasFileGetInfo()) {
			openChannel();
			FileServiceGrpc.FileServiceBlockingStub blockingStub = FileServiceGrpc.newBlockingStub(this.grpcChannel);
			response = blockingStub.getFileInfo(query.getProtobuf());
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());
		logger.trace("End - getFileInfo");
		return response;
	}
	
	private void shutdown() throws InterruptedException {
		logger.trace("Start - shutdown");
		if (this.grpcChannel != null) {
			this.grpcChannel.shutdownNow();
			do {
				// wait for channel to terminate
			} while (!this.grpcChannel.isTerminated());
			this.grpcChannel = null;
		}
		logger.trace("End - shutdown");
	}
	
	private void openChannel() {
		if (this.grpcChannel == null) {
			if (!host.equals("") && (port != 0)) {
				// open a grpcChannel
				grpcChannel = ManagedChannelBuilder.forAddress(this.host, this.port).usePlaintext().build();
			}
		}
	}
}
