package com.hedera.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.account.HederaAccountUpdateValues;
import com.hedera.sdk.common.HederaKey;
import com.hedera.sdk.common.HederaPrecheckResult;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.HederaTransactionStatus;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.transaction.HederaTransactionResult;

public final class AccountUpdate {
	public static HederaAccount update(HederaAccount account, HederaAccountUpdateValues updates) throws Exception {
		final Logger logger = LoggerFactory.getLogger(AccountUpdate.class);

		logger.info("");
		logger.info("CRYPTO UPDATE ACCOUNT");
		logger.info("");

		// perform the update
		HederaTransactionResult updateResult = account.update(updates);
		// was it successful ?
		if (updateResult.getPrecheckResult() == HederaPrecheckResult.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt  = Utilities.getReceipt(account.hederaTransactionID,  account.txQueryDefaults.node);
			logger.info("Ran Query for receipt");
			if (receipt.transactionStatus == HederaTransactionStatus.SUCCESS) {
				// if successful, print it
				logger.info("===>Update successful");
				// update acount keys if necessary
				if (updates.newKey != null) {
					account.accountKey = new HederaKey(updates.newKey.getKeyType(), updates.newKey.getPublicKey());
			        // the paying account is now the new account
			        account.txQueryDefaults.payingKeyPair = updates.newKey;
				}
			} else {
				logger.info("Failed with transactionStatus:" + receipt.transactionStatus.toString());
				return null;
			}
		} else {
			logger.info("Failed with getPrecheckResult:" + updateResult.getPrecheckResult().toString());
			return null;
		}
		return account;
	}
}
