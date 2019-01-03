package test.hedera.sdk.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedera.sdk.common.HederaAccountID;
import com.hederahashgraph.api.proto.java.AccountID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaAccountIDTest {

	protected static HederaAccountID accountID1;
	protected static HederaAccountID accountID2;
	
	@BeforeAll
	static void initAll() {
		accountID1 = new HederaAccountID(1, 2, 3);
		AccountID accountID1Proto = accountID1.getProtobuf();
		accountID2 = new HederaAccountID(accountID1Proto);
	}

	@Test
	@DisplayName("Checking account creation details")
	void testAccountCreate() {
		assertEquals(1, accountID2.shardNum);
		assertEquals(2, accountID2.realmNum);
		assertEquals(3, accountID2.accountNum);
	}
	@Test
	@DisplayName("Checking matching account details from protobuf")
	void testAccountProto() {
		assertEquals(accountID1.accountNum, accountID2.accountNum);
		assertEquals(accountID1.realmNum, accountID2.realmNum);
		assertEquals(accountID1.shardNum, accountID2.shardNum);
	}
}
