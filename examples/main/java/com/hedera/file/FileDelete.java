package com.hedera.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.file.HederaFile;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class FileDelete {
	public static boolean delete(HederaFile file) throws Exception {
		final Logger logger = LoggerFactory.getLogger(FileDelete.class);
		logger.info("");
		logger.info("DELETE FILE");
		logger.info("");

		// delete the file
		HederaTransactionResult deleteResult = file.delete();
		// was it successful ?
		if (deleteResult.getPrecheckResult() == ResponseCodeEnum.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt  = Utilities.getReceipt(file.hederaTransactionID,  file.txQueryDefaults.node);
			if (receipt.transactionStatus == ResponseCodeEnum.SUCCESS) {
				// if query successful, print it
				logger.info("===>Deletion successful");
				return true;
			} else {
				logger.info("Failed with transactionStatus:" + receipt.transactionStatus);
				return false;
			}
		} else if (file.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.info("system busy, try again later");
			return false;
		} else {
			logger.info("Failed with getPrecheckResult:" + deleteResult.getPrecheckResult());
			return false;
		}
	}

}
