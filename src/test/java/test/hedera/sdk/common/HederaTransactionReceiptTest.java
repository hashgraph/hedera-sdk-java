package test.hedera.sdk.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaContractID;
import com.hedera.sdk.common.HederaFileID;
import com.hedera.sdk.common.HederaPrecheckResult;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.HederaTransactionStatus;
import com.hederahashgraph.api.proto.java.NodeTransactionPrecheckCode;
import com.hederahashgraph.api.proto.java.ResponseHeader;
import com.hederahashgraph.api.proto.java.TransactionGetReceiptResponse;
import com.hederahashgraph.api.proto.java.TransactionReceipt;
import com.hederahashgraph.api.proto.java.TransactionStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaTransactionReceiptTest {

	@Test
	@DisplayName("TestHederaTransactionReceipt")
	void test() {
		HederaTransactionReceipt masterReceipt = new HederaTransactionReceipt();
		masterReceipt.accountID = new HederaAccountID(1, 2, 3);
		masterReceipt.contractID = new HederaContractID(4, 5, 6);
		masterReceipt.fileID = new HederaFileID(7, 8, 9);
		
		masterReceipt.transactionStatus = HederaTransactionStatus.SUCCESS;
		
		assertEquals(1, masterReceipt.accountID.shardNum);
		assertEquals(2, masterReceipt.accountID.realmNum);
		assertEquals(3, masterReceipt.accountID.accountNum);

		assertEquals(4, masterReceipt.contractID.shardNum);
		assertEquals(5, masterReceipt.contractID.realmNum);
		assertEquals(6, masterReceipt.contractID.contractNum);

		assertEquals(7, masterReceipt.fileID.shardNum);
		assertEquals(8, masterReceipt.fileID.realmNum);
		assertEquals(9, masterReceipt.fileID.fileNum);
		
		assertEquals(HederaTransactionStatus.SUCCESS, masterReceipt.transactionStatus);
		
		// build one from protobuf
		HederaTransactionReceipt receipt = new HederaTransactionReceipt(masterReceipt.getProtobuf());
		
		assertEquals(1, receipt.accountID.shardNum);
		assertEquals(2, receipt.accountID.realmNum);
		assertEquals(3, receipt.accountID.accountNum);

		assertEquals(4, receipt.contractID.shardNum);
		assertEquals(5, receipt.contractID.realmNum);
		assertEquals(6, receipt.contractID.contractNum);

		assertEquals(7, receipt.fileID.shardNum);
		assertEquals(8, receipt.fileID.realmNum);
		assertEquals(9, receipt.fileID.fileNum);
		
		assertEquals(HederaTransactionStatus.SUCCESS, receipt.transactionStatus);
		
		TransactionGetReceiptResponse.Builder receiptResponse = TransactionGetReceiptResponse.newBuilder();
		receiptResponse.setHeader(ResponseHeader.newBuilder().setNodeTransactionPrecheckCode(NodeTransactionPrecheckCode.DUPLICATE));
		receiptResponse.setReceipt(TransactionReceipt.newBuilder().setAccountID(new HederaAccountID(1, 2, 3).getProtobuf()));
		receipt = new HederaTransactionReceipt(receiptResponse.build());
		assertEquals(HederaPrecheckResult.DUPLICATE, receipt.nodePrecheck);

		receiptResponse.setHeader(ResponseHeader.newBuilder().setNodeTransactionPrecheckCode(NodeTransactionPrecheckCode.INSUFFICIENT_BALANCE));
		receiptResponse.setReceipt(TransactionReceipt.newBuilder().setStatus(TransactionStatus.FAIL_BALANCE).setFileID(new HederaFileID(1, 2, 3).getProtobuf()));
		receipt = new HederaTransactionReceipt(receiptResponse.build());
		assertEquals(HederaPrecheckResult.INSUFFICIENT_BALANCE, receipt.nodePrecheck);
		assertEquals(HederaTransactionStatus.FAIL_BALANCE, receipt.transactionStatus);

		receiptResponse.setHeader(ResponseHeader.newBuilder().setNodeTransactionPrecheckCode(NodeTransactionPrecheckCode.INSUFFICIENT_FEE));
		receiptResponse.setReceipt(TransactionReceipt.newBuilder().setStatus(TransactionStatus.FAIL_FEE).setContractID(new HederaContractID(1, 2, 3).getProtobuf()));
		receipt = new HederaTransactionReceipt(receiptResponse.build());
		assertEquals(HederaPrecheckResult.INSUFFICIENT_FEE, receipt.nodePrecheck);
		assertEquals(HederaTransactionStatus.FAIL_FEE, receipt.transactionStatus);

		receiptResponse.setHeader(ResponseHeader.newBuilder().setNodeTransactionPrecheckCode(NodeTransactionPrecheckCode.INVALID_ACCOUNT));
		receiptResponse.setReceipt(TransactionReceipt.newBuilder().setStatus(TransactionStatus.FAIL_INVALID));
		receipt = new HederaTransactionReceipt(receiptResponse.build());
		assertEquals(HederaPrecheckResult.INVALID_ACCOUNT, receipt.nodePrecheck);
		assertEquals(HederaTransactionStatus.FAIL_INVALID, receipt.transactionStatus);

		receiptResponse.setHeader(ResponseHeader.newBuilder().setNodeTransactionPrecheckCode(NodeTransactionPrecheckCode.INVALID_TRANSACTION));
		receiptResponse.setReceipt(TransactionReceipt.newBuilder().setStatus(TransactionStatus.SUCCESS));
		receipt = new HederaTransactionReceipt(receiptResponse.build());
		assertEquals(HederaPrecheckResult.INVALID_TRANSACTION, receipt.nodePrecheck);
		assertEquals(HederaTransactionStatus.SUCCESS, receipt.transactionStatus);
		

		TransactionReceipt.Builder receiptProto = TransactionReceipt.newBuilder();
		receiptProto.setStatus(TransactionStatus.FAIL_BALANCE);
		receipt = new HederaTransactionReceipt(receiptProto.build());
		assertEquals(HederaTransactionStatus.FAIL_BALANCE, receipt.transactionStatus);
		TransactionReceipt receiptP = receipt.getProtobuf();
		assertEquals(receiptP.getStatus(), receiptProto.getStatus());

		receiptProto = TransactionReceipt.newBuilder();
		receiptProto.setStatus(TransactionStatus.FAIL_FEE);
		receipt = new HederaTransactionReceipt(receiptProto.build());
		assertEquals(HederaTransactionStatus.FAIL_FEE, receipt.transactionStatus);
		receiptP = receipt.getProtobuf();
		assertEquals(receiptP.getStatus(), receiptProto.getStatus());

		receiptProto = TransactionReceipt.newBuilder();
		receiptProto.setStatus(TransactionStatus.FAIL_INVALID);
		receipt = new HederaTransactionReceipt(receiptProto.build());
		assertEquals(HederaTransactionStatus.FAIL_INVALID, receipt.transactionStatus);
		receiptP = receipt.getProtobuf();
		assertEquals(receiptP.getStatus(), receiptProto.getStatus());
		
		receiptProto = TransactionReceipt.newBuilder();
		receiptProto.setStatus(TransactionStatus.SUCCESS);
		receipt = new HederaTransactionReceipt(receiptProto.build());
		assertEquals(HederaTransactionStatus.SUCCESS, receipt.transactionStatus);
		receiptP = receipt.getProtobuf();
		assertEquals(receiptP.getStatus(), receiptProto.getStatus());

		receiptProto = TransactionReceipt.newBuilder();
		receiptProto.setStatus(TransactionStatus.UNKNOWN);
		receipt = new HederaTransactionReceipt(receiptProto.build());
		assertEquals(HederaTransactionStatus.UNKNOWN, receipt.transactionStatus);
		receiptP = receipt.getProtobuf();
		assertEquals(receiptP.getStatus(), receiptProto.getStatus());
		
		HederaTransactionStatus status = HederaTransactionStatus.SUCCESS;
		HederaAccountID account = new HederaAccountID(1, 2, 3);
		HederaFileID file = new HederaFileID(4, 5, 6);
		HederaContractID contract = new HederaContractID(7, 8, 9);
		receipt = new HederaTransactionReceipt(status, account, file, contract);
		assertEquals(status, receipt.transactionStatus);

		assertEquals(account.shardNum, receipt.accountID.shardNum);
		assertEquals(account.realmNum, receipt.accountID.realmNum);
		assertEquals(account.accountNum, receipt.accountID.accountNum);
		
		assertEquals(file.shardNum, receipt.fileID.shardNum);
		assertEquals(file.realmNum, receipt.fileID.realmNum);
		assertEquals(file.fileNum, receipt.fileID.fileNum);

		assertEquals(contract.shardNum, receipt.contractID.shardNum);
		assertEquals(contract.realmNum, receipt.contractID.realmNum);
		assertEquals(contract.contractNum, receipt.contractID.contractNum);
	}
}
