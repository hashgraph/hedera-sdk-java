package com.hedera.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaPrecheckResult;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.HederaTransactionStatus;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.transaction.HederaTransactionResult;

public final class AccountSend {
	public static void send(HederaAccount account, HederaAccount toAccount, long amount) throws Exception {
		final Logger logger = LoggerFactory.getLogger(AccountSend.class);
		
		logger.info("");
		logger.info("CRYPTO TRANSFER");
		logger.info("");

		// make the transfer
		HederaTransactionResult transferResult = account.send(toAccount.getHederaAccountID(), amount);
		// was it successful ?
		if (transferResult.getPrecheckResult() == HederaPrecheckResult.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt  = Utilities.getReceipt(account.hederaTransactionID,  account.txQueryDefaults.node);
			if (receipt.transactionStatus == HederaTransactionStatus.SUCCESS) {
				// if query successful, print it
				logger.info("===>Transfer successful");
			} else {
				logger.info("Failed with transactionStatus:" + receipt.transactionStatus.toString());
			}
		} else {
			logger.info("Failed with getPrecheckResult:" + transferResult.getPrecheckResult().toString());
		}
	}
}
