package com.hedera.sdk.node;

import java.io.Serializable;
import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.query.HederaQuery;
import com.hedera.sdk.transaction.HederaTransaction;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hederahashgraph.api.proto.java.AccountID;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
import com.hederahashgraph.api.proto.java.TransactionResponse;
import com.hederahashgraph.service.proto.java.CryptoServiceGrpc;
import com.hederahashgraph.service.proto.java.FileServiceGrpc;
import com.hederahashgraph.service.proto.java.SmartContractServiceGrpc;
import org.slf4j.LoggerFactory;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class HederaNode implements Serializable {
	/**
	 * Class to manage information associated with a Hedera Node 
	 */
	private static final long serialVersionUID = 1L;
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaNode.class);
	private String host = "";
    private int port = 0;
    private HederaAccountID accountID = null;
	// GRPC
	private ManagedChannel grpcChannel = null; 
    // BUSY network handling
	private int busyRetryCount = 2;
	private int waitMillisLong = 510;
	private int waitMillisShort = 11;
	
	/**
	 * The default fee associated with running an account create transaction
	 */
	public long accountCreateTransactionFee = 100000;
	/**
	 * The default fee associated with running a token transaction
	 */
	public long accountTransferTransactionFee = 100000;
	/**
	 * The default fee associated with running an update transaction
	 */
	public long accountUpdateTransactionFee = 100000;
	/**
	 * The default fee associated with deleting an account
	 */
	public long accountDeleteTransactionFee = 100000;
	/**
	 * The default fee associated with adding a claim to an account
	 */
	public long accountAddClaimTransactionFee = 100000;
	/**
	 * The default fee associated with deleting a claim from an account
	 */
	public long accountDeleteClaimTransactionFee = 100000;
	/**
	 * The default fee associated with querying an account balance
	 */
	public long accountBalanceQueryFee = 100000;
	/**
	 * The default fee associated with an account info query
	 */
	public long accountInfoQueryFee = 100000;
	/**
	 * The default fee associated with an get records query against an account
	 */
	public long accountGetRecordsQueryFee = 100000;
	
	/**
	 * The default fee associated with creating a file
	 */
	public long fileCreateTransactionFee = 100000;
	/**
	 * The default fee associated with deleting a file
	 */
	public long fileDeleteTransactionFee = 100000;
	/**
	 * The default fee associated with updating a file
	 */
	public long fileUpdateTransactionFee = 100000;
	/**
	 * The default fee associated with appending to a file
	 */
	public long fileAppendTransactionFee = 100000;
	/**
	 * The default fee associated with getting file contents
	 */
	public long fileGetContentsQueryFee = 100000;
	/**
	 * The default fee associated with getting file information
	 */
	public long fileGetInfoQueryFee = 100000;
	/**
	 * The default fee associated with a get records query against a file
	 */
	public long fileGetRecordsQueryFee = 100000;
	
	/**
	 * The default fee associated with creating a smart contract
	 */
	public long contractCreateTransactionFee = 100000;
	/**
	 * The default fee associated with updating a smart contract
	 */
	public long contractUpdateTransactionFee = 100000;
	/**
	 * The default fee associated with getting a smart contract's byte code
	 */
	public long contractGetByteCodeQueryFee = 100000;
	/**
	 * The default fee associated with calling a smart contract function
	 */
	public long contractCallTransactionFee = 100000;
	/**
	 * The default fee associated with getting smart contract information
	 */
	public long contractGetInfoQueryFee = 100000;
	/**
	 * The default fee associated with running a local call function
	 */
	public long contractCallLocalQueryFee = 100000;
	/**
	 * The default fee associated with querying by solidityID
	 */
	public long contractGetBySolidityId = 100000;
	/**
	 * The default fee associated with a get records query against a smart contract
	 */
	public long contractGetRecordsQueryFee = 100000;
	/**
	 * The default fee associated with a get records query against a transaction
	 */
	public long transactionGetRecordsQueryFee = 100000;

	/**
	 * Default Constructor
	 */
	public HederaNode() {


	}
	/**
	 * Constructor with host and port
	 * @param host the host to communicate with
	 * @param port the port to use for communication
	 */
	public HederaNode(String host, int port) {

		this.host = host;
		this.port = port;
		openChannel();

	}
	/**
	 * Constructor with host, port and accountID
	 * @param host the host to communicate with
	 * @param port the port to use for communication
	 * @param accountID the node's {@link AccountID}
	 */
	public HederaNode(String host, int port, HederaAccountID accountID) {

		this.host = host;
		this.port = port;
		this.accountID = accountID;
		openChannel();

	}
	/**
	 * Sets the host and port of the specified node
	 * @param host {@link String}
	 * @param port {@link Integer}
	 * @throws InterruptedException in the event of a node communication error 
	 */
	public void setHostPort(String host, int port) throws InterruptedException {

		if ((!host.equals(this.host)) || (port != this.port)) {
			// close the current connection if open
			shutdown();
			this.host = host;
			this.port = port;
			openChannel();
		}

    }
    
    /**
     * Gets the host for this node
	 * @return {@link String}
     */
    public String getHost() {


        return this.host;
    }

    /**
     * Gets the port used to access this node
	 * @return {@link Integer}
     */
    public int getPort() {


        return this.port;
    }

    /**
     * Sets the host for this node
	 * @param host {@link String}
	 * @throws InterruptedException in the event of a node communication error 
     */
    public void setHost(String host) throws InterruptedException {

		if (!host.equals(this.host)) {
			// close the current connection if open
			shutdown();
			this.host = host;
			openChannel();
		}

    }

    /**
     * Sets the port used to access this node
	 * @param port {@link Integer}
	 * @throws InterruptedException in the event of a node communication error 
     */
    public void setPort(int port) throws InterruptedException {

		this.port = port;
		if (port != this.port) {
			// close the current connection if open
			shutdown();
			this.port = port;
			openChannel();
		}

    }

    /**
     * Sets the account ID for this node based upon an existing HederaAccountID object
	 * @param accountID {@link HederaAccountID}
	 */
	public void setAccountID(HederaAccountID accountID) {

		this.accountID = accountID;

    }
    
    /**
     * Sets the account ID for this node based upon shardNum, realmNum and accountNum
	 * @param shardNum {@link Long}
	 * @param realmNum {@link Long}
	 * @param accountNum {@link Long}
	 */
	public void setAccountID(long shardNum, long realmNum, long accountNum) {

		HederaAccountID accountID = new HederaAccountID(shardNum, realmNum, accountNum);
		this.accountID = accountID;

    }
    
    /**
     * Gets the account ID for this node
	 * @return {@link HederaAccountID}
	 */
	public HederaAccountID getAccountID() {



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

		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasCryptoCreateAccount()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.createAccount(transaction.getProtobuf());
				// retry if busy
				if (response.getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}

		if (transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		}

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

		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasCryptoAddClaim()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.addClaim(transaction.getProtobuf());
				// retry if busy
				if (response.getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}
		if(transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		}

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

		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasCryptoTransfer()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.cryptoTransfer(transaction.getProtobuf());
				// retry if busy
				if (response.getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					if (transaction.getProtobuf().getBody().getGenerateRecord() == true) {
						// wait longer
						logger.info("System busy - sleeping for " + waitMillisLong + "ms");
						Thread.sleep(waitMillisLong);
					} else {
						// wait short time
						logger.info("System busy - sleeping for " + waitMillisShort + "ms");
						Thread.sleep(waitMillisShort);
					}
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}
		if(transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		}

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

		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasCryptoUpdateAccount()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.updateAccount(transaction.getProtobuf());
				// retry if busy
				if (response.getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}
		if(transResult != null && response != null) {		
		    transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		}

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

		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasFileAppend()) {
			openChannel();
			FileServiceGrpc.FileServiceBlockingStub blockingStub = FileServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.appendContent(transaction.getProtobuf());
				// retry if busy
				if (response.getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}
		if(transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		}

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

		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasFileCreate()) {
			openChannel();
			FileServiceGrpc.FileServiceBlockingStub blockingStub = FileServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.createFile(transaction.getProtobuf());
				// retry if busy
				if (response.getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}

		if(transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		}

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

		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasFileDelete()) {
			openChannel();
			FileServiceGrpc.FileServiceBlockingStub blockingStub = FileServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.deleteFile(transaction.getProtobuf());
				// retry if busy
				if (response.getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}

		if(transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		}

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

		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasFileUpdate()) {
			openChannel();
			FileServiceGrpc.FileServiceBlockingStub blockingStub = FileServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.updateFile(transaction.getProtobuf());
				// retry if busy
				if (response.getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}

		if(transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		}

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

		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasContractCall()) {
			openChannel();
			SmartContractServiceGrpc.SmartContractServiceBlockingStub blockingStub = SmartContractServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.contractCallMethod(transaction.getProtobuf());
				// retry if busy
				if (response.getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}

		if(transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		}

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

		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasContractCreateInstance()) {
			openChannel();
			SmartContractServiceGrpc.SmartContractServiceBlockingStub blockingStub = SmartContractServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.createContract(transaction.getProtobuf());
				// retry if busy
				if (response.getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}

		if(transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		}

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

		logger.info("SENDING TRANSACTION");
		logger.info(transaction.getProtobuf().toString());

		TransactionResponse response = null;
		HederaTransactionResult transResult = new HederaTransactionResult();
		
		if (transaction.getProtobuf().getBody().hasContractUpdateInstance()) {
			openChannel();
			SmartContractServiceGrpc.SmartContractServiceBlockingStub blockingStub = SmartContractServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.updateContract(transaction.getProtobuf());
				// retry if busy
				if (response.getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid transaction type.");
		}

		if(transResult != null && response != null) {		
			transResult.setPrecheckResult(response.getNodeTransactionPrecheckCode());
		}

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

		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;

		if (query.getProtobuf().hasContractCallLocal()) {
			openChannel();
			SmartContractServiceGrpc.SmartContractServiceBlockingStub blockingStub = SmartContractServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.contractCallLocalMethod(query.getProtobuf());
				// retry if busy
				if (response.getContractCallLocal().getHeader().getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());

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

		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;

		if (query.getProtobuf().hasContractGetBytecode()) {
			openChannel();
			SmartContractServiceGrpc.SmartContractServiceBlockingStub blockingStub = SmartContractServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.contractGetBytecode(query.getProtobuf());
				// retry if busy
				if (response.getContractGetBytecodeResponse().getHeader().getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());

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

		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;
		if (query.getProtobuf().hasGetBySolidityID()) {
			openChannel();
			SmartContractServiceGrpc.SmartContractServiceBlockingStub blockingStub = SmartContractServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.getBySolidityID(query.getProtobuf());
				// retry if busy
				if (response.getGetBySolidityID().getHeader().getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());

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

		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;

		if (query.getProtobuf().hasContractGetInfo()) {
			openChannel();
			SmartContractServiceGrpc.SmartContractServiceBlockingStub blockingStub = SmartContractServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.getContractInfo(query.getProtobuf());
				// retry if busy
				if (response.getContractGetInfo().getHeader().getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());

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

		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;

		if (query.getProtobuf().hasCryptogetAccountBalance()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.cryptoGetBalance(query.getProtobuf());
				// retry if busy
				if (response.getCryptogetAccountBalance().getHeader().getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());

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

		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;

		if (query.getProtobuf().hasCryptoGetAccountRecords()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.getAccountRecords(query.getProtobuf());
				// retry if busy
				if (response.getCryptoGetAccountRecords().getHeader().getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());

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

		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;

		if (query.getProtobuf().hasCryptoGetInfo()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.getAccountInfo(query.getProtobuf());
				// retry if busy
				if (response.getCryptoGetInfo().getHeader().getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());

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


		Response response = null;
		if (query.getProtobuf().hasTransactionGetReceipt()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.getTransactionReceipts(query.getProtobuf());
				// retry if busy
				if (response.getTransactionGetReceipt().getHeader().getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}


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

		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;
		if (query.getProtobuf().hasTransactionGetRecord()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.getTxRecordByTxID(query.getProtobuf());
				// retry if busy
				if (response.getTransactionGetRecord().getHeader().getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());

		return response;
	}

	/**
	 * Sends a query to a node to get a fast transaction record and returns the result of the request
	 * @param query the {@link HederaQuery} to send
	 * @return {@link Response} the result of the query
	 * @throws InterruptedException in the event of a node communication failure
	 * @throws StatusRuntimeException in the event of a node communication failure
	 */
	public Response getTransactionFastRecord(HederaQuery query) throws InterruptedException, StatusRuntimeException {

		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;
		if (query.getProtobuf().hasTransactionGetFastRecord()) {
			openChannel();
			CryptoServiceGrpc.CryptoServiceBlockingStub blockingStub = CryptoServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.getFastTransactionRecord(query.getProtobuf());
				// retry if busy
				if (response.getTransactionGetFastRecord().getHeader().getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());

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

		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;
		if (query.getProtobuf().hasFileGetContents()) {
			openChannel();
			FileServiceGrpc.FileServiceBlockingStub blockingStub = FileServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.getFileContent(query.getProtobuf());
				// retry if busy
				if (response.getFileGetContents().getHeader().getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());

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

		logger.info("RUNNING QUERY TO NODE");
		logger.info(query.getProtobuf().toString());

		Response response = null;
		if (query.getProtobuf().hasFileGetInfo()) {
			openChannel();
			FileServiceGrpc.FileServiceBlockingStub blockingStub = FileServiceGrpc.newBlockingStub(this.grpcChannel);
			for (int i=0; i < busyRetryCount; i++) {
				response = blockingStub.getFileInfo(query.getProtobuf());
				// retry if busy
				if (response.getFileGetInfo().getHeader().getNodeTransactionPrecheckCode() == ResponseCodeEnum.BUSY) {
					logger.info("System busy - sleeping for " + waitMillisLong + "ms");
					Thread.sleep(waitMillisLong);
				} else {
					break;
				}
			}
		} else {
			throw new IllegalStateException("Invalid Query Type");
		}

		logger.info("--->QUERY RESPONSE");
		logger.info(response.toString());

		return response;
	}
	
	private void shutdown() throws InterruptedException {

		if (this.grpcChannel != null) {
			this.grpcChannel.shutdownNow();
			do {
				// wait for channel to terminate
			} while (!this.grpcChannel.isTerminated());
			this.grpcChannel = null;
		}

	}
	
	private void openChannel() {
		if (this.grpcChannel == null) {
			if (!host.equals("") && (port != 0)) {
				// open a grpcChannel
				grpcChannel = ManagedChannelBuilder.forAddress(this.host, this.port).usePlaintext().build();
			} else {
				throw new IllegalStateException("Invalid Node IP or Port");
			}
		}
	}
}
