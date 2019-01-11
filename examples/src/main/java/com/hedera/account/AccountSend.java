package com.hedera.account;

public final class AccountSend {
	public static boolean send(HederaAccount account, HederaAccount toAccount, long amount) throws Exception {
		final Logger logger = LoggerFactory.getLogger(AccountSend.class);
		
		logger.info("");
		logger.info("CRYPTO TRANSFER");
		logger.info("");

		// make the transfer
		HederaTransactionResult transferResult = account.send(toAccount.getHederaAccountID(), amount);
		// was it successful ?
		if (transferResult.getPrecheckResult() == ResponseCodeEnum.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt  = Utilities.getReceipt(account.hederaTransactionID,  account.txQueryDefaults.node);
			if (receipt.transactionStatus == ResponseCodeEnum.SUCCESS) {
				// if query successful, print it
				ExampleUtilities.showResult("**   Transfer successful");
				return true;
			} else {
				ExampleUtilities.showResult(String.format("**   Failed with transactionStatus: %s", receipt.transactionStatus.toString()));
				return false;
			}
		} else if (transferResult.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.info("system busy, try again later");
			return false;
		} else {
			ExampleUtilities.showResult(String.format("**   Failed with getPrecheckResult: %s", transferResult.getPrecheckResult().toString()));
			return false;
		}
	}
}
