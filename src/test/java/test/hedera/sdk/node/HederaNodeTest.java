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
		assertEquals(10, node.accountCreateTransactionFee);
		assertEquals(10, node.accountTransferTransactionFee);
		assertEquals(10, node.accountUpdateTransactionFee);
		assertEquals(10, node.accountDeleteTransactionFee);
		assertEquals(10, node.accountAddClaimTransactionFee);
		assertEquals(10, node.accountDeleteClaimTransactionFee);
		assertEquals(10, node.accountBalanceQueryFee);
		assertEquals(10, node.accountInfoQueryFee);
		assertEquals(10, node.accountGetRecordsQueryFee);
		assertEquals(10, node.fileCreateTransactionFee);
		assertEquals(10, node.fileDeleteTransactionFee);
		assertEquals(10, node.fileUpdateTransactionFee);
		assertEquals(10, node.fileAppendTransactionFee);
		assertEquals(10, node.fileGetContentsQueryFee);
		assertEquals(10, node.fileGetInfoQueryFee);
		assertEquals(10, node.fileGetRecordsQueryFee);
		assertEquals(10, node.contractCreateTransactionFee);
		assertEquals(10, node.contractUpdateTransactionFee);
		assertEquals(10, node.contractGetByteCodeQueryFee);
		assertEquals(10, node.contractCallTransactionFee);
		assertEquals(10, node.contractGetInfoQueryFee);
		assertEquals(10, node.contractCallLocalQueryFee);
		assertEquals(10, node.contractGetBySolidityId);
		assertEquals(10, node.contractGetRecordsQueryFee);
		
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

