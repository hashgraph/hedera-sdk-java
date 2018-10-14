package com.hedera.contracts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaPrecheckResult;
import com.hedera.sdk.common.HederaTimeStamp;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.HederaTransactionStatus;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.transaction.HederaTransactionResult;

public final class ContractUpdate {
	public static HederaContract update(HederaContract contract, HederaTimeStamp expirationTime, HederaDuration autoRenewDuration) throws Exception {
		final Logger logger = LoggerFactory.getLogger(ContractUpdate.class);
		logger.info("");
		logger.info("CONTRACT UPDATE");
		logger.info("");

		// update the smart contract
		// smart contract update transaction
		HederaTransactionResult updateResult = contract.update(expirationTime, autoRenewDuration);
		// was it successful ?
		if (updateResult.getPrecheckResult() == HederaPrecheckResult.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt = Utilities.getReceipt(contract.hederaTransactionID,
					contract.txQueryDefaults.node);
			// was that successful ?
			if (receipt.transactionStatus == HederaTransactionStatus.SUCCESS) {
				// and print it out
				logger.info(String.format("===>Smart Contract update success"));
			} else {
				logger.info("Failed with transactionStatus:" + receipt.transactionStatus);
				return null;
			}
		} else {
			logger.info("getPrecheckResult not OK: " + updateResult.getPrecheckResult().name());
			return null;
		}
		return contract;
	}
}
