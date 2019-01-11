package com.hedera.examples.contractWrappers;

import org.slf4j.LoggerFactory;

import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.contract.HederaContract;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class ContractGetBytecode {
	public static boolean getByteCode(HederaContract contract) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ContractGetBytecode.class);

		ExampleUtilities.showResult("**    CONTRACT GET BYTECODE");

		// run a get bytecode
		byte[] bytecode = contract.getByteCode();
		if (bytecode != null) {
			// it was successful, print it
			ExampleUtilities.showResult("**    Got bytecode=" + bytecode.toString());
			return true;
		} else if (contract.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.info("system busy, try again later");
			return false;
		} else {
			// an error occurred
			ExampleUtilities.showResult("**    Getting bytecode - precheck ERROR");
			ExampleUtilities.showResult("**    " + contract.getPrecheckResult().toString());
			return false;
		}
	}

}
