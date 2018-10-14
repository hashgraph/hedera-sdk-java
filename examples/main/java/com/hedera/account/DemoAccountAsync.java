package com.hedera.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaPrecheckResult;
import com.hedera.sdk.common.HederaKey.KeyType;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.cryptography.HederaCryptoKeyPair;
import com.hedera.sdk.common.HederaTransactionState;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hedera.utilities.ExampleUtilities;

public final class DemoAccountAsync {
	
	public static void main (String... arguments) throws Exception {
		final Logger logger = LoggerFactory.getLogger(DemoAccountAsync.class);

		// setup a set of defaults for query and transactions
		HederaTransactionAndQueryDefaults txQueryDefaults = new HederaTransactionAndQueryDefaults();
		txQueryDefaults = ExampleUtilities.getTxQueryDefaults();
		
    	// new account objects
    	HederaAccount account = new HederaAccount();
    	HederaAccount accountXferTo = new HederaAccount();
    	
    	// setup transaction/query defaults (durations, etc...)
    	account.txQueryDefaults = txQueryDefaults;
    	accountXferTo.txQueryDefaults = txQueryDefaults;
    	
		// create an account
    	HederaCryptoKeyPair newAccountKey = new HederaCryptoKeyPair(KeyType.ED25519);
    	HederaCryptoKeyPair accountXferToKey = new HederaCryptoKeyPair(KeyType.ED25519);
    	
    	account = AccountCreate.create(account, newAccountKey,100000);
    	if (account == null) {
			logger.info("*******************************************");
			logger.info("FIRST ACCOUNT CREATE FAILED");
			logger.info("*******************************************");
			throw new Exception("Account create failure");
    	} 
	
		// the paying account is now the new account
		txQueryDefaults.payingAccountID = account.getHederaAccountID();
		txQueryDefaults.payingKeyPair = newAccountKey;
		
		// create a new account to transfer funds to
    	accountXferTo = AccountCreate.create(accountXferTo, accountXferToKey, 10000);
    	if (accountXferTo == null) {
			logger.info("*******************************************");
			logger.info("SECOND ACCOUNT CREATE FAILED");
			logger.info("*******************************************");
			throw new Exception("Account create failure");
    	}
    	
    	HederaTransactionState transactionState = new HederaTransactionState();
    	// define and start a thread to query for receipts asynchronously
    	class GetReceipts extends Thread 
    	{ 
    	    @Override
    	    public void run() 
    	    {
    	    	while (true) {
    	    		try {
						transactionState.refresh();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
    	    		try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
    	    	}
    	    } 
    	}     	

        GetReceipts getReceipts = new GetReceipts(); 
        getReceipts.start(); 

        logger.info("************************************");
        logger.info("*     RRRRR   U   U   N    N");
        logger.info("*     R   R   U   U   NN   N");
        logger.info("*     RRRRR   U   U   N N  N");
        logger.info("*     R  R    U   U   N  N N");
        logger.info("*     R   R   UUUUU   N    N");
        logger.info("************************************");
        
        for (int i=1; i <= 20; i++) {
			// make the transfer
			HederaTransactionResult transferResult = account.send(accountXferTo.getHederaAccountID(), 20 * i);
			// was it successful ?
			if (transferResult.getPrecheckResult() == HederaPrecheckResult.OK) {
				// yes, add Transaction to state for receipt collection
				transactionState.setTransaction(account.hederaTransactionID, account.txQueryDefaults.node);
			}
        }
        
        logger.info("************************************");
        logger.info("*   DDDDD   OOOOO   N    N   EEEEEE");
        logger.info("*    D  D   O   O   NN   N   E     ");
        logger.info("*    D  D   O   O   N N  N   EEE   ");
        logger.info("*    D  D   O   O   N  N N   E     ");
        logger.info("*   DDDDD   OOOOO   N    N   EEEEEE");
        logger.info("************************************");
        
        while (true) {
        	if (transactionState.getCount() == 0) {
        		getReceipts.stop();
        		break;
        	}
        }
	}
}