package test.hedera.sdk.file;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaFileID;
import com.hedera.sdk.common.HederaTimeStamp;
import com.hedera.sdk.common.HederaTransactionID;
import com.hedera.sdk.file.HederaFile;
import com.hederahashgraph.api.proto.java.TransactionBody;

class HederaFileTest {

	@Test
	@DisplayName("TestHederaFile")
	void test() {
		HederaFile file = new HederaFile();
		assertNotNull(file.txQueryDefaults);
		assertNotNull(file.expirationTime);
		assertNull(file.hederaTransactionID);
		assertArrayEquals(new byte[0], file.contents);
		assertNull(file.appendContents);
		assertEquals(0, file.shardNum);
		assertEquals(0, file.realmNum);
		assertEquals(0, file.fileNum);
		assertNull(file.newRealmAdminKey);

		file = new HederaFile(1, 2, 3);
		assertNotNull(file.txQueryDefaults);
		assertNotNull(file.expirationTime);
		assertNull(file.hederaTransactionID);
		assertArrayEquals(new byte[0], file.contents);
		assertNull(file.appendContents);
		assertEquals(1, file.shardNum);
		assertEquals(2, file.realmNum);
		assertEquals(3, file.fileNum);
		assertNull(file.newRealmAdminKey);

		HederaTransactionID txId = new HederaTransactionID(new HederaAccountID(2, 3, 4));
		txId.transactionValidStart = new HederaTimeStamp(10, 100);
		file = new HederaFile(txId);
		assertNotNull(file.hederaTransactionID);
		assertEquals(txId.accountID.shardNum, file.hederaTransactionID.accountID.shardNum);
		assertEquals(txId.accountID.realmNum, file.hederaTransactionID.accountID.realmNum);
		assertEquals(txId.accountID.accountNum, file.hederaTransactionID.accountID.accountNum);
		
		HederaFileID fileID = file.getFileID();
		assertEquals(file.shardNum, fileID.shardNum);
		assertEquals(file.realmNum, fileID.realmNum);
		assertEquals(file.fileNum, fileID.fileNum);
		
		file.setFileID(new HederaFileID(4, 5, 6));
		assertEquals(4, file.shardNum);
		assertEquals(5, file.realmNum);
		assertEquals(6, file.fileNum);
	}	
	@Test
	@DisplayName("TestPayload-Create")
	void TestPayload_Create() {
		
		HederaAccountID nodeAccount = new HederaAccountID(10, 20, 30);
		HederaDuration duration = new HederaDuration(40, 50);
		
		HederaTransactionID txId = new HederaTransactionID(new HederaAccountID(2, 3, 4));
		txId.transactionValidStart = new HederaTimeStamp(10, 100);
		HederaFile file = new HederaFile(txId);

		file.contents = "contents".getBytes();
		TransactionBody create = file.bodyToSignForCreate(txId, nodeAccount, 10, duration, true, "A Memo");
		
		assertEquals(2, create.getTransactionID().getAccountID().getShardNum());
		assertEquals(3, create.getTransactionID().getAccountID().getRealmNum());
		assertEquals(4, create.getTransactionID().getAccountID().getAccountNum());
		
		assertEquals(10, create.getNodeAccountID().getShardNum());
		assertEquals(20, create.getNodeAccountID().getRealmNum());
		assertEquals(30, create.getNodeAccountID().getAccountNum());
		
		assertEquals(40, create.getTransactionValidDuration().getSeconds());
		assertEquals(50, create.getTransactionValidDuration().getNanos());
		
		assertEquals(10, create.getTransactionFee());
		assertTrue(create.getGenerateRecord());
		assertEquals("A Memo", create.getMemo());

		assertEquals(file.expirationTime.getEpochSecond(), create.getFileCreate().getExpirationTime().getSeconds());
		assertEquals(file.expirationTime.getNano(), create.getFileCreate().getExpirationTime().getNanos());

		assertEquals(0, create.getFileCreate().getKeys().getKeysCount());
		assertEquals(0, create.getFileCreate().getKeys().getKeysCount());

		assertArrayEquals("contents".getBytes(), create.getFileCreate().getContents().toByteArray());

		assertEquals(file.shardNum, create.getFileCreate().getShardID().getShardNum());
		assertEquals(file.shardNum, create.getFileCreate().getRealmID().getShardNum());
		assertEquals(file.realmNum, create.getFileCreate().getRealmID().getRealmNum());

		assertFalse(create.getFileCreate().hasNewRealmAdminKey());
		
	}	

