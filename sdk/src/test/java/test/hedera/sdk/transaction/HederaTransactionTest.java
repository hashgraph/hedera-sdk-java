package test.hedera.sdk.transaction;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.common.HederaSignature;
import com.hedera.sdk.common.HederaSignatureList;
import com.hedera.sdk.common.HederaSignatures;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.node.HederaNode;
import com.hedera.sdk.transaction.HederaTransaction;
import com.hedera.sdk.transaction.HederaTransactionBody;
import com.hedera.sdk.transaction.HederaTransactionBody.TransactionType;
import com.hederahashgraph.api.proto.java.Transaction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaTransactionTest {
	@Test
	@DisplayName("Init body and key signature list")
	void testBodyKeySigs() {
		HederaTransactionBody body = new HederaTransactionBody();

		body.transactionType = TransactionType.CONTRACTCALL;
		body.memo = "body memo";
		body.data = new HederaContract().getCallTransactionBody();
		
		HederaSignatures keySigs = new HederaSignatures();
		keySigs.addSignature("key1".getBytes(), "signature1".getBytes());
		keySigs.addSignature("key2".getBytes(), "signature2".getBytes());
		
		HederaTransaction transaction = new HederaTransaction(body, keySigs);
		
		assertEquals("body memo", transaction.body.memo);

		Transaction trans = transaction.getProtobuf();
	}

	@Test
	@DisplayName("Init body and signature list")
	void testBodySigs() {
		HederaTransactionBody body = new HederaTransactionBody();

		body.transactionType = TransactionType.CONTRACTCALL;
		body.memo = "body memo";
		body.data = new HederaContract().getCallTransactionBody();

		HederaSignatures keySigs = new HederaSignatures();
		keySigs.addSignature("key1".getBytes(), "signature1".getBytes());
		keySigs.addSignature("key2".getBytes(), "signature2".getBytes());
		
		HederaTransaction transaction = new HederaTransaction(body, keySigs);
		
		assertEquals("body memo", transaction.body.memo);
		
		Transaction trans = transaction.getProtobuf();
	}

	@Test
	@DisplayName("Init test")
	void testInit() {
		HederaTransaction transaction = new HederaTransaction();
		HederaNode node = new HederaNode();
		
		transaction.setNode(node);
		assertNull(transaction.transactionReceipt());
		assertNull(transaction.transactionRecord());
		assertEquals(0, transaction.getCost());
		assertArrayEquals(new byte[0], transaction.getStateProof());

	}
}

