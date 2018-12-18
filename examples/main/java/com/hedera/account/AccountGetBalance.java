package com.hedera.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.account.HederaAccount;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

public final class AccountGetBalance {
	public static boolean getBalance(HederaAccount account) throws Exception {
		final Logger logger = LoggerFactory.getLogger(AccountGetBalance.class);

		 LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		  // print logback's internal status
		  StatusPrinter.print(lc);
		 		
		logger.debug("DEBUG");
		logger.info("");
		logger.info("CRYPTO GET BALANCE");
		logger.info("");
		
		// run a get balance query
		long balance = account.getBalance();
		if (balance != -1) {
			// it was successful, print it
			logger.info(String.format("===>Got balance=%d", balance));
			return true;
		} else if (account.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.info("system busy, try again later");
			return false;
		} else {
			// an error occurred
			logger.info("===>Getting balance - precheck ERROR");
			logger.info(account.getPrecheckResult().toString());
			return false;
		}
	}
}
