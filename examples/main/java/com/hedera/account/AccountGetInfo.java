package com.hedera.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.account.HederaAccount;

public final class AccountGetInfo {
	public static void getInfo(HederaAccount account) {
		final Logger logger = LoggerFactory.getLogger(AccountGetInfo.class);
		
		logger.info("");
		logger.info("CRYPTO GET INFO");
		logger.info("");

		try {
			// get info for the account
			if (account.getInfo()) {
				logger.info("===>Got info");
			} else {
				logger.info("===>Getting info - precheck ERROR" + account.getPrecheckResult().toString());
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
