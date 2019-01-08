package test.hedera.sdk.common;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaKeySignature;
import com.hedera.sdk.common.HederaSignature;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.common.HederaKey.KeyType;
import com.hedera.sdk.cryptography.HederaCryptoKeyPair;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

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

		HederaCryptoKeyPair keyPair = new HederaCryptoKeyPair(KeyType.ED25519);
		HederaKeySignature signature = Utilities.getKeySignature("Payload".getBytes(), keyPair);
		assertNotNull(signature.getSignature());
		
		signature = Utilities.getKeySignature("Payload".getBytes(), KeyType.ED25519, keyPair.getPublicKeyEncoded(), keyPair.getSecretKey());
		assertNotNull(signature.getSignature());
		
		HederaSignature sig = Utilities.getSignature("Payload".getBytes(), keyPair);
		assertNotNull(sig.getSignature());
		
		sig = Utilities.getSignature("Payload".getBytes(), KeyType.ED25519, keyPair.getPublicKeyEncoded(), keyPair.getSecretKey());
		assertNotNull(sig.getSignature());

	}
}
