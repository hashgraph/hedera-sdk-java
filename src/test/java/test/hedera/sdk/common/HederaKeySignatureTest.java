package test.hedera.sdk.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.hedera.sdk.common.HederaContractID;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.node.HederaNode;
import com.hedera.sdk.common.HederaKeySignature;
import com.hedera.sdk.common.HederaKeySignatureList;
import com.hedera.sdk.common.HederaKeySignatureThreshold;
import com.hedera.sdk.common.HederaKeyUUIDDescription;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HederaKeySignatureTest {
	protected static byte[] keyBytes = new byte[] {12};
	protected static byte[] sigBytes = new byte[] {34};
	protected static byte[] emptySig = new byte[0];
	protected static String description = "A Description";
	protected static HederaContractID contractID = new HederaContractID(1,2,3);
	protected static List<HederaKeySignature> keySigList = new ArrayList<HederaKeySignature>();
	protected static HederaKeySignatureThreshold thresholdKeySig;
	protected static int threshold = 10;
	protected static HederaKeySignatureList hederaKeySigList = new HederaKeySignatureList();

	@BeforeAll
	static void initAll() {
		keySigList.add(new HederaKeySignature(KeyType.ED25519, keyBytes, sigBytes));
		thresholdKeySig = new HederaKeySignatureThreshold(threshold, keySigList);
		hederaKeySigList.addKeySignaturePair(new HederaKeySignature(KeyType.ED25519, keyBytes, sigBytes));
		hederaKeySigList.addKeySignaturePair(KeyType.ED25519, keyBytes, sigBytes);
	}
	
	@ParameterizedTest
	@DisplayName("Checking RSA, ED and EC key sig init")
	@MethodSource("keyInit")
	void testKeyInit(HederaKeySignature masterKey, KeyType keyType, String description) { 
		assertEquals(keyType, masterKey.getKeyType());
		assertArrayEquals(keyBytes, masterKey.getKey());
		assertArrayEquals(sigBytes, masterKey.getSignature());
		assertEquals(description, masterKey.keyDescription);
		assertNotEquals(null, masterKey.uuid);

		HederaKeySignature jsonKey = new HederaKeySignature();
		jsonKey.fromJSON(masterKey.JSON());
		assertEquals(masterKey.getKeyType(), jsonKey.getKeyType());
		assertEquals(masterKey.uuid, jsonKey.uuid);
		assertEquals(masterKey.keyDescription, jsonKey.keyDescription);
		assertArrayEquals(masterKey.getKey(), jsonKey.getKey());
		assertArrayEquals(masterKey.getSignature(), jsonKey.getSignature());
	}
	 
	private static Stream<Arguments> keyInit() {
		return Stream.of(
			Arguments.of(new HederaKeySignature(KeyType.ED25519, keyBytes, sigBytes), KeyType.ED25519, ""),
			Arguments.of(new HederaKeySignature(KeyType.ED25519, keyBytes, sigBytes), KeyType.ED25519, ""),
			Arguments.of(new HederaKeySignature(KeyType.ED25519, keyBytes, sigBytes), KeyType.ED25519, ""),
			Arguments.of(new HederaKeySignature(KeyType.ED25519, keyBytes, sigBytes, description), KeyType.ED25519, description),
			Arguments.of(new HederaKeySignature(KeyType.ED25519, keyBytes, sigBytes, description), KeyType.ED25519, description),
			Arguments.of(new HederaKeySignature(KeyType.ED25519, keyBytes, sigBytes, description), KeyType.ED25519, description)
		);
	}
	
	@ParameterizedTest
	@DisplayName("Checking CONTRACTID key sig init")
	@MethodSource("keyInitCONTRACTID")
	void testKeyInitContractID(HederaKeySignature masterKey, String description) { 
		assertEquals(KeyType.CONTRACT, masterKey.getKeyType());
		assertEquals(null, masterKey.getKey());
		assertEquals(null, masterKey.getSignature());
		assertEquals(contractID.contractNum, masterKey.getContractIDKey().contractNum);
		assertEquals(contractID.shardNum, masterKey.getContractIDKey().shardNum);
		assertEquals(contractID.realmNum, masterKey.getContractIDKey().realmNum);
		assertEquals(description, masterKey.keyDescription);
		assertNotEquals(null, masterKey.uuid);

		HederaKeySignature jsonKey = new HederaKeySignature();
		jsonKey.fromJSON(masterKey.JSON());
		assertEquals(null, jsonKey.getKey());
		assertEquals(null, jsonKey.getSignature());
		assertEquals(masterKey.getKeyType(), jsonKey.getKeyType());
		assertEquals(contractID.contractNum, jsonKey.getContractIDKey().contractNum);
		assertEquals(contractID.shardNum, jsonKey.getContractIDKey().shardNum);
		assertEquals(contractID.realmNum, jsonKey.getContractIDKey().realmNum);
		assertEquals(description, jsonKey.keyDescription);
		assertEquals(masterKey.uuid, jsonKey.uuid);
	}
	 
	private static Stream<Arguments> keyInitCONTRACTID() {
		return Stream.of(
			Arguments.of(new HederaKeySignature(contractID), ""),
			Arguments.of(new HederaKeySignature(contractID, description), description)
		);
	}

	@ParameterizedTest
	@DisplayName("Checking THRESHOLD key init")
	@MethodSource("keyInitTHRESHOLD")
	void testKeyInitThreshold(HederaKeySignature masterKey, String description) { 
		assertEquals(KeyType.THRESHOLD, masterKey.getKeyType());
		assertEquals(description,  masterKey.keyDescription);
		assertEquals(threshold, masterKey.getThresholdKeySignaturePair().threshold);
		assertEquals(null, masterKey.getKey());
		assertEquals(null, masterKey.getSignature());
		assertEquals(masterKey.getThresholdKeySignaturePair().keySigPairs.get(0).keyDescription, thresholdKeySig.keySigPairs.get(0).keyDescription);
		assertEquals(masterKey.getThresholdKeySignaturePair().keySigPairs.get(0).getKeyType(), thresholdKeySig.keySigPairs.get(0).getKeyType());
		assertEquals(masterKey.getThresholdKeySignaturePair().keySigPairs.get(0).uuid, thresholdKeySig.keySigPairs.get(0).uuid);

		assertNotEquals(null, masterKey.uuid);

		// compare keys
		assertEquals(masterKey.getKey(), null);
		assertArrayEquals(keyBytes, masterKey.getThresholdKeySignaturePair().keySigPairs.get(0).getKey());

		HederaKeySignature jsonKey = new HederaKeySignature();
		jsonKey.fromJSON(masterKey.JSON());
		assertEquals(masterKey.getKeyType(), jsonKey.getKeyType());
		assertArrayEquals(masterKey.getKey(), jsonKey.getKey());
		assertArrayEquals(masterKey.getSignature(), jsonKey.getSignature());
		assertEquals(description,  jsonKey.keyDescription);
		// compare threshold key
		assertEquals(threshold, jsonKey.getThresholdKeySignaturePair().threshold);
		assertEquals(thresholdKeySig.keySigPairs.get(0).keyDescription, jsonKey.getThresholdKeySignaturePair().keySigPairs.get(0).keyDescription);
		assertEquals(thresholdKeySig.keySigPairs.get(0).getKeyType(), jsonKey.getThresholdKeySignaturePair().keySigPairs.get(0).getKeyType());
		// compare keys
		assertEquals(null, jsonKey.getKey());
		assertEquals(null, jsonKey.getSignature());
		assertArrayEquals(keyBytes, jsonKey.getThresholdKeySignaturePair().keySigPairs.get(0).getKey());
		assertArrayEquals(sigBytes, jsonKey.getThresholdKeySignaturePair().keySigPairs.get(0).getSignature());
		assertEquals(thresholdKeySig.keySigPairs.get(0).uuid, jsonKey.getThresholdKeySignaturePair().keySigPairs.get(0).uuid);
	}
	 
	private static Stream<Arguments> keyInitTHRESHOLD() {
		return Stream.of(
			Arguments.of(new HederaKeySignature(thresholdKeySig), ""),
			Arguments.of(new HederaKeySignature(thresholdKeySig, description), description)
		);
	}

	@ParameterizedTest
	@DisplayName("Checking KEYLIST key sig init")
	@MethodSource("keyInitKEYLIST")
	void testKeyInitList(HederaKeySignature masterKey, String description) { 
		// check key type
		assertEquals(KeyType.LIST, masterKey.getKeyType());
		assertEquals(description,  masterKey.keyDescription);
		assertEquals(null, masterKey.getKey());
		assertEquals(null, masterKey.getSignature());
		assertNotEquals(null, masterKey.uuid);

		// compare keylist
		assertEquals(masterKey.getKeySignaturePairList().keySigPairs.get(0).getKeyType(), hederaKeySigList.keySigPairs.get(0).getKeyType());
		
		// compare keys
		assertEquals(null, masterKey.getKey());
		assertEquals(null, masterKey.getSignature());
		assertArrayEquals(masterKey.getKeySignaturePairList().keySigPairs.get(0).getKey(), hederaKeySigList.keySigPairs.get(0).getKey());
		assertArrayEquals(masterKey.getKeySignaturePairList().keySigPairs.get(0).getSignature(), hederaKeySigList.keySigPairs.get(0).getSignature());

		HederaKeySignature jsonKey = new HederaKeySignature();
		jsonKey.fromJSON(masterKey.JSON());
		assertEquals(hederaKeySigList.keySigPairs.get(0).getKeyType(), jsonKey.getKeySignaturePairList().keySigPairs.get(0).getKeyType());
		assertEquals(null, jsonKey.getKey());
		assertEquals(null, jsonKey.getSignature());
		assertArrayEquals(hederaKeySigList.keySigPairs.get(0).getKey(), jsonKey.getKeySignaturePairList().keySigPairs.get(0).getKey());
		assertArrayEquals(hederaKeySigList.keySigPairs.get(0).getSignature(), jsonKey.getKeySignaturePairList().keySigPairs.get(0).getSignature());
		assertEquals(description, masterKey.keyDescription);
	}
	 
	private static Stream<Arguments> keyInitKEYLIST() {
		return Stream.of(
			Arguments.of(new HederaKeySignature(hederaKeySigList), ""),
			Arguments.of(new HederaKeySignature(hederaKeySigList, description), description)
		);
	}

	@Test
	@DisplayName("HederaKeySignature General tests")
	void HederaKeySignature() { 
		HederaNode node = new HederaNode();
		HederaKeySignature keySignature = new HederaKeySignature();
		keySignature.setNode(node);
		assertNotNull(keySignature.getNode());
		assertEquals(0, keySignature.getCost());
		assertNull(keySignature.getStateProof());
		assertArrayEquals(new byte[0], keySignature.getContractIDSignature());
	}		
	@Test
	@DisplayName("HederaKeySignature Set Signature tests")
	void HederaKeySignatureSet() { 
		// create key with empty signature
		byte[] aSignature = "signature".getBytes();
		byte[] aKey = "key".getBytes();
		
		// standard keys
		HederaKeySignature keySignature = new HederaKeySignature(KeyType.NOTSET, aKey, null);
		assertFalse(keySignature.setSignatureForKey("dummy".getBytes(), aSignature, true));
		assertNull(keySignature.getSignature());
		
		keySignature = new HederaKeySignature(KeyType.ED25519, aKey, null);
		assertFalse(keySignature.setSignatureForKey("dummy".getBytes(), aSignature, true));
		assertTrue(keySignature.setSignatureForKey(aKey, aSignature, false));
		assertEquals(aSignature, keySignature.getSignature());

		// false positive, if the signature is set, the "set" method shouldn't update
		keySignature = new HederaKeySignature(KeyType.ED25519, aKey, "dummy".getBytes());
		assertFalse(keySignature.setSignatureForKey(aKey, aSignature, false));
		assertNotEquals(aSignature, keySignature.getSignature());

		// threshold key
		HederaKeySignature pair1 = new HederaKeySignature(KeyType.ED25519, aKey, null);
		HederaKeySignature pair2 = new HederaKeySignature(KeyType.ED25519, aKey, null);
		List<HederaKeySignature> list = new ArrayList<HederaKeySignature>();
		list.add(pair1);
		list.add(pair2);
		HederaKeySignatureThreshold keySig = new HederaKeySignatureThreshold(1, list);

		keySignature = new HederaKeySignature(keySig);
		assertTrue(keySignature.setSignatureForKey(aKey, aSignature, true));
		// only the first should be set
		assertArrayEquals(aSignature, keySignature.getThresholdKeySignaturePair().keySigPairs.get(0).getSignature());
		assertNull(keySignature.getThresholdKeySignaturePair().keySigPairs.get(1).getSignature());
		
		
		// now try both
		pair1 = new HederaKeySignature(KeyType.ED25519, aKey, null);
		pair2 = new HederaKeySignature(KeyType.ED25519, aKey, null);
		list = new ArrayList<HederaKeySignature>();
		list.add(pair1);
		list.add(pair2);
		keySig = new HederaKeySignatureThreshold(1, list);

		keySignature = new HederaKeySignature(keySig);
		assertTrue(keySignature.setSignatureForKey(aKey, aSignature, false)); //<- set all matching keys
		// only the first should be set
		assertArrayEquals(aSignature, keySignature.getThresholdKeySignaturePair().keySigPairs.get(0).getSignature());
		assertArrayEquals(aSignature, keySignature.getThresholdKeySignaturePair().keySigPairs.get(1).getSignature());
		
		// key + signature list
		// reset keys
		pair1 = new HederaKeySignature(KeyType.ED25519, aKey, null);
		pair2 = new HederaKeySignature(KeyType.ED25519, aKey, null);
		list = new ArrayList<HederaKeySignature>();
		list.add(pair1);
		list.add(pair2);
		HederaKeySignatureList keySigList = new HederaKeySignatureList(list);

		keySignature = new HederaKeySignature(keySigList);
		assertTrue(keySignature.setSignatureForKey(aKey, aSignature, true));
		// only the first should be set
		assertArrayEquals(aSignature, keySignature.getKeySignaturePairList().keySigPairs.get(0).getSignature());
		assertNull(keySignature.getKeySignaturePairList().keySigPairs.get(1).getSignature());
		// now try both
		// reset keys
		pair1 = new HederaKeySignature(KeyType.ED25519, aKey, null);
		pair2 = new HederaKeySignature(KeyType.ED25519, aKey, null);
		list = new ArrayList<HederaKeySignature>();
		list.add(pair1);
		list.add(pair2);
		keySigList = new HederaKeySignatureList(list);
		keySignature = new HederaKeySignature(keySigList);
		assertTrue(keySignature.setSignatureForKey(aKey, aSignature, false)); //<- set all matching keys
		// only the first should be set
		assertArrayEquals(aSignature, keySignature.getKeySignaturePairList().keySigPairs.get(0).getSignature());
		assertArrayEquals(aSignature, keySignature.getKeySignaturePairList().keySigPairs.get(1).getSignature());
	}

	@Test
	@DisplayName("HederaKeySignature Set Signature tests - arrays")
	void HederaKeySignatureSetArrays() { 
		byte[] aSignature1 = "signature".getBytes();
		byte[] aSignature2 = "signature2".getBytes();
		byte[] aKey0 = "key0".getBytes();
		byte[] aKey1 = "key".getBytes();
		byte[] aKey2 = "key2".getBytes();
		
		byte[][] keys = new byte[2][];
		keys[0] = aKey1;
		keys[1] = aKey2;
		byte[][] signatures = new byte[2][];
		signatures[0] = aSignature1;
		signatures[1] = aSignature2;
		
		// standard keys
		HederaKeySignature keySignature = new HederaKeySignature(KeyType.ED25519, aKey0, null);
		// shouldn't update anything, signature key doesn't match array
		assertFalse(keySignature.setSignatureForKeys(keys, signatures, false));
		assertNull(keySignature.getSignature());

		keySignature = new HederaKeySignature(KeyType.ED25519, aKey1, null);
		// shouldn't update anything, signature key doesn't match array
		assertTrue(keySignature.setSignatureForKeys(keys, signatures, false));
		assertArrayEquals(aSignature1, keySignature.getSignature());

		// threshold key
		HederaKeySignature pair1 = new HederaKeySignature(KeyType.ED25519, aKey1, null);
		HederaKeySignature pair2 = new HederaKeySignature(KeyType.ED25519, aKey2, null);
		List<HederaKeySignature> list = new ArrayList<HederaKeySignature>();
		list.add(pair1);
		list.add(pair2);
		HederaKeySignatureThreshold keySig = new HederaKeySignatureThreshold(1, list);

		keySignature = new HederaKeySignature(keySig);
		assertTrue(keySignature.setSignatureForKeys(keys, signatures, false));
		// only the first should be set
		assertArrayEquals(aSignature1, keySignature.getThresholdKeySignaturePair().keySigPairs.get(0).getSignature());
		assertArrayEquals(aSignature2, keySignature.getThresholdKeySignaturePair().keySigPairs.get(1).getSignature());
		
		// key + signature list
		// reset keys
		pair1 = new HederaKeySignature(KeyType.ED25519, aKey1, null);
		pair2 = new HederaKeySignature(KeyType.ED25519, aKey2, null);
		list = new ArrayList<HederaKeySignature>();
		list.add(pair1);
		list.add(pair2);
		HederaKeySignatureList keySigList = new HederaKeySignatureList(list);

		keySignature = new HederaKeySignature(keySigList);
		assertTrue(keySignature.setSignatureForKeys(keys, signatures, false));
		assertArrayEquals(aSignature1, keySignature.getKeySignaturePairList().keySigPairs.get(0).getSignature());
		assertArrayEquals(aSignature2, keySignature.getKeySignaturePairList().keySigPairs.get(1).getSignature());
	}
	
	@Test
	@DisplayName("HederaKeySignature Set Signature via UUID tests")
	void HederaKeySignatureSetUUID() { 
		// create key with empty signature
		byte[] aSignature = "signature".getBytes();
		byte[] aKey = "key".getBytes();
		
		// standard keys
		HederaKeySignature keySignature = new HederaKeySignature(KeyType.NOTSET, aKey, null);
		assertFalse(keySignature.setSignatureForKeyUUID("dummy", aSignature));
		assertNull(keySignature.getSignature());
		
		keySignature = new HederaKeySignature(KeyType.ED25519, aKey, null);
		assertFalse(keySignature.setSignatureForKeyUUID("dummy", aSignature));
		assertTrue(keySignature.setSignatureForKeyUUID(keySignature.uuid, aSignature));
		assertEquals(aSignature, keySignature.getSignature());
		
		// threshold key
		HederaKeySignature pair1 = new HederaKeySignature(KeyType.ED25519, aKey, null);
		HederaKeySignature pair2 = new HederaKeySignature(KeyType.ED25519, aKey, null);
		List<HederaKeySignature> list = new ArrayList<HederaKeySignature>();
		list.add(pair1);
		list.add(pair2);
		HederaKeySignatureThreshold keySig = new HederaKeySignatureThreshold(1, list);

		keySignature = new HederaKeySignature(keySig);
		assertTrue(keySignature.setSignatureForKeyUUID(pair1.uuid, aSignature));
		// only the first should be set
		assertArrayEquals(aSignature, keySignature.getThresholdKeySignaturePair().keySigPairs.get(0).getSignature());
		assertNull(keySignature.getThresholdKeySignaturePair().keySigPairs.get(1).getSignature());
		// do the second
		byte[] aSignature2 = "SEcond sig".getBytes();
		assertTrue(keySignature.setSignatureForKeyUUID(pair2.uuid, aSignature2));
		// only the second should have changed
		assertArrayEquals(aSignature, keySignature.getThresholdKeySignaturePair().keySigPairs.get(0).getSignature());
		assertArrayEquals(aSignature2, keySignature.getThresholdKeySignaturePair().keySigPairs.get(1).getSignature());
		
		// key + signature list
		// reset keys
		pair1 = new HederaKeySignature(KeyType.ED25519, aKey, null);
		pair2 = new HederaKeySignature(KeyType.ED25519, aKey, null);
		list = new ArrayList<HederaKeySignature>();
		list.add(pair1);
		list.add(pair2);
		HederaKeySignatureList keySigList = new HederaKeySignatureList(list);

		keySignature = new HederaKeySignature(keySigList);
		assertTrue(keySignature.setSignatureForKeyUUID(pair1.uuid, aSignature));
		// only the first should be set
		assertArrayEquals(aSignature, keySignature.getKeySignaturePairList().keySigPairs.get(0).getSignature());
		assertNull(keySignature.getKeySignaturePairList().keySigPairs.get(1).getSignature());
		// do the second
		aSignature2 = "Second sig".getBytes();
		assertTrue(keySignature.setSignatureForKeyUUID(pair2.uuid, aSignature2));
		// only the second should have changed
		assertArrayEquals(aSignature, keySignature.getKeySignaturePairList().keySigPairs.get(0).getSignature());
		assertArrayEquals(aSignature2, keySignature.getKeySignaturePairList().keySigPairs.get(1).getSignature());
	}

	@Test
	@DisplayName("HederaKeySignature Get Key UUIDs")
	void HederaKeySignatureGetKeyUUID() { 
		// create key with empty signature
		byte[] aKey = "key".getBytes();
		byte[] aKey2 = "key2".getBytes();
		
		// standard keys
		HederaKeySignature keySignature = new HederaKeySignature(KeyType.ED25519, aKey, null);
		
		List<HederaKeyUUIDDescription> uuids = new ArrayList<HederaKeyUUIDDescription>();
		keySignature.getKeyUUIDs(uuids, aKey);
		assertEquals(1, uuids.size());

		uuids = new ArrayList<HederaKeyUUIDDescription>();
		keySignature.getKeyUUIDs(uuids, "dummmy".getBytes());
		assertEquals(0, uuids.size());
		
		// threshold key
		HederaKeySignature pair1 = new HederaKeySignature(KeyType.ED25519, aKey, null);
		HederaKeySignature pair2 = new HederaKeySignature(KeyType.ED25519, aKey2, null);
		List<HederaKeySignature> list = new ArrayList<HederaKeySignature>();
		list.add(pair1);
		list.add(pair2);
		HederaKeySignatureThreshold keySig = new HederaKeySignatureThreshold(1, list);
		keySignature = new HederaKeySignature(keySig);

		uuids = new ArrayList<HederaKeyUUIDDescription>();
		keySignature.getKeyUUIDs(uuids, aKey);
		assertEquals(1, uuids.size());

		uuids = new ArrayList<HederaKeyUUIDDescription>();
		keySignature.getKeyUUIDs(uuids, "dummmy".getBytes());
		assertEquals(0, uuids.size());
		
		// key + signature list
		// reset keys
		pair1 = new HederaKeySignature(KeyType.ED25519, aKey, null);
		pair2 = new HederaKeySignature(KeyType.ED25519, aKey2, null);
		list = new ArrayList<HederaKeySignature>();
		list.add(pair1);
		list.add(pair2);
		HederaKeySignatureList keySigList = new HederaKeySignatureList(list);
		keySignature = new HederaKeySignature(keySigList);

		uuids = new ArrayList<HederaKeyUUIDDescription>();
		keySignature.getKeyUUIDs(uuids, aKey);
		assertEquals(1, uuids.size());

		uuids = new ArrayList<HederaKeyUUIDDescription>();
		keySignature.getKeyUUIDs(uuids, "dummmy".getBytes());
		assertEquals(0, uuids.size());
	}

	@Test
	@DisplayName("HederaKeySignature Set Signature via UUIDs tests")
	void HederaKeySignatureSetUUIDs() { 
		// create key with empty signature
		byte[] aSignature1 = "signature1".getBytes();
		byte[] aSignature2 = "signature2".getBytes();
		byte[] aKey = "key".getBytes();
		byte[][] signatures = new byte[2][];
		signatures[0] = aSignature1;
		signatures[1] = aSignature2;
		
		String[] uuids = new String[2];
		uuids[0] = "dummy1";
		uuids[1] = "dummy2";
		
		HederaKeySignature keySignature = new HederaKeySignature(KeyType.ED25519, aKey, null);
		// uuids are dummy, should not update
		assertFalse(keySignature.setSignatureForKeyUUIDs(uuids, signatures));
		assertNull(keySignature.getSignature());
		
		uuids[0] = keySignature.uuid;
		
		assertTrue(keySignature.setSignatureForKeyUUIDs(uuids, signatures));
		assertEquals(aSignature1, keySignature.getSignature());
		
		// threshold key
		HederaKeySignature pair1 = new HederaKeySignature(KeyType.ED25519, aKey, null);
		HederaKeySignature pair2 = new HederaKeySignature(KeyType.ED25519, aKey, null);
		List<HederaKeySignature> list = new ArrayList<HederaKeySignature>();
		list.add(pair1);
		list.add(pair2);

		uuids[0] = "dummy1";
		uuids[1] = "dummy2";
		
		HederaKeySignatureThreshold keySig = new HederaKeySignatureThreshold(1, list);
		keySignature = new HederaKeySignature(keySig);
		// uuids are dummy, should not update
		assertFalse(keySignature.setSignatureForKeyUUIDs(uuids, signatures));
		assertNull(pair1.getSignature());
		assertNull(pair2.getSignature());
		
		uuids[0] = pair1.uuid;
		uuids[1] = pair2.uuid;
		assertTrue(keySignature.setSignatureForKeyUUIDs(uuids, signatures));
		assertArrayEquals(aSignature1,pair1.getSignature());
		assertArrayEquals(aSignature2,pair2.getSignature());

		// key + signature list
		// reset keys
		pair1 = new HederaKeySignature(KeyType.ED25519, aKey, null);
		pair2 = new HederaKeySignature(KeyType.ED25519, aKey, null);
		list = new ArrayList<HederaKeySignature>();
		list.add(pair1);
		list.add(pair2);
		HederaKeySignatureList keySigList = new HederaKeySignatureList(list);
		keySignature = new HederaKeySignature(keySigList);

		uuids[0] = "dummy1";
		uuids[1] = "dummy2";
		
		HederaKeySignatureList keyList = new HederaKeySignatureList(list);
		keySignature = new HederaKeySignature(keyList);
		// uuids are dummy, should not update
		assertFalse(keySignature.setSignatureForKeyUUIDs(uuids, signatures));
		assertNull(pair1.getSignature());
		assertNull(pair2.getSignature());
		
		uuids[0] = pair1.uuid;
		uuids[1] = pair2.uuid;
		assertTrue(keySignature.setSignatureForKeyUUIDs(uuids, signatures));
		assertArrayEquals(aSignature1,pair1.getSignature());
		assertArrayEquals(aSignature2,pair2.getSignature());
		
	}
	
	@Test
	@DisplayName("HederaKeySignature Update Signature tests")
	void updateSignatureForKey() { 
		// create key with empty signature
		byte[] aSignature1 = "signature1".getBytes();
		byte[] aSignature2 = "signature2".getBytes();
		byte[] aKey1 = "key1".getBytes();
		byte[] aKey2 = "key2".getBytes();
		
		// standard keys
		HederaKeySignature keySignature = new HederaKeySignature(KeyType.NOTSET, aKey1, "sig".getBytes());
		assertFalse(keySignature.updateSignatureForKey("dummy".getBytes(), aSignature1));
		assertArrayEquals("sig".getBytes(), keySignature.getSignature());
		
		keySignature = new HederaKeySignature(KeyType.ED25519, aKey1, aSignature1);
		assertFalse(keySignature.updateSignatureForKey("dummy".getBytes(), aSignature2));
		assertArrayEquals(aSignature1, keySignature.getSignature());
		assertTrue(keySignature.updateSignatureForKey(aKey1, aSignature2));
		assertArrayEquals(aSignature2, keySignature.getSignature());

		// threshold key
		HederaKeySignature pair1 = new HederaKeySignature(KeyType.ED25519, aKey1, "sig".getBytes());
		HederaKeySignature pair2 = new HederaKeySignature(KeyType.ED25519, aKey2, "sig".getBytes());
		List<HederaKeySignature> list = new ArrayList<HederaKeySignature>();
		list.add(pair1);
		list.add(pair2);
		HederaKeySignatureThreshold keySig = new HederaKeySignatureThreshold(1, list);

		keySignature = new HederaKeySignature(keySig);
		assertTrue(keySignature.updateSignatureForKey(aKey1, aSignature1));

		assertArrayEquals(aSignature1, keySignature.getThresholdKeySignaturePair().keySigPairs.get(0).getSignature());
		assertArrayEquals("sig".getBytes(), keySignature.getThresholdKeySignaturePair().keySigPairs.get(1).getSignature());
		
		// key + signature list
		// reset keys
		pair1 = new HederaKeySignature(KeyType.ED25519, aKey1, "sig".getBytes());
		pair2 = new HederaKeySignature(KeyType.ED25519, aKey2, "sig".getBytes());
		list = new ArrayList<HederaKeySignature>();
		list.add(pair1);
		list.add(pair2);
		HederaKeySignatureList keySigList = new HederaKeySignatureList(list);

		keySignature = new HederaKeySignature(keySigList);
		assertTrue(keySignature.updateSignatureForKey(aKey1, aSignature1));

		assertArrayEquals(aSignature1, keySignature.getKeySignaturePairList().keySigPairs.get(0).getSignature());
		assertArrayEquals("sig".getBytes(), keySignature.getKeySignaturePairList().keySigPairs.get(1).getSignature());
	}
	
	@Test
	@DisplayName("HederaKeySignature Updates Signature tests")
	void updateSignatureForKeys() { 
		// create key with empty signature
		byte[] aSignature1 = "signature1".getBytes();
		byte[] aSignature2 = "signature2".getBytes();
		byte[] aKey1 = "key1".getBytes();
		byte[] aKey2 = "key2".getBytes();
		
		byte[][] signatures = new byte[2][];
		byte[][] keys = new byte[2][];
		
		signatures[0] = aSignature1;
		signatures[1] = aSignature2;
		keys[0] = aKey1;
		keys[1] = aKey2;
		
		// standard keys
		HederaKeySignature keySignature = new HederaKeySignature(KeyType.NOTSET, aKey1, aSignature1);
		keys[0] = aKey2;
		keys[1] = "someBytes".getBytes();
		assertFalse(keySignature.updateSignatureForKeys(keys, signatures));
		assertArrayEquals(aSignature1, keySignature.getSignature());
		
		keySignature = new HederaKeySignature(KeyType.ED25519, aKey1, aSignature2);
		keys[0] = aKey1;
		keys[1] = "someBytes".getBytes();
		assertTrue(keySignature.updateSignatureForKeys(keys, signatures));
		assertArrayEquals(aSignature1, keySignature.getSignature());

		// threshold key
		HederaKeySignature pair1 = new HederaKeySignature(KeyType.ED25519, aKey1, "sig".getBytes());
		HederaKeySignature pair2 = new HederaKeySignature(KeyType.ED25519, aKey2, "sig".getBytes());
		List<HederaKeySignature> list = new ArrayList<HederaKeySignature>();
		list.add(pair1);
		list.add(pair2);
		HederaKeySignatureThreshold keySig = new HederaKeySignatureThreshold(1, list);

		keySignature = new HederaKeySignature(keySig);
		keys[0] = aKey1;
		keys[1] = "someBytes".getBytes();
		assertTrue(keySignature.updateSignatureForKeys(keys, signatures));

		assertArrayEquals(aSignature1, keySignature.getThresholdKeySignaturePair().keySigPairs.get(0).getSignature());
		assertArrayEquals("sig".getBytes(), keySignature.getThresholdKeySignaturePair().keySigPairs.get(1).getSignature());

		keys[0] = aKey1;
		keys[1] = aKey2;
		assertTrue(keySignature.updateSignatureForKeys(keys, signatures));

		assertArrayEquals(aSignature1, keySignature.getThresholdKeySignaturePair().keySigPairs.get(0).getSignature());
		assertArrayEquals(aSignature2, keySignature.getThresholdKeySignaturePair().keySigPairs.get(1).getSignature());
		
		// key + signature list
		// reset keys
		pair1 = new HederaKeySignature(KeyType.ED25519, aKey1, "sig".getBytes());
		pair2 = new HederaKeySignature(KeyType.ED25519, aKey2, "sig".getBytes());
		list = new ArrayList<HederaKeySignature>();
		list.add(pair1);
		list.add(pair2);
		HederaKeySignatureList keySigList = new HederaKeySignatureList(list);

		keySignature = new HederaKeySignature(keySigList);
		keys[0] = aKey1;
		keys[1] = "someBytes".getBytes();
		assertTrue(keySignature.updateSignatureForKeys(keys, signatures));

		assertArrayEquals(aSignature1, keySignature.getKeySignaturePairList().keySigPairs.get(0).getSignature());
		assertArrayEquals("sig".getBytes(), keySignature.getKeySignaturePairList().keySigPairs.get(1).getSignature());

		keys[0] = aKey1;
		keys[1] = aKey2;
		assertTrue(keySignature.updateSignatureForKeys(keys, signatures));

		assertArrayEquals(aSignature1, keySignature.getKeySignaturePairList().keySigPairs.get(0).getSignature());
		assertArrayEquals(aSignature2, keySignature.getKeySignaturePairList().keySigPairs.get(1).getSignature());
	}
	@Test
	@DisplayName("HederaKeySignature JSONString")
	void JSONString() {
		HederaKeySignature keySig = new HederaKeySignature();
		assertNotNull(keySig.JSONString());
	}
}
