package test.hedera.sdk.contract;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hedera.sdk.contract.HederaContractLogInfo;

class HederaContractLogInfoTest {

	@Test
	@DisplayName("TestHederaContractLogInfo")
	void test() {
		// this is tested in TestHederaRecord already, adding simple test here
		HederaContractLogInfo info = new HederaContractLogInfo();
	}
}
