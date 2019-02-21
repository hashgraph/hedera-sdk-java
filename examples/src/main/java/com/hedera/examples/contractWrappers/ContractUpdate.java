package com.hedera.examples.contractWrappers;

import org.slf4j.LoggerFactory;

import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaTimeStamp;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class ContractUpdate {
	public static HederaContract update(HederaContract contract, HederaTimeStamp expirationTime, HederaDuration autoRenewDuration) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ContractUpdate.class);

		ExampleUtilities.showResult("**    CONTRACT UPDATE");
		
		// update the smart contract
		// smart contract update transaction
		HederaTransactionResult updateResult = contract.update(expirationTime, autoRenewDuration);
		// was it successful ?
		if (updateResult.getPrecheckResult() == ResponseCodeEnum.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt = Utilities.getReceipt(contract.hederaTransactionID,
					contract.txQueryDefaults.node);
			// was that successful ?
			if (receipt.transactionStatus == ResponseCodeEnum.SUCCESS) {
				// and print it out
				ExampleUtilities.showResult("**    Smart Contract update success");
			} else {
				ExampleUtilities.showResult("**    Failed with transactionStatus:" + receipt.transactionStatus);
				return null;
			}
		} else if (contract.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.debug("system busy, try again later");
			return null;
		} else {
			ExampleUtilities.showResult("**    getPrecheckResult not OK: " + updateResult.getPrecheckResult().name());
			return null;
		}
		return contract;
	}
}
