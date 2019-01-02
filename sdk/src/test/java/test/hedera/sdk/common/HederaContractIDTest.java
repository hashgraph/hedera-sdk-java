package test.hedera.sdk.common;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedera.sdk.common.HederaContractID;
import com.hederahashgraph.api.proto.java.ContractID;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaContractIDTest {

	protected static HederaContractID contractID1;
	protected static HederaContractID contractID2;
	protected static HederaContractID contractID3;
	protected static JSONObject contractJSON;
	
	@BeforeAll
	static void initAll() {
		contractID1 = new HederaContractID(1, 2, 3);
		ContractID contractID1Proto = contractID1.getProtobuf();
		contractID2 = new HederaContractID(contractID1Proto);
		contractJSON = contractID1.JSON();
		contractID3 = new HederaContractID();
		contractID3.fromJSON(contractJSON);
	}

	@Test
	@DisplayName("Checking matching contract details from create")
	void testContractCreate() {
		assertEquals(1, contractID2.shardNum);
		assertEquals(2, contractID2.realmNum);
		assertEquals(3, contractID2.contractNum);
	}

	@Test
	@DisplayName("Checking matching contract details from protobuf")
	void testContractProtobuf() {
		assertEquals(contractID1.contractNum, contractID2.contractNum);
		assertEquals(contractID1.realmNum, contractID2.realmNum);
		assertEquals(contractID1.shardNum, contractID2.shardNum);
	}

	@Test
	@DisplayName("Repeat with JSON")
	void testContractJSON() {
		assertEquals(contractID1.contractNum, contractID3.contractNum);
		assertEquals(contractID1.realmNum, contractID3.realmNum);
		assertEquals(contractID1.shardNum, contractID3.shardNum);
		assertNotNull(contractID1.JSONString());
	}
	
}
