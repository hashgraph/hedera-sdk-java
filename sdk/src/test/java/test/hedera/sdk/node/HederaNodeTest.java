package test.hedera.sdk.node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.node.HederaNode;

class HederaNodeTest {

	@Test
	@DisplayName("HederaNodeTest")
	void test() {
		HederaNode node = new HederaNode();
		
		assertEquals("", node.getHost());
		assertEquals(0, node.getPort());
		
		HederaAccountID accountID = new HederaAccountID(1, 2, 3);
		node.setAccountID(accountID);
		assertEquals(1, node.getAccountID().shardNum);
		assertEquals(2, node.getAccountID().realmNum);
		assertEquals(3, node.getAccountID().accountNum);
		
		node.setAccountID(4, 5, 6);
		assertEquals(4, node.getAccountID().shardNum);
		assertEquals(5, node.getAccountID().realmNum);
		assertEquals(6, node.getAccountID().accountNum);
		
	}

}

