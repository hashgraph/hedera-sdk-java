package test.hedera.sdk.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaTimeStamp;
import com.hedera.sdk.common.HederaTransactionID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaTransactionIDTest {

	protected static HederaAccountID blankAccountID = new HederaAccountID();
	protected static HederaAccountID accountID = new HederaAccountID(2, 3, 4);
	protected static HederaTimeStamp timeStamp = new HederaTimeStamp();
	
	@BeforeAll
	static void initAll() {
	}

	@Test
	@DisplayName("TransactionID Init with 1 param")
	void TestTransactionIDInit1Param() {
		HederaTransactionID transId = new HederaTransactionID(accountID);
		
		assertEquals(accountID.shardNum, transId.accountID.shardNum);
		assertEquals(accountID.realmNum, transId.accountID.realmNum);
		assertEquals(accountID.accountNum, transId.accountID.accountNum);
		// can't test this, time may not be the same at init
		//assertEquals(timeStamp.time, transId.transactionValidStart.time);
	}

	@Test
	@DisplayName("TransactionID Init with 2 params")
	void transactionIDInit2Params() {
		HederaTransactionID transId = new HederaTransactionID(accountID, timeStamp);
		
		assertEquals(accountID.accountNum, transId.accountID.accountNum);
		assertEquals(accountID.realmNum, transId.accountID.realmNum);
		assertEquals(accountID.shardNum, transId.accountID.shardNum);
		assertEquals(timeStamp.time, transId.transactionValidStart.time);
	}

	@Test
	@DisplayName("TransactionID Init with protobuf")
	void transactionIDInitProtobuf() {
		HederaTransactionID transId = new HederaTransactionID(accountID, timeStamp);
		HederaTransactionID transProto = new HederaTransactionID(transId.getProtobuf());
		
		assertEquals(transId.accountID.accountNum, transProto.accountID.accountNum);
		assertEquals(transId.accountID.realmNum, transProto.accountID.realmNum);
		assertEquals(transId.accountID.shardNum, transProto.accountID.shardNum);
		assertEquals(transId.transactionValidStart.time, transProto.transactionValidStart.time);
	}
}

