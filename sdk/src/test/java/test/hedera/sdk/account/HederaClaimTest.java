package test.hedera.sdk.account;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hedera.sdk.account.HederaClaim;
import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.common.HederaKeySignature;
import com.hederahashgraph.api.proto.java.Claim;

class HederaClaimTest {

	@Test
	@DisplayName("TestHederaClaim")
	void test() {
		HederaClaim claim = new HederaClaim();
		assertEquals(0,  claim.shardNum);
		assertEquals(0,  claim.realmNum);
		assertEquals(0,  claim.accountNum);
		assertArrayEquals(new byte[0], claim.hash);
		assertEquals(0,  claim.keys.size());
		assertEquals(0,  claim.keySignatures.size());

		claim = new HederaClaim(2, 3, 4, "hash".getBytes());
		assertEquals(2,  claim.shardNum);
		assertEquals(3,  claim.realmNum);
		assertEquals(4,  claim.accountNum);
		assertArrayEquals("hash".getBytes(), claim.hash);
		assertEquals(0,  claim.keys.size());
		assertEquals(0,  claim.keySignatures.size());
		
		HederaAccountID accountID = new HederaAccountID(2, 3, 4);
		claim = new HederaClaim(accountID, "hash".getBytes());
		assertEquals(2,  claim.shardNum);
		assertEquals(3,  claim.realmNum);
		assertEquals(4,  claim.accountNum);
		assertArrayEquals("hash".getBytes(), claim.hash);
		assertEquals(0,  claim.keys.size());
		assertEquals(0,  claim.keySignatures.size());
		
		Claim claimProto = claim.getProtobuf();
		HederaClaim claim2 = new HederaClaim(claimProto);

		assertEquals(claim.shardNum,  claim2.shardNum);
		assertEquals(claim.realmNum,  claim2.realmNum);
		assertEquals(claim.accountNum,  claim2.accountNum);
		assertArrayEquals(claim.hash, claim2.hash);
		assertEquals(claim.keys.size(),  claim2.keys.size());
		assertEquals(claim.keySignatures.size(),  claim2.keySignatures.size());
		
		HederaKeyPair key = new HederaKeyPair(KeyType.ED25519);
		HederaKeyPair key2 = new HederaKeyPair(KeyType.ED25519);
		
		claim.addKey(key);
		claim.addKey(key2);
		assertEquals(2, claim.keys.size());
		assertEquals(2, claim.getKeys().size());
		
		claim.deleteKey(key);
		assertEquals(1, claim.keys.size());
		claim.deleteKey(key2);
		assertEquals(0, claim.keys.size());
		
		HederaKeySignature sig1 = new HederaKeySignature(KeyType.ED25519, key.getSecretKey(), "signature".getBytes());
		HederaKeySignature sig2 = new HederaKeySignature(KeyType.ED25519, key.getSecretKey(), "signature2".getBytes());
		claim.addKeySignaturePair(sig1);
		claim.addKeySignaturePair(sig2);
		assertEquals(2, claim.keySignatures.size());
		assertEquals(2, claim.getKeySignatures().size());

		claim.deleteKeySignaturePair(sig1);
		assertEquals(1,  claim.keySignatures.size());
		claim.deleteKeySignaturePair(sig2);
		assertEquals(0,  claim.keySignatures.size());
		
		claim.addKeySignaturePair(new HederaKeySignature(KeyType.ED25519, key.getPublicKey(), "signature".getBytes()));
		claim.addKeySignaturePair(new HederaKeySignature(KeyType.ED25519, key2.getPublicKey(), "signature2".getBytes()));
		claimProto = claim.getProtobuf();

		HederaClaim claimWithSig = new HederaClaim(claimProto);
		assertEquals(2, claimWithSig.keys.size());
		assertEquals(2, claimWithSig.keys.size());
		assertEquals(KeyType.ED25519, claimWithSig.keys.get(0).getKeyType());
		assertArrayEquals(key.getPublicKey(), claimWithSig.keys.get(0).getPublicKey());
		assertEquals(KeyType.ED25519, claimWithSig.keys.get(1).getKeyType());
		assertArrayEquals(key2.getPublicKey(), claimWithSig.keys.get(1).getPublicKey());
		
		assertEquals(2, claimWithSig.keySignatures.size());
		assertEquals(2, claimWithSig.keySignatures.size());
		assertEquals(KeyType.ED25519, claimWithSig.keySignatures.get(0).getKeyType());
		assertArrayEquals(key.getPublicKeyEncoded(), claimWithSig.keySignatures.get(0).getKey());
		assertEquals(KeyType.ED25519, claimWithSig.keySignatures.get(1).getKeyType());
		assertArrayEquals(key2.getPublicKeyEncoded(), claimWithSig.keySignatures.get(1).getKey());

		claim = new HederaClaim();
		claim.addKey(new HederaKeyPair(KeyType.ED25519, key.getPublicKey(), null));
		claim.addKey(new HederaKeyPair(KeyType.ED25519, key2.getPublicKey(), null));
		claimProto = claim.getProtobuf();

		claimWithSig = new HederaClaim(claimProto);

		assertEquals(2, claimWithSig.keys.size());
		assertEquals(2, claimWithSig.keys.size());
		assertEquals(KeyType.ED25519, claimWithSig.keys.get(0).getKeyType());
		assertArrayEquals(key.getPublicKey(), claimWithSig.keys.get(0).getPublicKey());
		assertEquals(KeyType.ED25519, claimWithSig.keys.get(1).getKeyType());
		assertArrayEquals(key2.getPublicKey(), claimWithSig.keys.get(1).getPublicKey());
		
		assertEquals(2, claimWithSig.keySignatures.size());
		assertEquals(2, claimWithSig.keySignatures.size());
		assertEquals(KeyType.ED25519, claimWithSig.keySignatures.get(0).getKeyType());
		assertArrayEquals(key.getPublicKeyEncoded(), claimWithSig.keySignatures.get(0).getKey());
		assertEquals(KeyType.ED25519, claimWithSig.keySignatures.get(1).getKeyType());
		assertArrayEquals(key2.getPublicKeyEncoded(), claimWithSig.keySignatures.get(1).getKey());
	}
}

