package com.hedera.sdk.transaction;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedera.sdk.common.HederaPrecheckResult;
import com.hedera.sdk.common.HederaTimeStamp;
import com.hedera.sdk.common.HederaTransactionID;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hederahashgraph.api.proto.java.NodeTransactionPrecheckCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaTransactionResultTest {

	@Test
	@DisplayName("TestHederaTransactionResult")
	void TestHederaTransactionResult() {
		HederaTransactionResult result = new HederaTransactionResult();
		
		HederaTransactionID trans = new HederaTransactionID();
		trans.accountID.shardNum = 1;
		trans.accountID.realmNum = 2;
		trans.accountID.accountNum = 3;
		trans.transactionValidStart = new HederaTimeStamp(100, 50);
		
		result.hederaTransactionID = trans;
		
		assertEquals(1, result.hederaTransactionID.accountID.shardNum);
		assertEquals(2, result.hederaTransactionID.accountID.realmNum);
		assertEquals(3, result.hederaTransactionID.accountID.accountNum);
		assertEquals(100, result.hederaTransactionID.transactionValidStart.seconds());
		assertEquals(50, result.hederaTransactionID.transactionValidStart.nanos());
		
		assertEquals(HederaPrecheckResult.OK, result.getPrecheckResult());
		
		result.setPrecheckResult(NodeTransactionPrecheckCode.DUPLICATE);
		assertEquals(HederaPrecheckResult.DUPLICATE, result.getPrecheckResult());
		assertEquals("DUPLICATE TRANSACTION", result.errorText());

		result.setPrecheckResult(NodeTransactionPrecheckCode.INSUFFICIENT_BALANCE);
		assertEquals(HederaPrecheckResult.INSUFFICIENT_BALANCE, result.getPrecheckResult());
		assertEquals("INSUFFICIENT BALANCE", result.errorText());
		
		result.setPrecheckResult(NodeTransactionPrecheckCode.INSUFFICIENT_FEE);
		assertEquals(HederaPrecheckResult.INSUFFICIENT_FEE, result.getPrecheckResult());
		assertEquals("INSUFFICIENT FEE", result.errorText());
		
		result.setPrecheckResult(NodeTransactionPrecheckCode.INVALID_ACCOUNT);
		assertEquals(HederaPrecheckResult.INVALID_ACCOUNT, result.getPrecheckResult());
		assertEquals("INVALID ACCOUNT", result.errorText());

		result.setPrecheckResult(NodeTransactionPrecheckCode.INVALID_TRANSACTION);
		assertEquals(HederaPrecheckResult.INVALID_TRANSACTION, result.getPrecheckResult());
		assertEquals("INVALID TRANSACTION", result.errorText());

		result.setError();
		assertEquals(HederaPrecheckResult.ERROR, result.getPrecheckResult());
		assertEquals("ERROR", result.errorText());

		result.setPrecheckResult(NodeTransactionPrecheckCode.OK);
		assertTrue(result.success());
		assertEquals("OK", result.errorText());
		
	}
}
