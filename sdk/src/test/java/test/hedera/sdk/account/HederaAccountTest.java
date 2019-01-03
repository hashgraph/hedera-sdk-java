package test.hedera.sdk.account;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaTimeStamp;
import com.hedera.sdk.common.HederaTransactionID;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hederahashgraph.api.proto.java.TransactionBody;

class HederaAccountTest {

	@Test
	@DisplayName("testHederaAccount")
	void test() {
		HederaAccount account = new HederaAccount();
		
		assertNotNull(account.txQueryDefaults);

		assertEquals(0, account.shardNum);
		assertEquals(0, account.realmNum);
		assertEquals(0, account.accountNum);

		assertNull(account.hederaTransactionID);
		assertNull(account.newRealmAdminKey);

		assertNull(account.accountKey);

		assertEquals(0, account.initialBalance);

		assertEquals(0, account.proxyAccountID.shardNum);
		assertEquals(0, account.proxyAccountID.realmNum);
		assertEquals(0, account.proxyAccountID.accountNum);
		assertEquals(0, account.proxyFraction);
		assertEquals(0, account.maxReceiveProxyFraction);
		assertEquals(Long.MAX_VALUE, account.sendRecordThreshold);
		assertEquals(Long.MAX_VALUE, account.receiveRecordThreshold);
		assertFalse(account.receiverSigRequired);
		assertEquals(60 * 60 * 24 * 30, account.autoRenewPeriod.seconds);
		assertEquals(0, account.autoRenewPeriod.nanos);
		assertEquals(0, account.claims.size());
		assertNull(account.expirationTime);

		assertEquals("", account.getSolidityContractAccountID());
		assertFalse(account.getDeleted());
		assertEquals(0, account.getProxyReceived());
		assertNotNull(account.getProxyStakers());
		
		account.setHederaAccountID(new HederaAccountID(3, 4, 5));
		assertEquals(3, account.shardNum);
		assertEquals(4, account.realmNum);
		assertEquals(5, account.accountNum);
		
		assertEquals(3, account.getHederaAccountID().shardNum);
		assertEquals(4, account.getHederaAccountID().realmNum);
		assertEquals(5, account.getHederaAccountID().accountNum);
		
		account = new HederaAccount(7, 8, 9);
		assertEquals(7, account.shardNum);
		assertEquals(8, account.realmNum);
		assertEquals(9, account.accountNum);
		
		HederaTransactionID txID = new HederaTransactionID(new HederaAccountID(6, 5, 4), new HederaTimeStamp(100, 10));
		account = new HederaAccount(txID);
		assertEquals(6, account.hederaTransactionID.accountID.shardNum);
		assertEquals(5, account.hederaTransactionID.accountID.realmNum);
		assertEquals(4, account.hederaTransactionID.accountID.accountNum);
		assertEquals(100, account.hederaTransactionID.transactionValidStart.seconds());
		assertEquals(10, account.hederaTransactionID.transactionValidStart.nanos());

		assertEquals(0, account.getCost());
		assertArrayEquals(new byte[0], account.getStateProof());

		account = new HederaAccount();
		HederaKeyPair key = new HederaKeyPair(KeyType.ED25519, "key".getBytes(), null);
		HederaKeyPair key2 = new HederaKeyPair(KeyType.ED25519, "key2".getBytes(), null);
		
		account.addKey(key);
		account.addKey(key2);
		assertEquals(2, account.getKeys().size());
		account.deleteKey(key);
		assertEquals(1, account.getKeys().size());
		account.deleteKey(key2);
		assertEquals(0, account.getKeys().size());
	}

	@Test
	@DisplayName("testHederaAccount-create")
	void test_create() {
		HederaAccount account = new HederaAccount();
		HederaTransactionID transactionID = new HederaTransactionID(new HederaAccountID(6, 5, 4), new HederaTimeStamp(100, 10));
		HederaAccountID nodeAccount = new HederaAccountID(1, 2, 3);
		HederaDuration transactionValidDuration = new HederaDuration(100, 10);
		
		account.autoRenewPeriod = new HederaDuration(200, 20);
		account.initialBalance = 10;
		account.receiverSigRequired = true;
		account.maxReceiveProxyFraction = 101;
		account.receiveRecordThreshold = 202;
		account.sendRecordThreshold = 303;
		account.realmNum = 200;
		account.shardNum = 300;
		account.proxyAccountID = new HederaAccountID(10, 20, 30);
		
		TransactionBody body = account.bodyToSignForCreate(transactionID, nodeAccount, 10, transactionValidDuration, true, "A memo");

		assertEquals(200, body.getCryptoCreateAccount().getAutoRenewPeriod().getSeconds());
		assertEquals(20, body.getCryptoCreateAccount().getAutoRenewPeriod().getNanos());
		assertEquals(10, body.getCryptoCreateAccount().getInitialBalance());
		assertTrue(body.getCryptoCreateAccount().getReceiverSigRequired());
		assertEquals(101, body.getCryptoCreateAccount().getMaxReceiveProxyFraction());
		assertEquals(202, body.getCryptoCreateAccount().getReceiveRecordThreshold());
		assertEquals(303, body.getCryptoCreateAccount().getSendRecordThreshold());
		assertFalse(body.getCryptoCreateAccount().hasKey());
		assertEquals(200, body.getCryptoCreateAccount().getRealmID().getRealmNum());
		assertEquals(300, body.getCryptoCreateAccount().getRealmID().getShardNum());
		assertEquals(300, body.getCryptoCreateAccount().getShardID().getShardNum());

		assertFalse(body.getCryptoCreateAccount().hasNewRealmAdminKey());

		assertEquals(300, body.getCryptoCreateAccount().getShardID().getShardNum());

		assertEquals(10, body.getCryptoCreateAccount().getProxyAccountID().getShardNum());
		assertEquals(20, body.getCryptoCreateAccount().getProxyAccountID().getRealmNum());
		assertEquals(30, body.getCryptoCreateAccount().getProxyAccountID().getAccountNum());
		
	}
}

