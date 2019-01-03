package test.hedera.sdk.common;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.hedera.sdk.common.HederaContractID;
import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyList;
import com.hedera.sdk.common.HederaKeyThreshold;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.node.HederaNode;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HederaKeyTest {
	protected static byte[] keyBytes;
	protected static String description = "A Description";
	protected static HederaContractID contractID = new HederaContractID(1,2,3);
	protected static List<HederaKeyPair> keyList = new ArrayList<HederaKeyPair>();
	protected static HederaKeyThreshold thresholdKey;
	protected static int threshold = 10;
	protected static HederaKeyList hederaKeyList = new HederaKeyList();

	@BeforeAll
	static void initAll() {
		HederaKeyPair key = new HederaKeyPair(KeyType.ED25519);
		keyBytes = key.getPublicKey();
		keyList.add(key);
		thresholdKey = new HederaKeyThreshold(threshold, keyList);
		hederaKeyList.addKey(key);
	}
	
	@ParameterizedTest
	@DisplayName("Checking RSA, ED and EC key init")
	@MethodSource("keyInit")
	void testKeyInit(HederaKeyPair masterKey, KeyType keyType, String description) { 
		assertEquals(keyType, masterKey.getKeyType());
		assertArrayEquals(keyBytes, masterKey.getPublicKey());
		assertEquals(description, masterKey.keyDescription);
		assertNotEquals(null, masterKey.uuid);

		HederaKeyPair protobufKey = new HederaKeyPair(masterKey.getProtobuf());
		assertEquals(masterKey.getKeyType(), protobufKey.getKeyType());
		assertArrayEquals(masterKey.getPublicKey(), protobufKey.getPublicKey());

		HederaKeyPair jsonKey = new HederaKeyPair();
		jsonKey.fromJSON(masterKey.JSON());
		assertEquals(masterKey.getKeyType(), jsonKey.getKeyType());
		assertEquals(masterKey.uuid, jsonKey.uuid);
		assertEquals(masterKey.keyDescription, jsonKey.keyDescription);
		assertArrayEquals(masterKey.getPublicKey(), jsonKey.getPublicKey());
	}
	 
	private static Stream<Arguments> keyInit() {
		return Stream.of(
			Arguments.of(new HederaKeyPair(KeyType.ED25519, keyBytes, null), KeyType.ED25519, ""),
			Arguments.of(new HederaKeyPair(KeyType.ED25519, keyBytes, null), KeyType.ED25519, ""),
			Arguments.of(new HederaKeyPair(KeyType.ED25519, keyBytes, null), KeyType.ED25519, ""),
			Arguments.of(new HederaKeyPair(KeyType.ED25519, keyBytes, null, description), KeyType.ED25519, description),
			Arguments.of(new HederaKeyPair(KeyType.ED25519, keyBytes, null, description), KeyType.ED25519, description),
			Arguments.of(new HederaKeyPair(KeyType.ED25519, keyBytes, null, description), KeyType.ED25519, description)
		);
	}
	
	@ParameterizedTest
	@DisplayName("Checking CONTRACTID key init")
	@MethodSource("keyInitCONTRACTID")
	void testKeyInitContractID(HederaKeyPair masterKey, String description) { 
		assertEquals(KeyType.CONTRACT, masterKey.getKeyType());
		assertEquals(null, masterKey.getPublicKey());
		assertEquals(contractID.contractNum, masterKey.getContractIDKey().contractNum);
		assertEquals(contractID.shardNum, masterKey.getContractIDKey().shardNum);
		assertEquals(contractID.realmNum, masterKey.getContractIDKey().realmNum);
		assertEquals(description, masterKey.keyDescription);
		assertNotEquals(null, masterKey.uuid);

		HederaKeyPair protobufKey = new HederaKeyPair(masterKey.getProtobuf());
		assertEquals(null, protobufKey.getPublicKey());
		assertEquals(KeyType.CONTRACT, protobufKey.getKeyType());
		assertEquals(contractID.contractNum, protobufKey.getContractIDKey().contractNum);
		assertEquals(contractID.shardNum, protobufKey.getContractIDKey().shardNum);
		assertEquals(contractID.realmNum, protobufKey.getContractIDKey().realmNum);

		HederaKeyPair jsonKey = new HederaKeyPair();
		jsonKey.fromJSON(masterKey.JSON());
		assertEquals(jsonKey.getPublicKey(), null);
		assertEquals(KeyType.CONTRACT, jsonKey.getKeyType());
		assertEquals(contractID.contractNum, jsonKey.getContractIDKey().contractNum);
		assertEquals(contractID.shardNum, jsonKey.getContractIDKey().shardNum);
		assertEquals(contractID.realmNum, jsonKey.getContractIDKey().realmNum);
		assertEquals(description, jsonKey.keyDescription);
		assertEquals(masterKey.uuid, jsonKey.uuid);
	}
	 
	private static Stream<Arguments> keyInitCONTRACTID() {
		return Stream.of(
			Arguments.of(new HederaKeyPair(contractID), ""),
			Arguments.of(new HederaKeyPair(contractID, description), description)
		);
	}

	@ParameterizedTest
	@DisplayName("Checking THRESHOLD key init")
	@MethodSource("keyInitTHRESHOLD")
	void testKeyInitThreshold(HederaKeyPair masterKey, String description) { 
		assertEquals(KeyType.THRESHOLD, masterKey.getKeyType());
		assertEquals(description,  masterKey.keyDescription);
		assertEquals(threshold, masterKey.getThresholdKey().threshold);
		assertEquals(null, masterKey.getPublicKey());
		assertEquals(masterKey.getThresholdKey().keys.get(0).keyDescription, thresholdKey.keys.get(0).keyDescription);
		assertEquals(masterKey.getThresholdKey().keys.get(0).getKeyType(), thresholdKey.keys.get(0).getKeyType());
		assertEquals(masterKey.getThresholdKey().keys.get(0).uuid, thresholdKey.keys.get(0).uuid);
		assertNotEquals(null, masterKey.uuid);

		// compare keys
		assertEquals(masterKey.getPublicKey(), null);
		assertArrayEquals(keyBytes, masterKey.getThresholdKey().keys.get(0).getPublicKey());

		HederaKeyPair protobufKey = new HederaKeyPair(masterKey.getProtobuf());
		assertEquals(masterKey.getKeyType(), protobufKey.getKeyType());
		assertArrayEquals(masterKey.getPublicKey(), protobufKey.getPublicKey());
		assertEquals(masterKey.getThresholdKey().threshold, thresholdKey.threshold);
		assertEquals(thresholdKey.keys.get(0).getKeyType(), protobufKey.getThresholdKey().keys.get(0).getKeyType());
		assertEquals(null, protobufKey.getPublicKey());
		assertArrayEquals(keyBytes, protobufKey.getThresholdKey().keys.get(0).getPublicKey());

		HederaKeyPair jsonKey = new HederaKeyPair();
		jsonKey.fromJSON(masterKey.JSON());
		assertEquals(masterKey.getKeyType(), jsonKey.getKeyType());
		assertArrayEquals(masterKey.getPublicKey(), jsonKey.getPublicKey());
		assertEquals(description,  jsonKey.keyDescription);
		// compare threshold key
		assertEquals(threshold, jsonKey.getThresholdKey().threshold);
		assertEquals(thresholdKey.keys.get(0).keyDescription, jsonKey.getThresholdKey().keys.get(0).keyDescription);
		assertEquals(thresholdKey.keys.get(0).getKeyType(), jsonKey.getThresholdKey().keys.get(0).getKeyType());
		// compare keys
		assertEquals(null, jsonKey.getPublicKey());
		assertArrayEquals(keyBytes, jsonKey.getThresholdKey().keys.get(0).getPublicKey());
		assertEquals(thresholdKey.keys.get(0).uuid, jsonKey.getThresholdKey().keys.get(0).uuid);
	}
	 
	private static Stream<Arguments> keyInitTHRESHOLD() {
		return Stream.of(
			Arguments.of(new HederaKeyPair(thresholdKey), ""),
			Arguments.of(new HederaKeyPair(thresholdKey, description), description)
		);
	}

	@ParameterizedTest
	@DisplayName("Checking KEYLIST key init")
	@MethodSource("keyInitKEYLIST")
	void testKeyInitList(HederaKeyPair masterKey, String description) { 
		// check key type
		assertEquals(KeyType.LIST, masterKey.getKeyType());
		assertEquals(description,  masterKey.keyDescription);
		assertEquals(null, masterKey.getPublicKey());
		assertNotEquals(null, masterKey.uuid);
		assertEquals(description, masterKey.keyDescription);

		// compare keylist
		assertEquals(masterKey.getKeyList().keys.get(0).getKeyType(), hederaKeyList.keys.get(0).getKeyType());
		
		// compare keys
		assertEquals(null, masterKey.getPublicKey());
		assertArrayEquals(masterKey.getKeyList().keys.get(0).getPublicKey(), hederaKeyList.keys.get(0).getPublicKey());

		HederaKeyPair protobufKey = new HederaKeyPair(masterKey.getProtobuf());
		assertEquals(masterKey.getKeyType(), protobufKey.getKeyType());
		assertArrayEquals(masterKey.getPublicKey(), protobufKey.getPublicKey());
		// compare keylist
		assertEquals(hederaKeyList.keys.get(0).getKeyType(), protobufKey.getKeyList().keys.get(0).getKeyType());
		assertArrayEquals(hederaKeyList.keys.get(0).getPublicKey(), protobufKey.getKeyList().keys.get(0).getPublicKey());

		HederaKeyPair jsonKey = new HederaKeyPair(KeyType.ED25519);
		jsonKey.fromJSON(masterKey.JSON());
		assertEquals(masterKey.getKeyType(), jsonKey.getKeyType());
		assertEquals(hederaKeyList.keys.get(0).getKeyType(), jsonKey.getKeyList().keys.get(0).getKeyType());
		assertEquals(null, jsonKey.getPublicKey());
		assertArrayEquals(hederaKeyList.keys.get(0).getPublicKey(), jsonKey.getKeyList().keys.get(0).getPublicKey());
		assertEquals(description, masterKey.keyDescription);
	}
	 
	private static Stream<Arguments> keyInitKEYLIST() {
		return Stream.of(
			Arguments.of(new HederaKeyPair(hederaKeyList), ""),
			Arguments.of(new HederaKeyPair(hederaKeyList, description), description)
		);
	}
	
	@Test
	@DisplayName("HederaKey init tests")
	void HederaKeyInit() {
		HederaKeyPair key = new HederaKeyPair();
		
		assertEquals(0, key.getCost());
		assertArrayEquals(new byte[0], key.getStateProof());
		
	}	
}
