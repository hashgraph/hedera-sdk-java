package com.hedera.account;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class AccountSend {
	public static boolean send(HederaAccount account, HederaAccount toAccount, long amount) throws Exception {
		final Logger logger = LoggerFactory.getLogger(AccountSend.class);
		
		logger.info("");
		logger.info("CRYPTO TRANSFER");
		logger.info("");

		// make the transfer
		HederaTransactionResult transferResult = account.send(toAccount.getHederaAccountID(), amount);
		logger.info("TX Sent Time" + Instant.now());
		// was it successful ?
		if (transferResult.getPrecheckResult() == ResponseCodeEnum.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt  = Utilities.getReceipt(account.hederaTransactionID,  account.txQueryDefaults.node);
			if (receipt.transactionStatus == ResponseCodeEnum.SUCCESS) {
				// if query successful, print it
				logger.info("===>Transfer successful");
				return true;
			} else {
				logger.info("Failed with transactionStatus:" + receipt.transactionStatus.toString());
				return false;
			}
		} else if (transferResult.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.info("system busy, try again later");
			return false;
		} else {
			logger.info("Failed with getPrecheckResult:" + transferResult.getPrecheckResult().toString());
			return false;
		}
	}
}
