package com.hedera.examples.accountWrappers;

import org.slf4j.LoggerFactory;

import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.account.HederaAccount;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

import ch.qos.logback.classic.Logger;

public final class AccountGetBalance {
	public static boolean getBalance(HederaAccount account) throws Exception {
		final ch.qos.logback.classic.Logger logger = (Logger) LoggerFactory.getLogger(AccountGetBalance.class);

		ExampleUtilities.showResult("**    CRYPTO GET BALANCE");
		
		// run a get balance query
		long balance = account.getBalance();
		if (balance != -1) {
			// it was successful, print it
			ExampleUtilities.showResult(String.format("**    Got balance=%d", balance));
			return true;
		} else if (account.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.info("system busy, try again later");
			return false;
		} else {
			// an error occurred
			ExampleUtilities.showResult(String.format("**    Getting balance - precheck ERROR\n**    %s",account.getPrecheckResult().toString()));
			return false;
		}
	}
}
