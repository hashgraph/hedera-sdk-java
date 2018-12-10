package com.hedera.file;

import java.security.spec.InvalidKeySpecException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hedera.account.*;
import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaKey.KeyType;
import com.hedera.sdk.cryptography.HederaCryptoKeyPair;
import com.hedera.sdk.file.HederaFile;
import com.hedera.utilities.*;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;

public final class DemoFile2 {
	final static Logger logger = LoggerFactory.getLogger(DemoFile2.class);
	
	public static void main (String... arguments) throws Exception {

		// setup a set of defaults for query and transactions
		HederaTransactionAndQueryDefaults txQueryDefaults = new HederaTransactionAndQueryDefaults();
		txQueryDefaults = ExampleUtilities.getTxQueryDefaults();

    	HederaAccount account = new HederaAccount();
    	account.setHederaAccountID(txQueryDefaults.payingAccountID);
    	
    	// setup transaction/query defaults (durations, etc...)
    	txQueryDefaults.generateRecord = false;
    	account.txQueryDefaults = txQueryDefaults;
    	account.autoRenewPeriod = new HederaDuration(31536000, 0);

    	// new file object
    	HederaFile file = new HederaFile(0, 0, 2091);
    	// setup transaction/query defaults (durations, etc...)
    	txQueryDefaults.fileWacl = new HederaCryptoKeyPair(KeyType.ED25519);
    	file.txQueryDefaults = txQueryDefaults;

		FileGetInfo.getInfo(file);
		Thread.sleep(5000);
		FileGetContents.getContents(file);
	}

}