	@Test
	@DisplayName("TestPayload-Delete")
	void TestPayload_Delete() {
		
		HederaAccountID nodeAccount = new HederaAccountID(10, 20, 30);
		HederaDuration duration = new HederaDuration(40, 50);
		
		HederaTransactionID txId = new HederaTransactionID(new HederaAccountID(2, 3, 4));
		txId.transactionValidStart = new HederaTimeStamp(10, 100);
		HederaFile file = new HederaFile(txId);
		
		TransactionBody transaction = file.bodyToSignForDelete(txId, nodeAccount, 10, duration, true, "A Memo");
		
		assertEquals(file.shardNum, transaction.getFileDelete().getFileID().getShardNum());
		assertEquals(file.realmNum, transaction.getFileDelete().getFileID().getRealmNum());
		assertEquals(file.fileNum, transaction.getFileDelete().getFileID().getFileNum());
		
	}	

	@Test
	@DisplayName("TestPayload-Update")
	void TestPayload_Update() {
		
		HederaAccountID nodeAccount = new HederaAccountID(10, 20, 30);
		HederaDuration duration = new HederaDuration(40, 50);
		
		HederaTransactionID txId = new HederaTransactionID(new HederaAccountID(2, 3, 4));
		txId.transactionValidStart = new HederaTimeStamp(10, 100);
		HederaFile file = new HederaFile(txId);
		file.contents = "New contents".getBytes();
		
		TransactionBody transaction = file.bodyToSignForUpdate(txId, nodeAccount, 10, duration, true, "A Memo");
		
		assertEquals(file.shardNum, transaction.getFileUpdate().getFileID().getShardNum());
		assertEquals(file.realmNum, transaction.getFileUpdate().getFileID().getRealmNum());
		assertEquals(file.fileNum, transaction.getFileUpdate().getFileID().getFileNum());

		assertArrayEquals("New contents".getBytes(), transaction.getFileUpdate().getContents().toByteArray());
		
		assertEquals(file.expirationTime.getEpochSecond(), transaction.getFileUpdate().getExpirationTime().getSeconds());
		assertEquals(file.expirationTime.getNano(), transaction.getFileUpdate().getExpirationTime().getNanos());
		
		assertFalse(transaction.getFileUpdate().hasKeys());
	}	

	@Test
	@DisplayName("TestPayload-Append")
	void TestPayload_Append() {
		
		HederaAccountID nodeAccount = new HederaAccountID(10, 20, 30);
		HederaDuration duration = new HederaDuration(40, 50);
		
		HederaTransactionID txId = new HederaTransactionID(new HederaAccountID(2, 3, 4));
		txId.transactionValidStart = new HederaTimeStamp(10, 100);
		HederaFile file = new HederaFile(txId);
		file.appendContents = "New contents".getBytes();
		
		TransactionBody transaction = file.bodyToSignForAppend(txId, nodeAccount, 10, duration, true, "A Memo");
		
		assertEquals(file.shardNum, transaction.getFileAppend().getFileID().getShardNum());
		assertEquals(file.realmNum, transaction.getFileAppend().getFileID().getRealmNum());
		assertEquals(file.fileNum, transaction.getFileAppend().getFileID().getFileNum());

		assertArrayEquals("New contents".getBytes(), transaction.getFileAppend().getContents().toByteArray());
		
	}	
}
