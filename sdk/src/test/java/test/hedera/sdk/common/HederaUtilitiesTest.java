package test.hedera.sdk.common;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.Utilities;

public class HederaUtilitiesTest {

	@Test
	@DisplayName("Testing Utilities")
	void testUtilities() throws Exception {
		HederaAccountID accountID = new HederaAccountID(1, 2, 3);
		byte[] serialise;
		
		try {
			serialise = Utilities.serialize(accountID);
			HederaAccountID newAccountID = (HederaAccountID) Utilities.deserialize(serialise);
			assertEquals(accountID.shardNum, newAccountID.shardNum);
			assertEquals(accountID.realmNum, newAccountID.realmNum);
			assertEquals(accountID.accountNum, newAccountID.accountNum);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		long testRandom = -1;
		testRandom = Utilities.getLongRandom();
		assertNotEquals(-1, testRandom);

	}
}
