package com.hedera.sdk

import com.hedera.sdk.account.AccountId
import com.hedera.sdk.proto.Response
import com.hedera.sdk.proto.ResponseCodeEnum
import com.hedera.sdk.proto.TransactionGetReceiptResponse
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

	def "nullable fields return null"() {
		given:
		def response = Response.newBuilder()
				.setTransactionGetReceipt(TransactionGetReceiptResponse.defaultInstance)
				.build()

		when:
		def receipt = new TransactionReceipt(response)

		then:
		receipt.status == ResponseCodeEnum.OK
		receipt.accountId == null
		receipt.contractId == null
		receipt.fileId == null
	}

	def "receipt with account ID"() {
		given:
		def response = Response.newBuilder()
				.setTransactionGetReceipt(TransactionGetReceiptResponse.newBuilder()
				.setReceipt(com.hedera.sdk.proto.TransactionReceipt.newBuilder()
				.setAccountID(new AccountId(1, 2, 3).toProto())
				.build()).build())
				.build()

		when:
		def receipt = new TransactionReceipt(response)

		then:
		receipt.accountId == new AccountId(1, 2, 3)
	}
}
