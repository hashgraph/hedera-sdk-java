package com.hedera.examples.contractWrappers;

import org.slf4j.LoggerFactory;

import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaFileID;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.HederaTransactionRecord;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class ContractCreate {
	public static HederaContract create(HederaContract contract, HederaFileID fileID, long gas, long initialBalance) throws Exception {
		return create(contract, fileID, gas, initialBalance, new byte[0]); 
	}
	
	public static HederaContract create(HederaContract contract, HederaFileID fileID, long gas, long initialBalance, byte[] constParams) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaContract.class);
		// new contract
		long shardNum = 0;
		long realmNum = 0;
		byte[] constructorParameters = constParams; 
		HederaDuration autoRenewPeriod = new HederaDuration(60); // auto renew every minute

		contract.txQueryDefaults.generateRecord = true;
		
		ExampleUtilities.showResult("**    CONTRACT CREATE");

		// create the new contract
		// contract creation transaction
		HederaTransactionResult createResult = contract.create(shardNum, realmNum, fileID, initialBalance, gas,
				constructorParameters, autoRenewPeriod);
		// was it successful ?
		if (createResult.getPrecheckResult() == ResponseCodeEnum.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt = Utilities.getReceipt(contract.hederaTransactionID,
					contract.txQueryDefaults.node, 10, 4000, 0);
			// was that successful ?
			if (receipt.transactionStatus == ResponseCodeEnum.SUCCESS) {
				contract.contractNum = receipt.contractID.contractNum;
				// and print it out
				ExampleUtilities.showResult(String.format("**    Your new contract number is %d", contract.contractNum));
				HederaTransactionRecord record = new HederaTransactionRecord(contract.hederaTransactionID, contract.txQueryDefaults.node.contractGetRecordsQueryFee, contract.txQueryDefaults);
			} else {
				ExampleUtilities.showResult("**    Failed with transactionStatus:" + receipt.transactionStatus.toString());
				return null;
			}
		} else if (contract.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.debug("system busy, try again later");
			return null;
		} else {
			ExampleUtilities.showResult("**    getPrecheckResult not OK: " + createResult.getPrecheckResult().name());
			return null;
		}
		return contract;
	}
}