//		public TransactionBody bodyToSignForTransfer(HederaTransactionID transactionID, HederaAccountID nodeAccount, 
//				long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, 
//				String memo, ArrayList<HederaAccountAmount> accountAmounts) {

//					, this.getTransferTransactionBody(accountAmounts));

//		public TransactionBody bodyToSignForDelete(HederaTransactionID transactionID, HederaAccountID nodeAccount, 
//				long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, 
//				String memo, HederaAccountID transferAccountID) {
//			
//					, this.getDeleteTransactionBody(transferAccountID));
//

//		public TransactionBody bodyToSignForUpdate(HederaTransactionID transactionID, HederaAccountID nodeAccount, 
//				long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, 
//				String memo) {
//					, this.getUpdateTransactionBody());
//

//		public TransactionBody bodyToSignForAddClaim(HederaTransactionID transactionID, HederaAccountID nodeAccount, 
//				long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, 
//				String memo, HederaClaim claim) {
//					, this.getAddClaimTransactionBody(claim));
//

//		public TransactionBody bodyToSignForDeleteClaim(HederaTransactionID transactionID, HederaAccountID nodeAccount, 
//				long transactionFee, HederaDuration transactionValidDuration, boolean generateRecord, 
//				String memo, HederaClaim claim) {
//					, this.getDeleteClaimTransactionBody(claim));

//		public CryptoTransferTransactionBody getTransferTransactionBody(ArrayList<HederaAccountAmount>accountAmounts) {
//		   	CryptoTransferTransactionBody.Builder transactionBody = CryptoTransferTransactionBody.newBuilder();
//		   	TransferList.Builder transferList = TransferList.newBuilder();
//		   	for (HederaAccountAmount accountAmount: accountAmounts) {
//		   		transferList.addAccountAmounts(accountAmount.getProtobuf());
//		   	}
//		   	transactionBody.setTransfers(transferList);
//		}

//		public CryptoUpdateTransactionBody getUpdateTransactionBody() {
//			updateTransaction.setAccountIDToUpdate(this.getHederaAccountID().getProtobuf());
//			if (this.autoRenewPeriod != null) {
//				updateTransaction.setAutoRenewPeriod(this.autoRenewPeriod.getProtobuf());
//			}
//			if (this.expirationTime != null) {
//				updateTransaction.setExpirationTime(this.expirationTime.getProtobuf());
//			}
//			if (this.accountKeySig != null) {
//				updateTransaction.setKey(this.accountKeySig.getKeyProtobuf());
//			} else if (this.accountKey != null) {
//				updateTransaction.setKey(this.accountKey.getProtobuf());
//			}
//			if (this.proxyAccountID != null) {
//				updateTransaction.setProxyAccountID(this.proxyAccountID.getProtobuf());
//			}
//			if (this.proxyFraction != -1) {
//				updateTransaction.setProxyFraction(this.proxyFraction);
//			}
//			if (this.receiveRecordThreshold != -1) {
//				updateTransaction.setReceiveRecordThreshold(this.receiveRecordThreshold);
//			}
//			if (this.sendRecordThreshold != -1) {
//				updateTransaction.setSendRecordThreshold(this.sendRecordThreshold);
//			}
//			
//		}

//		public CryptoDeleteTransactionBody getDeleteTransactionBody(HederaAccountID transferAccountID) {
//			// Generates the protobuf payload for this class
//		   	CryptoDeleteTransactionBody.Builder transactionBody = CryptoDeleteTransactionBody.newBuilder();
//		   	transactionBody.setDeleteAccountID(this.getHederaAccountID().getProtobuf());
//		   	if (transferAccountID != null) {
//		   		transactionBody.setTransferAccountID(transferAccountID.getProtobuf());
//		   	}
//		}

//		public CryptoAddClaimTransactionBody getAddClaimTransactionBody(HederaClaim claim) {
//			CryptoAddClaimTransactionBody.Builder transaction = CryptoAddClaimTransactionBody.newBuilder();
//			transaction.setAccountID(this.getHederaAccountID().getProtobuf());
//			transaction.setClaim(claim.getProtobuf());
//		}

//		public CryptoDeleteClaimTransactionBody getDeleteClaimTransactionBody(HederaClaim claim) {
//		   	CryptoDeleteClaimTransactionBody.Builder transaction = CryptoDeleteClaimTransactionBody.newBuilder();
//			transaction.setAccountIDToDeleteFrom(this.getHederaAccountID().getProtobuf());
//			transaction.setHashToDelete(ByteString.copyFrom(claim.hash));
//		}

