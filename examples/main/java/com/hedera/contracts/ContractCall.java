package com.hedera.contracts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.common.HederaPrecheckResult;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.HederaTransactionStatus;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.transaction.HederaTransactionResult;

public final class ContractCall {
	public static void call(HederaContract contract, long gas, long amount, byte[] functionParameters) throws Exception {
		final Logger logger = LoggerFactory.getLogger(ContractCall.class);
		logger.info("");
		logger.info("CONTRACT CALL");
		logger.info("");

		// call the smart contract
		// smart contract call transaction
		HederaTransactionResult callResult = contract.call(gas, amount, functionParameters);
		// was it successful ?
		if (callResult.getPrecheckResult() == HederaPrecheckResult.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt = Utilities.getReceipt(contract.hederaTransactionID,
					contract.txQueryDefaults.node);
			// was that successful ?
			if (receipt.transactionStatus == HederaTransactionStatus.SUCCESS) {
				// and print it out
				logger.info(String.format("===>Smart Contract call success"));
			} else {
				logger.info("Failed with transactionStatus:" + receipt.transactionStatus.toString());
			}
		}
	}

}
