package test.hedera.sdk.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hedera.sdk.account.HederaProxyStaker;

class HederaProxyStakerTest {

	@Test
	@DisplayName("HederaProxyStakerTest")
	void test() {
		HederaProxyStaker staker = new HederaProxyStaker();
		assertEquals(0, staker.shardNum);
		assertEquals(-1, staker.realmNum);
		assertEquals(1, staker.accountNum);
		assertEquals(0, staker.amount);

		staker = new HederaProxyStaker(1, 2, 3, 10);
		assertEquals(1, staker.shardNum);
		assertEquals(2, staker.realmNum);
		assertEquals(3, staker.accountNum);
		assertEquals(10, staker.amount);
		
		HederaProxyStaker staker2 = new HederaProxyStaker(staker.getProtobuf());

		assertEquals(staker.shardNum, staker2.shardNum);
		assertEquals(staker.realmNum, staker2.realmNum);
		assertEquals(staker.accountNum, staker2.accountNum);
		assertEquals(staker.amount, staker2.amount);
		
	}
}

