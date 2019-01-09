package test.hedera.sdk.transaction;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.common.HederaSignature;
import com.hedera.sdk.common.HederaSignatureList;
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
		
		HederaSignatureList keySigs = new HederaSignatureList();
		keySigs.addSignature(new HederaSignature(KeyType.ED25519, "signature1".getBytes()));
		keySigs.addSignature(new HederaSignature(KeyType.ED25519, "signature2".getBytes()));
		
		HederaTransaction transaction = new HederaTransaction(body, keySigs);
		
		assertEquals(2, transaction.signatureList.signatures.size());
		assertEquals("body memo", transaction.body.memo);
		assertEquals(2, transaction.signatureList.signatures.size());
		
		assertEquals(KeyType.ED25519, transaction.signatureList.signatures.get(0).getSignatureType());
		assertArrayEquals("signature1".getBytes(), transaction.signatureList.signatures.get(0).getSignature());

		assertEquals(KeyType.ED25519, transaction.signatureList.signatures.get(1).getSignatureType());
		assertArrayEquals("signature2".getBytes(), transaction.signatureList.signatures.get(1).getSignature());

		assertEquals(KeyType.ED25519, transaction.signatureList.signatures.get(0).getSignatureType());
		assertArrayEquals("signature1".getBytes(), transaction.signatureList.signatures.get(0).getSignature());

		assertEquals(KeyType.ED25519, transaction.signatureList.signatures.get(1).getSignatureType());
		assertArrayEquals("signature2".getBytes(), transaction.signatureList.signatures.get(1).getSignature());

		transaction.addSignature(new HederaSignature(KeyType.ED25519, "signature3".getBytes()));
		assertEquals(3, transaction.signatureList.signatures.size());
		assertEquals(KeyType.ED25519, transaction.signatureList.signatures.get(2).getSignatureType());
		assertArrayEquals("signature3".getBytes(), transaction.signatureList.signatures.get(2).getSignature());

		transaction.addSignature(new HederaSignature(KeyType.ED25519, "signature4".getBytes()));
		assertEquals(4, transaction.signatureList.signatures.size());
		assertEquals(KeyType.ED25519, transaction.signatureList.signatures.get(3).getSignatureType());
		assertArrayEquals("signature4".getBytes(), transaction.signatureList.signatures.get(3).getSignature());

		Transaction trans = transaction.getProtobuf();
		assertEquals(4,  trans.getSigs().getSigsCount());
		assertEquals("body memo", trans.getBody().getMemo());
	}

	@Test
	@DisplayName("Init body and signature list")
	void testBodySigs() {
		HederaTransactionBody body = new HederaTransactionBody();

		body.transactionType = TransactionType.CONTRACTCALL;
		body.memo = "body memo";
		body.data = new HederaContract().getCallTransactionBody();

		HederaSignatureList sigs = new HederaSignatureList();
		sigs.addSignature(new HederaSignature(KeyType.ED25519, "signature1".getBytes()));
		sigs.addSignature(new HederaSignature(KeyType.ED25519, "signature2".getBytes()));
		
		HederaTransaction transaction = new HederaTransaction(body, sigs);
		
		assertEquals(2, transaction.signatureList.signatures.size());
		assertEquals("body memo", transaction.body.memo);
		
		assertEquals(KeyType.ED25519, transaction.signatureList.signatures.get(0).getSignatureType());
		assertArrayEquals("signature1".getBytes(), transaction.signatureList.signatures.get(0).getSignature());

		assertEquals(KeyType.ED25519, transaction.signatureList.signatures.get(1).getSignatureType());
		assertArrayEquals("signature2".getBytes(), transaction.signatureList.signatures.get(1).getSignature());

		Transaction trans = transaction.getProtobuf();
		assertEquals(2,  trans.getSigs().getSigsCount());
		assertEquals("body memo", trans.getBody().getMemo());
				
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

