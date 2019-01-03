package test.hedera.sdk.common;

import static org.junit.Assert.assertEquals;

import com.hedera.sdk.common.HederaKeyUUIDDescription;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaKeyUUIDDescriptionTest {
	protected static String uuid = "UUID!@£!£";
	protected static String description = "A Description";
	protected static HederaKeyUUIDDescription key1 = new HederaKeyUUIDDescription();
	protected static HederaKeyUUIDDescription key2 = new HederaKeyUUIDDescription(uuid, description);
	
	@BeforeAll
	static void initAll() {
	}

	@Test
	@DisplayName("Init no parameters")
	void testKeyInit() { 
		assertEquals("", key1.description);
		assertEquals("", key1.uuid);
	}

	@Test
	@DisplayName("Init with parameters")
	void testKeyInitWithParam() { 
		assertEquals(description, key2.description);
		assertEquals(uuid, key2.uuid);
	}
}
