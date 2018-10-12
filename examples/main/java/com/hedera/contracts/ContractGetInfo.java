package com.hedera.contracts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.contract.HederaContract;

public final class ContractGetInfo {
	public static void getInfo(HederaContract contract) throws Exception {
		final Logger logger = LoggerFactory.getLogger(ContractGetInfo.class);
		
		logger.info("");
		logger.info("CONTRACT GET INFO");
		logger.info("");

		// get info for the contract
		if (contract.getInfo()) {
			logger.info("===>Got info");
		} else {
			logger.info("===>Getting info - precheck ERROR " + contract.getPrecheckResult());
		}
	}

}
