package com.hedera.examples.fileWrappers;

import org.slf4j.LoggerFactory;

import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.file.HederaFile;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class FileDelete {
	public static boolean delete(HederaFile file) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(FileDelete.class);

		ExampleUtilities.showResult("**    DELETE FILE");

		// delete the file
		HederaTransactionResult deleteResult = file.delete();
		// was it successful ?
		if (deleteResult.getPrecheckResult() == ResponseCodeEnum.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt  = Utilities.getReceipt(file.hederaTransactionID,  file.txQueryDefaults.node);
			if (receipt.transactionStatus == ResponseCodeEnum.SUCCESS) {
				// if query successful, print it
				ExampleUtilities.showResult("**    Deletion successful");
				return true;
			} else {
				ExampleUtilities.showResult("**    Failed with transactionStatus:" + receipt.transactionStatus);
				return false;
			}
		} else if (file.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.debug("system busy, try again later");
			return false;
		} else {
			ExampleUtilities.showResult("**    Failed with getPrecheckResult:" + deleteResult.getPrecheckResult());
			return false;
		}
	}

}
