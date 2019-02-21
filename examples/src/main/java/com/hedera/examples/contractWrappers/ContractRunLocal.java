package com.hedera.examples.contractWrappers;

import org.slf4j.LoggerFactory;

import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.contract.HederaContractFunctionResult;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class ContractRunLocal {
	public static HederaContractFunctionResult runLocal(HederaContract contract, long gas, long maxResultSize, byte[] function) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ContractRunLocal.class);

		ExampleUtilities.showResult("**    CONTRACT RUN LOCAL");

		HederaContractFunctionResult functionResult = new HederaContractFunctionResult();
		// run a call local query
		functionResult = contract.callLocal(gas, function, maxResultSize);
		if (functionResult != null) {
			// it was successful, print it
			ExampleUtilities.showResult(String.format("**   Got error message=%s\n"
					+ "**    Got gas used=%d\n"
					+ "**    Got contract num=%d"
					,functionResult.errorMessage()
					,functionResult.gasUsed()
					,functionResult.contractID().contractNum));
			
		} else if (contract.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.debug("system busy, try again later");
			return null;
		} else {
			// an error occurred
			ExampleUtilities.showResult("**    Running local function - precheck ERROR " + contract.getPrecheckResult());
			return null;
		}
		return functionResult;
	}

}
