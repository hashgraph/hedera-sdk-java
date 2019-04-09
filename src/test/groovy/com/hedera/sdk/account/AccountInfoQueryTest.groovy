package com.hedera.sdk.account

import com.hedera.sdk.AccountId
import spock.lang.Specification

class AccountInfoQueryTest extends Specification{
	def "Query can be built with defaults"() {
		when:
		def query = new AccountInfoQuery()

		then:
		query.inner.build().toString() == """cryptoGetInfo {
}
"""
	}

	def "Query can be built"() {
		when:
		def query = new AccountInfoQuery().with(true, {
            account = new AccountId(5)
		})

		then:
		query.inner.build().toString() == """cryptoGetInfo {
  accountID {
    accountNum: 5
  }
}
"""
	}
}
