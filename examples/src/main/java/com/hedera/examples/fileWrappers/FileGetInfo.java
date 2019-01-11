package com.hedera.examples.fileWrappers;

import org.slf4j.LoggerFactory;

import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.file.HederaFile;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class FileGetInfo {
	public static boolean getInfo(HederaFile file) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(FileGetInfo.class);
		
		ExampleUtilities.showResult("**    FILE GET INFO");

		// get info for the file
		if (file.getInfo()) {
			ExampleUtilities.showResult("**    Got info");
			return true;
		} else if (file.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.info("system busy, try again later");
			return false;
		} else {
			ExampleUtilities.showResult("**    Getting info - precheck ERROR " + file.getPrecheckResult());
			return false;
		}
		
	}
}
