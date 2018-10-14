package test.hedera.sdk.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import com.hedera.sdk.common.HederaKey;
import com.hedera.sdk.common.HederaKey.KeyType;
import com.hedera.sdk.common.HederaKeyList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TestHederaKeyList {

	@Test
	@DisplayName("Checking key init")
	void testKeyListInit() {
		HederaKey key1 = new HederaKey(KeyType.ECDSA384, new byte[] {12,34,56}, "ECDSA");
		HederaKey key2 = new HederaKey(KeyType.ED25519, new byte[] {78,90,12}, "ED25519");
		List<HederaKey> keys = new ArrayList<HederaKey>();
		keys.add(key1);
		keys.add(key2);
		
		HederaKeyList masterKeyList = new HederaKeyList(keys);
		
		assertEquals(keys.size(), masterKeyList.keys.size());
		assertEquals(keys.get(0).uuid, masterKeyList.keys.get(0).uuid);
		assertEquals(keys.get(1).uuid, masterKeyList.keys.get(1).uuid);
		
		masterKeyList.addKey(key1);
		masterKeyList.addKey(key2);
		masterKeyList.addKey(KeyType.RSA3072, new byte[] {23, 45, 22});

		assertEquals(5, masterKeyList.keys.size());

		masterKeyList.deleteKey(key1);
		masterKeyList.deleteKey(key2);

		assertEquals(3, masterKeyList.keys.size());
		
		HederaKeyList protobufList = new HederaKeyList(masterKeyList.getProtobuf());
		assertEquals(masterKeyList.keys.size(), protobufList.keys.size());
		assertArrayEquals(masterKeyList.keys.get(0).getKey(), protobufList.keys.get(0).getKey());
		assertArrayEquals(masterKeyList.keys.get(1).getKey(), protobufList.keys.get(1).getKey());
		assertArrayEquals(masterKeyList.keys.get(2).getKey(), protobufList.keys.get(2).getKey());

		HederaKeyList jsonList = new HederaKeyList();
		jsonList.fromJSON(masterKeyList.JSON());
		assertEquals(masterKeyList.keys.size(), jsonList.keys.size());
		assertEquals(masterKeyList.keys.get(0).uuid, jsonList.keys.get(0).uuid);
		assertEquals(masterKeyList.keys.get(1).uuid, jsonList.keys.get(1).uuid);
		assertEquals(masterKeyList.keys.get(2).uuid, jsonList.keys.get(2).uuid);
		assertArrayEquals(masterKeyList.keys.get(0).getKey(), jsonList.keys.get(0).getKey());
		assertArrayEquals(masterKeyList.keys.get(1).getKey(), jsonList.keys.get(1).getKey());
		assertArrayEquals(masterKeyList.keys.get(2).getKey(), jsonList.keys.get(2).getKey());
		
		assertNotNull(masterKeyList.JSONString());
	}

}
