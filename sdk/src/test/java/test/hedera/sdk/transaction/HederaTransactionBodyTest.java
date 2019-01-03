package test.hedera.sdk.transaction;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.account.HederaAccountAmount;
import com.hedera.sdk.account.HederaClaim;
import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaFileID;
import com.hedera.sdk.common.HederaTransactionID;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.file.HederaFile;
import com.hedera.sdk.transaction.HederaTransactionBody;
import com.hedera.sdk.transaction.HederaTransactionBody.TransactionType;
import com.hederahashgraph.api.proto.java.TransactionBody;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaTransactionBodyTest {

	@Test
	@DisplayName("TestHederaTransactionBody")
	void TestHederaTransactionBody() {

		HederaTransactionID transactionID = new HederaTransactionID();
		transactionID.accountID = new HederaAccountID(1, 2, 3);
		HederaAccountID nodeAccount = new HederaAccountID(4, 5, 6);

		long transactionFee = 20;
		HederaDuration transactionValidDuration = new HederaDuration(100, 60);

		HederaTransactionBody  body = new HederaTransactionBody(TransactionType.CONTRACTCALL
				, transactionID
				, nodeAccount
				, transactionFee
				, transactionValidDuration
				, true
				, "transaction memo"
				, new HederaContract().getCallTransactionBody());

		// check individual values
		assertEquals(1,  body.transactionId.accountID.shardNum);
		assertEquals(2,  body.transactionId.accountID.realmNum);
		assertEquals(3,  body.transactionId.accountID.accountNum);

		assertEquals(4,  body.nodeAccount.shardNum);
		assertEquals(5,  body.nodeAccount.realmNum);
		assertEquals(6,  body.nodeAccount.accountNum);

		assertEquals(20,  body.transactionFee);
		assertEquals(100, body.transactionValidDuration.seconds);
		assertEquals(60, body.transactionValidDuration.nanos);

		assertEquals(TransactionType.CONTRACTCALL, body.transactionType);
		assertTrue(body.generateRecord);
		assertEquals("transaction memo", body.memo);

		TransactionBody tbody = body.getProtobuf();

		// check individual values
		assertEquals(1,  tbody.getTransactionID().getAccountID().getShardNum());
		assertEquals(2,  tbody.getTransactionID().getAccountID().getRealmNum());
		assertEquals(3,  tbody.getTransactionID().getAccountID().getAccountNum());

		assertEquals(4,  tbody.getNodeAccountID().getShardNum());
		assertEquals(5,  tbody.getNodeAccountID().getRealmNum());
		assertEquals(6,  tbody.getNodeAccountID().getAccountNum());

		assertEquals(20,  tbody.getTransactionFee());
		assertEquals(100, tbody.getTransactionValidDuration().getSeconds());
		assertEquals(60, tbody.getTransactionValidDuration().getNanos());

		assertTrue(tbody.hasContractCall());
		assertTrue(tbody.getGenerateRecord());
		assertEquals("transaction memo", tbody.getMemo());

		HederaContract contract = new HederaContract();
		contract.autoRenewPeriod = new HederaDuration(10, 10);
		contract.fileID = new HederaFileID(1, 1, 1);
		
		body = new HederaTransactionBody(TransactionType.CONTRACTCREATEINSTANCE, transactionID, nodeAccount, transactionFee, transactionValidDuration
				, true, "transaction memo", contract.getCreateTransactionBody());
		assertEquals(TransactionType.CONTRACTCREATEINSTANCE, body.transactionType);
		TransactionBody protoBody = body.getProtobuf();
		assertNotNull(protoBody);

		body = new HederaTransactionBody(TransactionType.CONTRACTUPDATEINSTANCE, transactionID, nodeAccount, transactionFee, transactionValidDuration
				, true, "transaction memo", contract.getUpdateTransactionBody());
		assertEquals(TransactionType.CONTRACTUPDATEINSTANCE, body.transactionType);
		protoBody = body.getProtobuf();
		assertNotNull(protoBody);

		body = new HederaTransactionBody(TransactionType.CRYPTOADDCLAIM, transactionID, nodeAccount, transactionFee, transactionValidDuration
				, true, "transaction memo", new HederaAccount().getAddClaimTransactionBody(new HederaClaim()));
		assertEquals(TransactionType.CRYPTOADDCLAIM, body.transactionType);
		protoBody = body.getProtobuf();
		assertNotNull(protoBody);

		body = new HederaTransactionBody(TransactionType.CRYPTOCREATEACCOUNT, transactionID, nodeAccount, transactionFee, transactionValidDuration
				, true, "transaction memo", new HederaAccount().getCreateTransactionBody());
		assertEquals(TransactionType.CRYPTOCREATEACCOUNT, body.transactionType);
		protoBody = body.getProtobuf();
		assertNotNull(protoBody);

		body = new HederaTransactionBody(TransactionType.CRYPTODELETE, transactionID, nodeAccount, transactionFee, transactionValidDuration
				, true, "transaction memo", new HederaAccount().getDeleteTransactionBody(new HederaAccountID()));
		assertEquals(TransactionType.CRYPTODELETE, body.transactionType);
		protoBody = body.getProtobuf();
		assertNotNull(protoBody);

		body = new HederaTransactionBody(TransactionType.CRYPTODELETECLAIM, transactionID, nodeAccount, transactionFee, transactionValidDuration
				, true, "transaction memo", new HederaAccount().getDeleteClaimTransactionBody(new HederaClaim()));
		assertEquals(TransactionType.CRYPTODELETECLAIM, body.transactionType);
		protoBody = body.getProtobuf();
		assertNotNull(protoBody);

		body = new HederaTransactionBody(TransactionType.CRYPTOTRANSFER, transactionID, nodeAccount, transactionFee, transactionValidDuration
				, true, "transaction memo", new HederaAccount().getTransferTransactionBody(new ArrayList<HederaAccountAmount>()));
		assertEquals(TransactionType.CRYPTOTRANSFER, body.transactionType);
		protoBody = body.getProtobuf();
		assertNotNull(protoBody);

		body = new HederaTransactionBody(TransactionType.CRYPTOUPDATEACCOUNT, transactionID, nodeAccount, transactionFee, transactionValidDuration
				, true, "transaction memo", new HederaAccount().getUpdateTransactionBody());
		assertEquals(TransactionType.CRYPTOUPDATEACCOUNT, body.transactionType);
		protoBody = body.getProtobuf();
		assertNotNull(protoBody);

		body = new HederaTransactionBody(TransactionType.FILEAPPEND, transactionID, nodeAccount, transactionFee, transactionValidDuration
				, true, "transaction memo", new HederaFile().getAppendTransactionBody());
		assertEquals(TransactionType.FILEAPPEND, body.transactionType);
		protoBody = body.getProtobuf();
		assertNotNull(protoBody);

		body = new HederaTransactionBody(TransactionType.FILECREATE, transactionID, nodeAccount, transactionFee, transactionValidDuration
				, true, "transaction memo", new HederaFile().getCreateTransactionBody());
		assertEquals(TransactionType.FILECREATE, body.transactionType);
		protoBody = body.getProtobuf();
		assertNotNull(protoBody);

		body = new HederaTransactionBody(TransactionType.FILEDELETE, transactionID, nodeAccount, transactionFee, transactionValidDuration
				, true, "transaction memo", new HederaFile().getDeleteTransactionBody());
		assertEquals(TransactionType.FILEDELETE, body.transactionType);
		protoBody = body.getProtobuf();
		assertNotNull(protoBody);

		body = new HederaTransactionBody(TransactionType.FILEUPDATE, transactionID, nodeAccount, transactionFee, transactionValidDuration
				, true, "transaction memo", new HederaFile().getUpdateTransactionBody());
		assertEquals(TransactionType.FILEUPDATE, body.transactionType);
		protoBody = body.getProtobuf();
		assertNotNull(protoBody);
	}
}
