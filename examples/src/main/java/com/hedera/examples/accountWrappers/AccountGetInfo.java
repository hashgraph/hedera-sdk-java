package com.hedera.examples.accountWrappers;

import org.slf4j.LoggerFactory;

import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.account.HederaAccount;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class AccountGetInfo {
	public static boolean getInfo(HederaAccount account) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(AccountGetInfo.class);
		
		ExampleUtilities.showResult("**    CRYPTO GET INFO");

		// get info for the account
		if (account.getInfo()) {
			ExampleUtilities.showResult("**    Got info");
			return true;
		} else if (account.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.info("system busy, try again later");
			return false;
		} else {
			ExampleUtilities.showResult("**    Getting info - precheck ERROR" + account.getPrecheckResult().toString());
			return false;
		}
	}
}
