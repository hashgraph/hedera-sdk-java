package com.hedera.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaPrecheckResult;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.HederaTransactionStatus;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.cryptography.HederaCryptoKeyPair;
import com.hedera.sdk.transaction.HederaTransactionResult;

public final class AccountCreate {
	public static HederaAccount create(HederaAccount account, HederaCryptoKeyPair newAccountKey, long initialBalance) throws Exception {
		final Logger logger = LoggerFactory.getLogger(AccountCreate.class);
		// new account properties
		long shardNum = 0;
		long realmNum = 0;
		
		logger.info("");
		logger.info("CRYPTO CREATE ACCOUNT");
		logger.info("");

		// create the new account
		// account creation transaction
		HederaTransactionResult createResult = account.create(shardNum, realmNum, newAccountKey.getPublicKey(), newAccountKey.getKeyType(), initialBalance, null);
		// was it successful ?
		if (createResult.getPrecheckResult() == HederaPrecheckResult.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt = Utilities.getReceipt(account.hederaTransactionID, account.txQueryDefaults.node);
			// was that successful ?
			if (receipt.transactionStatus == HederaTransactionStatus.SUCCESS) {
				// yes, get the new account number from the receipt
				account.accountNum = receipt.accountID.accountNum;
				// and print it out
				logger.info(String.format("===>Your new account number is %d", account.accountNum));
			} else {
				logger.info("transactionStatus not SUCCESS: " + receipt.transactionStatus.name());
				return null;
			}
		} else {
			logger.info("getPrecheckResult not OK: " + createResult.getPrecheckResult().name());
			return null;
		}
		return account;
	}
}
