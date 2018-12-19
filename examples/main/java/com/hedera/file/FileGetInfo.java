package com.hedera.file;

import org.slf4j.LoggerFactory;

import com.hedera.sdk.file.HederaFile;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class FileGetInfo {
	public static boolean getInfo(HederaFile file) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(FileGetInfo.class);
		
		logger.info("");
		logger.info("FILE GET INFO");
		logger.info("");

		// get info for the file
		if (file.getInfo()) {
			logger.info("===>Got info");
			return true;
		} else if (file.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.info("system busy, try again later");
			return false;
		} else {
			logger.info("===>Getting info - precheck ERROR " + file.getPrecheckResult());
			return false;
		}
		
	}
}
