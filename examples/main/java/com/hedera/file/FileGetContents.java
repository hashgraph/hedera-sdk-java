package com.hedera.file;

import org.slf4j.LoggerFactory;

import com.hedera.sdk.file.HederaFile;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class FileGetContents {
	public static boolean getContents(HederaFile file) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(FileGetContents.class);
		
		logger.info("");
		logger.info("FILE GET CONTENTS");
		logger.info("");
		
		// run a get contents query
		byte[] contents = file.getContents();
		if (contents != null) {
			// it was successful, print it
			logger.info("===>Got contents=");
			logger.info(new String(contents,"UTF-8"));
			return true;
		} else if (file.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.info("system busy, try again later");
			return false;
		} else {
			// an error occurred
			logger.info("===>Getting contents - precheck ERROR" + file.getPrecheckResult());
			return false;
		}
	}

}
