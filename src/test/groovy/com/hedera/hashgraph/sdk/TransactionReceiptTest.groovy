package com.hedera.hashgraph.sdk

import com.hedera.hashgraph.sdk.account.AccountId
import com.hedera.hashgraph.sdk.proto.Response
import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum
import com.hedera.hashgraph.sdk.proto.TransactionGetReceiptResponse
import spock.lang.Specification

class TransactionReceiptTest extends Specification {
	def "requires `TransactionGetReceipt`"() {
		given:
		def response = Response.defaultInstance

		when:
		new TransactionReceipt(response)

		then:
		thrown(IllegalArgumentException)
	}

	def "missing fields throw"() {
		given:
		def response = Response.newBuilder()
				.setTransactionGetReceipt(TransactionGetReceiptResponse.defaultInstance)
				.build()

		def receipt = new TransactionReceipt(response)

		expect:
		receipt.status == ResponseCodeEnum.OK

		when:
		receipt.accountId

		then:
		def eAcct = thrown(IllegalStateException)
		eAcct.message == "receipt does not contain an account ID"

		when:
		receipt.contractId

		then:
		def eCont = thrown(IllegalStateException)
		eCont.message == "receipt does not contain a contract ID"


		when:
		receipt.fileId

		then:
		def eFile = thrown(IllegalStateException)
		eFile.message == "receipt does not contain a file ID"
	}

	def "receipt with account ID"() {
		given:
		def response = Response.newBuilder()
				.setTransactionGetReceipt(TransactionGetReceiptResponse.newBuilder()
				.setReceipt(com.hedera.hashgraph.sdk.proto.TransactionReceipt.newBuilder()
				.setAccountID(new AccountId(1, 2, 3).toProto())
				.build()).build())
				.build()

		when:
		def receipt = new TransactionReceipt(response)

		then:
		receipt.accountId == new AccountId(1, 2, 3)
	}
}
