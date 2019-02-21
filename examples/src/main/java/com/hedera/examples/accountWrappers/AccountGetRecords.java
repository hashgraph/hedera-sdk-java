package com.hedera.examples.accountWrappers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaTransactionRecord;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class AccountGetRecords {
	public static boolean getRecords(HederaAccount account) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(AccountGetRecords.class);
		
		ExampleUtilities.showResult("**    CRYPTO GET RECORDS");

		// get records for the account
		List<HederaTransactionRecord> records = new ArrayList<HederaTransactionRecord>();
		
		records = account.getRecords();
		if (records != null) {
			for (HederaTransactionRecord record : records) {
				ExampleUtilities.showResult(String.format("**    Record transaction hash\n**    %s",record.transactionHash.toString()));
			}
			return true;
		} else if (account.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.debug("system busy, try again later");
			return false;
		} else {
			ExampleUtilities.showResult("**    Getting records - precheck ERROR" + account.getPrecheckResult().toString());
			return false;
		}
		
	}
}
