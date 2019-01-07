package test.hedera.sdk.common;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.common.HederaKeySignature;
import com.hedera.sdk.common.HederaKeySignatureList;
import com.hedera.sdk.common.HederaKeyUUIDDescription;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaKeySignatureListTest {

	@Test
	@DisplayName("Checking matching account details")
	void testKeyListInit() {
		HederaKeyPair keyPair = new HederaKeyPair(KeyType.ED25519);
		HederaKeySignature key1 = new HederaKeySignature(KeyType.ED25519, keyPair.getPublicKeyEncoded(), new byte[] {12,34}, "ED25519");
		keyPair = new HederaKeyPair(KeyType.ED25519);
		HederaKeySignature key2 = new HederaKeySignature(KeyType.ED25519,  keyPair.getPublicKeyEncoded(), new byte[] {12,34}, "ED25519");
		List<HederaKeySignature> keys = new ArrayList<HederaKeySignature>();
		keys.add(key1);
		keys.add(key2);
		
		HederaKeySignatureList masterKeyList = new HederaKeySignatureList(keys);
		
		assertEquals(keys.size(), masterKeyList.keySigPairs.size());
		assertEquals(keys.get(0).uuid, masterKeyList.keySigPairs.get(0).uuid);
		assertEquals(keys.get(1).uuid, masterKeyList.keySigPairs.get(1).uuid);
		
		masterKeyList.addKeySignaturePair(key1);
		masterKeyList.addKeySignaturePair(key2);
		keyPair = new HederaKeyPair(KeyType.ED25519);
		masterKeyList.addKeySignaturePair(KeyType.ED25519, keyPair.getPublicKeyEncoded(), new byte[] {12,34});

		assertEquals(5, masterKeyList.keySigPairs.size());

		masterKeyList.deleteKeySigPair(key1);
		masterKeyList.deleteKeySigPair(key2);

		assertEquals(3, masterKeyList.keySigPairs.size());
		
		HederaKeySignatureList jsonList = new HederaKeySignatureList();
		jsonList.fromJSON(masterKeyList.JSON());
		assertEquals(masterKeyList.keySigPairs.size(), jsonList.keySigPairs.size());
		assertEquals(masterKeyList.keySigPairs.get(0).uuid, jsonList.keySigPairs.get(0).uuid);
		assertEquals(masterKeyList.keySigPairs.get(1).uuid, jsonList.keySigPairs.get(1).uuid);
		assertEquals(masterKeyList.keySigPairs.get(2).uuid, jsonList.keySigPairs.get(2).uuid);
		assertArrayEquals(masterKeyList.keySigPairs.get(0).getKey(), jsonList.keySigPairs.get(0).getKey());
		assertArrayEquals(masterKeyList.keySigPairs.get(0).getSignature(), jsonList.keySigPairs.get(0).getSignature());
		assertArrayEquals(masterKeyList.keySigPairs.get(1).getKey(), jsonList.keySigPairs.get(1).getKey());
		assertArrayEquals(masterKeyList.keySigPairs.get(1).getSignature(), jsonList.keySigPairs.get(1).getSignature());
		assertArrayEquals(masterKeyList.keySigPairs.get(2).getKey(), jsonList.keySigPairs.get(2).getKey());
		assertArrayEquals(masterKeyList.keySigPairs.get(2).getSignature(), jsonList.keySigPairs.get(2).getSignature());
	}

	@Test
	@DisplayName("HederaKeySignatureListTest Set Signature tests")
	void HederaKeySignatureListSet() { 
		// create key with empty signature
		byte[] aSignature = "signature".getBytes();
		byte[] aSignature2 = "signature2".getBytes();
		HederaKeyPair keyPair = new HederaKeyPair(KeyType.ED25519);
		byte[] aKey = keyPair.getPublicKey();
		
		// standard keys
		HederaKeySignature keySignature = new HederaKeySignature(KeyType.ED25519, aKey, null);
		
		HederaKeySignatureList sigList = new HederaKeySignatureList();
		sigList.addKeySignaturePair(keySignature);
		
		assertFalse(sigList.setSignatureForKey("dummy".getBytes(), aSignature, true));
		assertNull(sigList.keySigPairs.get(0).getSignature());
		
		assertTrue(sigList.setSignatureForKey(aKey, aSignature, false));
		assertArrayEquals(aSignature, sigList.keySigPairs.get(0).getSignature());

		// false positive, if the signature is set, the "set" method shouldn't update
		assertFalse(sigList.setSignatureForKey(aKey, aSignature2, false));
		assertArrayEquals(aSignature, sigList.keySigPairs.get(0).getSignature());

	}

	@Test
	@DisplayName("HederaKeySignatureListTest Set Signature tests - arrays")
	void HederaKeySignatureListSetArrays() { 
		byte[] aSignature1 = "signature".getBytes();
		byte[] aSignature2 = "signature2".getBytes();
		HederaKeyPair keyPair = new HederaKeyPair(KeyType.ED25519);
		byte[] aKey0 = keyPair.getPublicKey();
		keyPair = new HederaKeyPair(KeyType.ED25519);
		byte[] aKey1 = keyPair.getPublicKey();
		keyPair = new HederaKeyPair(KeyType.ED25519);
		byte[] aKey2 = keyPair.getPublicKey();
		
		byte[][] keys = new byte[2][];
		keys[0] = aKey1;
		keys[1] = aKey2;
		byte[][] signatures = new byte[2][];
		signatures[0] = aSignature1;
		signatures[1] = aSignature2;
		
		// standard keys
		HederaKeySignature keySignature = new HederaKeySignature(KeyType.ED25519, aKey0, null);
		HederaKeySignatureList sigList = new HederaKeySignatureList();
		sigList.addKeySignaturePair(keySignature);

		// shouldn't update anything, signature key doesn't match array
		assertFalse(sigList.setSignatureForKeys(keys, signatures, false));
		assertNull(sigList.keySigPairs.get(0).getSignature());

		// shouldn't update anything, signature key doesn't match array
		sigList = new HederaKeySignatureList();
		sigList.addKeySignaturePair(new HederaKeySignature(KeyType.ED25519, aKey1, null));
		assertTrue(sigList.setSignatureForKeys(keys, signatures, false));
		assertArrayEquals(aSignature1, sigList.keySigPairs.get(0).getSignature());
	}
	
	@Test
	@DisplayName("HederaKeySignatureListTest Set Signature via UUID tests")
	void HederaKeySignatureListTestUUID() { 
		// create key with empty signature
		byte[] aSignature = "signature".getBytes();
		HederaKeyPair keyPair = new HederaKeyPair(KeyType.ED25519);
		byte[] aKey = keyPair.getPublicKey();
		
		// standard keys
		HederaKeySignature keySignature = new HederaKeySignature(KeyType.NOTSET, aKey, null);
		HederaKeySignatureList sigList = new HederaKeySignatureList();
		sigList.addKeySignaturePair(keySignature);

		assertFalse(sigList.setSignatureForKeyUUID("dummy", aSignature));
		assertNull(keySignature.getSignature());
		
		keySignature = new HederaKeySignature(KeyType.ED25519, aKey, null);
		sigList = new HederaKeySignatureList();
		sigList.addKeySignaturePair(keySignature);

		assertFalse(sigList.setSignatureForKeyUUID("dummy", aSignature));
		assertTrue(sigList.setSignatureForKeyUUID(keySignature.uuid, aSignature));
		assertArrayEquals(aSignature, sigList.keySigPairs.get(0).getSignature());
		
	}

	@Test
	@DisplayName("HederaKeySignatureListTest Get Key UUIDs")
	void HederaKeySignatureListTestGetKeyUUIDs() { 
		// create key with empty signature

		HederaKeyPair keyPair = new HederaKeyPair(KeyType.ED25519);
		byte[] aKey = keyPair.getPublicKey();
		keyPair = new HederaKeyPair(KeyType.ED25519);
		
		// standard keys
		HederaKeySignature keySignature = new HederaKeySignature(KeyType.ED25519, aKey, null);
		HederaKeySignatureList sigList = new HederaKeySignatureList();
		sigList.addKeySignaturePair(keySignature);
		
		List<HederaKeyUUIDDescription> uuids = new ArrayList<HederaKeyUUIDDescription>();
		sigList.getKeyUUIDs(uuids, aKey);
		assertEquals(1, uuids.size());

		uuids = new ArrayList<HederaKeyUUIDDescription>();
		sigList.getKeyUUIDs(uuids, "dummmy".getBytes());
		assertEquals(0, uuids.size());
		
	}

	@Test
	@DisplayName("HederaKeySignatureListTest Set Signature via UUIDs tests")
	void HederaKeySignatureListTestSedUUIDs() { 
		// create key with empty signature
		byte[] aSignature1 = "signature1".getBytes();
		byte[] aSignature2 = "signature2".getBytes();
		HederaKeyPair keyPair = new HederaKeyPair(KeyType.ED25519);
		byte[] aKey = keyPair.getPublicKey();
		byte[][] signatures = new byte[2][];
		signatures[0] = aSignature1;
		signatures[1] = aSignature2;
		
		String[] uuids = new String[2];
		uuids[0] = "dummy1";
		uuids[1] = "dummy2";
		
		HederaKeySignature keySignature = new HederaKeySignature(KeyType.ED25519, aKey, null);
		HederaKeySignatureList sigList = new HederaKeySignatureList();
		sigList.addKeySignaturePair(keySignature);
		// uuids are dummy, should not update
		assertFalse(sigList.setSignatureForKeyUUIDs(uuids, signatures));
		assertNull(sigList.keySigPairs.get(0).getSignature());
		
		uuids[0] = keySignature.uuid;
		
		assertTrue(sigList.setSignatureForKeyUUIDs(uuids, signatures));
		assertEquals(aSignature1, sigList.keySigPairs.get(0).getSignature());
		
	}
	
	@Test
	@DisplayName("HederaKeySignatureListTest Update Signature tests")
	void HederaKeySignatureListTestUpdateUUID() { 
		// create key with empty signature
		byte[] aSignature1 = "signature1".getBytes();
		byte[] aSignature2 = "signature2".getBytes();
		HederaKeyPair keyPair = new HederaKeyPair(KeyType.ED25519);
		byte[] aKey1 = keyPair.getPublicKey();
		keyPair = new HederaKeyPair(KeyType.ED25519);

		// standard keys
		HederaKeySignature keySignature = new HederaKeySignature(KeyType.NOTSET, aKey1, "sig".getBytes());
		HederaKeySignatureList sigList = new HederaKeySignatureList();
		sigList.addKeySignaturePair(keySignature);

		assertFalse(sigList.updateSignatureForKey("dummy".getBytes(), aSignature1));
		assertArrayEquals("sig".getBytes(), sigList.keySigPairs.get(0).getSignature());
		
		keySignature = new HederaKeySignature(KeyType.ED25519, aKey1, aSignature1);
		sigList = new HederaKeySignatureList();
		sigList.addKeySignaturePair(keySignature);
		
		assertFalse(sigList.updateSignatureForKey("dummy".getBytes(), aSignature2));
		assertArrayEquals(aSignature1, sigList.keySigPairs.get(0).getSignature());
		assertTrue(sigList.updateSignatureForKey(aKey1, aSignature2));
		assertArrayEquals(aSignature2, sigList.keySigPairs.get(0).getSignature());
	}
	
	@Test
	@DisplayName("HederaKeySignatureListTest Updates Signature tests")
	void HederaKeySignatureListUpdateforKeys() { 
		// create key with empty signature
		byte[] aSignature1 = "signature1".getBytes();
		byte[] aSignature2 = "signature2".getBytes();
		HederaKeyPair keyPair = new HederaKeyPair(KeyType.ED25519);
		byte[] aKey1 = keyPair.getPublicKey();
		keyPair = new HederaKeyPair(KeyType.ED25519);
		byte[] aKey2 = keyPair.getPublicKey();
		
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
		HederaKeySignatureList sigList = new HederaKeySignatureList();
		sigList.addKeySignaturePair(keySignature);

		assertFalse(sigList.updateSignatureForKeys(keys, signatures));
		assertArrayEquals(aSignature1, sigList.keySigPairs.get(0).getSignature());
		
		keySignature = new HederaKeySignature(KeyType.ED25519, aKey1, aSignature2);
		sigList = new HederaKeySignatureList();
		sigList.addKeySignaturePair(keySignature);
		keys[0] = aKey1;
		keys[1] = "someBytes".getBytes();
		assertTrue(sigList.updateSignatureForKeys(keys, signatures));
		assertArrayEquals(aSignature1, sigList.keySigPairs.get(0).getSignature());

	}
	@Test
	@DisplayName("HederaKeySignatureListTest JSONString")
	void JSONString() {
		HederaKeySignature keySig = new HederaKeySignature();
		HederaKeySignatureList sigList = new HederaKeySignatureList();
		sigList.addKeySignaturePair(keySig);
		assertNotNull(sigList.JSONString());
	}
	
}

