package com.hedera.examples.clientserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import org.apache.commons.codec.DecoderException;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.account.HederaAccountAmount;
import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaSignatures;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.common.HederaTransactionID;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.node.HederaNodeList;
import com.hedera.sdk.transaction.HederaTransaction;
import com.hedera.sdk.transaction.HederaTransactionBody;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hedera.sdk.transaction.HederaTransactionBody.TransactionType;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionBody;

public class ReceiveSignSend {

	public static void main(String... arguments) throws Exception {

	    Socket socket;
		
    	Thread t = new Thread(new Runnable(){
            @Override
            public void run() {
        		ServerSocket serverSocket;
        	    Socket socket;
                
				try {
					serverSocket = new ServerSocket(8080);
					System.out.println("SERVER started on port " + serverSocket.getLocalPort());
					
	                while(true){
						System.out.println("SERVER Accepting");
    		    		socket = serverSocket.accept();
    		    		
    		    		InputStream in = socket.getInputStream();
    		    		// message is a transaction protobuf message
    		    		Transaction transaction = Transaction.parseFrom(in);
    		    		// which type is it
    		    		// get the transaction body
    		    		TransactionBody body;
    		    		if (transaction.hasBody()) {
    		    			body = transaction.getBody();
    		    		} else {
    		    			body = TransactionBody.parseFrom(transaction.getBodyBytes());
    		    		}
    		    		
    		    		if (body.hasCryptoTransfer()) {
    		    			System.out.println("Got Crypto Transfer");
    		    			sendCrypto(transaction);
    		    		} else {
    		    			System.out.println("Got unsupported transaction body");
    		    		}
    		    		
    			        socket.close();
    			    }
	                
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        });
    	
        t.start();

        try {
        	// let's construct a transaction and send to the server
        	// the server will sign and send to hedera
        	
    		// txQueryDefaults to get paying account details
    		HederaTransactionAndQueryDefaults txQueryDefaults = new HederaTransactionAndQueryDefaults();
    		txQueryDefaults = ExampleUtilities.getTxQueryDefaults();
    		/**
    		 * setup inputs
    		 */
    		HederaAccount payingAccount = new HederaAccount();
    		payingAccount.accountNum = txQueryDefaults.payingAccountID.accountNum;
    		
    		// paying the node account id, but this could be any other account
    		HederaAccountID receivingAccount = HederaNodeList.randomNode().getAccountID();

    		// we'll need a node account ID, get this from txQueryDefaults
    		HederaAccountID nodeAccount = HederaNodeList.randomNode().getAccountID();

    		// generate the transaction body
    		HederaTransaction txBody = cryptoTransfer(payingAccount, receivingAccount, nodeAccount);
    		
            socket = new Socket(InetAddress.getLocalHost(),8080);  
            OutputStream output = socket.getOutputStream();
            System.out.println("Sending");
            txBody.getProtobuf().writeTo(output);
            output.flush();
            socket.close();
    		
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("Ending");
    }
	
	private static HederaTransaction cryptoTransfer(HederaAccount payingAccount, HederaAccountID toAccount, HederaAccountID nodeAccount) throws Exception {
		
		ArrayList<HederaAccountAmount>accountAmounts = new ArrayList<HederaAccountAmount>(); 
		HederaAccountAmount accountAmountFrom = new HederaAccountAmount(payingAccount.getHederaAccountID(), -10);
		HederaAccountAmount accountAmountTo = new HederaAccountAmount(toAccount, 10);
		
		accountAmounts.add(accountAmountFrom);
		accountAmounts.add(accountAmountTo);
		
		// generate a hedera Transaction ID
		HederaTransactionID transactionID = new HederaTransactionID(payingAccount.getHederaAccountID());

		HederaTransactionBody transactionBody = new HederaTransactionBody(
				TransactionType.CRYPTOTRANSFER
				, transactionID
				, nodeAccount
				, 100_000 // transaction fee
				, new HederaDuration(120) // transaction valid duration 2 minutes
				, false // generate record
				, "test"
				, payingAccount.getTransferTransactionBody(accountAmounts));

		HederaTransaction transaction = new HederaTransaction();
		transaction.body = transactionBody;
		// note we could sign the transaction here, the server will add its signature
		
		// return the transaction
		return transaction;
	}
	private static void sendCrypto(Transaction transaction) throws InterruptedException, InvalidKeySpecException, DecoderException, InvalidProtocolBufferException {
		// now sign the message and send onto Hedera
		// get account details 
		HederaTransactionAndQueryDefaults txQueryDefaults = new HederaTransactionAndQueryDefaults();
		txQueryDefaults = ExampleUtilities.getTxQueryDefaults();
		
		// get the existing signatures if any
		// create a signature map
		HederaSignatures sigsForTransaction = new HederaSignatures();

		// get transaction id
		HederaTransactionID transactionID;
		if (transaction.hasBody()) {
			transactionID = new HederaTransactionID(transaction.getBody().getTransactionID());
		} else {
			TransactionBody body = TransactionBody.parseFrom(transaction.getBodyBytes());
			transactionID = new HederaTransactionID(body.getTransactionID());
		}
		//paying signature
		if (transaction.hasBody()) {
			sigsForTransaction.addSignature(txQueryDefaults.payingKeyPair.getPublicKey()
					,txQueryDefaults.payingKeyPair.signMessage(transaction.getBody().toByteArray()));
		} else {
			sigsForTransaction.addSignature(txQueryDefaults.payingKeyPair.getPublicKey()
					,txQueryDefaults.payingKeyPair.signMessage(transaction.getBodyBytes().toByteArray()));
		}

		// refresh the transaction signatures
		transaction = sigsForTransaction.transactionAddSignatures(transaction);
		
		HederaTransactionResult hederaTransactionResult = HederaNodeList.randomNode().sendCryptoTransaction(transaction);

		hederaTransactionResult.hederaTransactionID = transactionID;

		if (hederaTransactionResult.getPrecheckResult() == ResponseCodeEnum.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt = Utilities.getReceipt(transactionID
					,HederaNodeList.randomNode());
			
			// was that successful ?
			if (receipt.transactionStatus == ResponseCodeEnum.SUCCESS) {
				// and print it out
				ExampleUtilities.showResult(String.format("**    Crypto transfer call success"));
			} else {
				ExampleUtilities.showResult("**    Failed with transactionStatus:" + receipt.transactionStatus.toString());
				System.exit(0);
			}
		} else if (hederaTransactionResult.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			ExampleUtilities.showResult("system busy, try again later");
			System.exit(0);
		} else {
			ExampleUtilities.showResult("**    Failed with getPrecheckResult:" + hederaTransactionResult.getPrecheckResult());
			System.exit(0);
		}
	}
}
