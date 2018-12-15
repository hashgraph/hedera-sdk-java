package com.hedera.account;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.account.HederaAccountUpdateValues;
import com.hedera.sdk.account.HederaClaim;
import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.common.HederaTransactionID;
import com.hedera.sdk.common.HederaTransactionRecord;
import com.hedera.utilities.ExampleUtilities;

public final class DemoAccount {
	
	public static void main (String... arguments) throws Exception {
		final Logger logger = LoggerFactory.getLogger(DemoAccount.class);
		
		//DO NOT CHANGE THESE, CHANGE BELOW INSTEAD
		boolean create = false; //OK
    	boolean balance = false; //OK
    	boolean send = false; //NOK
    	boolean info = false; //OK
    	boolean update = false; //OK
    	boolean doAddClaim = false;//OK
		boolean getTXRecord = false;
		
    	// new account objects
    	HederaAccount newAccount = new HederaAccount();
    	HederaAccount accountXferTo = new HederaAccount();
    	
    	// setup transaction/query defaults (durations, etc...)
    	newAccount.txQueryDefaults = ExampleUtilities.getTxQueryDefaults();
    	accountXferTo.txQueryDefaults = ExampleUtilities.getTxQueryDefaults();
    	
    	create = true;
    	balance = true;
    	send = true;
    	info = true;
    	update = true;
    	doAddClaim = false; //-- not implemented ?
    	getTXRecord = true;
    	
		// create an account
    	if (create) {
    		newAccount.txQueryDefaults.generateRecord = getTXRecord;
    		HederaKeyPair newAccountKey = new HederaKeyPair(KeyType.ED25519);
    		newAccount.accountKey = newAccountKey;
    		HederaKeyPair accountXferToKey = new HederaKeyPair(KeyType.ED25519);
    		
    		System.out.println(newAccount.txQueryDefaults.payingKeyPair.getKeyList().keys.get(0).getProtobuf().toString());
    		
	    	newAccount = AccountCreate.create(newAccount, newAccountKey.getPublicKeyHex(), newAccountKey.getKeyType(), 10000000);
	    	
	    	if (newAccount == null) {
    			logger.info("*******************************************");
    			logger.info("FIRST ACCOUNT CREATE FAILED");
    			logger.info("*******************************************");
    			throw new Exception("Account create failure");
	    	} else {
	    		AccountGetInfo.getInfo(newAccount);
	    		
	    		if (getTXRecord) {
	    			  HederaTransactionID txID = newAccount.hederaTransactionID;
	    			  HederaTransactionRecord txRecord = new HederaTransactionRecord(txID, newAccount.txQueryDefaults.node.transactionGetRecordsQueryFee, newAccount.txQueryDefaults);
    			}
	    	}
    		newAccount.txQueryDefaults.generateRecord = false;
	    	if (newAccount != null) {
	    		// the paying account is now the new account
	    		newAccount.txQueryDefaults.payingAccountID = newAccount.getHederaAccountID();
	    		newAccount.txQueryDefaults.payingKeyPair = newAccountKey;
	    		
	    		// get balance for the account
	    		if (balance) {
	    			AccountGetBalance.getBalance(newAccount);
	    		}
	    	}

	    	if (send) {
		    	accountXferTo = AccountCreate.create(newAccount, accountXferToKey.getPublicKeyHex(), newAccountKey.getKeyType(), 100000);
		    	if (accountXferTo == null) {
	    			logger.info("*******************************************");
	    			logger.info("SECOND ACCOUNT CREATE FAILED");
	    			logger.info("*******************************************");
	    			throw new Exception("Account create failure");
		    	}
	    	}
	    	
	    	if (newAccount != null) {

		        // get balance for the account
		    	if (balance) {
		    		AccountGetBalance.getBalance(newAccount);
		    	}
		
				// send some crypto
		    	if (send) {
		    		logger.info("Start Time" + Instant.now());
		    		AccountSend.send(newAccount, accountXferTo, 100);
		    		logger.info("End Time" + Instant.now());
		    	}
				// get balance for the account
		    	if (balance) {
		    		AccountGetBalance.getBalance(newAccount);
		    	}
				// get account info
		    	if (info) {
		    		AccountGetInfo.getInfo(newAccount);
		    	}
		
				// update the account
		    	if (update) {
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
		    		
		    		// set the current key pair for the account
		    		// Note: This was partially overwritten earlier with getInfo which only retrieves a public key from the network
		    		newAccount.accountKey = newAccountKey;
		    		// reset Account ID (this was overwritten by the creation of the transfer to account)
		    		newAccount.accountNum = newAccount.txQueryDefaults.payingAccountID.accountNum;
		    		newAccount = AccountUpdate.update(newAccount, updates);
		    		if (newAccount != null) {
		    			AccountGetInfo.getInfo(newAccount);
		    		} else {
		    			logger.info("*******************************************");
		    			logger.info("ACCOUNT UPDATE FAILED - account is now null");
		    			logger.info("*******************************************");
		    		}
		    	}
		
		    	if ((newAccount != null) && (doAddClaim)) {
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
		    	} else if (newAccount == null) {
	    			logger.info("*******************************************");
	    			logger.info("ACCOUNT object is null, skipping claim tests");
	    			logger.info("*******************************************");
		    	}
	    	}
    	}	    	
	}
}