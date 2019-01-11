package com.hedera.sdk.query;
/**
 * This class handles the generation of protobuf for Queries to Hedera Hashgraph
 */

import java.io.Serializable;
import com.hederahashgraph.api.proto.java.ContractCallLocalQuery;
import com.hederahashgraph.api.proto.java.ContractGetBytecodeQuery;
import com.hederahashgraph.api.proto.java.ContractGetInfoQuery;
import com.hederahashgraph.api.proto.java.CryptoGetAccountBalanceQuery;
import com.hederahashgraph.api.proto.java.CryptoGetAccountRecordsQuery;
import com.hederahashgraph.api.proto.java.CryptoGetClaimQuery;
import com.hederahashgraph.api.proto.java.CryptoGetInfoQuery;
import com.hederahashgraph.api.proto.java.CryptoGetStakersQuery;
import com.hederahashgraph.api.proto.java.FileGetContentsQuery;
import com.hederahashgraph.api.proto.java.FileGetInfoQuery;
import com.hederahashgraph.api.proto.java.GetByKeyQuery;
import com.hederahashgraph.api.proto.java.GetBySolidityIDQuery;
import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.TransactionGetFastRecordQuery;
import com.hederahashgraph.api.proto.java.TransactionGetReceiptQuery;
import com.hederahashgraph.api.proto.java.TransactionGetRecordQuery;
import org.slf4j.LoggerFactory;

public class HederaQuery implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaQuery.class);
	private static final long serialVersionUID = 1;
	
	/* 
	 * list of allowed types of query being handled by the instance of this object
	 */
	public enum QueryType {
		GETBYKEY,
		CONTRACTCALLLOCAL,
		CONTRACTGETINFO,
		CONTRACTGETBYTECODE,
		CONTRACTGETRECORDS,
		CRYPTOGETACCOUNTBALANCE,
		CRYPTOGETACCOUNTRECORDS,
		CRYPTOGETINFO,
		CRYPTOGETCLAIM,
		CRYPTOGETPROXYSTAKERS,
		FILEGETCONTENTS,
		FILEGETINFO,
		GETBYSOLIDITYID,
		TRANSACTIONGETRECEIPT,
		TRANSACTIONGETRECORD,
		TRANSACTIONGETFASTRECORD,
		NOTSET
	}
	/**
	 * Current query type
	 */
	public QueryType queryType = QueryType.NOTSET;
	/**
	 * Generic query data object
	 */
	public Object queryData = null;
	
	/**
	 * Default constructor
	 */
	public HederaQuery() {


	}
	/**
	 * Constructor with query type and data
	 * @param queryType the type of query to run
	 * @param queryData the query data
	 */
	public HederaQuery(QueryType queryType, Object queryData) {

		this.queryType = queryType;
		this.queryData = queryData;

	}
	/**
	 * Returns a {@link Query} object containing the protobuf data for this query object
	 * @return {@link Query}
	 */
	public Query getProtobuf() {

		// Generates the protobuf payload for this class
		Query.Builder query = Query.newBuilder();
		switch (this.queryType) {
			case CONTRACTCALLLOCAL:
				query.setContractCallLocal((ContractCallLocalQuery)this.queryData);
				break;
			case CONTRACTGETBYTECODE:
				query.setContractGetBytecode((ContractGetBytecodeQuery)this.queryData);
				break;
			case CONTRACTGETINFO:
				query.setContractGetInfo((ContractGetInfoQuery)this.queryData);
				break;
			case CRYPTOGETACCOUNTBALANCE:
				query.setCryptogetAccountBalance((CryptoGetAccountBalanceQuery)this.queryData);
				break;
			case CRYPTOGETACCOUNTRECORDS:
				query.setCryptoGetAccountRecords((CryptoGetAccountRecordsQuery)this.queryData);
				break;
			case CRYPTOGETCLAIM:
				query.setCryptoGetClaim((CryptoGetClaimQuery)this.queryData);
				break;
			case CRYPTOGETINFO:
				query.setCryptoGetInfo((CryptoGetInfoQuery)this.queryData);
				break;
			case CRYPTOGETPROXYSTAKERS:
				query.setCryptoGetProxyStakers((CryptoGetStakersQuery)this.queryData);
				break;
			case FILEGETCONTENTS:
				query.setFileGetContents((FileGetContentsQuery)this.queryData);
				break;
			case FILEGETINFO:
				query.setFileGetInfo((FileGetInfoQuery)this.queryData);
				break;
			case GETBYKEY:
				query.setGetByKey((GetByKeyQuery)this.queryData);
				break;
			case GETBYSOLIDITYID:
				query.setGetBySolidityID((GetBySolidityIDQuery)this.queryData);
				break;
			case TRANSACTIONGETRECEIPT:
				query.setTransactionGetReceipt((TransactionGetReceiptQuery)this.queryData);
				break;
			case TRANSACTIONGETRECORD:
				query.setTransactionGetRecord((TransactionGetRecordQuery)this.queryData);
				break;
			case TRANSACTIONGETFASTRECORD:
				query.setTransactionGetFastRecord((TransactionGetFastRecordQuery)this.queryData);
				break;
			case NOTSET:

	            throw new IllegalArgumentException("Query type not set. Unable to generate data.");			
		}

		
		return query.build();
	}
}
