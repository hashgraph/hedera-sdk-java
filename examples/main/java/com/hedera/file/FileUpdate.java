package com.hedera.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.common.HederaPrecheckResult;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.HederaTransactionStatus;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.file.HederaFile;
import com.hedera.sdk.transaction.HederaTransactionResult;

public final class FileUpdate {
	public static HederaFile update(HederaFile file, long expireSeconds, int expireNanos, byte[] newContents) throws Exception {
		final Logger logger = LoggerFactory.getLogger(FileUpdate.class);
		logger.info("");
		logger.info("FILE UPDATE");
		logger.info("");

		// update the file
		// file update transaction
		HederaTransactionResult updateResult = file.update(expireSeconds, expireNanos, newContents);
		// was it successful ?
		if (updateResult.getPrecheckResult() == HederaPrecheckResult.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt  = Utilities.getReceipt(file.hederaTransactionID,  file.txQueryDefaults.node);
			// was that successful ?
			if (receipt.transactionStatus == HederaTransactionStatus.SUCCESS) {
				// and print it out
				logger.info(String.format("===>File update success"));
			} else {
				logger.info("Failed with transactionStatus:" + receipt.transactionStatus);
				return null;
			}
		} else {
			logger.info("Failed with getPrecheckResult:" + updateResult.getPrecheckResult());
			return null;
		}
		return file;
	}

}
