package com.hedera.examples.accountWrappers;

import org.slf4j.LoggerFactory;

import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.node.HederaNodeList;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class AccountSend {
	public static boolean send(HederaAccount account, HederaAccount toAccount, long amount) throws Exception {
		final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(AccountSend.class);
		
		ExampleUtilities.showResult("**    CRYPTO TRANSFER");

		// make the transfer
		HederaTransactionResult transferResult = account.send(toAccount.getHederaAccountID(), amount);
		// was it successful ?
		if (transferResult.getPrecheckResult() == ResponseCodeEnum.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt  = Utilities.getReceipt(account.hederaTransactionID,  HederaNodeList.randomNode(), 10, 3000, 0);
			if (receipt.transactionStatus == ResponseCodeEnum.SUCCESS) {
				// if query successful, print it
				ExampleUtilities.showResult("**    Transfer successful");
				return true;
			} else {
				ExampleUtilities.showResult("**    Failed with transactionStatus:" + receipt.transactionStatus.toString());
				return false;
			}
		} else if (transferResult.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.debug("system busy, try again later");
			return false;
		} else {
			ExampleUtilities.showResult("**    Failed with getPrecheckResult:" + transferResult.getPrecheckResult().toString());
			return false;
		}
	}
}
