package com.hedera.examples.simple;

import com.hedera.examples.accountWrappers.AccountAddClaim;
import com.hedera.examples.accountWrappers.AccountCreate;
import com.hedera.examples.accountWrappers.AccountGetBalance;
import com.hedera.examples.accountWrappers.AccountGetInfo;
import com.hedera.examples.accountWrappers.AccountGetRecords;
import com.hedera.examples.accountWrappers.AccountSend;
import com.hedera.examples.accountWrappers.AccountUpdate;
import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.account.HederaAccountUpdateValues;
import com.hedera.sdk.account.HederaClaim;
import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.node.HederaNodeList;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.common.HederaTransactionID;
import com.hedera.sdk.common.HederaTransactionRecord;

public final class DemoAccountCreateSend {
	
	public static void main (String... arguments) throws Exception {
	   
		HederaTransactionAndQueryDefaults txQueryDefaults = ExampleUtilities.getTxQueryDefaults();
		
		// setup my account
    	HederaAccount myAccount = new HederaAccount();
    	// setup transaction/query defaults (durations, etc...)
    	myAccount.txQueryDefaults = txQueryDefaults;
    	// setup my account number from properties file
    	myAccount.accountNum = myAccount.txQueryDefaults.payingAccountID.accountNum;

    	/* Create a new account  */
    	
		// setup new account object
    	HederaAccount newAccount = new HederaAccount();
    	// setup transaction/query defaults (durations, etc...)
    	// note: This txQueryDefaults contains a payingAccountID which is the account paying for the transaction
    	newAccount.txQueryDefaults = txQueryDefaults;

		HederaKeyPair newAccountKey = new HederaKeyPair(KeyType.ED25519);
		ExampleUtilities.showResult(String.format("New account public key = %s\nNew account private/secret key %s=",newAccountKey.getPublicKeyHex(), newAccountKey.getSecretKeyHex()));

		// create a new account with a balance of 110000 tinybar, using the above generated public key
		newAccount = AccountCreate.create(newAccount, newAccountKey.getPublicKeyHex(), newAccountKey.getKeyType(), 110000);
		newAccount.txQueryDefaults.payingKeyPair = newAccountKey;
		newAccount.txQueryDefaults.payingAccountID = newAccount.getHederaAccountID();

		// send some crypto from the new account to myaccount
    	// setup my account number from properties file
		AccountSend.send(newAccount, myAccount, 100);

	}
}