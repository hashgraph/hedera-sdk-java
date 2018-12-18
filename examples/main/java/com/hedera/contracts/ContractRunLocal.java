package com.hedera.contracts;

import org.slf4j.LoggerFactory;

import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.contract.HederaContractFunctionResult;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class ContractRunLocal {
	public static HederaContractFunctionResult runLocal(HederaContract contract, long gas, long maxResultSize, byte[] function) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ContractRunLocal.class);
		logger.info("");
		logger.info("CONTRACT RUN LOCAL");
		logger.info("");
		HederaContractFunctionResult functionResult = new HederaContractFunctionResult();
		// run a call local query
		functionResult = contract.callLocal(gas, function, maxResultSize);
		if (functionResult != null) {
			// it was successful, print it
			logger.info("===>Got functionResult=" + functionResult.contractCallResult());
			logger.info("===>Got error message=" + functionResult.errorMessage());
			logger.info("===>Got gas used=" + functionResult.gasUsed());
			logger.info("===>Got bloom=" + functionResult.bloom());
			logger.info("===>Got contract num=" + functionResult.contractID().contractNum);
		} else if (contract.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.info("system busy, try again later");
			return null;
		} else {
			// an error occurred
			logger.info("===>Running local function - precheck ERROR " + contract.getPrecheckResult());
			return null;
		}
		return functionResult;
	}

}
