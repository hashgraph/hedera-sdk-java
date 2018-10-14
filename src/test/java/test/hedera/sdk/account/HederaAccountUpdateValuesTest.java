package test.hedera.sdk.account;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hedera.sdk.account.HederaAccountUpdateValues;

class HederaAccountUpdateValuesTest {

	@Test
	@DisplayName("HederaAccountUpdateValuesTest")
	void test() {
		HederaAccountUpdateValues values = new HederaAccountUpdateValues();
		
		assertNull(values.newKey);
		assertEquals(-1,values.proxyAccountShardNum);
		assertEquals(-1,values.proxyAccountRealmNum);
		assertEquals(-1,values.proxyAccountAccountNum);
		assertEquals(-1,values.proxyFraction);
		assertEquals(-1,values.sendRecordThreshold);
		assertEquals(-1,values.receiveRecordThreshold);
		assertEquals(-1,values.autoRenewPeriodSeconds);
		assertEquals(-1,values.autoRenewPeriosNanos);
		assertEquals(-1,values.expirationTimeSeconds);
		assertEquals(-1,values.expirationTimeNanos);
	}
}

