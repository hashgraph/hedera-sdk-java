package com.hedera.examples.contractWrappers;

import org.slf4j.LoggerFactory;

import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.contract.HederaContract;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class ContractGetInfo {
	public static boolean getInfo(HederaContract contract) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ContractGetInfo.class);
		
		ExampleUtilities.showResult("**    CONTRACT GET INFO");

		// get info for the contract
		if (contract.getInfo()) {
			ExampleUtilities.showResult("**    Got info");
			return true;
		} else if (contract.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.info("system busy, try again later");
			return false;
		} else {
			ExampleUtilities.showResult("**    Getting info - precheck ERROR " + contract.getPrecheckResult());
			return false;
		}
	}

}
