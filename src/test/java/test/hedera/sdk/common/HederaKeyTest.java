package test.hedera.sdk.common;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.hedera.sdk.common.HederaContractID;
import com.hedera.sdk.common.HederaKey;
import com.hedera.sdk.common.HederaKeyList;
import com.hedera.sdk.common.HederaKeyThreshold;
import com.hedera.sdk.common.HederaKey.KeyType;
import com.hedera.sdk.node.HederaNode;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HederaKeyTest {
	protected static byte[] keyBytes = new byte[] {12};
	protected static String description = "A Description";
	protected static HederaContractID contractID = new HederaContractID(1,2,3);
	protected static List<HederaKey> keyList = new ArrayList<HederaKey>();
	protected static HederaKeyThreshold thresholdKey;
	protected static int threshold = 10;
	protected static HederaKeyList hederaKeyList = new HederaKeyList();

	@BeforeAll
	static void initAll() {
		keyList.add(new HederaKey(KeyType.ED25519, keyBytes));
		thresholdKey = new HederaKeyThreshold(threshold, keyList);
		hederaKeyList.addKey(new HederaKey(KeyType.ED25519, keyBytes));
	}
	
	@ParameterizedTest
	@DisplayName("Checking RSA, ED and EC key init")
	@MethodSource("keyInit")
	void testKeyInit(HederaKey masterKey, KeyType keyType, String description) { 
		assertEquals(keyType, masterKey.getKeyType());
		assertArrayEquals(keyBytes, masterKey.getKey());
		assertEquals(description, masterKey.keyDescription);
		assertNotEquals(null, masterKey.uuid);

		HederaKey protobufKey = new HederaKey(masterKey.getProtobuf());
		assertEquals(masterKey.getKeyType(), protobufKey.getKeyType());
		assertArrayEquals(masterKey.getKey(), protobufKey.getKey());

		HederaKey jsonKey = new HederaKey();
		jsonKey.fromJSON(masterKey.JSON());
		assertEquals(masterKey.getKeyType(), jsonKey.getKeyType());
		assertEquals(masterKey.uuid, jsonKey.uuid);
		assertEquals(masterKey.keyDescription, jsonKey.keyDescription);
		assertArrayEquals(masterKey.getKey(), jsonKey.getKey());
	}
	 
	private static Stream<Arguments> keyInit() {
		return Stream.of(
			Arguments.of(new HederaKey(KeyType.ECDSA384, keyBytes), KeyType.ECDSA384, ""),
			Arguments.of(new HederaKey(KeyType.RSA3072, keyBytes), KeyType.RSA3072, ""),
			Arguments.of(new HederaKey(KeyType.ED25519, keyBytes), KeyType.ED25519, ""),
			Arguments.of(new HederaKey(KeyType.ECDSA384, keyBytes, description), KeyType.ECDSA384, description),
			Arguments.of(new HederaKey(KeyType.RSA3072, keyBytes, description), KeyType.RSA3072, description),
			Arguments.of(new HederaKey(KeyType.ED25519, keyBytes, description), KeyType.ED25519, description)
		);
	}
	
	@ParameterizedTest
	@DisplayName("Checking CONTRACTID key init")
	@MethodSource("keyInitCONTRACTID")
	void testKeyInitContractID(HederaKey masterKey, String description) { 
		assertEquals(KeyType.CONTRACT, masterKey.getKeyType());
		assertEquals(null, masterKey.getKey());
		assertEquals(contractID.contractNum, masterKey.getContractIDKey().contractNum);
		assertEquals(contractID.shardNum, masterKey.getContractIDKey().shardNum);
		assertEquals(contractID.realmNum, masterKey.getContractIDKey().realmNum);
		assertEquals(description, masterKey.keyDescription);
		assertNotEquals(null, masterKey.uuid);

		HederaKey protobufKey = new HederaKey(masterKey.getProtobuf());
		assertEquals(null, protobufKey.getKey());
		assertEquals(KeyType.CONTRACT, protobufKey.getKeyType());
		assertEquals(contractID.contractNum, protobufKey.getContractIDKey().contractNum);
		assertEquals(contractID.shardNum, protobufKey.getContractIDKey().shardNum);
		assertEquals(contractID.realmNum, protobufKey.getContractIDKey().realmNum);

		HederaKey jsonKey = new HederaKey();
		jsonKey.fromJSON(masterKey.JSON());
		assertEquals(jsonKey.getKey(), null);
		assertEquals(KeyType.CONTRACT, jsonKey.getKeyType());
		assertEquals(contractID.contractNum, jsonKey.getContractIDKey().contractNum);
		assertEquals(contractID.shardNum, jsonKey.getContractIDKey().shardNum);
		assertEquals(contractID.realmNum, jsonKey.getContractIDKey().realmNum);
		assertEquals(description, jsonKey.keyDescription);
		assertEquals(masterKey.uuid, jsonKey.uuid);
	}
	 
	private static Stream<Arguments> keyInitCONTRACTID() {
		return Stream.of(
			Arguments.of(new HederaKey(contractID), ""),
			Arguments.of(new HederaKey(contractID, description), description)
		);
	}

	@ParameterizedTest
	@DisplayName("Checking THRESHOLD key init")
	@MethodSource("keyInitTHRESHOLD")
	void testKeyInitThreshold(HederaKey masterKey, String description) { 
		assertEquals(KeyType.THRESHOLD, masterKey.getKeyType());
		assertEquals(description,  masterKey.keyDescription);
		assertEquals(threshold, masterKey.getThresholdKey().threshold);
		assertEquals(null, masterKey.getKey());
		assertEquals(masterKey.getThresholdKey().keys.get(0).keyDescription, thresholdKey.keys.get(0).keyDescription);
		assertEquals(masterKey.getThresholdKey().keys.get(0).getKeyType(), thresholdKey.keys.get(0).getKeyType());
		assertEquals(masterKey.getThresholdKey().keys.get(0).uuid, thresholdKey.keys.get(0).uuid);
		assertNotEquals(null, masterKey.uuid);

		// compare keys
		assertEquals(masterKey.getKey(), null);
		assertArrayEquals(keyBytes, masterKey.getThresholdKey().keys.get(0).getKey());

		HederaKey protobufKey = new HederaKey(masterKey.getProtobuf());
		assertEquals(masterKey.getKeyType(), protobufKey.getKeyType());
		assertArrayEquals(masterKey.getKey(), protobufKey.getKey());
		assertEquals(masterKey.getThresholdKey().threshold, thresholdKey.threshold);
		assertEquals(thresholdKey.keys.get(0).getKeyType(), protobufKey.getThresholdKey().keys.get(0).getKeyType());
		assertEquals(null, protobufKey.getKey());
		assertArrayEquals(keyBytes, protobufKey.getThresholdKey().keys.get(0).getKey());

		HederaKey jsonKey = new HederaKey();
		jsonKey.fromJSON(masterKey.JSON());
		assertEquals(masterKey.getKeyType(), jsonKey.getKeyType());
		assertArrayEquals(masterKey.getKey(), jsonKey.getKey());
		assertEquals(description,  jsonKey.keyDescription);
		// compare threshold key
		assertEquals(threshold, jsonKey.getThresholdKey().threshold);
		assertEquals(thresholdKey.keys.get(0).keyDescription, jsonKey.getThresholdKey().keys.get(0).keyDescription);
		assertEquals(thresholdKey.keys.get(0).getKeyType(), jsonKey.getThresholdKey().keys.get(0).getKeyType());
		// compare keys
		assertEquals(null, jsonKey.getKey());
		assertArrayEquals(keyBytes, jsonKey.getThresholdKey().keys.get(0).getKey());
		assertEquals(thresholdKey.keys.get(0).uuid, jsonKey.getThresholdKey().keys.get(0).uuid);
	}
	 
	private static Stream<Arguments> keyInitTHRESHOLD() {
		return Stream.of(
			Arguments.of(new HederaKey(thresholdKey), ""),
			Arguments.of(new HederaKey(thresholdKey, description), description)
		);
	}

	@ParameterizedTest
	@DisplayName("Checking KEYLIST key init")
	@MethodSource("keyInitKEYLIST")
	void testKeyInitList(HederaKey masterKey, String description) { 
		// check key type
		assertEquals(KeyType.LIST, masterKey.getKeyType());
		assertEquals(description,  masterKey.keyDescription);
		assertEquals(null, masterKey.getKey());
		assertNotEquals(null, masterKey.uuid);
		assertEquals(description, masterKey.keyDescription);

		// compare keylist
		assertEquals(masterKey.getKeyList().keys.get(0).getKeyType(), hederaKeyList.keys.get(0).getKeyType());
		
		// compare keys
		assertEquals(null, masterKey.getKey());
		assertArrayEquals(masterKey.getKeyList().keys.get(0).getKey(), hederaKeyList.keys.get(0).getKey());

		HederaKey protobufKey = new HederaKey(masterKey.getProtobuf());
		assertEquals(masterKey.getKeyType(), protobufKey.getKeyType());
		assertArrayEquals(masterKey.getKey(), protobufKey.getKey());
		// compare keylist
		assertEquals(hederaKeyList.keys.get(0).getKeyType(), protobufKey.getKeyList().keys.get(0).getKeyType());
		assertArrayEquals(hederaKeyList.keys.get(0).getKey(), protobufKey.getKeyList().keys.get(0).getKey());

		HederaKey jsonKey = new HederaKey();
		jsonKey.fromJSON(masterKey.JSON());
		assertEquals(masterKey.getKeyType(), jsonKey.getKeyType());
		assertEquals(hederaKeyList.keys.get(0).getKeyType(), jsonKey.getKeyList().keys.get(0).getKeyType());
		assertEquals(null, jsonKey.getKey());
		assertArrayEquals(hederaKeyList.keys.get(0).getKey(), jsonKey.getKeyList().keys.get(0).getKey());
		assertEquals(description, masterKey.keyDescription);
	}
	 
	private static Stream<Arguments> keyInitKEYLIST() {
		return Stream.of(
			Arguments.of(new HederaKey(hederaKeyList), ""),
			Arguments.of(new HederaKey(hederaKeyList, description), description)
		);
	}
	
	@Test
	@DisplayName("HederaKey init tests")
	void HederaKeyInit() {
		HederaKey key = new HederaKey();
		HederaNode node = new HederaNode("localhost",10);
		key.setNode(node);
		
		assertEquals(0, key.getCost());
		assertArrayEquals(new byte[0], key.getStateProof());
		assertEquals(node.getHost(), key.getNode().getHost());
		assertEquals(node.getPort(), key.getNode().getPort());
		
	}	
}
