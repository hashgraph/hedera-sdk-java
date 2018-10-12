package com.hedera.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.account.HederaClaim;
import com.hedera.sdk.common.HederaPrecheckResult;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.HederaTransactionStatus;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.cryptography.HederaCryptoKeyPair;
import com.hedera.sdk.transaction.HederaTransactionResult;

public final class AccountAddClaim {
	public static boolean addClaim(HederaAccount account, HederaClaim claim, HederaCryptoKeyPair claimKeyPair) throws Exception {
		final Logger logger = LoggerFactory.getLogger(AccountAddClaim.class);

		logger.info("");
		logger.info("# CRYPTO ADD CLAIM");
		logger.info("");

    	// add the claim
		HederaTransactionResult claimAddResult = account.addClaim(claim, claimKeyPair);
		// was it successful ?
		if (claimAddResult.getPrecheckResult() == HederaPrecheckResult.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt  = Utilities.getReceipt(account.hederaTransactionID,  account.txQueryDefaults.node);
			if (receipt.transactionStatus == HederaTransactionStatus.SUCCESS) {
				// if query successful, print it
				logger.info("===>Claim addition successful");
			} else {
				logger.info("Failed with transactionStatus:" + receipt.transactionStatus);
				return false;
			}
		} else {
			logger.info("Failed with getPrecheckResult:" + claimAddResult.getPrecheckResult());
			return false;
		}
		return true;
	}
}
