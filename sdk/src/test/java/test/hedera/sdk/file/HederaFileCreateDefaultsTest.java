package test.hedera.sdk.file;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.file.HederaFileCreateDefaults;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaFileCreateDefaultsTest {
	
	@Test
	@DisplayName("Checking file creation defaults")
	void testAccountCreate() {
		HederaFileCreateDefaults defaults = new HederaFileCreateDefaults();
		
		assertNotNull(defaults.expirationTimeSeconds);
		assertEquals(0, defaults.expirationTimeNanos);
		
		HederaKeyPair key = new HederaKeyPair(KeyType.ED25519);
		
		defaults.setNewRealmAdminPublicKey(key.getKeyType(), key.getPublicKeyEncoded());
		assertNotNull(defaults.getNewRealmAdminPublicKey());
		assertArrayEquals(key.getPublicKeyEncoded(), defaults.getNewRealmAdminPublicKey().getPublicKeyEncoded());
		
	}
}
