package com.hedera.sdk.account

import spock.lang.Specification
import com.hedera.sdk.AccountId

class AccountClaimQueryTest extends Specification {
	def "Query can be built with defaults"() {
		when:
		def query = new AccountClaimQuery()

        then:
		query.inner.build().toString() == """cryptoGetClaim {
}
"""
    }

	def "Query can be built"() {
		when:
		def query = new AccountClaimQuery().with(true, {
			account = new AccountId(5)
			hash = [1, 2, 5, 5]
		})

		then:
		query.inner.build().toString() == """cryptoGetClaim {
  accountID {
    accountNum: 5
  }
  hash: "\\001\\002\\005\\005"
}
"""
	}
}
