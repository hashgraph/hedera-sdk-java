package test.hedera.sdk.contract;

import static org.junit.Assert.assertArrayEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hedera.sdk.contract.HederaContractFunctionResult;

class HederaContractFunctionResultTest {

	@Test
	@DisplayName("TestHederaContractFunctionResult")
	void test() {
		// this is tested in TestHederaRecord already, adding a few tests here

		HederaContractFunctionResult result = new HederaContractFunctionResult();
		assertArrayEquals(new byte[0], result.contractCallResult());
	}

}
