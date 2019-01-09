package test.hedera.sdk.account;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hedera.sdk.account.HederaAccountAmount;
import com.hedera.sdk.common.HederaAccountID;
import com.hederahashgraph.api.proto.java.AccountAmount;

public class HederaAccountAmountTest {

	@Test
	@DisplayName("HederaAccountAmountTest")
	void test() {
		HederaAccountAmount amount = new HederaAccountAmount();
		assertEquals(0, amount.shardNum);
		assertEquals(0, amount.realmNum);
		assertEquals(0, amount.accountNum);;
		assertEquals(0, amount.amount);

		amount = new HederaAccountAmount(2,3,4,10);
		assertEquals(2, amount.shardNum);
		assertEquals(3, amount.realmNum);
		assertEquals(4, amount.accountNum);;
		assertEquals(10, amount.amount);

		HederaAccountID accountID = new HederaAccountID(5, 6, 7);
		amount = new HederaAccountAmount(accountID,10);
		assertEquals(5, amount.shardNum);
		assertEquals(6, amount.realmNum);
		assertEquals(7, amount.accountNum);;
		assertEquals(10, amount.amount);
		
		AccountAmount protoBuf = amount.getProtobuf();
		
		HederaAccountAmount amount2 = new HederaAccountAmount(protoBuf);
		assertEquals(amount.shardNum, amount2.shardNum);
		assertEquals(amount.realmNum, amount2.realmNum);
		assertEquals(amount.accountNum, amount2.accountNum);
		assertEquals(amount.amount, amount2.amount);
	}
}