package com.hedera.account;

import java.io.UnsupportedEncodingException;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    	boolean balance = false; //OK
    	boolean send = false; //NOK
    	boolean info = false; //OK
    	boolean update = false; //OK
    	boolean doAddClaim = false;//OK
		boolean getTXRecord = false;
		
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
    	
		// create an account
    	if (create) {
    		account.txQueryDefaults.generateRecord = getTXRecord;
	    	HederaCryptoKeyPair newAccountKey = new HederaCryptoKeyPair(KeyType.ED25519);
	    	HederaCryptoKeyPair accountXferToKey = new HederaCryptoKeyPair(KeyType.ED25519);
	    	
	    	account = AccountCreate.create(account, newAccountKey,100000);
	    	if (account == null) {
    			logger.info("*******************************************");
    			logger.info("FIRST ACCOUNT CREATE FAILED");
    			logger.info("*******************************************");
    			throw new Exception("Account create failure");
	    	} else {
	    		if (getTXRecord) {
	    			  HederaTransactionID txID = account.hederaTransactionID;
	    			  HederaTransactionRecord txRecord = new HederaTransactionRecord(txID, 10, txQueryDefaults);
    			}
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
		    		logger.info("End Time" + Instant.now());
		    	}
				// get balance for the account
		    	if (balance) {
		    		AccountGetBalance.getBalance(account);
		    	}
				// get account info
		    	if (info) {
		    		AccountGetInfo.getInfo(account);
		    	}
		
				// update the account
		    	if (update) {
		    		// setup an object to contain values to update
		    		HederaAccountUpdateValues updates = new HederaAccountUpdateValues();
		    		
		    		// create a new key
		    	    HederaCryptoKeyPair ed25519Key = new HederaCryptoKeyPair(KeyType.ED25519);
		    	    			
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
		    		
		    		account = AccountUpdate.update(account, updates);
		    		if (account != null) {
		    			AccountGetInfo.getInfo(account);
		    		} else {
		    			logger.info("*******************************************");
		    			logger.info("ACCOUNT UPDATE FAILED - account is now null");
		    			logger.info("*******************************************");
		    		}
		    	}
		
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
	    	}
    	}	    	
	}
}