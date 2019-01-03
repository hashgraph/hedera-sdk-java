package test.hedera.sdk.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.ByteString;
import com.hedera.sdk.account.HederaAccountAmount;
import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaContractID;
import com.hedera.sdk.common.HederaFileID;
import com.hedera.sdk.common.HederaTimeStamp;
import com.hedera.sdk.common.HederaTransactionID;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.HederaTransactionRecord;
import com.hedera.sdk.contract.HederaContractFunctionResult;
import com.hederahashgraph.api.proto.java.ContractFunctionResult;
import com.hederahashgraph.api.proto.java.ContractLoginfo;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaRecordTest {

	@Test
	@DisplayName("TestHederaRecord-ContractCallResult")
	void testContractCallResult() {
		
		// create and populate a transaction record
		HederaTransactionRecord masterRecord = new HederaTransactionRecord();
		masterRecord.setBodyContractCallResult();
		masterRecord.transactionHash = "hash".getBytes();
		masterRecord.consensusTimeStamp = new HederaTimeStamp(60,10);
		masterRecord.transactionId = new HederaTransactionID(new HederaAccountID(1, 2, 3));
		masterRecord.memo = "memo";
		masterRecord.transactionFee = 10;
		masterRecord.transactionReceipt = new HederaTransactionReceipt(
				ResponseCodeEnum.OK,
				ResponseCodeEnum.SUCCESS, 
			new HederaAccountID(1, 2, 3), 
			new HederaFileID(4, 5, 6), 
			new HederaContractID(7, 8, 9));
		
		// build protobuf for contractCallResult
		ContractFunctionResult.Builder protoContractResult = ContractFunctionResult.newBuilder();
		protoContractResult.setBloom(ByteString.copyFromUtf8("Bloom"));
		HederaContractID contractId = new HederaContractID(10, 20, 30);
		protoContractResult.setContractID(contractId.getProtobuf());
		protoContractResult.setErrorMessage("Error message");
		protoContractResult.setGasUsed(90);
		
		ContractLoginfo.Builder contractLogInfo = ContractLoginfo.newBuilder();
		contractLogInfo.setBloom(ByteString.copyFromUtf8("Bloom2"));
		HederaContractID contractId2 = new HederaContractID(11, 21, 31);
		contractLogInfo.setContractID(contractId2.getProtobuf());
		contractLogInfo.setData(ByteString.copyFromUtf8("data"));
		contractLogInfo.addTopic(ByteString.copyFromUtf8("Topic1"));
		contractLogInfo.addTopic(ByteString.copyFromUtf8("Topic2"));
		
		protoContractResult.addLogInfo(contractLogInfo);

		contractLogInfo = ContractLoginfo.newBuilder();
		contractLogInfo.setBloom(ByteString.copyFromUtf8("Bloom3"));
		contractId2 = new HederaContractID(12, 22, 32);
		contractLogInfo.setContractID(contractId2.getProtobuf());
		contractLogInfo.setData(ByteString.copyFromUtf8("data2"));
		contractLogInfo.addTopic(ByteString.copyFromUtf8("Topic3"));
		contractLogInfo.addTopic(ByteString.copyFromUtf8("Topic4"));

		protoContractResult.addLogInfo(contractLogInfo);
		
		HederaContractFunctionResult contractCallResult = new HederaContractFunctionResult(protoContractResult.build());

		masterRecord.contractCallResult = contractCallResult;

		// check values
		assertArrayEquals("hash".getBytes(), masterRecord.transactionHash);
		
		assertEquals(60, masterRecord.consensusTimeStamp.time.getEpochSecond());
		assertEquals(10, masterRecord.consensusTimeStamp.time.getNano());
		
		assertEquals(1, masterRecord.transactionId.accountID.shardNum);
		assertEquals(2, masterRecord.transactionId.accountID.realmNum);
		assertEquals(3, masterRecord.transactionId.accountID.accountNum);

		assertEquals("memo", masterRecord.memo);
		assertEquals(10, masterRecord.transactionFee);

		assertEquals(ResponseCodeEnum.SUCCESS, masterRecord.transactionReceipt.transactionStatus);
		assertEquals(1, masterRecord.transactionReceipt.accountID.shardNum);
		assertEquals(2, masterRecord.transactionReceipt.accountID.realmNum);
		assertEquals(3, masterRecord.transactionReceipt.accountID.accountNum);
		assertEquals(4, masterRecord.transactionReceipt.fileID.shardNum);
		assertEquals(5, masterRecord.transactionReceipt.fileID.realmNum);
		assertEquals(6, masterRecord.transactionReceipt.fileID.fileNum);
		assertEquals(7, masterRecord.transactionReceipt.contractID.shardNum);
		assertEquals(8, masterRecord.transactionReceipt.contractID.realmNum);
		assertEquals(9, masterRecord.transactionReceipt.contractID.contractNum);
		
		assertArrayEquals("Bloom".getBytes(), masterRecord.contractCallResult.bloom());

		assertEquals(10, masterRecord.contractCallResult.contractID().shardNum);
		assertEquals(20, masterRecord.contractCallResult.contractID().realmNum);
		assertEquals(30, masterRecord.contractCallResult.contractID().contractNum);
		assertEquals("Error message", masterRecord.contractCallResult.errorMessage());
		assertEquals(90, masterRecord.contractCallResult.gasUsed());

		assertArrayEquals("Bloom2".getBytes(), masterRecord.contractCallResult.contractLogInfo().get(0).bloom());
		assertEquals(11, masterRecord.contractCallResult.contractLogInfo().get(0).contractID().shardNum);
		assertEquals(21, masterRecord.contractCallResult.contractLogInfo().get(0).contractID().realmNum);
		assertEquals(31, masterRecord.contractCallResult.contractLogInfo().get(0).contractID().contractNum);
		assertArrayEquals("data".getBytes(), masterRecord.contractCallResult.contractLogInfo().get(0).data());
		byte[][] topics = masterRecord.contractCallResult.contractLogInfo().get(0).topics();
		assertArrayEquals("Topic1".getBytes(), topics[0]);
		assertArrayEquals("Topic2".getBytes(), topics[1]);
		
		assertArrayEquals("Bloom3".getBytes(), masterRecord.contractCallResult.contractLogInfo().get(1).bloom());
		assertEquals(12, masterRecord.contractCallResult.contractLogInfo().get(1).contractID().shardNum);
		assertEquals(22, masterRecord.contractCallResult.contractLogInfo().get(1).contractID().realmNum);
		assertEquals(32, masterRecord.contractCallResult.contractLogInfo().get(1).contractID().contractNum);
		assertArrayEquals("data2".getBytes(), masterRecord.contractCallResult.contractLogInfo().get(1).data());
		topics = masterRecord.contractCallResult.contractLogInfo().get(1).topics();
		assertArrayEquals("Topic3".getBytes(), topics[0]);
		assertArrayEquals("Topic4".getBytes(), topics[1]);

		// now build one from protobuf
		HederaTransactionRecord protoRecord = new HederaTransactionRecord(masterRecord.getProtobuf());
		// and check all values the same
		assertArrayEquals("hash".getBytes(), protoRecord.transactionHash);
		
		assertEquals(60, protoRecord.consensusTimeStamp.time.getEpochSecond());
		assertEquals(10, protoRecord.consensusTimeStamp.time.getNano());
		
		assertEquals(1, protoRecord.transactionId.accountID.shardNum);
		assertEquals(2, protoRecord.transactionId.accountID.realmNum);
		assertEquals(3, protoRecord.transactionId.accountID.accountNum);

		assertEquals("memo", protoRecord.memo);
		assertEquals(10, protoRecord.transactionFee);

		assertEquals(ResponseCodeEnum.SUCCESS, protoRecord.transactionReceipt.transactionStatus);
		assertEquals(1, protoRecord.transactionReceipt.accountID.shardNum);
		assertEquals(2, protoRecord.transactionReceipt.accountID.realmNum);
		assertEquals(3, protoRecord.transactionReceipt.accountID.accountNum);
		assertEquals(4, protoRecord.transactionReceipt.fileID.shardNum);
		assertEquals(5, protoRecord.transactionReceipt.fileID.realmNum);
		assertEquals(6, protoRecord.transactionReceipt.fileID.fileNum);
		assertEquals(7, protoRecord.transactionReceipt.contractID.shardNum);
		assertEquals(8, protoRecord.transactionReceipt.contractID.realmNum);
		assertEquals(9, protoRecord.transactionReceipt.contractID.contractNum);
		
		assertArrayEquals("Bloom".getBytes(), protoRecord.contractCallResult.bloom());

		assertEquals(10, protoRecord.contractCallResult.contractID().shardNum);
		assertEquals(20, protoRecord.contractCallResult.contractID().realmNum);
		assertEquals(30, protoRecord.contractCallResult.contractID().contractNum);
		assertEquals("Error message", protoRecord.contractCallResult.errorMessage());
		assertEquals(90, protoRecord.contractCallResult.gasUsed());

		assertArrayEquals("Bloom2".getBytes(), protoRecord.contractCallResult.contractLogInfo().get(0).bloom());
		assertEquals(11, protoRecord.contractCallResult.contractLogInfo().get(0).contractID().shardNum);
		assertEquals(21, protoRecord.contractCallResult.contractLogInfo().get(0).contractID().realmNum);
		assertEquals(31, protoRecord.contractCallResult.contractLogInfo().get(0).contractID().contractNum);
		assertArrayEquals("data".getBytes(), protoRecord.contractCallResult.contractLogInfo().get(0).data());
		topics = protoRecord.contractCallResult.contractLogInfo().get(0).topics();
		assertArrayEquals("Topic1".getBytes(), topics[0]);
		assertArrayEquals("Topic2".getBytes(), topics[1]);
		
		assertArrayEquals("Bloom3".getBytes(), protoRecord.contractCallResult.contractLogInfo().get(1).bloom());
		assertEquals(12, protoRecord.contractCallResult.contractLogInfo().get(1).contractID().shardNum);
		assertEquals(22, protoRecord.contractCallResult.contractLogInfo().get(1).contractID().realmNum);
		assertEquals(32, protoRecord.contractCallResult.contractLogInfo().get(1).contractID().contractNum);
		assertArrayEquals("data2".getBytes(), protoRecord.contractCallResult.contractLogInfo().get(1).data());
		topics = protoRecord.contractCallResult.contractLogInfo().get(1).topics();
		assertArrayEquals("Topic3".getBytes(), topics[0]);
		assertArrayEquals("Topic4".getBytes(), topics[1]);
		
	}

	@Test
	@DisplayName("TestHederaRecord-ContractCreateResult")
	void testContractCreateResult() {
		
		// create and populate a transaction record
		HederaTransactionRecord masterRecord = new HederaTransactionRecord();
		masterRecord.setBodyContractCreateResult();
		masterRecord.transactionHash = "hash".getBytes();
		masterRecord.consensusTimeStamp = new HederaTimeStamp(60,10);
		masterRecord.transactionId = new HederaTransactionID(new HederaAccountID(1, 2, 3));
		masterRecord.memo = "memo";
		masterRecord.transactionFee = 10;
		masterRecord.transactionReceipt = new HederaTransactionReceipt(
				ResponseCodeEnum.OK,
				ResponseCodeEnum.SUCCESS, 
			new HederaAccountID(1, 2, 3), 
			new HederaFileID(4, 5, 6), 
			new HederaContractID(7, 8, 9));
		
		// build protobuf for contractCallResult
		ContractFunctionResult.Builder protoContractResult = ContractFunctionResult.newBuilder();
		protoContractResult.setBloom(ByteString.copyFromUtf8("Bloom"));
		HederaContractID contractId = new HederaContractID(10, 20, 30);
		protoContractResult.setContractID(contractId.getProtobuf());
		protoContractResult.setErrorMessage("Error message");
		protoContractResult.setGasUsed(90);
		
		ContractLoginfo.Builder contractLogInfo = ContractLoginfo.newBuilder();
		contractLogInfo.setBloom(ByteString.copyFromUtf8("Bloom2"));
		HederaContractID contractId2 = new HederaContractID(11, 21, 31);
		contractLogInfo.setContractID(contractId2.getProtobuf());
		contractLogInfo.setData(ByteString.copyFromUtf8("data"));
		contractLogInfo.addTopic(ByteString.copyFromUtf8("Topic1"));
		contractLogInfo.addTopic(ByteString.copyFromUtf8("Topic2"));
		
		protoContractResult.addLogInfo(contractLogInfo);

		contractLogInfo = ContractLoginfo.newBuilder();
		contractLogInfo.setBloom(ByteString.copyFromUtf8("Bloom3"));
		contractId2 = new HederaContractID(12, 22, 32);
		contractLogInfo.setContractID(contractId2.getProtobuf());
		contractLogInfo.setData(ByteString.copyFromUtf8("data2"));
		contractLogInfo.addTopic(ByteString.copyFromUtf8("Topic3"));
		contractLogInfo.addTopic(ByteString.copyFromUtf8("Topic4"));

		protoContractResult.addLogInfo(contractLogInfo);
		
		HederaContractFunctionResult contractFunctionResult = new HederaContractFunctionResult(protoContractResult.build());

		masterRecord.contractCreateResult = contractFunctionResult;

		// check values
		assertArrayEquals("hash".getBytes(), masterRecord.transactionHash);
		
		assertEquals(60, masterRecord.consensusTimeStamp.time.getEpochSecond());
		assertEquals(10, masterRecord.consensusTimeStamp.time.getNano());
		
		assertEquals(1, masterRecord.transactionId.accountID.shardNum);
		assertEquals(2, masterRecord.transactionId.accountID.realmNum);
		assertEquals(3, masterRecord.transactionId.accountID.accountNum);

		assertEquals("memo", masterRecord.memo);
		assertEquals(10, masterRecord.transactionFee);

		assertEquals(ResponseCodeEnum.SUCCESS, masterRecord.transactionReceipt.transactionStatus);
		assertEquals(1, masterRecord.transactionReceipt.accountID.shardNum);
		assertEquals(2, masterRecord.transactionReceipt.accountID.realmNum);
		assertEquals(3, masterRecord.transactionReceipt.accountID.accountNum);
		assertEquals(4, masterRecord.transactionReceipt.fileID.shardNum);
		assertEquals(5, masterRecord.transactionReceipt.fileID.realmNum);
		assertEquals(6, masterRecord.transactionReceipt.fileID.fileNum);
		assertEquals(7, masterRecord.transactionReceipt.contractID.shardNum);
		assertEquals(8, masterRecord.transactionReceipt.contractID.realmNum);
		assertEquals(9, masterRecord.transactionReceipt.contractID.contractNum);
		
		assertArrayEquals("Bloom".getBytes(), masterRecord.contractCreateResult.bloom());

		assertEquals(10, masterRecord.contractCreateResult.contractID().shardNum);
		assertEquals(20, masterRecord.contractCreateResult.contractID().realmNum);
		assertEquals(30, masterRecord.contractCreateResult.contractID().contractNum);
		assertEquals("Error message", masterRecord.contractCreateResult.errorMessage());
		assertEquals(90, masterRecord.contractCreateResult.gasUsed());

		assertArrayEquals("Bloom2".getBytes(), masterRecord.contractCreateResult.contractLogInfo().get(0).bloom());
		assertEquals(11, masterRecord.contractCreateResult.contractLogInfo().get(0).contractID().shardNum);
		assertEquals(21, masterRecord.contractCreateResult.contractLogInfo().get(0).contractID().realmNum);
		assertEquals(31, masterRecord.contractCreateResult.contractLogInfo().get(0).contractID().contractNum);
		assertArrayEquals("data".getBytes(), masterRecord.contractCreateResult.contractLogInfo().get(0).data());
		byte[][] topics = masterRecord.contractCreateResult.contractLogInfo().get(0).topics();
		assertArrayEquals("Topic1".getBytes(), topics[0]);
		assertArrayEquals("Topic2".getBytes(), topics[1]);
		
		assertArrayEquals("Bloom3".getBytes(), masterRecord.contractCreateResult.contractLogInfo().get(1).bloom());
		assertEquals(12, masterRecord.contractCreateResult.contractLogInfo().get(1).contractID().shardNum);
		assertEquals(22, masterRecord.contractCreateResult.contractLogInfo().get(1).contractID().realmNum);
		assertEquals(32, masterRecord.contractCreateResult.contractLogInfo().get(1).contractID().contractNum);
		assertArrayEquals("data2".getBytes(), masterRecord.contractCreateResult.contractLogInfo().get(1).data());
		topics = masterRecord.contractCreateResult.contractLogInfo().get(1).topics();
		assertArrayEquals("Topic3".getBytes(), topics[0]);
		assertArrayEquals("Topic4".getBytes(), topics[1]);

		// now build one from protobuf
		HederaTransactionRecord protoRecord = new HederaTransactionRecord(masterRecord.getProtobuf());
		// and check all values the same
		assertArrayEquals("hash".getBytes(), protoRecord.transactionHash);
		
		assertEquals(60, protoRecord.consensusTimeStamp.time.getEpochSecond());
		assertEquals(10, protoRecord.consensusTimeStamp.time.getNano());
		
		assertEquals(1, protoRecord.transactionId.accountID.shardNum);
		assertEquals(2, protoRecord.transactionId.accountID.realmNum);
		assertEquals(3, protoRecord.transactionId.accountID.accountNum);

		assertEquals("memo", protoRecord.memo);
		assertEquals(10, protoRecord.transactionFee);

		assertEquals(ResponseCodeEnum.SUCCESS, protoRecord.transactionReceipt.transactionStatus);
		assertEquals(1, protoRecord.transactionReceipt.accountID.shardNum);
		assertEquals(2, protoRecord.transactionReceipt.accountID.realmNum);
		assertEquals(3, protoRecord.transactionReceipt.accountID.accountNum);
		assertEquals(4, protoRecord.transactionReceipt.fileID.shardNum);
		assertEquals(5, protoRecord.transactionReceipt.fileID.realmNum);
		assertEquals(6, protoRecord.transactionReceipt.fileID.fileNum);
		assertEquals(7, protoRecord.transactionReceipt.contractID.shardNum);
		assertEquals(8, protoRecord.transactionReceipt.contractID.realmNum);
		assertEquals(9, protoRecord.transactionReceipt.contractID.contractNum);
		
		assertArrayEquals("Bloom".getBytes(), protoRecord.contractCreateResult.bloom());

		assertEquals(10, protoRecord.contractCreateResult.contractID().shardNum);
		assertEquals(20, protoRecord.contractCreateResult.contractID().realmNum);
		assertEquals(30, protoRecord.contractCreateResult.contractID().contractNum);
		assertEquals("Error message", protoRecord.contractCreateResult.errorMessage());
		assertEquals(90, protoRecord.contractCreateResult.gasUsed());

		assertArrayEquals("Bloom2".getBytes(), protoRecord.contractCreateResult.contractLogInfo().get(0).bloom());
		assertEquals(11, protoRecord.contractCreateResult.contractLogInfo().get(0).contractID().shardNum);
		assertEquals(21, protoRecord.contractCreateResult.contractLogInfo().get(0).contractID().realmNum);
		assertEquals(31, protoRecord.contractCreateResult.contractLogInfo().get(0).contractID().contractNum);
		assertArrayEquals("data".getBytes(), protoRecord.contractCreateResult.contractLogInfo().get(0).data());
		topics = protoRecord.contractCreateResult.contractLogInfo().get(0).topics();
		assertArrayEquals("Topic1".getBytes(), topics[0]);
		assertArrayEquals("Topic2".getBytes(), topics[1]);
		
		assertArrayEquals("Bloom3".getBytes(), protoRecord.contractCreateResult.contractLogInfo().get(1).bloom());
		assertEquals(12, protoRecord.contractCreateResult.contractLogInfo().get(1).contractID().shardNum);
		assertEquals(22, protoRecord.contractCreateResult.contractLogInfo().get(1).contractID().realmNum);
		assertEquals(32, protoRecord.contractCreateResult.contractLogInfo().get(1).contractID().contractNum);
		assertArrayEquals("data2".getBytes(), protoRecord.contractCreateResult.contractLogInfo().get(1).data());
		topics = protoRecord.contractCreateResult.contractLogInfo().get(1).topics();
		assertArrayEquals("Topic3".getBytes(), topics[0]);
		assertArrayEquals("Topic4".getBytes(), topics[1]);
		
	}

	@Test
	@DisplayName("TestHederaRecord-TransferListResult")
	void testTransferListResult() {
		// create and populate a transaction record
		HederaTransactionRecord masterRecord = new HederaTransactionRecord();
		masterRecord.setBodyTransferList();
		masterRecord.transactionHash = "hash".getBytes();
		masterRecord.consensusTimeStamp = new HederaTimeStamp(60,10);
		masterRecord.transactionId = new HederaTransactionID(new HederaAccountID(1, 2, 3));
		masterRecord.memo = "memo";
		masterRecord.transactionFee = 10;
		masterRecord.transactionReceipt = new HederaTransactionReceipt(
				ResponseCodeEnum.OK,
			ResponseCodeEnum.SUCCESS, 
			new HederaAccountID(1, 2, 3), 
			new HederaFileID(4, 5, 6), 
			new HederaContractID(7, 8, 9));
		
		// build protobuf for transferList
		HederaAccountAmount amount1 = new HederaAccountAmount();
		amount1.shardNum = 1;
		amount1.realmNum = 2;
		amount1.accountNum = 3;
		amount1.amount = 10;
		
		HederaAccountAmount amount2 = new HederaAccountAmount();
		amount2.shardNum = 12;
		amount2.realmNum = 22;
		amount2.accountNum = 32;
		amount2.amount = 102;
		
		masterRecord.transferList.add(amount1);
		masterRecord.transferList.add(amount2);
		
		// check values
		assertArrayEquals("hash".getBytes(), masterRecord.transactionHash);
		
		assertEquals(60, masterRecord.consensusTimeStamp.time.getEpochSecond());
		assertEquals(10, masterRecord.consensusTimeStamp.time.getNano());
		
		assertEquals(1, masterRecord.transactionId.accountID.shardNum);
		assertEquals(2, masterRecord.transactionId.accountID.realmNum);
		assertEquals(3, masterRecord.transactionId.accountID.accountNum);

		assertEquals("memo", masterRecord.memo);
		assertEquals(10, masterRecord.transactionFee);

		assertEquals(ResponseCodeEnum.SUCCESS, masterRecord.transactionReceipt.transactionStatus);
		assertEquals(1, masterRecord.transactionReceipt.accountID.shardNum);
		assertEquals(2, masterRecord.transactionReceipt.accountID.realmNum);
		assertEquals(3, masterRecord.transactionReceipt.accountID.accountNum);
		assertEquals(4, masterRecord.transactionReceipt.fileID.shardNum);
		assertEquals(5, masterRecord.transactionReceipt.fileID.realmNum);
		assertEquals(6, masterRecord.transactionReceipt.fileID.fileNum);
		assertEquals(7, masterRecord.transactionReceipt.contractID.shardNum);
		assertEquals(8, masterRecord.transactionReceipt.contractID.realmNum);
		assertEquals(9, masterRecord.transactionReceipt.contractID.contractNum);

		assertEquals(1, masterRecord.transferList.get(0).shardNum);
		assertEquals(2, masterRecord.transferList.get(0).realmNum);
		assertEquals(3, masterRecord.transferList.get(0).accountNum);
		assertEquals(10, masterRecord.transferList.get(0).amount);

		assertEquals(12, masterRecord.transferList.get(1).shardNum);
		assertEquals(22, masterRecord.transferList.get(1).realmNum);
		assertEquals(32, masterRecord.transferList.get(1).accountNum);
		assertEquals(102, masterRecord.transferList.get(1).amount);
		
		// now build one from protobuf
		HederaTransactionRecord protoRecord = new HederaTransactionRecord(masterRecord.getProtobuf());
		// and check all values the same
		assertArrayEquals("hash".getBytes(), protoRecord.transactionHash);
		
		assertEquals(60, protoRecord.consensusTimeStamp.time.getEpochSecond());
		assertEquals(10, protoRecord.consensusTimeStamp.time.getNano());
		
		assertEquals(1, protoRecord.transactionId.accountID.shardNum);
		assertEquals(2, protoRecord.transactionId.accountID.realmNum);
		assertEquals(3, protoRecord.transactionId.accountID.accountNum);

		assertEquals("memo", protoRecord.memo);
		assertEquals(10, protoRecord.transactionFee);

		assertEquals(ResponseCodeEnum.SUCCESS, protoRecord.transactionReceipt.transactionStatus);
		assertEquals(1, protoRecord.transactionReceipt.accountID.shardNum);
		assertEquals(2, protoRecord.transactionReceipt.accountID.realmNum);
		assertEquals(3, protoRecord.transactionReceipt.accountID.accountNum);
		assertEquals(4, protoRecord.transactionReceipt.fileID.shardNum);
		assertEquals(5, protoRecord.transactionReceipt.fileID.realmNum);
		assertEquals(6, protoRecord.transactionReceipt.fileID.fileNum);
		assertEquals(7, protoRecord.transactionReceipt.contractID.shardNum);
		assertEquals(8, protoRecord.transactionReceipt.contractID.realmNum);
		assertEquals(9, protoRecord.transactionReceipt.contractID.contractNum);
		
		assertEquals(1, protoRecord.transferList.get(0).shardNum);
		assertEquals(2, protoRecord.transferList.get(0).realmNum);
		assertEquals(3, protoRecord.transferList.get(0).accountNum);
		assertEquals(10, protoRecord.transferList.get(0).amount);

		assertEquals(12, protoRecord.transferList.get(1).shardNum);
		assertEquals(22, protoRecord.transferList.get(1).realmNum);
		assertEquals(32, protoRecord.transferList.get(1).accountNum);
		assertEquals(102, protoRecord.transferList.get(1).amount);
	}		
}
