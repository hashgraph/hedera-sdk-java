package com.hedera.examples.accountWrappers;

import org.slf4j.LoggerFactory;

import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.account.HederaClaim;
import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class AccountAddClaim {
	public static boolean addClaim(HederaAccount account, HederaClaim claim, HederaKeyPair claimKeyPair) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(AccountAddClaim.class);

		ExampleUtilities.showResult("**    CRYPTO ADD CLAIM");

    	// add the claim
		HederaTransactionResult claimAddResult = account.addClaim(claim, claimKeyPair);
		// was it successful ?
		if (claimAddResult.getPrecheckResult() == ResponseCodeEnum.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt  = Utilities.getReceipt(account.hederaTransactionID,  account.txQueryDefaults.node);
			if (receipt.transactionStatus == ResponseCodeEnum.SUCCESS) {
				// if query successful, print it
				ExampleUtilities.showResult("**    Claim addition successful");
				return true;
			} else {
				ExampleUtilities.showResult("**    Failed with transactionStatus:" + receipt.transactionStatus);
				return false;
			}
		} else if (account.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.debug("system busy, try again later");
			return false;
		} else {
			ExampleUtilities.showResult("**    Failed with getPrecheckResult:" + claimAddResult.getPrecheckResult());
			return false;
		}
	}
}
