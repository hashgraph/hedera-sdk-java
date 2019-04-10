package com.hedera.sdk.account

import com.hedera.sdk.AccountId
import spock.lang.Specification

class AccountStakersQueryTest extends Specification {
	def "Query can be built with defaults"() {
		when:
		def query = new AccountStakersQuery()

		then:
		query.inner.build().toString() == """cryptoGetProxyStakers {
}
"""
	}

	def "Query can be built"() {
		when:
		def query = new AccountStakersQuery().with(true, {
			account = new AccountId(5)
		})

		then:
		query.inner.build().toString() == """cryptoGetProxyStakers {
  accountID {
    accountNum: 5
  }
}
"""
	}
}
