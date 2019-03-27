package com.hedera.examples.fileWrappers;

import org.slf4j.LoggerFactory;
import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.file.HederaFile;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class FileCreate {

	public static HederaFile create(HederaFile file, byte[] contents) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory
				.getLogger(FileCreate.class);
		// new file
		long shardNum = 0;
		long realmNum = 0;

		ExampleUtilities.showResult("**    FILE CREATE");

		final int FILE_PART_SIZE = 3000; // 3K bytes

		int numParts = contents.length / FILE_PART_SIZE;
		int remainder = contents.length % FILE_PART_SIZE;

		byte[] firstPartBytes = null;
		if (contents.length <= FILE_PART_SIZE) {
			firstPartBytes = contents;
			remainder = 0;
		} else {
			firstPartBytes = ExampleUtilities.copyBytes(0, FILE_PART_SIZE, contents);
		}

		System.out.println("@@@ file size=" + contents.length + "; FILE_PART_SIZE=" + FILE_PART_SIZE + "; numParts="
				+ numParts + "; remainder=" + remainder);

		// create the new file
		// file creation transaction
		HederaTransactionResult createResult = file.create(shardNum, realmNum, firstPartBytes, null);
		// was it successful ?
		if (createResult.getPrecheckResult() == ResponseCodeEnum.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt = Utilities.getReceipt(file.hederaTransactionID,
					file.txQueryDefaults.node, 10, 4000, 0);
			// was that successful ?
			if (receipt.transactionStatus == ResponseCodeEnum.SUCCESS) {
				// yes, get the new account number from the receipt
				file.fileNum = receipt.fileID.fileNum;
				// and print it out
				ExampleUtilities.showResult("**    Your new file number is " + file.fileNum);

				// append the rest of the parts
				for (int i = 1; i < numParts; i++) {
					byte[] partBytes = ExampleUtilities.copyBytes(i * FILE_PART_SIZE, FILE_PART_SIZE, contents);

					ExampleUtilities.showResult("**    Appending remaining data");
					if (file.append(partBytes) != null) {
						// continue
					} else if (file.getPrecheckResult() == ResponseCodeEnum.BUSY) {
						logger.debug("system busy, try again later");
						return null;
					} else {
						ExampleUtilities.showResult("**    Appending Failure");
						System.exit(0);
					}

				}

				if (remainder > 0) {
					byte[] partBytes = ExampleUtilities.copyBytes(numParts * FILE_PART_SIZE, remainder, contents);
					ExampleUtilities.showResult("**    Appending remaining data");
					if (file.append(partBytes) != null) {
						// continue
					} else if (file.getPrecheckResult() == ResponseCodeEnum.BUSY) {
						logger.debug("system busy, try again later");
						return null;
					} else {
						ExampleUtilities.showResult("**    Appending Failure");
						System.exit(0);
					}
				}
			} else {
				ExampleUtilities.showResult("**    Failed with transactionStatus:" + receipt.transactionStatus);
				return null;
			}
		} else if (file.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.debug("system busy, try again later");
			return null;
		} else {
			ExampleUtilities.showResult("**    Failed with getPrecheckResult:" + createResult.getPrecheckResult());
			return null;
		}
		return file;
	}
}
