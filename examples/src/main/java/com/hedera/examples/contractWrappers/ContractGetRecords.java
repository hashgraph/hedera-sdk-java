package com.hedera.examples.contractWrappers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaTransactionRecord;
import com.hedera.sdk.contract.HederaContract;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class ContractGetRecords {
	public static boolean getRecords(HederaContract contract) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ContractGetRecords.class);
		
		ExampleUtilities.showResult("**    CONTRACT GET RECORDS");

		// get records for the account
		List<HederaTransactionRecord> records = new ArrayList<HederaTransactionRecord>();
		
		records = contract.getRecords();
		if (records != null) {
			for (HederaTransactionRecord record : records) {
				ExampleUtilities.showResult(String.format("**    Record transaction hash\n**    %s",record.transactionHash.toString()));
			}
			return true;
		} else if (contract.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.debug("system busy, try again later");
			return false;
		} else {
			ExampleUtilities.showResult("**    Getting records - precheck ERROR" + contract.getPrecheckResult().toString());
			return false;
		}
		
	}
}
