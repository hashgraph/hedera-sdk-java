package test.hedera.sdk.query;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.hedera.sdk.common.HederaKeyPair.KeyType;
<<<<<<< HEAD:sdk/src/test/java/test/hedera/sdk/query/QueryHeaderTest.java
import com.hedera.sdk.common.HederaKeySignatureList;
=======
import com.hedera.sdk.common.HederaSignature;
import com.hedera.sdk.common.HederaSignatureList;
>>>>>>> 959a7d6... Removed trace logging. Updated pom.xml to ignore errors on javadocs:src/test/java/test/hedera/sdk/query/QueryHeaderTest.java
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.query.HederaQueryHeader;
import com.hedera.sdk.transaction.HederaTransaction;
import com.hedera.sdk.transaction.HederaTransactionBody;
import com.hedera.sdk.transaction.HederaTransactionBody.TransactionType;
import com.hederahashgraph.api.proto.java.QueryHeader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QueryHeaderTest {

	@Test
	@DisplayName("TestQueryHeader")
	void TestQueryHeader() {
		HederaQueryHeader header = new HederaQueryHeader();

		assertNull(header.payment);
		assertEquals(com.hedera.sdk.query.HederaQueryHeader.QueryResponseType.ANSWER_ONLY, header.responseType);
		HederaTransaction payment = new HederaTransaction();
		header.payment = payment;
		assertNotNull(header.payment);

		HederaTransactionBody body = new HederaTransactionBody();

		body.transactionType = TransactionType.CONTRACTCALL;
		body.memo = "body memo";
		body.data = new HederaContract().getCallTransactionBody();
		
		HederaKeySignatureList keySigs = new HederaKeySignatureList();
		keySigs.addKeySignaturePair(KeyType.ED25519, "key1".getBytes(), "signature1".getBytes());
//		keySigs.addKeySignaturePair(KeyType.ECDSA384, "key2".getBytes(), "signature2".getBytes());
		
		HederaTransaction transaction = new HederaTransaction(body, keySigs);
		
		header = new HederaQueryHeader(transaction, com.hedera.sdk.query.HederaQueryHeader.QueryResponseType.ANSWER_ONLY);
		assertNotNull(header.payment);
		assertEquals(com.hedera.sdk.query.HederaQueryHeader.QueryResponseType.ANSWER_ONLY, header.responseType);

		header = new HederaQueryHeader(transaction, com.hedera.sdk.query.HederaQueryHeader.QueryResponseType.NOTSET);
		assertEquals(com.hedera.sdk.query.HederaQueryHeader.QueryResponseType.NOTSET, header.responseType);

		header = new HederaQueryHeader(transaction, com.hedera.sdk.query.HederaQueryHeader.QueryResponseType.ANSWER_STATE_PROOF);
		assertEquals(com.hedera.sdk.query.HederaQueryHeader.QueryResponseType.ANSWER_STATE_PROOF, header.responseType);

		header = new HederaQueryHeader(transaction, com.hedera.sdk.query.HederaQueryHeader.QueryResponseType.ANSWER_STATE_PROOF);
		assertEquals(com.hedera.sdk.query.HederaQueryHeader.QueryResponseType.ANSWER_STATE_PROOF, header.responseType);

		header = new HederaQueryHeader(transaction, com.hedera.sdk.query.HederaQueryHeader.QueryResponseType.COST_ANSWER_STATE_PROOF);
		assertEquals(com.hedera.sdk.query.HederaQueryHeader.QueryResponseType.COST_ANSWER_STATE_PROOF, header.responseType);

		
		QueryHeader protoHeader = header.getProtobuf();
		
		assertTrue(protoHeader.hasPayment());
		assertEquals(com.hederahashgraph.api.proto.java.ResponseType.COST_ANSWER_STATE_PROOF_VALUE, protoHeader.getResponseType().getNumber());
		
	}
}
