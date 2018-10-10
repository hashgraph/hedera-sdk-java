package com.hedera.file;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.file.HederaFile;

public final class FileGetContents {
	public static void getContents(HederaFile file) {
		final Logger logger = LoggerFactory.getLogger(FileGetContents.class);
		
		logger.info("");
		logger.info("FILE GET CONTENTS");
		logger.info("");
		
		try {
			// run a get contents query
			byte[] contents = file.getContents();
			if (contents != null) {
				// it was successful, print it
				logger.info("===>Got contents=");
				try {
					logger.info(new String(contents,"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			} else {
				// an error occurred
				logger.info("===>Getting contents - precheck ERROR" + file.getPrecheckResult());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
