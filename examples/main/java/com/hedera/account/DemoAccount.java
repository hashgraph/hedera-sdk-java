package com.hedera.account;

import java.time.Instant;

import org.slf4j.LoggerFactory;

import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.account.HederaAccountUpdateValues;
import com.hedera.sdk.account.HederaClaim;
import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.common.HederaTransactionID;
import com.hedera.sdk.common.HederaTransactionRecord;
import com.hedera.utilities.ExampleUtilities;

import ch.qos.logback.classic.Logger;

public final class DemoAccount {
	
	public static void main (String... arguments) throws Exception {
	   final org.slf4j.Logger logger = LoggerFactory.getLogger(DemoAccount.class);		
		//DO NOT CHANGE THESE, CHANGE BELOW INSTEAD
		boolean create = false; //OK
    	boolean getBalance = false; //OK
    	boolean send = false; //NOK
    	boolean getInfo = false; //OK
    	boolean update = false; //OK
    	boolean doAddClaim = false;//OK
		boolean getTXRecord = false;

//    	create = true;
    	getBalance = true;
//    	send = true;
//    	getInfo = true;
//    	update = true;
//    	doAddClaim = true; //-- not implemented ?
//    	getTXRecord = true; //-- records not supported
		
    	/* 
    	 * check my balance
    	 * This populates the account object's balance property 
    	 */
    	if (getBalance) {
    		// setup my account
        	HederaAccount myAccount = new HederaAccount();
        	// setup transaction/query defaults (durations, etc...)
        	myAccount.txQueryDefaults = ExampleUtilities.getTxQueryDefaults();
	    	// setup my account number from properties file
	    	myAccount.accountNum = myAccount.txQueryDefaults.payingAccountID.accountNum;
    	
    		AccountGetBalance.getBalance(myAccount);
    		System.out.println(String.format("My balance=%d tinybar", myAccount.balance()));
    		System.exit(0);
    	}
    	
    	/*
    	 * get my account info
    	 * This populates the account object's properties from network information
    	 */
    	if (getInfo) {
    		// setup my account
        	HederaAccount myAccount = new HederaAccount();
        	// setup transaction/query defaults (durations, etc...)
        	myAccount.txQueryDefaults = ExampleUtilities.getTxQueryDefaults();
	    	// setup my account number from properties file
	    	myAccount.accountNum = myAccount.txQueryDefaults.payingAccountID.accountNum;
    		AccountGetInfo.getInfo(myAccount);
    	}

    	/* 
    	 * create a new account
    	 */
    	if (create) {
    		// setup new account object
        	HederaAccount newAccount = new HederaAccount();
        	// setup transaction/query defaults (durations, etc...)
        	// note: This txQueryDefaults contains a payingAccountID which is the account paying for the transaction
        	newAccount.txQueryDefaults = ExampleUtilities.getTxQueryDefaults();

        	// optionally generate a record for this transaction
        	newAccount.txQueryDefaults.generateRecord = getTXRecord;
        	// create a new keypair
    		HederaKeyPair newAccountKey = new HederaKeyPair(KeyType.ED25519);
    		System.out.println(String.format("New account public key %s=",newAccountKey.getPublicKeyHex()));
    		System.out.println(String.format("New account private/secret key %s=",newAccountKey.getSecretKeyHex()));
    		// create a new account with a balance of 10000000 tinybar, using the above generated public key
    		newAccount = AccountCreate.create(newAccount, newAccountKey.getPublicKeyHex(), newAccountKey.getKeyType(), 10000000);

	    	if (newAccount == null) {
    			logger.info("*******************************************");
    			logger.info("ACCOUNT CREATE FAILED");
    			logger.info("*******************************************");
    			throw new Exception("Account create failure");
	    	} else {
	    		// the helper function (AccountCreate) populated the newAccount object's accountNum from the transaction receipt it 
	    		// obtained. 
	    		// Note: The HederaAccount.create only sends the transaction to the network, obtaining a receipt/record to recover the
	    		// new account number is a separate (and asynchronous) operation
	    		// the newly created account had its

    			// get info for the newly created account
	    		if (getInfo) {
	    			AccountGetInfo.getInfo(newAccount);
	    		}
	    		
    			// optionally retrieve a record for the transaction
	    		if (getTXRecord) {
	    			  HederaTransactionID txID = newAccount.hederaTransactionID;
	    			  HederaTransactionRecord txRecord = new HederaTransactionRecord(txID, newAccount.txQueryDefaults.node.transactionGetRecordsQueryFee, newAccount.txQueryDefaults);
	    			  // stop getting records unnecessarily
	    			  getTXRecord = false;
    			}
	    		
		    	if (send) {
					// send some crypto from my account to the new account
		    		// setup my account
		        	HederaAccount myAccount = new HederaAccount();
		        	// setup transaction/query defaults (durations, etc...)
		        	myAccount.txQueryDefaults = ExampleUtilities.getTxQueryDefaults();
			    	// setup my account number from properties file
			    	myAccount.accountNum = myAccount.txQueryDefaults.payingAccountID.accountNum;

			    	logger.info("Start Time" + Instant.now());
		    		AccountSend.send(myAccount, newAccount, 100);
		    		logger.info("End Time" + Instant.now());
					// get balance for my account
			    	if (getBalance) {
			    		AccountGetBalance.getBalance(myAccount);
			    		System.out.println(String.format("My balance=%d tinybar", myAccount.balance()));
			    	}
					// get balance for the new account
			    	if (getBalance) {
			    		// let's make the account pay for it's own transactions and queries
			    		newAccount.txQueryDefaults.payingAccountID = newAccount.getHederaAccountID();
			    		newAccount.txQueryDefaults.payingKeyPair = newAccountKey;
			    		AccountGetBalance.getBalance(newAccount);
			    		System.out.println(String.format("New account balance=%d tinybar", newAccount.balance()));
			    	}
		    	}
		    	
		    	if (update) {
		    		// update the new account with some changes properties
		    		// note: the original keypair for the newAccount object needs to be reset
		    		// get information only recovers the public key, and the private/secret key
		    		// is necessary here to sign for and therefore approve the change
		    		newAccount.accountKey = newAccountKey;
		    		
		    		// setup an object to contain values to update
		    		HederaAccountUpdateValues updates = new HederaAccountUpdateValues();
	    		
		    		// create a new key
		    		HederaKeyPair ed25519Key = new HederaKeyPair(KeyType.ED25519);
	    	    			
		    	    // set the new key for the account
		    		updates.newKey = ed25519Key;
		    		// new proxy account details
		    		updates.proxyAccountShardNum = 0;
		    		updates.proxyAccountRealmNum = 0;
		    		updates.proxyAccountAccountNum = 1;
		    		// new proxy fraction
		    		updates.proxyFraction = 2;
		    		// new threshold for sending
		    		updates.sendRecordThreshold = 4000;
		    		// new threshold for receiving
		    		updates.receiveRecordThreshold = 3000;
		    		// new auto renew period
		    		updates.autoRenewPeriodSeconds = 10;
		    		updates.autoRenewPeriosNanos = 20;
		    		// new expiration time
		    		updates.expirationTimeSeconds = 200;
		    		updates.expirationTimeNanos = 100;
	    		
		    		newAccount = AccountUpdate.update(newAccount, updates);
		    		if (newAccount != null) {
		    			// need to update the paying key pair to be the new account's key
		    			newAccount.txQueryDefaults.payingKeyPair = ed25519Key;
		    			AccountGetInfo.getInfo(newAccount);
		    		} else {
		    			logger.info("*******************************************");
		    			logger.info("ACCOUNT UPDATE FAILED - account is now null");
		    			logger.info("*******************************************");
		    		}
		    	}
		    	if (doAddClaim) {
		    		HederaKeyPair claimKeyPair = new HederaKeyPair(KeyType.ED25519);
		    		HederaKeyPair claimKey = new HederaKeyPair(claimKeyPair.getKeyType(), claimKeyPair.getPublicKey());
		
					// Create a new claim object
					HederaClaim claim;
					claim = new HederaClaim(newAccount.shardNum, newAccount.realmNum, newAccount.accountNum, "ClaimHash".getBytes("UTF-8"));
					// add a key to the claim
					claim.addKey(claimKey);
			        // add a claim
			        if (AccountAddClaim.addClaim(newAccount,claim, claimKeyPair)) {
			        	
			        }
		        }
	    	}
    	}
	}
}