package com.hedera.examples.accountWrappers;

import org.slf4j.LoggerFactory;

import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.account.HederaAccountUpdateValues;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class AccountUpdate {
	public static HederaAccount update(HederaAccount account, HederaAccountUpdateValues updates) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(AccountUpdate.class);

		ExampleUtilities.showResult("**    CRYPTO UPDATE ACCOUNT");

		// perform the update
		HederaTransactionResult updateResult = account.update(updates);
		// was it successful ?
		if (updateResult.getPrecheckResult() == ResponseCodeEnum.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt  = Utilities.getReceipt(account.hederaTransactionID,  account.txQueryDefaults.node);
			ExampleUtilities.showResult("**    Ran Query for receipt");
			if (receipt.transactionStatus == ResponseCodeEnum.SUCCESS) {
				// if successful, print it
				ExampleUtilities.showResult("**    Update successful");
				// update acount keys if necessary
				if (updates.newKey != null) {
					account.accountKey = updates.newKey;
				}
			} else {
				ExampleUtilities.showResult("**    Failed with transactionStatus:" + receipt.transactionStatus.toString());
				return null;
			}
		} else if (updateResult.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.info("system busy, try again later");
			return null;
		} else {
			ExampleUtilities.showResult("**    Failed with getPrecheckResult:" + updateResult.getPrecheckResult().toString());
			return null;
		}
		return account;
	}
}
