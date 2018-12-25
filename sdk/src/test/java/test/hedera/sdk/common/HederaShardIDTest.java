package test.hedera.sdk.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedera.sdk.common.HederaShardID;
import com.hederahashgraph.api.proto.java.ShardID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaShardIDTest {

	protected static HederaShardID ID1;
	protected static HederaShardID ID2;
	
	@BeforeAll
	static void initAll() {
		
		ID1 = new HederaShardID();
		assertEquals(0, ID1.shardNum);
				
		ID1 = new HederaShardID(2);
		ShardID proto = ID1.getProtobuf();
		ID2 = new HederaShardID(proto);
	}

	@Test
	@DisplayName("Checking matching shard details")
	void testAccount() {
		assertEquals(ID1.shardNum, ID2.shardNum);
	}

}
