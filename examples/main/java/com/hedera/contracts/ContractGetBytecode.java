package com.hedera.contracts;

import org.slf4j.LoggerFactory;

import com.hedera.sdk.contract.HederaContract;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class ContractGetBytecode {
	public static boolean getByteCode(HederaContract contract) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ContractGetBytecode.class);

		logger.info("");
		logger.info("CONTRACT GET BYTECODE");
		logger.info("");

		// run a get bytecode
		byte[] bytecode = contract.getByteCode();
		if (bytecode != null) {
			// it was successful, print it
			logger.info("===>Got bytecode=" + bytecode.toString());
			return true;
		} else if (contract.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.info("system busy, try again later");
			return false;
		} else {
			// an error occurred
			logger.info("===>Getting bytecode - precheck ERROR");
			logger.info(contract.getPrecheckResult().toString());
			return false;
		}
	}

}
