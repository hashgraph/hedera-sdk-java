package com.hedera.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.common.HederaPrecheckResult;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.HederaTransactionStatus;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.file.HederaFile;
import com.hedera.sdk.transaction.HederaTransactionResult;

public final class FileAppend {
	public static void append(HederaFile file, byte[] newContents) throws Exception {
		final Logger logger = LoggerFactory.getLogger(FileAppend.class);
		logger.info("");
		logger.info("FILE APPEND");
		logger.info("");

		// append to the file
		// file append transaction
		HederaTransactionResult appendResult = file.append(newContents);
		// was it successful ?
		if (appendResult.getPrecheckResult() == HederaPrecheckResult.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt  = Utilities.getReceipt(file.hederaTransactionID,  file.txQueryDefaults.node);
			// was that successful ?
			if (receipt.transactionStatus == HederaTransactionStatus.SUCCESS) {
				// and print it out
				logger.info(String.format("===>File append success"));
			} else {
				logger.info("Failed with transactionStatus:" + receipt.transactionStatus);
			}
		} else {
			logger.info("Failed with getPrecheckResult:" + appendResult.getPrecheckResult().toString());
		}
	}

}
