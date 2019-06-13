package com.hedera.hashgraph.sdk.examples.advanced;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;
import com.hedera.hashgraph.sdk.Transaction;

public final class TxGenerateSignSend {
	
	// this example demonstrates how to generate an unsigned transaction (by specifying a null client) into a byte array
	// then having this byte array signed independently before sending it onto Hedera for processing
	
    public static void main(String[] args) throws HederaException, HederaNetworkException, InvalidProtocolBufferException {
        // Generate a Ed25519 private, public key pair
        var newKey = Ed25519PrivateKey.generate();
        var newPublicKey = newKey.getPublicKey();

        System.out.println("private key = " + newKey);
        System.out.println("public key = " + newPublicKey);

        var client = ExampleHelper.createHederaClient();

        // create a transaction id
        var transactionId = new TransactionId(ExampleHelper.getOperatorId());
        
        // create a new transaction with a null client
        var txBytes = new AccountCreateTransaction(null)
        		.setKey(newPublicKey)
        		.setInitialBalance(10)
        		.setTransactionId(transactionId)
        		.setNodeAccountId(AccountId.fromString("0.0.3"))
        		.toBytes(false);
        
        // sign the transaction and return a byte array
        byte[] signedTX = Transaction.fromBytes(client, txBytes)
        .sign(ExampleHelper.getOperatorKey())
        .toBytes();
        
        // Execute the transaction
        // This will wait for the receipt to become available
        var receipt = Transaction.fromBytes(client, signedTX)
        .executeForReceipt();

        var newAccountId = receipt.getAccountId();

        System.out.println("account = " + newAccountId);
    }
}