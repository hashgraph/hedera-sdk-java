package test.hedera.sdk.query;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hedera.sdk.query.HederaQuery;
import com.hedera.sdk.query.HederaQuery.QueryType;
import com.hederahashgraph.api.proto.java.ContractCallLocalQuery;
import com.hederahashgraph.api.proto.java.ContractGetBytecodeQuery;
import com.hederahashgraph.api.proto.java.ContractGetInfoQuery;
import com.hederahashgraph.api.proto.java.CryptoGetAccountBalanceQuery;
import com.hederahashgraph.api.proto.java.CryptoGetAccountRecordsQuery;
import com.hederahashgraph.api.proto.java.CryptoGetClaimQuery;
import com.hederahashgraph.api.proto.java.CryptoGetInfoQuery;
import com.hederahashgraph.api.proto.java.FileGetContentsQuery;
import com.hederahashgraph.api.proto.java.FileGetInfoQuery;
import com.hederahashgraph.api.proto.java.GetByKeyQuery;
import com.hederahashgraph.api.proto.java.GetBySolidityIDQuery;
import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.TransactionGetReceiptQuery;
import com.hederahashgraph.api.proto.java.TransactionGetRecordQuery;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaQueryTest {

	@Test
	@DisplayName("TestHederaQuery")
	void TestHederaQuery() {
		HederaQuery query = new HederaQuery();
		assertEquals(QueryType.NOTSET, query.queryType);
		assertNull(query.queryData);
		
		Object queryData = new Object();
		query = new HederaQuery(QueryType.GETBYKEY, queryData);
		assertEquals(QueryType.GETBYKEY, query.queryType);
		assertNotNull(query.queryData);
		
		ContractCallLocalQuery.Builder callLocalQuery = ContractCallLocalQuery.newBuilder();
		query = new HederaQuery(QueryType.CONTRACTCALLLOCAL, callLocalQuery.build());
		Query protoQuery = query.getProtobuf();
		assertEquals(QueryType.CONTRACTCALLLOCAL, query.queryType);
		assertTrue(protoQuery.hasContractCallLocal());
		
		ContractGetBytecodeQuery.Builder getBytecode = ContractGetBytecodeQuery.newBuilder();
		query = new HederaQuery(QueryType.CONTRACTGETBYTECODE, getBytecode.build());
		protoQuery = query.getProtobuf();
		assertEquals(QueryType.CONTRACTGETBYTECODE, query.queryType);
		assertTrue(protoQuery.hasContractGetBytecode());

		ContractGetInfoQuery.Builder getInfo = ContractGetInfoQuery.newBuilder();
		query = new HederaQuery(QueryType.CONTRACTGETINFO, getInfo.build());
		protoQuery = query.getProtobuf();
		assertEquals(QueryType.CONTRACTGETINFO, query.queryType);
		assertTrue(protoQuery.hasContractGetInfo());

		CryptoGetAccountBalanceQuery.Builder getBalance = CryptoGetAccountBalanceQuery.newBuilder();
		query = new HederaQuery(QueryType.CRYPTOGETACCOUNTBALANCE, getBalance.build());
		protoQuery = query.getProtobuf();
		assertEquals(QueryType.CRYPTOGETACCOUNTBALANCE, query.queryType);
		assertTrue(protoQuery.hasCryptogetAccountBalance());

		CryptoGetAccountRecordsQuery.Builder getARecords = CryptoGetAccountRecordsQuery.newBuilder();
		query = new HederaQuery(QueryType.CRYPTOGETACCOUNTRECORDS, getARecords.build());
		protoQuery = query.getProtobuf();
		assertEquals(QueryType.CRYPTOGETACCOUNTRECORDS, query.queryType);
		assertTrue(protoQuery.hasCryptoGetAccountRecords());
		
		CryptoGetClaimQuery.Builder claimquery = CryptoGetClaimQuery.newBuilder();
		query = new HederaQuery(QueryType.CRYPTOGETCLAIM, claimquery.build());
		protoQuery = query.getProtobuf();
		assertEquals(QueryType.CRYPTOGETCLAIM, query.queryType);
		assertTrue(protoQuery.hasCryptoGetClaim());

		CryptoGetInfoQuery.Builder cryptoGetInfoQuery = CryptoGetInfoQuery.newBuilder();
		query = new HederaQuery(QueryType.CRYPTOGETINFO, cryptoGetInfoQuery.build());
		protoQuery = query.getProtobuf();
		assertEquals(QueryType.CRYPTOGETINFO, query.queryType);
		assertTrue(protoQuery.hasCryptoGetInfo());
		
		FileGetContentsQuery.Builder fileGetContents = FileGetContentsQuery.newBuilder();
		query = new HederaQuery(QueryType.FILEGETCONTENTS, fileGetContents.build());
		protoQuery = query.getProtobuf();
		assertEquals(QueryType.FILEGETCONTENTS, query.queryType);
		assertTrue(protoQuery.hasFileGetContents());

		FileGetInfoQuery.Builder fileGetInfo = FileGetInfoQuery.newBuilder();
		query = new HederaQuery(QueryType.FILEGETINFO, fileGetInfo.build());
		protoQuery = query.getProtobuf();
		assertEquals(QueryType.FILEGETINFO, query.queryType);
		assertTrue(protoQuery.hasFileGetInfo());

		GetByKeyQuery.Builder getByKey = GetByKeyQuery.newBuilder();
		query = new HederaQuery(QueryType.GETBYKEY, getByKey.build());
		protoQuery = query.getProtobuf();
		assertEquals(QueryType.GETBYKEY, query.queryType);
		assertTrue(protoQuery.hasGetByKey());

		GetBySolidityIDQuery.Builder getBySolidityID = GetBySolidityIDQuery.newBuilder();
		query = new HederaQuery(QueryType.GETBYSOLIDITYID, getBySolidityID.build());
		protoQuery = query.getProtobuf();
		assertEquals(QueryType.GETBYSOLIDITYID, query.queryType);
		assertTrue(protoQuery.hasGetBySolidityID());
		
		TransactionGetReceiptQuery.Builder getReceipt = TransactionGetReceiptQuery.newBuilder();
		query = new HederaQuery(QueryType.TRANSACTIONGETRECEIPT, getReceipt.build());
		protoQuery = query.getProtobuf();
		assertEquals(QueryType.TRANSACTIONGETRECEIPT, query.queryType);
		assertTrue(protoQuery.hasTransactionGetReceipt());

		TransactionGetRecordQuery.Builder getRecord = TransactionGetRecordQuery.newBuilder();
		query = new HederaQuery(QueryType.TRANSACTIONGETRECORD, getRecord.build());
		protoQuery = query.getProtobuf();
		assertEquals(QueryType.TRANSACTIONGETRECORD, query.queryType);
		assertTrue(protoQuery.hasTransactionGetRecord());
		
	}
}

