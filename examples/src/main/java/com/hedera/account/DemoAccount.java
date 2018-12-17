package com.hedera.account;

import java.io.UnsupportedEncodingException;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.DefaultMessageCodesResolver.Format;

import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.account.HederaAccountUpdateValues;
import com.hedera.sdk.account.HederaClaim;
import com.hedera.sdk.common.HederaKey;
import com.hedera.sdk.common.HederaKey.KeyType;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.common.HederaTransactionID;
import com.hedera.sdk.common.HederaTransactionRecord;
import com.hedera.sdk.cryptography.HederaCryptoKeyPair;
import com.hedera.utilities.ExampleUtilities;

public final class DemoAccount {
	
	public static void main (String... arguments) throws Exception {
		final Logger logger = LoggerFactory.getLogger(DemoAccount.class);
		
		//DO NOT CHANGE THESE, CHANGE BELOW INSTEAD
		boolean create = false; //OK
    	boolean getBalance = false; //OK
    	boolean send = false; //NOK
    	boolean getInfo = false; //OK
    	boolean update = false; //OK
    	boolean doAddClaim = false;//OK
		boolean getTXRecord = false;
<<<<<<< HEAD:examples/src/main/java/com/hedera/account/DemoAccount.java
		
		// setup a set of defaults for query and transactions
		HederaTransactionAndQueryDefaults txQueryDefaults = new HederaTransactionAndQueryDefaults();
		txQueryDefaults = ExampleUtilities.getTxQueryDefaults();
		
    	// new account objects
    	HederaAccount account = new HederaAccount();
    	HederaAccount accountXferTo = new HederaAccount();
    	
    	// setup transaction/query defaults (durations, etc...)
    	account.txQueryDefaults = txQueryDefaults;
    	accountXferTo.txQueryDefaults = txQueryDefaults;
    	
    	create = true;
//    	balance = true;
    	send = true;
//    	info = true;
//    	update = true;
//    	doAddClaim = true; -- not implemented ?
//    	getTXRecord = true;
=======

    	create = true;
    	getBalance = true;
    	send = true;
    	getInfo = true;
    	update = true;
    	doAddClaim = false; //-- not implemented ?
    	getTXRecord = true;
		
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
>>>>>>> f76e9c4... Unit tests pass:examples/main/java/com/hedera/account/DemoAccount.java
    	
    		AccountGetBalance.getBalance(myAccount);
    		System.out.println(String.format("My balance=%d tinybar", myAccount.balance()));
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
<<<<<<< HEAD:examples/src/main/java/com/hedera/account/DemoAccount.java
    		account.txQueryDefaults.generateRecord = getTXRecord;
	    	HederaCryptoKeyPair newAccountKey = new HederaCryptoKeyPair(KeyType.ED25519);
	    	HederaCryptoKeyPair accountXferToKey = new HederaCryptoKeyPair(KeyType.ED25519);
	    	
	    	account = AccountCreate.create(account, newAccountKey,100000);
	    	if (account == null) {
=======
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
>>>>>>> f76e9c4... Unit tests pass:examples/main/java/com/hedera/account/DemoAccount.java
    			logger.info("*******************************************");
    			logger.info("ACCOUNT CREATE FAILED");
    			logger.info("*******************************************");
    			throw new Exception("Account create failure");
	    	} else {
<<<<<<< HEAD:examples/src/main/java/com/hedera/account/DemoAccount.java
=======
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
>>>>>>> f76e9c4... Unit tests pass:examples/main/java/com/hedera/account/DemoAccount.java
	    		if (getTXRecord) {
	    			  HederaTransactionID txID = account.hederaTransactionID;
	    			  HederaTransactionRecord txRecord = new HederaTransactionRecord(txID, 10, txQueryDefaults);
    			}
<<<<<<< HEAD:examples/src/main/java/com/hedera/account/DemoAccount.java
	    	}
    		account.txQueryDefaults.generateRecord = false;
	    	if (account != null) {
	    		// the paying account is now the new account
	    		txQueryDefaults.payingAccountID = account.getHederaAccountID();
	    		txQueryDefaults.payingKeyPair = newAccountKey;

	    		// get balance for the account
	    		if (balance) {
	    			AccountGetBalance.getBalance(account);
	    		}
	    	}

	    	if (send) {
		    	accountXferTo = AccountCreate.create(accountXferTo, accountXferToKey, 10000);
		    	if (accountXferTo == null) {
	    			logger.info("*******************************************");
	    			logger.info("SECOND ACCOUNT CREATE FAILED");
	    			logger.info("*******************************************");
	    			throw new Exception("Account create failure");
		    	}
	    	}
	    	
	    	if (account != null) {

		        // get balance for the account
		    	if (balance) {
		    		AccountGetBalance.getBalance(account);
		    	}
		
				// send some crypto
		    	if (send) {
		    		logger.info("Start Time" + Instant.now());
		    		AccountSend.send(account, accountXferTo, 100);
=======
	    		
		    	if (send) {
					// send some crypto form my account to the new account
		    		// setup my account
		        	HederaAccount myAccount = new HederaAccount();
		        	// setup transaction/query defaults (durations, etc...)
		        	myAccount.txQueryDefaults = ExampleUtilities.getTxQueryDefaults();
			    	// setup my account number from properties file
			    	myAccount.accountNum = myAccount.txQueryDefaults.payingAccountID.accountNum;

			    	logger.info("Start Time" + Instant.now());
		    		AccountSend.send(myAccount, newAccount, 100);
>>>>>>> f76e9c4... Unit tests pass:examples/main/java/com/hedera/account/DemoAccount.java
		    		logger.info("End Time" + Instant.now());
					// get balance for my account
			    	if (getBalance) {
			    		AccountGetBalance.getBalance(myAccount);
			    		System.out.println(String.format("My balance=%d tinybar", myAccount.balance()));
			    	}
					// get balance for the new account
			    	if (getBalance) {
			    		AccountGetBalance.getBalance(newAccount);
			    		System.out.println(String.format("New account balance=%d tinybar", newAccount.balance()));
			    	}
		    	}
<<<<<<< HEAD:examples/src/main/java/com/hedera/account/DemoAccount.java
				// get balance for the account
		    	if (balance) {
		    		AccountGetBalance.getBalance(account);
		    	}
				// get account info
		    	if (info) {
		    		AccountGetInfo.getInfo(account);
		    	}
		
				// update the account
=======
		    	
>>>>>>> f76e9c4... Unit tests pass:examples/main/java/com/hedera/account/DemoAccount.java
		    	if (update) {
		    		// update the new account with some changes properties
		    		// note: the original keypair for the newAccount object needs to be reset
		    		// get information only recovers the public key, and the private/secret key
		    		// is necessary here to sign for and therefore approve the change
		    		newAccount.accountKey = newAccountKey;
		    		
		    		// setup an object to contain values to update
		    		HederaAccountUpdateValues updates = new HederaAccountUpdateValues();
	    		
		    		// create a new key
<<<<<<< HEAD:examples/src/main/java/com/hedera/account/DemoAccount.java
		    	    HederaCryptoKeyPair ed25519Key = new HederaCryptoKeyPair(KeyType.ED25519);
		    	    			
=======
		    		HederaKeyPair ed25519Key = new HederaKeyPair(KeyType.ED25519);
	    	    			
>>>>>>> f76e9c4... Unit tests pass:examples/main/java/com/hedera/account/DemoAccount.java
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
<<<<<<< HEAD:examples/src/main/java/com/hedera/account/DemoAccount.java
		    		
		    		account = AccountUpdate.update(account, updates);
		    		if (account != null) {
		    			AccountGetInfo.getInfo(account);
=======
	    		
		    		newAccount = AccountUpdate.update(newAccount, updates);
		    		if (newAccount != null) {
		    			AccountGetInfo.getInfo(newAccount);
>>>>>>> f76e9c4... Unit tests pass:examples/main/java/com/hedera/account/DemoAccount.java
		    		} else {
		    			logger.info("*******************************************");
		    			logger.info("ACCOUNT UPDATE FAILED - account is now null");
		    			logger.info("*******************************************");
		    		}
		    	}
<<<<<<< HEAD:examples/src/main/java/com/hedera/account/DemoAccount.java
		
		    	if ((account != null) && (doAddClaim)) {
			    	HederaCryptoKeyPair claimKeyPair = new HederaCryptoKeyPair(KeyType.ED25519);
			        HederaKey claimKey = new HederaKey(claimKeyPair.getKeyType(), claimKeyPair.getPublicKey());
			
					// Create a new claim object
					HederaClaim claim;
					claim = new HederaClaim(account.shardNum, account.realmNum, account.accountNum, "ClaimHash".getBytes("UTF-8"));
					// add a key to the claim
					claim.addKey(claimKey);
			        // add a claim
			        if (AccountAddClaim.addClaim(account,claim, claimKeyPair)) {
			        }
		    	} else if (account == null) {
	    			logger.info("*******************************************");
	    			logger.info("ACCOUNT object is null, skipping claim tests");
	    			logger.info("*******************************************");
		    	}
=======
	    		
>>>>>>> f76e9c4... Unit tests pass:examples/main/java/com/hedera/account/DemoAccount.java
	    	}
    	}
    	
    	
    	
    	
//	    	
//	    	if (myAccount != null) {
//
//				// update the account
//		
//		    	if ((myAccount != null) && (doAddClaim)) {
//		    		HederaKeyPair claimKeyPair = new HederaKeyPair(KeyType.ED25519);
//			        HederaKeyPair claimKey = new HederaKeyPair(claimKeyPair.getKeyType(), claimKeyPair.getPublicKey());
//			
//					// Create a new claim object
//					HederaClaim claim;
//					claim = new HederaClaim(myAccount.shardNum, myAccount.realmNum, myAccount.accountNum, "ClaimHash".getBytes("UTF-8"));
//					// add a key to the claim
//					claim.addKey(claimKey);
//			        // add a claim
//			        if (AccountAddClaim.addClaim(myAccount,claim, claimKeyPair)) {
//			        }
//		    	} else if (myAccount == null) {
//	    			logger.info("*******************************************");
//	    			logger.info("ACCOUNT object is null, skipping claim tests");
//	    			logger.info("*******************************************");
//		    	}
//	    	}
//    	}	    	
	}
}