package com.hedera.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.file.HederaFile;

public final class FileGetContents {
	public static void getContents(HederaFile file) throws Exception {
		final Logger logger = LoggerFactory.getLogger(FileGetContents.class);
		
		logger.info("");
		logger.info("FILE GET CONTENTS");
		logger.info("");
		
		// run a get contents query
		byte[] contents = file.getContents();
		if (contents != null) {
			// it was successful, print it
			logger.info("===>Got contents=");
			logger.info(new String(contents,"UTF-8"));
		} else {
			// an error occurred
			logger.info("===>Getting contents - precheck ERROR" + file.getPrecheckResult());
		}
	}

}
