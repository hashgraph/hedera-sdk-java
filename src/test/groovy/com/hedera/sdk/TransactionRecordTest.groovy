package com.hedera.sdk

import com.hedera.sdk.proto.Response
import com.hedera.sdk.proto.TransactionGetRecordResponse
import com.hedera.sdk.proto.TransactionGetFastRecordResponse
import com.hedera.sdk.proto.TransactionID
import spock.lang.Specification

class TransactionRecordTest extends Specification {
	def "record requires correct response"() {
		given:
		def response = Response.defaultInstance

		when:
		new TransactionRecord(response)

		then:
		thrown(IllegalArgumentException)
	}

	def "record can use `TransactionGetRecord`"() {
		given:
		def response = Response.newBuilder()
				.setTransactionGetRecord(TransactionGetRecordResponse.defaultInstance)
				.build()

		when:
		def record = new TransactionRecord(response)

		then:
		record.transactionId == new TransactionId(TransactionID.defaultInstance)
		record.transactionFee == 0
		record.transactionHash == null
		record.consensusTimestamp == null
		record.memo == null
		record.callResult == null
		record.createResult == null
		record.transfers == null
	}

	def "record can use `TransactionGetFastRecord`"() {
		given:
		def response = Response.newBuilder()
				.setTransactionGetFastRecord(TransactionGetFastRecordResponse.defaultInstance)
				.build()

		when:
		def record = new TransactionRecord(response)

		then:
		record.transactionId == new TransactionId(TransactionID.defaultInstance)
		record.transactionFee == 0
		record.transactionHash == null
		record.consensusTimestamp == null
		record.memo == null
		record.callResult == null
		record.createResult == null
		record.transfers == null
	}
}
