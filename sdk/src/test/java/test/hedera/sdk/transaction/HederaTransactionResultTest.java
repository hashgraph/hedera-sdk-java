package test.hedera.sdk.transaction;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedera.sdk.common.HederaTimeStamp;
import com.hedera.sdk.common.HederaTransactionID;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

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
		
		assertEquals(ResponseCodeEnum.OK, result.getPrecheckResult());
		
		result.setPrecheckResult(ResponseCodeEnum.FAIL_BALANCE);
		assertEquals(ResponseCodeEnum.FAIL_BALANCE, result.getPrecheckResult());

		result.setPrecheckResult(ResponseCodeEnum.OK);
		assertTrue(result.success());
	}
}
