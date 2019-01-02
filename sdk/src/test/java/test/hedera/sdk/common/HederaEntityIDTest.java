package test.hedera.sdk.common;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.hedera.sdk.account.HederaClaim;
import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaContractID;
import com.hedera.sdk.common.HederaEntityID;
import com.hedera.sdk.common.HederaFileID;
import com.hederahashgraph.api.proto.java.EntityID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HederaEntityIDTest {

	protected static HederaEntityID entitySlave;
	
	protected static HederaClaim claim;
	protected static HederaContractID contractID;
	protected static HederaFileID fileID;
	
	@Test
	@DisplayName("Checking matching account details")
	public void testEntityIDAccount() {
		HederaAccountID accountID = new HederaAccountID(1, 2, 3);
		EntityID.Builder entityID = EntityID.newBuilder();
		entityID.setAccountID(accountID.getProtobuf());
		entitySlave = new HederaEntityID(entityID.build());

		assertEquals(accountID.accountNum, entitySlave.accountID.accountNum);
		assertEquals(accountID.realmNum, entitySlave.accountID.realmNum);
		assertEquals(accountID.shardNum, entitySlave.accountID.shardNum);
		assertEquals(null, entitySlave.contractID);
		assertEquals(null, entitySlave.fileID);
		assertEquals(null, entitySlave.claim);
	}

	@Test
	@DisplayName("Checking matching contract details")
	public void testEntityIDContract() {
		HederaContractID contractID = new HederaContractID(4, 5, 6);
		EntityID.Builder entityID = EntityID.newBuilder();
		entityID.setContractID(contractID.getProtobuf());
		entitySlave = new HederaEntityID(entityID.build());

		assertEquals(contractID.contractNum, entitySlave.contractID.contractNum);
		assertEquals(contractID.realmNum, entitySlave.contractID.realmNum);
		assertEquals(contractID.shardNum, entitySlave.contractID.shardNum);
		assertEquals(null, entitySlave.accountID);
		assertEquals(null, entitySlave.fileID);
		assertEquals(null, entitySlave.claim);
	}

	@Test
	@DisplayName("Checking matching file details")
	public void testEntityIDFile() {
		HederaFileID fileID = new HederaFileID(7, 8, 9);
		EntityID.Builder entityID = EntityID.newBuilder();
		entityID.setFileID(fileID.getProtobuf());
		entitySlave = new HederaEntityID(entityID.build());

		assertEquals(fileID.fileNum, entitySlave.fileID.fileNum);
		assertEquals(fileID.realmNum, entitySlave.fileID.realmNum);
		assertEquals(fileID.shardNum, entitySlave.fileID.shardNum);
		assertEquals(null, entitySlave.accountID);
		assertEquals(null, entitySlave.contractID);
		assertEquals(null, entitySlave.claim);
	}

	@Test
	@DisplayName("Checking matching hash details")
	public void testEntityIDClaim() {
		HederaClaim claim = new HederaClaim(10, 11, 12, new byte[] {1,4,7,9,12});
		EntityID.Builder entityID = EntityID.newBuilder();
		entityID.setClaim(claim.getProtobuf());
		entitySlave = new HederaEntityID(entityID.build());

		assertEquals(claim.accountNum, entitySlave.claim.accountNum);
		assertEquals(claim.realmNum, entitySlave.claim.realmNum);
		assertEquals(claim.shardNum, entitySlave.claim.shardNum);
		assertArrayEquals(claim.hash, entitySlave.claim.hash);

		assertEquals(null, entitySlave.fileID);
		assertEquals(null, entitySlave.accountID);
		assertEquals(null, entitySlave.contractID);
	}
}
