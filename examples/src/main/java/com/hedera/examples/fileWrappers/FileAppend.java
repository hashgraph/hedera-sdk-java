package com.hedera.examples.fileWrappers;

import org.slf4j.LoggerFactory;

import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.file.HederaFile;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class FileAppend {
	public static boolean append(HederaFile file, byte[] newContents) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(FileAppend.class);

		ExampleUtilities.showResult("**    FILE APPEND");

		// append to the file
		// file append transaction
		HederaTransactionResult appendResult = file.append(newContents);
		// was it successful ?
		if (appendResult.getPrecheckResult() == ResponseCodeEnum.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt  = Utilities.getReceipt(file.hederaTransactionID,  file.txQueryDefaults.node, 10, 4000, 0);
			// was that successful ?
			if (receipt.transactionStatus == ResponseCodeEnum.SUCCESS) {
				// and print it out
				ExampleUtilities.showResult(String.format("**    File append success"));
				return true;
			} else {
				ExampleUtilities.showResult("**    Failed with transactionStatus:" + receipt.transactionStatus);
				return false;
			}
		} else if (file.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.debug("system busy, try again later");
			return false;
		} else {
			ExampleUtilities.showResult("**    Failed with getPrecheckResult:" + appendResult.getPrecheckResult().toString());
			return false;
		}
	}

}
