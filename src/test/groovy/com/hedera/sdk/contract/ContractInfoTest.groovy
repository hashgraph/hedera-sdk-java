package com.hedera.sdk.contract

import com.hedera.sdk.AccountId
import com.hedera.sdk.ContractId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import com.hedera.sdk.proto.ContractGetInfoResponse
import com.hedera.sdk.proto.Response
import spock.lang.Specification

class ContractInfoTest extends Specification {
	def privateKey = Ed25519PrivateKey.generate()

	def "won't deserialize from the wrong kind of response"() {
		when:
		new ContractInfo(Response.defaultInstance)
		then:
		thrown(IllegalArgumentException)
	}

	def "doesn't require a key"() {
		given:
		def response = Response.newBuilder()
				.setContractGetInfo(ContractGetInfoResponse.defaultInstance)
				.build()
		when:
		def contractInfo = new ContractInfo(response)
		then:
		contractInfo.adminKey == null
	}

	def "deserializes from a correct response"() {
		given:
		def response = Response.newBuilder()
				.setContractGetInfo(
				ContractGetInfoResponse.newBuilder()
				.setContractInfo(ContractGetInfoResponse.ContractInfo.newBuilder()
				.setStorage(1234))
				).build()

		when:
		def contractInfo = new ContractInfo(response)

		then:
		contractInfo.contractId == new ContractId(0, 0, 0)
		contractInfo.accountId == new AccountId(0, 0, 0)
		contractInfo.contractAccountId == ""
		contractInfo.storage == 1234
	}
}
