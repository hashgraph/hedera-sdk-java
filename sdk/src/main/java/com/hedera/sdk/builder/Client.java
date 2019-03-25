package com.hedera.sdk.builder;

import com.google.protobuf.ByteString;
import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.common.HederaRealmID;
import com.hedera.sdk.common.HederaShardID;
import com.hedera.sdk.common.HederaSignature;
import com.hedera.sdk.common.HederaSignatureList;
import com.hedera.sdk.common.HederaSignatures;
import com.hedera.sdk.common.HederaTransactionID;
import com.hedera.sdk.cryptography.EDKeyPair;
import com.hedera.sdk.node.HederaNode;
import com.hedera.sdk.transaction.HederaTransaction;
import com.hedera.sdk.transaction.HederaTransactionBody;
import com.hedera.sdk.transaction.HederaTransactionBody.TransactionType;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hederahashgraph.api.proto.java.CryptoCreateTransactionBody;
import com.hederahashgraph.api.proto.java.Duration;
import com.hederahashgraph.api.proto.java.Key;
import com.hederahashgraph.api.proto.java.TransactionBody;

import io.grpc.StatusRuntimeException;

public class Client {
	private Client() {
	}
	
    public static class CreateAccount {
    	private String publicKey;
    	private long initialBalance = 0;
    	private HederaAccountID operatorAccountID;
    	private HederaAccountID nodeAccountID;
    	private HederaAccountID proxyAccountID;
    	private String memo = "";
    	private EDKeyPair privateKey;
    	private boolean receiverSigRequired = false;
    	private boolean generateRecord = false;
    	private long receiveRecordThreshold = Long.MAX_VALUE;
    	private long sendRecordThreshold = Long.MAX_VALUE;
    	private long realmNum = 0;
    	private long shardNum = 0;
    	private EDKeyPair newRealmAdminKey;
		private HederaSignatureList sigsForTransaction = new HederaSignatureList();
		private HederaNode node;

		// Account properties
        public CreateAccount key(String publicKey) {
            this.publicKey = publicKey;
            return this;  
        }
        public CreateAccount newRealmAdminKey(EDKeyPair newRealmAdminKey) {
            this.newRealmAdminKey = newRealmAdminKey;
            return this;  
        }
        public CreateAccount initialBalance(long initialBalance) {
            this.initialBalance = initialBalance;
            return this;  
        }
        public CreateAccount proxyAccountID(HederaAccountID proxyAccountID) {
            this.proxyAccountID = proxyAccountID;
            return this;  
        }
        public CreateAccount receiverSigRequired(boolean receiverSigRequired) {
            this.receiverSigRequired = receiverSigRequired;
            return this;  
        }
        public CreateAccount receiveRecordThreshold(long receiveRecordThreshold) {
            this.receiveRecordThreshold = receiveRecordThreshold;
            return this;  
        }
        public CreateAccount sendRecordThreshold(long sendRecordThreshold) {
            this.sendRecordThreshold = sendRecordThreshold;
            return this;  
        }
        public CreateAccount realm(long realmNum) {
            this.realmNum = realmNum;
            return this;  
        }
        public CreateAccount shard(long shardNum) {
            this.shardNum = shardNum;
            return this;  
        }
		// Common methods
        public CreateAccount sign(EDKeyPair privateKey) {
            this.privateKey = privateKey;
            return this;  
        }
    	public CreateAccount dial(String url) {
			if (!url.contains(":")) {
				throw new IllegalArgumentException("url must contain a colon between host and port (e.g. host:port)");
			}
			String[] connection = url.split(":");

			int port;
			try {
				port = Integer.parseInt(connection[1]);
			}
			catch (NumberFormatException e)
			{
				throw new IllegalArgumentException("url port must be numeric");
			}		
			this.node = new HederaNode(connection[0], port);
			return this;
    	}
    	// randomly picks a node
    	public CreateAccount dial() {
			this.node = new HederaNode("testnet.hedera.com", 50003);
			return this;
    	}
        public CreateAccount operator(HederaAccountID operatorAccountID) {
            this.operatorAccountID = operatorAccountID;
            return this;  
        }
        public CreateAccount node(HederaAccountID nodeAccountID) {
            this.nodeAccountID = nodeAccountID;
            return this;  
        }
        public CreateAccount generateRecord(boolean generateRecord) {
            this.generateRecord = generateRecord;
            return this;  
        }
        public CreateAccount memo(String memo) {
        	if (memo.length() > 100) {
    			throw new IllegalArgumentException("memo must be 100 bytes maximum");
        	}
            this.memo = memo;
            return this;  
        }
        
        public HederaTransactionResult execute() throws StatusRuntimeException, InterruptedException {
        	// validate required inputs
        	
        	// construct transaction body
    	   	CryptoCreateTransactionBody.Builder transactionData = CryptoCreateTransactionBody.newBuilder();
    	   	transactionData.setAutoRenewPeriod(Duration.newBuilder().setSeconds(10));
    	   	transactionData.setInitialBalance(this.initialBalance);
    	   	transactionData.setReceiverSigRequired(this.receiverSigRequired);
       		transactionData.setReceiveRecordThreshold(this.receiveRecordThreshold);
       		transactionData.setSendRecordThreshold(this.sendRecordThreshold);
	   		transactionData.setKey(Key.newBuilder().setEd25519(ByteString.copyFrom(this.publicKey.getBytes())));
    	   	
    	   	if (this.realmNum > 0) {
    	   		transactionData.setRealmID(new HederaRealmID(this.shardNum, this.realmNum).getProtobuf());
    	   	} else if (this.realmNum == -1) {
    		   	if (this.newRealmAdminKey != null) {
    		   		transactionData.setNewRealmAdminKey(Key.newBuilder().setEd25519(ByteString.copyFrom(this.newRealmAdminKey.getPublicKey())));
    		   	}
    	   	}
    	   	if (this.proxyAccountID != null) {
    	   		transactionData.setProxyAccountID(this.proxyAccountID.getProtobuf());
    	   	}
    	   	if (this.shardNum > 0) {
    	   		transactionData.setShardID(new HederaShardID(this.shardNum).getProtobuf());
    	   	}

    	   	// now the transaction itself
    	   	HederaTransactionBody hederaTransactionBody = new HederaTransactionBody(
    	   			TransactionType.CRYPTOCREATEACCOUNT
    	   			, new HederaTransactionID(this.operatorAccountID)
    	   			, this.nodeAccountID
    	   			, 100000
    	   			, new HederaDuration(120)
    	   			, this.generateRecord
    	   			, this.memo
    	   			, transactionData.build());

    		//paying signature
    		HederaSignatures sigsForTransaction = new HederaSignatures();
    		sigsForTransaction.addSignature(this.publicKey, this.privateKey.signMessage(hederaTransactionBody.getProtobuf().toByteArray()));
    		// new realm admin if necessary
    		if (this.newRealmAdminKey != null) {
        		sigsForTransaction.addSignature(this.newRealmAdminKey.getPublicKeyEncodedHex(), this.newRealmAdminKey.signMessage(hederaTransactionBody.getProtobuf().toByteArray()));
    		}

         	HederaTransaction transaction = new HederaTransaction(hederaTransactionBody, sigsForTransaction);
        	
        	return this.node.accountCreate(transaction);
        	
        }
    }
}
