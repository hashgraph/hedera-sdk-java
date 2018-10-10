package com.hedera.sdk.contract;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaFileID;
import com.hedera.sdk.common.HederaKey;
import com.hedera.sdk.common.HederaKey.KeyType;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.common.HederaKeySignature;
import com.hedera.sdk.common.HederaPrecheckResult;
import com.hedera.sdk.common.HederaTimeStamp;
import com.hedera.sdk.common.HederaTransactionID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaContractTest {

	@Test
	@DisplayName("TestHederaContract")
	void test() {
		HederaContract masterContract = new HederaContract();
		masterContract.adminKey = new HederaKey(KeyType.ECDSA384, "adminkey".getBytes());
		masterContract.adminKeySignature = new HederaKeySignature(KeyType.ED25519, "key".getBytes(), "signature".getBytes(), "keyDescription");
		masterContract.amount = 10;
		masterContract.autoRenewPeriod = new HederaDuration(60, 10);
		masterContract.constructionParameters = "construct".getBytes();
		masterContract.expirationTime = new HederaTimeStamp(100, 20);
		masterContract.fileID = new HederaFileID(1, 2, 3);
		masterContract.gas = 123;
		masterContract.initialBalance = 321;
		masterContract.shardNum = 4;
		masterContract.realmNum = 5;
		
		assertEquals(KeyType.ECDSA384, masterContract.adminKey.getKeyType());
		assertArrayEquals("adminkey".getBytes(), masterContract.adminKey.getKey());
		assertEquals(KeyType.ED25519, masterContract.adminKeySignature.getKeyType());
		assertArrayEquals("key".getBytes(), masterContract.adminKeySignature.getKey());
		assertArrayEquals("signature".getBytes(), masterContract.adminKeySignature.getSignature());
		assertEquals("keyDescription", masterContract.adminKeySignature.keyDescription);
		assertEquals(10,  masterContract.amount);
		assertEquals(60, masterContract.autoRenewPeriod.seconds);
		assertEquals(10, masterContract.autoRenewPeriod.nanos);
		assertArrayEquals("construct".getBytes(), masterContract.constructionParameters);
		assertEquals(100, masterContract.expirationTime.seconds());
		assertEquals(20, masterContract.expirationTime.nanos());
		assertEquals(1, masterContract.fileID.shardNum);
		assertEquals(2, masterContract.fileID.realmNum);
		assertEquals(3, masterContract.fileID.fileNum);
		assertEquals(123, masterContract.gas);
		assertEquals(321, masterContract.initialBalance);
		assertEquals(4, masterContract.shardNum);
		assertEquals(5, masterContract.realmNum);

		assertEquals("", masterContract.getSolidityContractAccountID());
		assertEquals(0, masterContract.getStorage());
		assertArrayEquals(new byte[0], masterContract.byteCode());
		assertNotNull(masterContract.getTransactionRecords());
		assertNull(masterContract.hederaContractFunctionResult());
		assertEquals(HederaPrecheckResult.NOTSET, masterContract.getPrecheckResult());
		assertEquals(0, masterContract.getCost());
		assertArrayEquals(new byte[0], masterContract.getStateProof());

		HederaAccountID accountId = new HederaAccountID(9, 8, 7);
		HederaTimeStamp validStart = new HederaTimeStamp(50, 60);
		HederaTransactionID transId = new HederaTransactionID(accountId, validStart);
		HederaContract contract = new HederaContract(transId);
		
		assertEquals(9, contract.hederaTransactionID.accountID.shardNum);
		assertEquals(8, contract.hederaTransactionID.accountID.realmNum);
		assertEquals(7, contract.hederaTransactionID.accountID.accountNum);
		
		assertEquals(50, contract.hederaTransactionID.transactionValidStart.seconds());
		assertEquals(60, contract.hederaTransactionID.transactionValidStart.nanos());
		
		contract = new HederaContract(10, 11, 12);
		assertEquals(10, contract.shardNum);
		assertEquals(11, contract.realmNum);
		assertEquals(12, contract.contractNum);
		
	}

}
