package com.hedera.sdk.account

import com.hedera.sdk.AccountId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import com.hedera.sdk.proto.CryptoGetInfoResponse
import com.hedera.sdk.proto.Response
import spock.lang.Specification

class AccountInfoTest extends Specification {
	def privateKey = Ed25519PrivateKey.generate()

	def "won't deserialize from the wrong kind of response"() {
		when:
		new AccountInfo(Response.defaultInstance)
		then:
		thrown(IllegalArgumentException)
	}

	def "requires a key"() {
		given:
		def response = Response.newBuilder()
				.setCryptoGetInfo(CryptoGetInfoResponse.defaultInstance)
				.build()
		when:
		new AccountInfo(response)
		then:
		def e = thrown(IllegalArgumentException)
		e.message == "query response missing key"
	}

	def "deserializes from a correct response"() {
		given:
		def response = Response.newBuilder()
				.setCryptoGetInfo(
				CryptoGetInfoResponse.newBuilder()
				.setAccountInfo(CryptoGetInfoResponse.AccountInfo.newBuilder()
				.setKey(privateKey.publicKey.toKeyProto()))
				).build()

		when:
		def accountInfo = new AccountInfo(response)

		then:
		accountInfo.accountId == new AccountId(0, 0, 0)
		accountInfo.contractAccountId == ""
		accountInfo.proxyAccountId == null
		accountInfo.claims == List.of()
	}
}
