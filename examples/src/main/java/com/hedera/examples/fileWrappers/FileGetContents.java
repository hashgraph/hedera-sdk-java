package com.hedera.examples.fileWrappers;

import org.slf4j.LoggerFactory;

import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.file.HederaFile;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class FileGetContents {
	public static boolean getContents(HederaFile file) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(FileGetContents.class);
		
		ExampleUtilities.showResult("**    FILE GET CONTENTS");
		
		// run a get contents query
		byte[] contents = file.getContents();
		if (contents != null) {
			// it was successful, print it
			ExampleUtilities.showResult("**    Got contents=");
			ExampleUtilities.showResult(new String(contents,"UTF-8"));
			return true;
		} else if (file.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.info("system busy, try again later");
			return false;
		} else {
			// an error occurred
			ExampleUtilities.showResult("**    Getting contents - precheck ERROR" + file.getPrecheckResult());
			return false;
		}
	}

}
