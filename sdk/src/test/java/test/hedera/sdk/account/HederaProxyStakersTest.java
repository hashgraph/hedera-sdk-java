package test.hedera.sdk.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hedera.sdk.account.HederaProxyStaker;
import com.hedera.sdk.account.HederaProxyStakers;
import com.hederahashgraph.api.proto.java.AllProxyStakers;

class HederaProxyStakersTest {

	@Test
	@DisplayName("HederaProxyStakersTest")
	void test() {
		HederaProxyStakers stakers = new HederaProxyStakers();
		assertEquals(0, stakers.shardNum);
		assertEquals(0,  stakers.realmNum);
		assertEquals(0, stakers.accountNum);
		assertEquals(0, stakers.proxyStakers.size());

		stakers = new HederaProxyStakers(2, 3, 4);
		assertEquals(2, stakers.shardNum);
		assertEquals(3,  stakers.realmNum);
		assertEquals(4, stakers.accountNum);
		assertEquals(0, stakers.proxyStakers.size());

		HederaProxyStaker staker = new HederaProxyStaker(1, 2, 3, 1);
		HederaProxyStaker staker2 = new HederaProxyStaker(4, 5, 6, 10);
		stakers.addProxyStaker(staker);
		stakers.addProxyStaker(staker2);
		assertEquals(2,  stakers.proxyStakers.size());

		AllProxyStakers allStakers = stakers.getProtobuf();
		
		HederaProxyStakers newStakers = new HederaProxyStakers(allStakers);
		assertEquals(stakers.shardNum, newStakers.shardNum);
		assertEquals(stakers.realmNum,  newStakers.realmNum);
		assertEquals(stakers.accountNum, newStakers.accountNum);
		assertEquals(stakers.proxyStakers.size(), newStakers.proxyStakers.size());
		assertEquals(stakers.proxyStakers.size(), newStakers.proxyStakers.size());
		
	}
}