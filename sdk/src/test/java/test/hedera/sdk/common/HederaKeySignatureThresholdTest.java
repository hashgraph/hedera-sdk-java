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
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.common.HederaKeySignature;
import com.hedera.sdk.common.HederaKeySignatureThreshold;
import com.hedera.sdk.common.HederaKeyUUIDDescription;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaKeySignatureThresholdTest {
	@Test
	@DisplayName("HederaKeySignatureThreshold Set Signature tests")
	void HederaKeySignatureThresholdSet() { 
		// create key with empty signature
		byte[] aSignature = "signature".getBytes();
		byte[] aKey = "key".getBytes();
		
		// standard keys
		HederaKeySignature keySignature = new HederaKeySignature(KeyType.NOTSET, aKey, null);
		List<HederaKeySignature> keySigs = new ArrayList<HederaKeySignature>();
		keySigs.add(keySignature);
		HederaKeySignatureThreshold threshold = new HederaKeySignatureThreshold(10, keySigs);
		
		assertFalse(threshold.setSignatureForKey("dummy".getBytes(), aSignature, true));
		assertNull(threshold.keySigPairs.get(0).getSignature());
		
		keySignature = new HederaKeySignature(KeyType.ED25519, aKey, null);
		keySigs = new ArrayList<HederaKeySignature>();
		keySigs.add(keySignature);
		threshold = new HederaKeySignatureThreshold(10, keySigs);

		assertFalse(threshold.setSignatureForKey("dummy".getBytes(), aSignature, true));
		assertTrue(threshold.setSignatureForKey(aKey, aSignature, false));
		assertEquals(aSignature, threshold.keySigPairs.get(0).getSignature());

		// false positive, if the signature is set, the "set" method shouldn't update
		keySignature = new HederaKeySignature(KeyType.ED25519, aKey, "dummy".getBytes());

		keySigs = new ArrayList<HederaKeySignature>();
		keySigs.add(keySignature);
		threshold = new HederaKeySignatureThreshold(10, keySigs);
		
		assertFalse(threshold.setSignatureForKey(aKey, aSignature, false));
		assertNotEquals(aSignature, threshold.keySigPairs.get(0).getSignature());

	}

	@Test
	@DisplayName("HederaKeySignatureThreshold Set Signature tests - arrays")
	void HederaKeySignatureThresholdSetArrays() { 
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

		List<HederaKeySignature> keySigs = new ArrayList<HederaKeySignature>();
		keySigs.add(keySignature);
		HederaKeySignatureThreshold threshold = new HederaKeySignatureThreshold(10, keySigs);

		// shouldn't update anything, signature key doesn't match array
		assertFalse(threshold.setSignatureForKeys(keys, signatures, false));
		assertNull(threshold.keySigPairs.get(0).getSignature());

		keySignature = new HederaKeySignature(KeyType.ED25519, aKey1, null);
		keySigs = new ArrayList<HederaKeySignature>();
		keySigs.add(keySignature);
		threshold = new HederaKeySignatureThreshold(10, keySigs);

		// shouldn't update anything, signature key doesn't match array
		assertTrue(threshold.setSignatureForKeys(keys, signatures, false));
		assertArrayEquals(aSignature1, threshold.keySigPairs.get(0).getSignature());

	}
	
	@Test
	@DisplayName("HederaKeySignatureThreshold Set Signature via UUID tests")
	void HederaKeySignatureThresholdSetUUID() { 
		// create key with empty signature
		byte[] aSignature = "signature".getBytes();
		byte[] aKey = "key".getBytes();
		
		// standard keys
		HederaKeySignature keySignature = new HederaKeySignature(KeyType.NOTSET, aKey, null);
		List<HederaKeySignature> keySigs = new ArrayList<HederaKeySignature>();
		keySigs.add(keySignature);
		HederaKeySignatureThreshold threshold = new HederaKeySignatureThreshold(10, keySigs);

		assertFalse(threshold.setSignatureForKeyUUID("dummy", aSignature));
		assertNull(threshold.keySigPairs.get(0).getSignature());
		
		keySignature = new HederaKeySignature(KeyType.ED25519, aKey, null);
		keySigs = new ArrayList<HederaKeySignature>();
		keySigs.add(keySignature);
		threshold = new HederaKeySignatureThreshold(10, keySigs);

		assertFalse(threshold.setSignatureForKeyUUID("dummy", aSignature));
		assertTrue(threshold.setSignatureForKeyUUID(keySignature.uuid, aSignature));
		assertEquals(aSignature, threshold.keySigPairs.get(0).getSignature());
		
	}

	@Test
	@DisplayName("HederaKeySignatureThreshold Get Key UUIDs")
	void HederaKeySignatureThresholdGetKeyUUIDs() { 
		// create key with empty signature
		byte[] aKey = "key".getBytes();
		
		// standard keys
		HederaKeySignature keySignature = new HederaKeySignature(KeyType.ED25519, aKey, null);
		List<HederaKeySignature> keySigs = new ArrayList<HederaKeySignature>();
		keySigs.add(keySignature);
		HederaKeySignatureThreshold threshold = new HederaKeySignatureThreshold(10, keySigs);
		
		List<HederaKeyUUIDDescription> uuids = new ArrayList<HederaKeyUUIDDescription>();
		threshold.getKeyUUIDs(uuids, aKey);
		assertEquals(1, uuids.size());

		uuids = new ArrayList<HederaKeyUUIDDescription>();
		threshold.getKeyUUIDs(uuids, "dummmy".getBytes());
		assertEquals(0, uuids.size());
		
	}

	@Test
	@DisplayName("HederaKeySignatureThreshold Set Signature via UUIDs tests")
	void HederaKeySignatureThresholdSetUUIDs() { 
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
		List<HederaKeySignature> keySigs = new ArrayList<HederaKeySignature>();
		keySigs.add(keySignature);
		HederaKeySignatureThreshold threshold = new HederaKeySignatureThreshold(10, keySigs);

		// uuids are dummy, should not update
		assertFalse(threshold.setSignatureForKeyUUIDs(uuids, signatures));
		assertNull(threshold.keySigPairs.get(0).getSignature());
		
		uuids[0] = keySignature.uuid;
		
		assertTrue(threshold.setSignatureForKeyUUIDs(uuids, signatures));
		assertEquals(aSignature1, threshold.keySigPairs.get(0).getSignature());
		
	}
	
	@Test
	@DisplayName("HederaKeySignatureThreshold Update Signature tests")
	void HederaKeySignatureThresholdForKey() { 
		// create key with empty signature
		byte[] aSignature1 = "signature1".getBytes();
		byte[] aSignature2 = "signature2".getBytes();
		byte[] aKey1 = "key1".getBytes();
		
		// standard keys
		HederaKeySignature keySignature = new HederaKeySignature(KeyType.NOTSET, aKey1, "sig".getBytes());
		List<HederaKeySignature> keySigs = new ArrayList<HederaKeySignature>();
		keySigs.add(keySignature);
		HederaKeySignatureThreshold threshold = new HederaKeySignatureThreshold(10, keySigs);

		assertFalse(threshold.updateSignatureForKey("dummy".getBytes(), aSignature1));
		assertArrayEquals("sig".getBytes(), threshold.keySigPairs.get(0).getSignature());
		
		keySignature = new HederaKeySignature(KeyType.ED25519, aKey1, aSignature1);
		keySigs = new ArrayList<HederaKeySignature>();
		keySigs.add(keySignature);
		threshold = new HederaKeySignatureThreshold(10, keySigs);

		assertFalse(threshold.updateSignatureForKey("dummy".getBytes(), aSignature2));
		assertArrayEquals(aSignature1, keySignature.getSignature());
		assertTrue(threshold.updateSignatureForKey(aKey1, aSignature2));
		assertArrayEquals(aSignature2, threshold.keySigPairs.get(0).getSignature());

	}
	
	@Test
	@DisplayName("HederaKeySignatureThreshold Updates Signature tests")
	void HederaKeySignatureThresholdUpdateForKey() { 
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
		List<HederaKeySignature> keySigs = new ArrayList<HederaKeySignature>();
		keySigs.add(keySignature);
		HederaKeySignatureThreshold threshold = new HederaKeySignatureThreshold(10, keySigs);

		keys[0] = aKey2;
		keys[1] = "someBytes".getBytes();
		assertFalse(threshold.updateSignatureForKeys(keys, signatures));
		assertArrayEquals(aSignature1, threshold.keySigPairs.get(0).getSignature());
		
		keySignature = new HederaKeySignature(KeyType.ED25519, aKey1, aSignature2);
		keySigs = new ArrayList<HederaKeySignature>();
		keySigs.add(keySignature);
		threshold = new HederaKeySignatureThreshold(10, keySigs);
		keys[0] = aKey1;
		keys[1] = "someBytes".getBytes();
		assertTrue(threshold.updateSignatureForKeys(keys, signatures));
		assertArrayEquals(aSignature1, threshold.keySigPairs.get(0).getSignature());

	}
	@Test
	@DisplayName("HederaKeySignatureThreshold JSONString")
	void JSONString() {
		HederaKeySignatureThreshold keySig = new HederaKeySignatureThreshold();
		assertNotNull(keySig.JSONString());
	}
	
}
