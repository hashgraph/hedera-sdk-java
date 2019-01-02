package test.hedera.sdk.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedera.sdk.common.HederaRealmID;
import com.hederahashgraph.api.proto.java.RealmID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaRealmIDTest {

	protected static HederaRealmID ID1;
	protected static HederaRealmID ID2;
	
	@BeforeAll
	static void initAll() {
		ID1 = new HederaRealmID();
		assertEquals(0, ID1.shardNum);
		assertEquals(0, ID1.realmNum);
		
		ID1 = new HederaRealmID(1, 2);
		RealmID proto = ID1.getProtobuf();
		ID2 = new HederaRealmID(proto);
	}

	@Test
	@DisplayName("Checking matching realm details")
	void testAccount() {
		assertEquals(ID1.realmNum, ID2.realmNum);
		assertEquals(ID1.shardNum, ID2.shardNum);
	}

}
