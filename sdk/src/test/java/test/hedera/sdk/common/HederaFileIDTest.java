package test.hedera.sdk.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedera.sdk.common.HederaFileID;
import com.hederahashgraph.api.proto.java.FileID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaFileIDTest {

	protected static HederaFileID ID1;
	protected static HederaFileID ID2;
	
	@BeforeAll
	static void initAll() {
		
		ID1 = new HederaFileID();
		assertEquals(0, ID1.shardNum);
		assertEquals(0, ID1.realmNum);
		assertEquals(0, ID1.fileNum);

		
		ID1 = new HederaFileID(1, 2, 3);
		FileID proto = ID1.getProtobuf();
		ID2 = new HederaFileID(proto);
	}

	@Test
	@DisplayName("Checking matching file details from create")
	void testFileCreate() {
		assertEquals(1, ID2.shardNum);
		assertEquals(2, ID2.realmNum);
		assertEquals(3, ID2.fileNum);
	}
	@Test
	@DisplayName("Checking matching file details from protobuf")
	void testFileProtobuf() {
		assertEquals(ID1.fileNum, ID2.fileNum);
		assertEquals(ID1.realmNum, ID2.realmNum);
		assertEquals(ID1.shardNum, ID2.shardNum);
	}
}
